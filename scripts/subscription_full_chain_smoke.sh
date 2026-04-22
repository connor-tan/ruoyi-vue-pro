#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

BASE_URL="${BASE_URL:-http://127.0.0.1:48080}"
MYSQL_BIN="${MYSQL_BIN:-/opt/homebrew/opt/mysql-client/bin/mysql}"
DB_HOST="${DB_HOST:-192.168.101.66}"
DB_PORT="${DB_PORT:-3307}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-123456}"
DB_NAME="${DB_NAME:-ruoyi-vue-pro}"
TENANT_ID="${TENANT_ID:-1}"
ADMIN_USERNAME="${ADMIN_USERNAME:-admin}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-admin123}"
APP_PASSWORD="${APP_PASSWORD:-Test@123456}"
REPORT_PATH="${REPORT_PATH:-${ROOT_DIR}/tmp/subscription_full_chain_report.md}"

PUBLICATION_SQL="${ROOT_DIR}/sql/mysql/20260413_publication_fixture_seed.sql"
E2E_SQL="${ROOT_DIR}/sql/mysql/20260413_subscription_e2e_fixture_seed.sql"

ADMIN_TOKEN=""
APP_TOKEN=""

mkdir -p "$(dirname "${REPORT_PATH}")"
: > "${REPORT_PATH}"

log() {
  printf '%s\n' "$*" | tee -a "${REPORT_PATH}"
}

mysql_exec() {
  "${MYSQL_BIN}" -h "${DB_HOST}" -P "${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" -D "${DB_NAME}" -N -B -e "$1"
}

mysql_file() {
  "${MYSQL_BIN}" -h "${DB_HOST}" -P "${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" -D "${DB_NAME}" < "$1"
}

year_catalog_id() {
  local year_start="$1"
  local year_end="$2"
  mysql_exec "SELECT id FROM edu_year_catalog WHERE year_start = ${year_start} AND year_end = ${year_end} AND deleted = b'0' ORDER BY id DESC LIMIT 1;"
}

json_assert_success() {
  local response="$1"
  local label="$2"
  local code
  code="$(printf '%s' "${response}" | jq -r '.code // empty')"
  if [[ "${code}" != "0" ]]; then
    log "FAIL ${label}: ${response}"
    exit 1
  fi
}

admin_request() {
  local method="$1"
  local path="$2"
  local body="${3:-}"
  if [[ -n "${body}" ]]; then
    curl -sS -X "${method}" "${BASE_URL}/admin-api${path}" \
      -H "Authorization: Bearer ${ADMIN_TOKEN}" \
      -H "tenant-id: ${TENANT_ID}" \
      -H "Content-Type: application/json" \
      --data "${body}"
  else
    curl -sS -X "${method}" "${BASE_URL}/admin-api${path}" \
      -H "Authorization: Bearer ${ADMIN_TOKEN}" \
      -H "tenant-id: ${TENANT_ID}"
  fi
}

app_request() {
  local method="$1"
  local path="$2"
  curl -sS -X "${method}" "${BASE_URL}/app-api${path}" \
    -H "Authorization: Bearer ${APP_TOKEN}" \
    -H "tenant-id: ${TENANT_ID}"
}

admin_login() {
  local response
  response="$(curl -sS -X POST "${BASE_URL}/admin-api/system/auth/login" \
    -H "tenant-id: ${TENANT_ID}" \
    -H "Content-Type: application/json" \
    --data "{\"username\":\"${ADMIN_USERNAME}\",\"password\":\"${ADMIN_PASSWORD}\"}")"
  json_assert_success "${response}" "admin login"
  ADMIN_TOKEN="$(printf '%s' "${response}" | jq -r '.data.accessToken')"
  if [[ -z "${ADMIN_TOKEN}" || "${ADMIN_TOKEN}" == "null" ]]; then
    log "FAIL admin login: accessToken missing"
    exit 1
  fi
}

app_login() {
  local mobile="$1"
  local response
  response="$(curl -sS -X POST "${BASE_URL}/app-api/member/auth/login" \
    -H "tenant-id: ${TENANT_ID}" \
    -H "Content-Type: application/json" \
    --data "{\"mobile\":\"${mobile}\",\"password\":\"${APP_PASSWORD}\"}")"
  json_assert_success "${response}" "app login ${mobile}"
  APP_TOKEN="$(printf '%s' "${response}" | jq -r '.data.accessToken')"
  if [[ -z "${APP_TOKEN}" || "${APP_TOKEN}" == "null" ]]; then
    log "FAIL app login ${mobile}: accessToken missing"
    exit 1
  fi
}

