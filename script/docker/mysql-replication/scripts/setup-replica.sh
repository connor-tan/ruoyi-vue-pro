#!/bin/bash

set -euo pipefail

MASTER_HOST="${MASTER_HOST:-mysql-master}"
MASTER_PORT="${MASTER_PORT:-3306}"
SLAVE_HOST="${SLAVE_HOST:-mysql-slave}"
SLAVE_PORT="${SLAVE_PORT:-3306}"
MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-123456}"
REPLICATION_USER="${REPLICATION_USER:-repl}"
REPLICATION_PASSWORD="${REPLICATION_PASSWORD:-repl123456}"

mysql_exec() {
  local host="$1"
  local port="$2"
  shift 2
  mysql -h"${host}" -P"${port}" -uroot -p"${MYSQL_ROOT_PASSWORD}" --protocol=tcp "$@"
}

wait_mysql() {
  local host="$1"
  local port="$2"
  local label="$3"
  local retries=60
  while (( retries > 0 )); do
    if mysqladmin -h"${host}" -P"${port}" -uroot -p"${MYSQL_ROOT_PASSWORD}" --protocol=tcp ping --silent >/dev/null 2>&1; then
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

mysql_exec "${SLAVE_HOST}" "${SLAVE_PORT}" <<SQL
SET GLOBAL super_read_only = OFF;
SET GLOBAL read_only = OFF;
STOP REPLICA;
RESET REPLICA ALL;
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
