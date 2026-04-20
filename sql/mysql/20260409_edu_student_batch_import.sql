-- =========================================================
-- 批量生成 edu 学生与家长测试数据（多孩家庭跨校版）
-- 口径：
--   - 幼儿园：每班 25 人
--   - 小学：每班 45 人
--   - 初中：每班 40 人
--   - 三孩家庭 1000 个，二孩家庭 5000 个，其余单孩家庭
--   - 家长与学生均采用稳定规则生成，脚本可重复执行
-- =========================================================

SET NAMES utf8mb4;

DROP TEMPORARY TABLE IF EXISTS tmp_seq_45;
CREATE TEMPORARY TABLE tmp_seq_45 (
    n INT NOT NULL PRIMARY KEY
) ENGINE=InnoDB;

INSERT INTO tmp_seq_45 (n)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 45
)
SELECT n FROM seq;

DROP TEMPORARY TABLE IF EXISTS tmp_surname;
CREATE TEMPORARY TABLE tmp_surname (
    id INT NOT NULL PRIMARY KEY,
    val VARCHAR(4) NOT NULL
) ENGINE=InnoDB;

INSERT INTO tmp_surname (id, val) VALUES
    (1, '王'), (2, '李'), (3, '张'), (4, '刘'), (5, '陈'), (6, '杨'), (7, '黄'), (8, '赵'),
    (9, '周'), (10, '吴'), (11, '徐'), (12, '孙'), (13, '胡'), (14, '朱'), (15, '高'), (16, '林'),
    (17, '何'), (18, '郭'), (19, '马'), (20, '罗'), (21, '梁'), (22, '宋'), (23, '郑'), (24, '谢'),
    (25, '韩'), (26, '唐'), (27, '冯'), (28, '于'), (29, '董'), (30, '萧'), (31, '程'), (32, '曹'),
    (33, '袁'), (34, '邓'), (35, '许'), (36, '傅'), (37, '沈'), (38, '曾'), (39, '彭'), (40, '吕'),
    (41, '苏'), (42, '卢'), (43, '蒋'), (44, '蔡'), (45, '贾'), (46, '丁'), (47, '魏'), (48, '薛');

DROP TEMPORARY TABLE IF EXISTS tmp_given_char;
CREATE TEMPORARY TABLE tmp_given_char (
    id INT NOT NULL PRIMARY KEY,
    val VARCHAR(4) NOT NULL
) ENGINE=InnoDB;

INSERT INTO tmp_given_char (id, val) VALUES
    (1, '子'), (2, '一'), (3, '安'), (4, '可'), (5, '乐'), (6, '雨'), (7, '欣'), (8, '晨'),
    (9, '梓'), (10, '宇'), (11, '宸'), (12, '辰'), (13, '希'), (14, '诺'), (15, '然'), (16, '宁'),
    (17, '睿'), (18, '涵'), (19, '歆'), (20, '妍'), (21, '琪'), (22, '悦'), (23, '彤'), (24, '恩'),
    (25, '嘉'), (26, '怡'), (27, '嘉'), (28, '睿'), (29, '萌'), (30, '铭'), (31, '轩'), (32, '洋'),
    (33, '昊'), (34, '文'), (35, '博'), (36, '清'), (37, '景'), (38, '知'), (39, '言'), (40, '芮'),
    (41, '诗'), (42, '锦'), (43, '奕'), (44, '柏'), (45, '朗'), (46, '思'), (47, '语'), (48, '成');

DROP TEMPORARY TABLE IF EXISTS tmp_given_char_2;
CREATE TEMPORARY TABLE tmp_given_char_2 AS
SELECT * FROM tmp_given_char;

SET @surname_count := 48;
SET @given_count := 48;