cleanup_smoke_rules() {
  mysql_exec "
    DROP TEMPORARY TABLE IF EXISTS tmp_smoke_window_ids;
    CREATE TEMPORARY TABLE tmp_smoke_window_ids AS
    SELECT id FROM sub_window
    WHERE creator = 'codex-subscription-smoke'
       OR name LIKE 'SUB_E2E_%';

    DROP TEMPORARY TABLE IF EXISTS tmp_smoke_window_spu_ids;
    CREATE TEMPORARY TABLE tmp_smoke_window_spu_ids AS
    SELECT id FROM sub_window_spu
    WHERE window_id IN (SELECT id FROM tmp_smoke_window_ids)
       OR creator = 'codex-subscription-smoke';

    DELETE FROM sub_window_sku
    WHERE window_spu_id IN (SELECT id FROM tmp_smoke_window_spu_ids)
       OR creator = 'codex-subscription-smoke';

    DELETE FROM sub_window_spu_rule
    WHERE window_spu_id IN (SELECT id FROM tmp_smoke_window_spu_ids)
       OR creator = 'codex-subscription-smoke';

    DELETE FROM sub_window_spu_grade
    WHERE window_spu_id IN (SELECT id FROM tmp_smoke_window_spu_ids)
       OR creator = 'codex-subscription-smoke';

    DELETE FROM sub_window_spu
    WHERE id IN (SELECT id FROM tmp_smoke_window_spu_ids)
       OR creator = 'codex-subscription-smoke';

    DELETE FROM sub_window
    WHERE id IN (SELECT id FROM tmp_smoke_window_ids)
       OR creator = 'codex-subscription-smoke';

    DELETE FROM sub_window_template
    WHERE name = 'SUB_E2E_PROMOTED_CURRENT'
      AND built_in = b'0';
  " >/dev/null
}

activate_only_smoke_window() {
  local window_id="$1"
  mysql_exec "
    UPDATE sub_window
       SET status = 1, update_time = NOW(), updater = 'codex-subscription-smoke'
     WHERE creator = 'codex-subscription-smoke'
        OR name LIKE 'SUB_E2E_%';

    UPDATE sub_window
       SET status = 0,
           start_time = '2026-04-01 00:00:00',
           end_time = '2026-05-31 23:59:59',
           create_time = NOW(),
           update_time = NOW(),
           updater = 'codex-subscription-smoke'
     WHERE id = ${window_id};
  " >/dev/null
}

disable_smoke_windows() {
  mysql_exec "
    UPDATE sub_window
       SET status = 1, update_time = NOW(), updater = 'codex-subscription-smoke'
     WHERE creator = 'codex-subscription-smoke'
        OR name LIKE 'SUB_E2E_%';
  " >/dev/null
}

create_window() {
  local name="$1"
  local template_id="$2"
  local target_year_catalog_id="$3"
  local start_time="$4"
  local end_time="$5"
  local status="$6"
  local body
  local response
  body="$(jq -nc \
    --arg name "${name}" \
    --arg startTime "${start_time}" \
    --arg endTime "${end_time}" \
    --argjson templateId "${template_id}" \
    --argjson targetYearCatalogId "${target_year_catalog_id}" \
    --argjson status "${status}" \
    '{
      name: $name,
      startTime: $startTime,
      endTime: $endTime,
      targetYearCatalogId: $targetYearCatalogId,
      templateId: $templateId,
      status: $status,
      remark: "codex subscription full-chain smoke"
    }')"
  response="$(admin_request POST "/subscription/window/create" "${body}")"
  json_assert_success "${response}" "create window ${name}"
  printf '%s' "${response}" | jq -r '.data'
}

