SET NAMES utf8mb4;

-- Dedicated subscription E2E students and class bindings.
-- Repeatable cleanup is limited to creator/mark/name prefixes used by this script.

DROP TEMPORARY TABLE IF EXISTS tmp_sub_e2e_student_ids;
CREATE TEMPORARY TABLE tmp_sub_e2e_student_ids AS
SELECT id
FROM edu_student
WHERE creator = 'codex-subscription-e2e'
   OR student_name LIKE 'SUB_E2E_%';

DELETE FROM edu_student_class
WHERE student_id IN (SELECT id FROM tmp_sub_e2e_student_ids)
   OR creator = 'codex-subscription-e2e';

DELETE FROM edu_student
WHERE id IN (SELECT id FROM tmp_sub_e2e_student_ids);

DELETE FROM member_user
WHERE mark = 'SUBSCRIPTION_E2E_FIXTURE'
   OR mobile BETWEEN '18866660001' AND '18866660010';

DROP TEMPORARY TABLE IF EXISTS tmp_sub_e2e_class_ids;
CREATE TEMPORARY TABLE tmp_sub_e2e_class_ids AS
SELECT id
FROM edu_school_class
WHERE creator = 'codex-subscription-e2e'
  AND class_no IN (98, 99);

DELETE FROM edu_school_class
WHERE id IN (SELECT id FROM tmp_sub_e2e_class_ids);

DELETE y
FROM edu_school_year y
LEFT JOIN edu_school_class c ON c.school_year_id = y.id AND c.deleted = b'0'
WHERE y.creator = 'codex-subscription-e2e'
  AND y.year_start IN (2025, 2026)
  AND c.id IS NULL;