DROP TEMPORARY TABLE IF EXISTS tmp_student_seed;
CREATE TEMPORARY TABLE tmp_student_seed (
    global_seq BIGINT NOT NULL,
    school_id BIGINT NOT NULL,
    area_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    entry_year INT NOT NULL,
    class_no INT NOT NULL,
    grade_name VARCHAR(20) NOT NULL,
    stage VARCHAR(20) NOT NULL,
    grade_rank INT NOT NULL,
    seat_no INT NOT NULL,
    family_seq BIGINT DEFAULT NULL,
    parent_mobile CHAR(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
    student_code INT DEFAULT NULL,
    student_name VARCHAR(20) DEFAULT NULL
) ENGINE=InnoDB;

INSERT INTO tmp_student_seed (
    global_seq, school_id, area_id, class_id, entry_year, class_no, grade_name,
    stage, grade_rank, seat_no, family_seq, parent_mobile, student_code, student_name
)
SELECT
    ROW_NUMBER() OVER (
        ORDER BY CRC32(CONCAT(LPAD(c.id, 6, '0'), '-', LPAD(seq.n, 2, '0'))), c.id, seq.n
    ) AS global_seq,
    s.id AS school_id,
    s.area_id,
    c.id AS class_id,
    c.entry_year,
    c.class_no,
    gc.grade_name,
    gc.stage,
    CASE gc.id
        WHEN 1 THEN 1
        WHEN 2 THEN 2
        WHEN 3 THEN 3
        WHEN 4 THEN 1
        WHEN 5 THEN 2
        WHEN 6 THEN 3
        WHEN 7 THEN 4
        WHEN 8 THEN 5
        WHEN 9 THEN 6
        WHEN 10 THEN 1
        WHEN 11 THEN 2
        WHEN 12 THEN 3
    END AS grade_rank,
    seq.n AS seat_no,
    NULL AS family_seq,
    NULL AS parent_mobile,
    NULL AS student_code,
    NULL AS student_name
FROM edu_school_class c
JOIN edu_school s ON s.id = c.school_id AND s.deleted = b'0'
JOIN edu_school_grade sg ON sg.id = c.school_grade_id AND sg.deleted = b'0'
JOIN edu_grade_catalog gc ON gc.id = sg.grade_catalog_id
JOIN tmp_seq_45 seq
WHERE c.deleted = b'0'
  AND seq.n <= CASE gc.stage
      WHEN 'kindergarten' THEN 25
      WHEN 'primary' THEN 45
      WHEN 'middle' THEN 40
      ELSE 0
  END;

ALTER TABLE tmp_student_seed
    ADD PRIMARY KEY (global_seq),
    ADD KEY idx_tmp_student_seed_family (family_seq),
    ADD KEY idx_tmp_student_seed_parent_mobile (parent_mobile),
    ADD KEY idx_tmp_student_seed_school_code (school_id, student_code),
    ADD KEY idx_tmp_student_seed_class (class_id);

UPDATE tmp_student_seed
SET family_seq = CASE
    WHEN global_seq <= 3000 THEN CEIL(global_seq / 3)
    WHEN global_seq <= 13000 THEN 1000 + CEIL((global_seq - 3000) / 2)
    ELSE 6000 + (global_seq - 13000)
END;

UPDATE tmp_student_seed
SET parent_mobile = CONCAT('19', LPAD(family_seq, 9, '0')),
    student_code = 90000000 + school_id * 1000000 + grade_rank * 10000 + class_no * 100 + seat_no;

UPDATE tmp_student_seed seed
JOIN tmp_surname sn
  ON sn.id = MOD(seed.global_seq - 1, @surname_count) + 1
JOIN tmp_given_char g1
  ON g1.id = MOD(FLOOR((seed.global_seq - 1) / @surname_count), @given_count) + 1
JOIN tmp_given_char_2 g2
  ON g2.id = MOD(FLOOR((seed.global_seq - 1) / (@surname_count * @given_count)), @given_count) + 1
SET seed.student_name = CASE
    WHEN MOD(seed.global_seq, 4) = 0 THEN CONCAT(sn.val, g1.val)
    ELSE CONCAT(sn.val, g1.val, g2.val)
END;

DROP TEMPORARY TABLE IF EXISTS tmp_family_head;
CREATE TEMPORARY TABLE tmp_family_head AS
SELECT
    family_seq,
    MIN(global_seq) AS head_global_seq,
    MAX(parent_mobile) AS parent_mobile
FROM tmp_student_seed
GROUP BY family_seq;

ALTER TABLE tmp_family_head
    ADD PRIMARY KEY (family_seq),
    ADD KEY idx_tmp_family_head_global_seq (head_global_seq),
    ADD KEY idx_tmp_family_head_mobile (parent_mobile);

START TRANSACTION;

INSERT INTO member_user (
    mobile, password, status, register_ip, register_terminal, login_ip, login_date,
    nickname, avatar, name, sex, area_id, birthday, mark, point, tag_ids,
    level_id, experience, group_id, creator, updater, deleted, tenant_id
)
SELECT
    fam.parent_mobile,
    '$2a$04$7BixfD4z0Xs/pOlILmlmSeEJ0Cy3aKVcpGIi3.xkYWwR5I8EIV/qy',
    0,
    '127.0.0.1',
    20,
    '',
    NULL,
    CONCAT('家长', LPAD(fam.family_seq, 7, '0')),
    '',
    CONCAT('家长', LPAD(fam.family_seq, 7, '0')),
    0,
    fam.area_id,
    NULL,
    'EDU_STUDENT_FIXTURE',
    0,
    NULL,
    NULL,
    0,
    NULL,
    'codex-student-fixture',
    'codex-student-fixture',
    b'0',
    1
FROM (
    SELECT family_seq, head_global_seq, parent_mobile
    FROM tmp_family_head
) family_head
JOIN tmp_student_seed fam ON fam.global_seq = family_head.head_global_seq
LEFT JOIN member_user mu
       ON mu.mobile = fam.parent_mobile
      AND mu.deleted = b'0'
WHERE mu.id IS NULL;

COMMIT;

START TRANSACTION;

INSERT INTO edu_student (
    student_name, belong_to, current_school_id, entry_year, student_code, status,
    creator, updater, deleted
)
SELECT
    seed.student_name,
    mu.id,
    seed.school_id,
    seed.entry_year,
    seed.student_code,
    1,
    'codex-student-fixture',
    'codex-student-fixture',
    b'0'
FROM tmp_student_seed seed
JOIN member_user mu
  ON mu.mobile = seed.parent_mobile
 AND mu.deleted = b'0'
LEFT JOIN edu_student st
       ON st.current_school_id = seed.school_id
      AND st.student_code = seed.student_code
      AND st.deleted = b'0'
WHERE st.id IS NULL;

COMMIT;

START TRANSACTION;

INSERT IGNORE INTO edu_student_class (
    student_id, class_id, start_date, end_date, creator, updater, deleted
)
SELECT
    st.id,
    seed.class_id,
    DATE('2026-09-01'),
    NULL,
    'codex-student-fixture',
    'codex-student-fixture',
    b'0'
FROM tmp_student_seed seed
JOIN edu_student st
  ON st.current_school_id = seed.school_id
 AND st.student_code = seed.student_code
 AND st.deleted = b'0'
;

COMMIT;

SELECT 'member_user_fixture_count' AS metric, COUNT(*) AS total
FROM member_user
WHERE mark = 'EDU_STUDENT_FIXTURE'
  AND deleted = b'0'

UNION ALL

SELECT 'edu_student_fixture_count' AS metric, COUNT(*) AS total
FROM edu_student
WHERE creator = 'codex-student-fixture'
  AND deleted = b'0'

UNION ALL

SELECT 'edu_student_class_fixture_count' AS metric, COUNT(*) AS total
FROM edu_student_class
WHERE creator = 'codex-student-fixture'
  AND deleted = b'0';

SELECT child_count, COUNT(*) AS family_count
FROM (
    SELECT
        s.belong_to,
        COUNT(*) AS child_count
    FROM edu_student s
    JOIN member_user mu ON mu.id = s.belong_to
    WHERE s.creator = 'codex-student-fixture'
      AND s.deleted = b'0'
      AND mu.mark = 'EDU_STUDENT_FIXTURE'
      AND mu.deleted = b'0'
    GROUP BY s.belong_to
) t
GROUP BY child_count
ORDER BY child_count;

SELECT gc.stage, COUNT(*) AS student_count
FROM edu_student s
JOIN edu_student_class sc ON sc.student_id = s.id AND sc.end_date IS NULL AND sc.deleted = b'0'
JOIN edu_school_class c ON c.id = sc.class_id AND c.deleted = b'0'
JOIN edu_school_grade sg ON sg.id = c.school_grade_id AND sg.deleted = b'0'
JOIN edu_grade_catalog gc ON gc.id = sg.grade_catalog_id
WHERE s.creator = 'codex-student-fixture'
  AND s.deleted = b'0'
GROUP BY gc.stage
ORDER BY FIELD(gc.stage, 'kindergarten', 'primary', 'middle');

DROP TEMPORARY TABLE IF EXISTS tmp_student_seed;
DROP TEMPORARY TABLE IF EXISTS tmp_family_head;
DROP TEMPORARY TABLE IF EXISTS tmp_given_char_2;
DROP TEMPORARY TABLE IF EXISTS tmp_given_char;
DROP TEMPORARY TABLE IF EXISTS tmp_surname;
DROP TEMPORARY TABLE IF EXISTS tmp_seq_45;