batch_create() {
  local window_id="$1"
  local items_json="$2"
  local body
  local response
  body="$(jq -nc --argjson windowId "${window_id}" --argjson items "${items_json}" '{windowId: $windowId, items: $items}')"
  response="$(admin_request POST "/subscription/window-spu/batch-create" "${body}")"
  json_assert_success "${response}" "batch-create window ${window_id}"
  local created_grade_count
  created_grade_count="$(printf '%s' "${response}" | jq -r '.data.createdGradeCount // 0')"
  if [[ "${created_grade_count}" -lt 1 ]]; then
    log "FAIL batch-create window ${window_id}: expected at least one grade relation, got ${response}"
    exit 1
  fi
  printf '%s\n' "${response}" >> "${REPORT_PATH}"
}

window_spu_id() {
  local window_id="$1"
  local spu_id="$2"
  mysql_exec "SELECT id FROM sub_window_spu WHERE window_id = ${window_id} AND product_spu_id = ${spu_id} AND deleted = b'0' ORDER BY id DESC LIMIT 1;"
}

student_id() {
  local student_name="$1"
  mysql_exec "SELECT id FROM edu_student WHERE student_name = '${student_name}' AND deleted = b'0' ORDER BY id DESC LIMIT 1;"
}

fixture_spu_id() {
  local seq="$1"
  mysql_exec "SELECT id FROM product_spu WHERE name LIKE CONCAT('SUB_FIX_PUB_', LPAD(${seq}, 3, '0'), '_%') AND deleted = b'0' ORDER BY id DESC LIMIT 1;"
}

assert_preview_contains() {
  local window_id="$1"
  local student="$2"
  local product_spu_id="$3"
  local label="$4"
  local response
  response="$(admin_request POST "/subscription/preview/execute" "{\"windowId\":${window_id},\"studentId\":${student}}")"
  json_assert_success "${response}" "preview ${label}"
  if ! printf '%s' "${response}" | jq -e --argjson spu "${product_spu_id}" '.data.publications // [] | any(.productSpuId == $spu)' >/dev/null; then
    log "FAIL preview ${label}: productSpuId ${product_spu_id} not visible"
    log "${response}"
    exit 1
  fi
}

assert_preview_not_contains() {
  local window_id="$1"
  local student="$2"
  local product_spu_id="$3"
  local label="$4"
  local response
  response="$(admin_request POST "/subscription/preview/execute" "{\"windowId\":${window_id},\"studentId\":${student}}")"
  json_assert_success "${response}" "preview ${label}"
  if printf '%s' "${response}" | jq -e --argjson spu "${product_spu_id}" '.data.publications // [] | any(.productSpuId == $spu)' >/dev/null; then
    log "FAIL preview ${label}: productSpuId ${product_spu_id} should not be visible"
    log "${response}"
    exit 1
  fi
}

assert_preview_blocked() {
  local window_id="$1"
  local student="$2"
  local keyword="$3"
  local label="$4"
  local response
  response="$(admin_request POST "/subscription/preview/execute" "{\"windowId\":${window_id},\"studentId\":${student}}")"
  json_assert_success "${response}" "preview blocked ${label}"
  if ! printf '%s' "${response}" | jq -e --arg keyword "${keyword}" '.data.blockedReason // "" | contains($keyword)' >/dev/null; then
    log "FAIL preview blocked ${label}: expected reason containing ${keyword}"
    log "${response}"
    exit 1
  fi
}

assert_app_page_contains() {
  local student="$1"
  local product_spu_id="$2"
  local label="$3"
  local response
  response="$(app_request GET "/subscription/publication/page?pageNo=1&pageSize=50&studentId=${student}")"
  json_assert_success "${response}" "app publication page ${label}"
  if ! printf '%s' "${response}" | jq -e --argjson spu "${product_spu_id}" '.data.list // [] | any(.productSpuId == $spu)' >/dev/null; then
    log "FAIL app publication page ${label}: productSpuId ${product_spu_id} not visible"
    log "${response}"
    exit 1
  fi
  response="$(app_request GET "/subscription/publication/get?studentId=${student}&productSpuId=${product_spu_id}")"
  json_assert_success "${response}" "app publication get ${label}"
}

log "# Subscription Full-Chain Smoke"
log ""
log "- Base URL: ${BASE_URL}"
log "- DB: ${DB_HOST}:${DB_PORT}/${DB_NAME}"
log "- Started: $(date '+%Y-%m-%d %H:%M:%S')"
log ""