DROP TEMPORARY TABLE IF EXISTS tmp_sub_e2e_school;
CREATE TEMPORARY TABLE tmp_sub_e2e_school (
    school_name VARCHAR(128) NOT NULL PRIMARY KEY
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

INSERT INTO tmp_sub_e2e_school (school_name) VALUES
    ('宋庆龄幼儿园'),
    ('新城小学'),
    ('江南中学（新城校区）'),
    ('育红小学');

DROP TEMPORARY TABLE IF EXISTS tmp_sub_e2e_school_resolved;
CREATE TEMPORARY TABLE tmp_sub_e2e_school_resolved AS
SELECT s.id AS school_id, s.school_name, s.area_id
FROM edu_school s
JOIN tmp_sub_e2e_school required_school ON required_school.school_name = s.school_name
WHERE s.deleted = b'0';

DROP TEMPORARY TABLE IF EXISTS tmp_sub_e2e_current_school;
CREATE TEMPORARY TABLE tmp_sub_e2e_current_school AS
SELECT * FROM tmp_sub_e2e_school_resolved;

DROP TEMPORARY TABLE IF EXISTS tmp_sub_e2e_future_school;
CREATE TEMPORARY TABLE tmp_sub_e2e_future_school AS
SELECT * FROM tmp_sub_e2e_school_resolved;

DROP TEMPORARY TABLE IF EXISTS tmp_sub_e2e_fallback_school;
CREATE TEMPORARY TABLE tmp_sub_e2e_fallback_school AS
SELECT * FROM tmp_sub_e2e_school_resolved;

INSERT INTO edu_school_year (
    school_id, year_start, year_end, start_date, end_date, creator, updater, deleted
)
SELECT school_id, 2025, 2026, '2025-09-01', '2026-06-30',
       'codex-subscription-e2e', 'codex-subscription-e2e', b'0'
FROM tmp_sub_e2e_school_resolved school
WHERE NOT EXISTS (
    SELECT 1 FROM edu_school_year y
    WHERE y.school_id = school.school_id
      AND y.year_start = 2025
      AND y.deleted = b'0'
);

INSERT INTO edu_school_year (
    school_id, year_start, year_end, start_date, end_date, creator, updater, deleted
)
SELECT school_id, 2026, 2027, '2026-09-01', '2027-06-30',
       'codex-subscription-e2e', 'codex-subscription-e2e', b'0'
FROM tmp_sub_e2e_school_resolved school
WHERE NOT EXISTS (
    SELECT 1 FROM edu_school_year y
    WHERE y.school_id = school.school_id
      AND y.year_start = 2026
      AND y.deleted = b'0'
);

DROP TEMPORARY TABLE IF EXISTS tmp_sub_e2e_class_req;
CREATE TEMPORARY TABLE tmp_sub_e2e_class_req (
    school_name VARCHAR(128) NOT NULL,
    grade_catalog_id BIGINT NOT NULL,
    year_start INT NOT NULL,
    class_no INT NOT NULL,
    PRIMARY KEY (school_name, grade_catalog_id, year_start, class_no)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

INSERT INTO tmp_sub_e2e_class_req (school_name, grade_catalog_id, year_start, class_no) VALUES
    ('宋庆龄幼儿园', 1, 2025, 99),
    ('宋庆龄幼儿园', 2, 2025, 99),
    ('宋庆龄幼儿园', 3, 2025, 99),
    ('宋庆龄幼儿园', 1, 2026, 98),
    ('新城小学', 4, 2025, 99),
    ('新城小学', 5, 2025, 99),
    ('新城小学', 9, 2025, 99),
    ('新城小学', 4, 2026, 98),
    ('江南中学（新城校区）', 10, 2025, 99),
    ('江南中学（新城校区）', 12, 2025, 99),
    ('江南中学（新城校区）', 10, 2026, 98),
    ('育红小学', 4, 2025, 99),
    ('育红小学', 5, 2026, 98);

INSERT INTO edu_school_class (
    school_id, entry_year, school_grade_id, school_year_id, class_no, class_name, creator, updater, deleted
)
SELECT
    school.school_id,
    req.year_start - CASE
        WHEN req.grade_catalog_id BETWEEN 1 AND 3 THEN req.grade_catalog_id - 1
        WHEN req.grade_catalog_id BETWEEN 4 AND 9 THEN req.grade_catalog_id - 4
        ELSE req.grade_catalog_id - 10
    END AS entry_year,
    school_grade.id,
    school_year.id,
    req.class_no,
    CONCAT(
        req.year_start - CASE
            WHEN req.grade_catalog_id BETWEEN 1 AND 3 THEN req.grade_catalog_id - 1
            WHEN req.grade_catalog_id BETWEEN 4 AND 9 THEN req.grade_catalog_id - 4
            ELSE req.grade_catalog_id - 10
        END,
        '级',
        grade.grade_name,
        req.class_no,
        '班'
    ) AS class_name,
    'codex-subscription-e2e',
    'codex-subscription-e2e',
    b'0'
FROM tmp_sub_e2e_class_req req
JOIN tmp_sub_e2e_school_resolved school ON school.school_name = req.school_name
JOIN edu_school_grade school_grade
  ON school_grade.school_id = school.school_id
 AND school_grade.grade_catalog_id = req.grade_catalog_id
 AND school_grade.deleted = b'0'
JOIN edu_grade_catalog grade ON grade.id = req.grade_catalog_id AND grade.deleted = b'0'
JOIN edu_school_year school_year
  ON school_year.school_id = school.school_id
 AND school_year.year_start = req.year_start
 AND school_year.deleted = b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM edu_school_class existing
    WHERE existing.school_id = school.school_id
      AND existing.school_year_id = school_year.id
      AND existing.school_grade_id = school_grade.id
      AND existing.class_no = req.class_no
      AND existing.deleted = b'0'
);

DROP TEMPORARY TABLE IF EXISTS tmp_sub_e2e_parent_plan;
CREATE TEMPORARY TABLE tmp_sub_e2e_parent_plan (
    parent_no INT NOT NULL PRIMARY KEY,
    mobile VARCHAR(11) NOT NULL,
    parent_name VARCHAR(30) NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

INSERT INTO tmp_sub_e2e_parent_plan (parent_no, mobile, parent_name) VALUES
    (1, '18866660001', '订刊E2E家长01'),
    (2, '18866660002', '订刊E2E家长02'),
    (3, '18866660003', '订刊E2E家长03'),
    (4, '18866660004', '订刊E2E家长04'),
    (5, '18866660005', '订刊E2E家长05'),
    (6, '18866660006', '订刊E2E家长06'),
    (7, '18866660007', '订刊E2E家长07'),
    (8, '18866660008', '订刊E2E家长08'),
    (9, '18866660009', '订刊E2E家长09'),
    (10, '18866660010', '订刊E2E家长10');

INSERT INTO member_user (
    mobile, password, status, register_ip, register_terminal, login_ip, login_date, nickname, avatar, name,
    sex, area_id, birthday, mark, point, tag_ids, level_id, experience, group_id, creator, updater, deleted, tenant_id
)
SELECT
    parent.mobile,
    '$2a$10$qX0OT1lk05ri1czou.DB/O/LRXfLOugygo/90JhL4oksTp5Va8L/6',
    0,
    '127.0.0.1',
    20,
    '',
    NULL,
    parent.parent_name,
    '',
    parent.parent_name,
    0,
    COALESCE(school.area_id, 320214),
    NULL,
    'SUBSCRIPTION_E2E_FIXTURE',
    0,
    NULL,
    NULL,
    0,
    NULL,
    'codex-subscription-e2e',
    'codex-subscription-e2e',
    b'0',
    1
FROM tmp_sub_e2e_parent_plan parent
LEFT JOIN tmp_sub_e2e_school_resolved school ON school.school_name = '宋庆龄幼儿园';

DROP TEMPORARY TABLE IF EXISTS tmp_sub_e2e_student_plan;
CREATE TEMPORARY TABLE tmp_sub_e2e_student_plan (
    row_no INT NOT NULL PRIMARY KEY,
    student_name VARCHAR(20) NOT NULL,
    parent_no INT NOT NULL,
    status TINYINT NOT NULL,
    current_school_name VARCHAR(128) NULL,
    current_grade_catalog_id BIGINT NULL,
    future_school_name VARCHAR(128) NULL,
    future_grade_catalog_id BIGINT NULL,
    student_code INT NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

INSERT INTO tmp_sub_e2e_student_plan (
    row_no, student_name, parent_no, status, current_school_name, current_grade_catalog_id,
    future_school_name, future_grade_catalog_id, student_code
) VALUES
    (1, 'SUB_E2E_当前小班', 1, 1, '宋庆龄幼儿园', 1, NULL, NULL, 91000001),
    (2, 'SUB_E2E_当前一年级', 1, 1, '新城小学', 4, NULL, NULL, 91000002),
    (3, 'SUB_E2E_当前六年级', 2, 1, '新城小学', 9, NULL, NULL, 91000003),
    (4, 'SUB_E2E_当前初一', 2, 1, '江南中学（新城校区）', 10, NULL, NULL, 91000004),
    (5, 'SUB_E2E_未来小班', 3, 1, NULL, NULL, '宋庆龄幼儿园', 1, 91000005),
    (6, 'SUB_E2E_未来一年级', 3, 1, NULL, NULL, '新城小学', 4, 91000006),
    (7, 'SUB_E2E_未来初一', 4, 1, NULL, NULL, '江南中学（新城校区）', 10, 91000007),
    (8, 'SUB_E2E_待升学有未来班', 4, 4, NULL, NULL, '宋庆龄幼儿园', 1, 91000008),
    (9, 'SUB_E2E_待升学无未来班', 5, 4, NULL, NULL, NULL, NULL, 91000009),
    (10, 'SUB_E2E_未来跨校', 5, 1, '新城小学', 4, '育红小学', 5, 91000010),
    (11, 'SUB_E2E_当前大班', 6, 1, '宋庆龄幼儿园', 3, NULL, NULL, 91000011),
    (12, 'SUB_E2E_当前初三', 6, 1, '江南中学（新城校区）', 12, NULL, NULL, 91000012),
    (13, 'SUB_E2E_当前二年级', 7, 1, '新城小学', 5, NULL, NULL, 91000013),
    (14, 'SUB_E2E_当前中班', 7, 1, '宋庆龄幼儿园', 2, NULL, NULL, 91000014),
    (15, 'SUB_E2E_学校规则新城', 8, 1, '新城小学', 4, NULL, NULL, 91000015),
    (16, 'SUB_E2E_学校规则育红', 8, 1, '育红小学', 4, NULL, NULL, 91000016),
    (17, 'SUB_E2E_未来二年级育红', 9, 1, NULL, NULL, '育红小学', 5, 91000017),
    (18, 'SUB_E2E_休学学生', 10, 3, '新城小学', 4, NULL, NULL, 91000018);

INSERT INTO edu_student (
    student_name, belong_to, current_school_id, entry_year, student_code, status, creator, updater, deleted
)
SELECT
    plan.student_name,
    parent_user.id,
    COALESCE(current_school.school_id, future_school.school_id, fallback_school.school_id),
    CASE
        WHEN plan.current_grade_catalog_id IS NOT NULL THEN
            2025 - CASE
                WHEN plan.current_grade_catalog_id BETWEEN 1 AND 3 THEN plan.current_grade_catalog_id - 1
                WHEN plan.current_grade_catalog_id BETWEEN 4 AND 9 THEN plan.current_grade_catalog_id - 4
                ELSE plan.current_grade_catalog_id - 10
            END
        WHEN plan.future_grade_catalog_id IS NOT NULL THEN
            2026 - CASE
                WHEN plan.future_grade_catalog_id BETWEEN 1 AND 3 THEN plan.future_grade_catalog_id - 1
                WHEN plan.future_grade_catalog_id BETWEEN 4 AND 9 THEN plan.future_grade_catalog_id - 4
                ELSE plan.future_grade_catalog_id - 10
            END
        ELSE 2026
    END,
    plan.student_code,
    plan.status,
    'codex-subscription-e2e',
    'codex-subscription-e2e',
    b'0'
FROM tmp_sub_e2e_student_plan plan
JOIN tmp_sub_e2e_parent_plan parent_plan ON parent_plan.parent_no = plan.parent_no
JOIN member_user parent_user ON parent_user.mobile = parent_plan.mobile COLLATE utf8mb4_general_ci AND parent_user.deleted = b'0'
LEFT JOIN tmp_sub_e2e_current_school current_school ON current_school.school_name = plan.current_school_name
LEFT JOIN tmp_sub_e2e_future_school future_school ON future_school.school_name = plan.future_school_name
LEFT JOIN tmp_sub_e2e_fallback_school fallback_school ON fallback_school.school_name = '宋庆龄幼儿园';

INSERT INTO edu_student_class (
    student_id, class_id, start_date, end_date, creator, updater, deleted
)
SELECT
    student.id,
    class.id,
    '2025-09-01',
    NULL,
    'codex-subscription-e2e',
    'codex-subscription-e2e',
    b'0'
FROM tmp_sub_e2e_student_plan plan
JOIN edu_student student ON student.student_code = plan.student_code AND student.creator = 'codex-subscription-e2e' AND student.deleted = b'0'
JOIN tmp_sub_e2e_school_resolved school ON school.school_name = plan.current_school_name
JOIN edu_school_year school_year ON school_year.school_id = school.school_id AND school_year.year_start = 2025 AND school_year.deleted = b'0'
JOIN edu_school_grade school_grade
  ON school_grade.school_id = school.school_id
 AND school_grade.grade_catalog_id = plan.current_grade_catalog_id
 AND school_grade.deleted = b'0'
JOIN edu_school_class class
  ON class.school_id = school.school_id
 AND class.school_year_id = school_year.id
 AND class.school_grade_id = school_grade.id
 AND class.class_no = 99
 AND class.deleted = b'0'
WHERE plan.current_grade_catalog_id IS NOT NULL;

INSERT INTO edu_student_class (
    student_id, class_id, start_date, end_date, creator, updater, deleted
)
SELECT
    student.id,
    class.id,
    '2026-09-01',
    NULL,
    'codex-subscription-e2e',
    'codex-subscription-e2e',
    b'0'
FROM tmp_sub_e2e_student_plan plan
JOIN edu_student student ON student.student_code = plan.student_code AND student.creator = 'codex-subscription-e2e' AND student.deleted = b'0'
JOIN tmp_sub_e2e_school_resolved school ON school.school_name = plan.future_school_name
JOIN edu_school_year school_year ON school_year.school_id = school.school_id AND school_year.year_start = 2026 AND school_year.deleted = b'0'
JOIN edu_school_grade school_grade
  ON school_grade.school_id = school.school_id
 AND school_grade.grade_catalog_id = plan.future_grade_catalog_id
 AND school_grade.deleted = b'0'
JOIN edu_school_class class
  ON class.school_id = school.school_id
 AND class.school_year_id = school_year.id
 AND class.school_grade_id = school_grade.id
 AND class.class_no = 98
 AND class.deleted = b'0'
WHERE plan.future_grade_catalog_id IS NOT NULL;

SELECT
    (SELECT COUNT(*) FROM member_user WHERE mark = 'SUBSCRIPTION_E2E_FIXTURE' AND deleted = b'0') AS fixture_parent_count,
    (SELECT COUNT(*) FROM edu_student WHERE creator = 'codex-subscription-e2e' AND deleted = b'0') AS fixture_student_count,
    (SELECT COUNT(*) FROM edu_student_class WHERE creator = 'codex-subscription-e2e' AND deleted = b'0') AS fixture_student_class_count,
    (SELECT COUNT(*) FROM edu_school_class WHERE creator = 'codex-subscription-e2e' AND deleted = b'0') AS fixture_school_class_count;
