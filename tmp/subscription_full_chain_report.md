# Subscription Full-Chain Smoke

- Base URL: http://127.0.0.1:48080
- DB: 192.168.101.66:3307/ruoyi-vue-pro
- Started: 2026-04-14 15:38:26

## 1. Seed fixture data
fixture_spu_count	fixture_sku_count	covered_grade_count
120	252	12
fixture_parent_count	fixture_student_count	fixture_student_class_count	fixture_school_class_count
10	18	18	13
PASS fixture counts: publications=120, students=18
## 2. Login
PASS admin/app login
## 3. Prepare rule-center windows through admin API
PASS available-page P1
{"code":0,"msg":"","data":{"createdWindowSpuCount":5,"createdGradeCount":5,"skippedCount":0,"skippedItems":[]}}
{"code":0,"msg":"","data":{"createdWindowSpuCount":4,"createdGradeCount":4,"skippedCount":0,"skippedItems":[]}}
{"code":0,"msg":"","data":{"createdWindowSpuCount":2,"createdGradeCount":2,"skippedCount":0,"skippedItems":[]}}
{"code":0,"msg":"","data":{"createdWindowSpuCount":1,"createdGradeCount":1,"skippedCount":0,"skippedItems":[]}}
PASS duplicate batch-create skip
## 4. Admin preview assertions
PASS admin current-chain preview
PASS admin promoted-chain preview
PASS admin presell preview
PASS admin closed-window preview
## 5. App API assertions
PASS app API

## Result
PASS subscription publication fixture + rule-center + preview + app full-chain smoke
- Finished: 2026-04-14 15:38:41