command -v jq >/dev/null
"${MYSQL_BIN}" --version >/dev/null

log "## 1. Seed fixture data"
mysql_file "${PUBLICATION_SQL}" | tee -a "${REPORT_PATH}"
mysql_file "${E2E_SQL}" | tee -a "${REPORT_PATH}"

publication_count="$(mysql_exec "SELECT COUNT(*) FROM product_spu WHERE creator = 'codex-publication-fixture' AND deleted = b'0';")"
student_count="$(mysql_exec "SELECT COUNT(*) FROM edu_student WHERE creator = 'codex-subscription-e2e' AND deleted = b'0';")"
if [[ "${publication_count}" -lt 120 || "${student_count}" -ne 18 ]]; then
  log "FAIL fixture counts: publications=${publication_count}, students=${student_count}"
  exit 1
fi
log "PASS fixture counts: publications=${publication_count}, students=${student_count}"

log "## 2. Login"
admin_login
app_login "18866660001"
log "PASS admin/app login"

log "## 3. Prepare rule-center windows through admin API"
cleanup_smoke_rules

promoted_template_response="$(admin_request POST "/subscription/window-template/create" '{
  "name":"SUB_E2E_PROMOTED_CURRENT",
  "targetPeriod":"FIRST_TERM",
  "gradeCalcRule":"PROMOTED_GRADE",
  "gradeResolveMode":"CURRENT_CHAIN",
  "description":"subscription smoke promoted-current template",
  "status":0,
  "sort":9900,
  "remark":"codex subscription full-chain smoke"
}')"
json_assert_success "${promoted_template_response}" "create promoted template"
promoted_template_id="$(printf '%s' "${promoted_template_response}" | jq -r '.data')"

current_template_id="$(mysql_exec "SELECT id FROM sub_window_template WHERE code = 'BACK_TO_SCHOOL_RESTOCK' AND deleted = b'0' ORDER BY id DESC LIMIT 1;")"
presell_template_id="$(mysql_exec "SELECT id FROM sub_window_template WHERE code = 'NEW_YEAR_PRE_SALE' AND deleted = b'0' ORDER BY id DESC LIMIT 1;")"
target_year_catalog_id_2026="$(year_catalog_id 2026 2027)"
if [[ -z "${target_year_catalog_id_2026}" ]]; then
  log "FAIL missing edu_year_catalog 2026-2027"
  exit 1
fi

current_window_id="$(create_window "SUB_E2E_W_APP_CURRENT_OPEN" "${current_template_id}" "${target_year_catalog_id_2026}" "2026-04-01 00:00:00" "2026-05-31 23:59:59" 1)"
presell_window_id="$(create_window "SUB_E2E_W_APP_PRESELL_OPEN" "${presell_template_id}" "${target_year_catalog_id_2026}" "2026-04-01 00:00:00" "2026-05-31 23:59:59" 1)"
promoted_window_id="$(create_window "SUB_E2E_W_ADMIN_PROMOTED_OPEN" "${promoted_template_id}" "${target_year_catalog_id_2026}" "2026-04-01 00:00:00" "2026-05-31 23:59:59" 1)"
closed_window_id="$(create_window "SUB_E2E_W_ADMIN_CLOSED" "${current_template_id}" "${target_year_catalog_id_2026}" "2026-01-01 00:00:00" "2026-02-01 00:00:00" 1)"

mysql_exec "UPDATE sub_window SET creator = 'codex-subscription-smoke', updater = 'codex-subscription-smoke' WHERE id IN (${current_window_id},${presell_window_id},${promoted_window_id},${closed_window_id}); UPDATE sub_window_template SET creator = 'codex-subscription-smoke', updater = 'codex-subscription-smoke' WHERE id = ${promoted_template_id};" >/dev/null

spu_k1="$(fixture_spu_id 1)"
spu_p1="$(fixture_spu_id 16)"
spu_p1_disabled="$(fixture_spu_id 17)"
spu_p2="$(fixture_spu_id 21)"
spu_m1="$(fixture_spu_id 46)"

available_response="$(admin_request GET "/subscription/window-spu/available-page?pageNo=1&pageSize=10&windowId=${current_window_id}&baseGradeCatalogIds=4")"
json_assert_success "${available_response}" "available-page P1"
if [[ "$(printf '%s' "${available_response}" | jq -r '.data.total')" -lt 1 ]]; then
  log "FAIL available-page P1 returned no candidates"
  log "${available_response}"
  exit 1
