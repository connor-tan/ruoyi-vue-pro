#!/bin/bash

set -euo pipefail

MASTER_HOST="${MASTER_HOST:-mysql-master}"
MASTER_PORT="${MASTER_PORT:-3306}"
SLAVE_HOST="${SLAVE_HOST:-mysql-slave}"
SLAVE_PORT="${SLAVE_PORT:-3306}"
MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-123456}"
MYSQL_DATABASE="${MYSQL_DATABASE:-ruoyi-vue-pro}"
REPLICATION_USER="${REPLICATION_USER:-repl}"
REPLICATION_PASSWORD="${REPLICATION_PASSWORD:-repl123456}"
REPLICA_RESEED_ON_INIT="${REPLICA_RESEED_ON_INIT:-true}"

mysql_exec() {
  local host="$1"
  local port="$2"
  shift 2
  MYSQL_PWD="${MYSQL_ROOT_PASSWORD}" mysql -h"${host}" -P"${port}" -uroot --protocol=tcp "$@"
}

mysql_dump_master() {
  MYSQL_PWD="${MYSQL_ROOT_PASSWORD}" mysqldump \
    -h"${MASTER_HOST}" \
    -P"${MASTER_PORT}" \
    -uroot \
    --protocol=tcp \
    --single-transaction \
    --routines \
    --events \
    --triggers \
    --hex-blob \
    --set-gtid-purged=ON \
    --add-drop-database \
    --databases "${MYSQL_DATABASE}"
}

replica_already_healthy() {
  local status
  local retries=15
  local announced="false"
  while (( retries > 0 )); do
    status="$(mysql_exec "${SLAVE_HOST}" "${SLAVE_PORT}" -e "SHOW REPLICA STATUS\\G" 2>/dev/null || true)"
    if [[ -z "${status}" ]]; then
      return 1
    fi
    if [[ "${status}" != *"Source_Host: ${MASTER_HOST}"* || "${status}" != *"Source_Port: ${MASTER_PORT}"* ]]; then
      return 1
    fi
    if [[ "${status}" == *"Replica_IO_Running: Yes"* && "${status}" == *"Replica_SQL_Running: Yes"* ]]; then
      echo "Replication is already healthy; skip initialization"
      return 0
    fi
    if [[ "${announced}" == "false" ]]; then
      echo "Existing replication found but is not healthy yet; waiting before reinitializing"
      announced="true"
    fi
    retries=$(( retries - 1 ))
    sleep 2
  done
  return 1
}

wait_mysql() {
  local host="$1"
  local port="$2"
  local label="$3"
  local retries=60
  while (( retries > 0 )); do
    if MYSQL_PWD="${MYSQL_ROOT_PASSWORD}" mysqladmin -h"${host}" -P"${port}" -uroot --protocol=tcp ping --silent >/dev/null 2>&1; then
      echo "${label} is ready"
      return 0
    fi
    retries=$(( retries - 1 ))
    sleep 2
  done
  echo "Timed out waiting for ${label}" >&2
  return 1
}

wait_mysql "${MASTER_HOST}" "${MASTER_PORT}" "mysql-master"
wait_mysql "${SLAVE_HOST}" "${SLAVE_PORT}" "mysql-slave"

mysql_exec "${MASTER_HOST}" "${MASTER_PORT}" <<SQL
CREATE USER IF NOT EXISTS '${REPLICATION_USER}'@'%' IDENTIFIED BY '${REPLICATION_PASSWORD}';
GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO '${REPLICATION_USER}'@'%';
FLUSH PRIVILEGES;
SQL

if replica_already_healthy; then
  exit 0
fi

mysql_exec "${SLAVE_HOST}" "${SLAVE_PORT}" -e "SET GLOBAL super_read_only = OFF; SET GLOBAL read_only = OFF;"
mysql_exec "${SLAVE_HOST}" "${SLAVE_PORT}" -e "STOP REPLICA;" || true
mysql_exec "${SLAVE_HOST}" "${SLAVE_PORT}" -e "RESET REPLICA ALL;" || true
mysql_exec "${SLAVE_HOST}" "${SLAVE_PORT}" <<SQL
SET GLOBAL super_read_only = OFF;
SET GLOBAL read_only = OFF;
RESET BINARY LOGS AND GTIDS;
SQL

if [[ "${REPLICA_RESEED_ON_INIT}" == "true" ]]; then
  echo "Re-seeding mysql-slave database '${MYSQL_DATABASE}' from mysql-master"
  mysql_dump_master | mysql_exec "${SLAVE_HOST}" "${SLAVE_PORT}"
fi

mysql_exec "${SLAVE_HOST}" "${SLAVE_PORT}" <<SQL
CHANGE REPLICATION SOURCE TO
  SOURCE_HOST='${MASTER_HOST}',
  SOURCE_PORT=${MASTER_PORT},
  SOURCE_USER='${REPLICATION_USER}',
  SOURCE_PASSWORD='${REPLICATION_PASSWORD}',
  SOURCE_AUTO_POSITION=1,
  GET_SOURCE_PUBLIC_KEY=1;
START REPLICA;
SET GLOBAL read_only = ON;
SET GLOBAL super_read_only = ON;
SQL

retries=60
while (( retries > 0 )); do
  io_state="$(mysql_exec "${SLAVE_HOST}" "${SLAVE_PORT}" -Nse "SELECT SERVICE_STATE FROM performance_schema.replication_connection_status LIMIT 1;" || true)"
  sql_state="$(mysql_exec "${SLAVE_HOST}" "${SLAVE_PORT}" -Nse "SELECT SERVICE_STATE FROM performance_schema.replication_applier_status LIMIT 1;" || true)"
  if [[ "${io_state}" == "ON" && "${sql_state}" == "ON" ]]; then
    echo "Replication configured successfully"
    exit 0
  fi
  retries=$(( retries - 1 ))
  sleep 2
done

echo "Replication did not reach a healthy state in time" >&2
mysql_exec "${SLAVE_HOST}" "${SLAVE_PORT}" -e "SHOW REPLICA STATUS\\G" >&2 || true
exit 1