fi
log "PASS available-page P1"

batch_create "${current_window_id}" "[
  {\"gradeCatalogId\":1,\"productSpuId\":${spu_k1}},
  {\"gradeCatalogId\":4,\"productSpuId\":${spu_p1}},
  {\"gradeCatalogId\":4,\"productSpuId\":${spu_p1_disabled}},
  {\"gradeCatalogId\":5,\"productSpuId\":${spu_p2}},
  {\"gradeCatalogId\":10,\"productSpuId\":${spu_m1}}
]"
batch_create "${presell_window_id}" "[
  {\"gradeCatalogId\":1,\"productSpuId\":${spu_k1}},
  {\"gradeCatalogId\":4,\"productSpuId\":${spu_p1}},
  {\"gradeCatalogId\":5,\"productSpuId\":${spu_p2}},
  {\"gradeCatalogId\":10,\"productSpuId\":${spu_m1}}
]"
batch_create "${promoted_window_id}" "[
  {\"gradeCatalogId\":3,\"productSpuId\":$(fixture_spu_id 11)},
  {\"gradeCatalogId\":5,\"productSpuId\":${spu_p2}}
]"
batch_create "${closed_window_id}" "[
  {\"gradeCatalogId\":4,\"productSpuId\":${spu_p1}}
]"

ws_p2_current="$(window_spu_id "${current_window_id}" "${spu_p2}")"
school_newcity="$(mysql_exec "SELECT id FROM edu_school WHERE school_name = '新城小学' AND deleted = b'0' ORDER BY id DESC LIMIT 1;")"
rule_include_grade="$(admin_request POST "/subscription/window-spu-rule/create" "{\"windowSpuId\":${ws_p2_current},\"effectType\":\"INCLUDE\",\"scopeType\":\"GRADE\",\"gradeCatalogId\":4,\"sort\":10,\"remark\":\"smoke include p1\"}")"
json_assert_success "${rule_include_grade}" "create INCLUDE_GRADE rule"
rule_exclude_school_grade="$(admin_request POST "/subscription/window-spu-rule/create" "{\"windowSpuId\":${ws_p2_current},\"effectType\":\"EXCLUDE\",\"scopeType\":\"SCHOOL_GRADE\",\"schoolId\":${school_newcity},\"gradeCatalogId\":4,\"sort\":20,\"remark\":\"smoke exclude newcity p1\"}")"
json_assert_success "${rule_exclude_school_grade}" "create EXCLUDE_SCHOOL_GRADE rule"

ws_disabled="$(window_spu_id "${current_window_id}" "${spu_p1_disabled}")"
sku_list="$(admin_request GET "/subscription/window-sku/list-by-window-spu?windowSpuId=${ws_disabled}")"
json_assert_success "${sku_list}" "window-sku list"
sku_update_items="$(printf '%s' "${sku_list}" | jq -c '[.data[] | {id, status: 1, sort, maxQuantityPerStudent, remark}]')"
sku_update="$(admin_request PUT "/subscription/window-sku/batch-update" "$(jq -nc --argjson windowSpuId "${ws_disabled}" --argjson items "${sku_update_items}" '{windowSpuId: $windowSpuId, items: $items}')")"
json_assert_success "${sku_update}" "disable all SKUs for fixture SPU"

duplicate_response="$(admin_request POST "/subscription/window-spu/batch-create" "$(jq -nc --argjson windowId "${current_window_id}" --argjson spu "${spu_p1}" '{windowId: $windowId, items: [{gradeCatalogId: 4, productSpuId: $spu}]}')")"
json_assert_success "${duplicate_response}" "duplicate batch-create"
if [[ "$(printf '%s' "${duplicate_response}" | jq -r '.data.skippedCount // 0')" -lt 1 ]]; then
  log "FAIL duplicate batch-create should skip existing grade relation: ${duplicate_response}"
  exit 1
fi
log "PASS duplicate batch-create skip"

student_current_p1="$(student_id 'SUB_E2E_当前一年级')"
student_current_k1="$(student_id 'SUB_E2E_当前小班')"
student_yuhong_p1="$(student_id 'SUB_E2E_学校规则育红')"
student_newcity_p1="$(student_id 'SUB_E2E_学校规则新城')"
student_future_p1="$(student_id 'SUB_E2E_未来一年级')"
student_future_cross="$(student_id 'SUB_E2E_未来跨校')"
student_pending_future="$(student_id 'SUB_E2E_待升学有未来班')"
student_pending_missing="$(student_id 'SUB_E2E_待升学无未来班')"
student_current_p6="$(student_id 'SUB_E2E_当前六年级')"

log "## 4. Admin preview assertions"
activate_only_smoke_window "${current_window_id}"
assert_preview_contains "${current_window_id}" "${student_current_p1}" "${spu_p1}" "CURRENT_CHAIN current P1 base grade"
assert_preview_contains "${current_window_id}" "${student_current_k1}" "${spu_k1}" "CURRENT_CHAIN current K1 base grade"
assert_preview_contains "${current_window_id}" "${student_yuhong_p1}" "${spu_p2}" "INCLUDE_GRADE allows P1 from Yuhong"
assert_preview_not_contains "${current_window_id}" "${student_newcity_p1}" "${spu_p2}" "EXCLUDE_SCHOOL_GRADE overrides INCLUDE"
assert_preview_not_contains "${current_window_id}" "${student_current_p1}" "${spu_p1_disabled}" "all window SKU disabled hides SPU"
log "PASS admin current-chain preview"

activate_only_smoke_window "${promoted_window_id}"
assert_preview_contains "${promoted_window_id}" "${student_current_p1}" "${spu_p2}" "PROMOTED_GRADE P1 -> P2"
assert_preview_blocked "${promoted_window_id}" "${student_current_p6}" "末级" "terminal P6 promoted grade"
log "PASS admin promoted-chain preview"

activate_only_smoke_window "${presell_window_id}"
assert_preview_contains "${presell_window_id}" "${student_future_p1}" "${spu_p1}" "TARGET_CLASS_FIRST future P1"
assert_preview_contains "${presell_window_id}" "${student_future_cross}" "${spu_p2}" "TARGET_CLASS_FIRST cross-school future P2"
assert_preview_contains "${presell_window_id}" "${student_pending_future}" "${spu_k1}" "PENDING_ADVANCE with future class"
assert_preview_blocked "${presell_window_id}" "${student_pending_missing}" "目标学年班级绑定" "PENDING_ADVANCE missing future class"
log "PASS admin presell preview"

disable_smoke_windows
mysql_exec "UPDATE sub_window SET status = 0, start_time = '2026-01-01 00:00:00', end_time = '2026-02-01 00:00:00', update_time = NOW() WHERE id = ${closed_window_id};" >/dev/null
assert_preview_blocked "${closed_window_id}" "${student_current_p1}" "窗口未开放" "closed window"
log "PASS admin closed-window preview"

log "## 5. App API assertions"
activate_only_smoke_window "${current_window_id}"
app_login "18866660001"
current_window_response="$(app_request GET "/subscription/window/current")"
json_assert_success "${current_window_response}" "app current window current-chain"
if ! printf '%s' "${current_window_response}" | jq -e --argjson id "${current_window_id}" '.data.id == $id' >/dev/null; then
  log "FAIL app current window should be current-chain fixture: ${current_window_response}"
  exit 1
fi
assert_app_page_contains "${student_current_p1}" "${spu_p1}" "CURRENT_CHAIN app page/get"

activate_only_smoke_window "${presell_window_id}"
app_login "18866660003"
presell_window_response="$(app_request GET "/subscription/window/current")"
json_assert_success "${presell_window_response}" "app current window presell"
if ! printf '%s' "${presell_window_response}" | jq -e --argjson id "${presell_window_id}" '.data.id == $id' >/dev/null; then
  log "FAIL app current window should be presell fixture: ${presell_window_response}"
  exit 1
fi
assert_app_page_contains "${student_future_p1}" "${spu_p1}" "TARGET_CLASS_FIRST app page/get"
log "PASS app API"

disable_smoke_windows

log ""
log "## Result"
log "PASS subscription publication fixture + rule-center + preview + app full-chain smoke"
log "- Finished: $(date '+%Y-%m-%d %H:%M:%S')"
