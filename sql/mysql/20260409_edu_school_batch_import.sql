SET NAMES utf8mb4;

-- 无锡学校批量导入脚本
-- 说明：
-- 1. 数据来源于用户提供的学校文本清单
-- 2. area_id 映射：
--    市属 -> 320200（无锡市）
--    滨湖区 -> 320211
--    梁溪区 -> 320213
--    新吴区 -> 320214
-- 3. 仅导入当前库中不存在的学校；同名学校跳过，不覆盖原有年级/学年/班级
-- 4. 统一创建 2026-2027 学年，开始于 2026-09-01，结束于 2027-06-30

START TRANSACTION;

DROP TEMPORARY TABLE IF EXISTS tmp_school_batch_stage;
CREATE TEMPORARY TABLE tmp_school_batch_stage (
    row_no INT NOT NULL PRIMARY KEY,
    area_name VARCHAR(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    school_name VARCHAR(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    stage_name VARCHAR(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

INSERT INTO tmp_school_batch_stage (row_no, area_name, school_name, stage_name) VALUES
    (1, '市属', '学前总部', '小学'),
    (2, '市属', '新城小学', '小学'),
    (3, '市属', '阳光小学', '小学'),
    (4, '滨湖区', '峰影小学', '小学'),
    (5, '滨湖区', '梅园小学', '小学'),
    (6, '滨湖区', '河埒中心小学', '小学'),
    (7, '滨湖区', '稻香小学', '小学'),
    (8, '滨湖区', '立人小学', '小学'),
    (9, '滨湖区', '育红小学', '小学'),
    (10, '滨湖区', '育英实验小学', '小学'),
    (11, '滨湖区', '育英实验（华晶校区）', '小学'),
    (12, '滨湖区', '育英小学胜利校区', '小学'),
    (13, '滨湖区', '育英文旅实验小学', '小学'),
    (14, '滨湖区', '育英锦园小学', '小学'),
    (15, '滨湖区', '滨湖区南泉小学', '小学'),
    (16, '滨湖区', '雪浪中心小学', '小学'),
    (17, '滨湖区', '滨湖实幼青祁园区', '幼儿园'),
    (18, '滨湖区', '滨湖实幼万达园区', '幼儿园'),
    (19, '滨湖区', '滨湖实幼誉品园区', '幼儿园'),
    (20, '梁溪区', '侨谊中学', '初中'),
    (21, '梁溪区', '侨谊古运河中学', '初中'),
    (22, '梁溪区', '侨谊明德中学', '初中'),
    (23, '梁溪区', '凤翔中学', '初中'),
    (24, '梁溪区', '刘潭中学', '初中'),
    (25, '梁溪区', '南长实验中学', '初中'),
    (26, '梁溪区', '塔影中学', '初中'),
    (27, '梁溪区', '东林古运河小学', '小学'),
    (28, '梁溪区', '东林小学', '小学'),
    (29, '梁溪区', '东林惠畅实验学校', '小学'),
    (30, '梁溪区', '五河小学', '小学'),
    (31, '梁溪区', '五爱小学', '小学'),
    (32, '梁溪区', '亭子桥小学', '小学'),
    (33, '梁溪区', '兰亭小学', '小学'),
    (34, '梁溪区', '凤翔小学', '小学'),
    (35, '梁溪区', '刘潭二村小学', '小学'),
    (36, '梁溪区', '刘潭实验学校小学部', '小学'),
    (37, '梁溪区', '南湖小学', '小学'),
    (38, '梁溪区', '南长街小学', '小学'),
    (39, '梁溪区', '双河小学', '小学'),
    (40, '梁溪区', '吴桥实验小学', '小学'),
    (41, '梁溪区', '塔影中心小学', '小学'),
    (42, '梁溪区', '夹城里中心小学', '小学'),
    (43, '梁溪区', '山北中心小学', '小学'),
    (44, '梁溪区', '广勤中学', '初中'),
    (45, '梁溪区', '江南中学（通扬校区）', '初中'),
    (46, '梁溪区', '江南中学（阳光校区）', '初中'),
    (47, '梁溪区', '清名桥中学', '初中'),
    (48, '梁溪区', '积余中学', '初中'),
    (49, '梁溪区', '崇宁路小学', '小学'),
    (50, '梁溪区', '广瑞小学', '小学'),
    (51, '梁溪区', '广益中心小学', '小学'),
    (52, '梁溪区', '惠山小学', '小学'),
    (53, '梁溪区', '扬名中心小学', '小学'),
    (54, '梁溪区', '新开河小学', '小学'),
    (55, '梁溪区', '明德实验学校（小学部）', '小学'),
    (56, '梁溪区', '梨庄实验小学', '小学'),
    (57, '梁溪区', '沁园实验小学', '小学'),
    (58, '梁溪区', '沁园实验小学（五星校区）', '小学'),
    (59, '梁溪区', '滨河实验小学', '小学'),
    (60, '梁溪区', '积余小学', '小学'),
    (61, '梁溪区', '积余小学运河分校', '小学'),
    (62, '梁溪区', '芦庄二小', '小学'),
    (63, '梁溪区', '芦庄实验小学', '小学'),
    (64, '梁溪区', '花园实验小学', '小学'),
    (65, '梁溪区', '连元寄畅小学', '小学'),
    (66, '梁溪区', '连元街小学', '小学'),
    (67, '梁溪区', '连元街小学蘅芳分校', '小学'),
    (68, '梁溪区', '通德桥实验小学', '小学'),
    (69, '梁溪区', '通江实验小学', '小学'),
    (70, '梁溪区', '靖海小学', '小学'),
    (71, '新吴区', '丽景中学', '初中'),
    (72, '新吴区', '太科城中学', '初中'),
    (73, '新吴区', '文博中学', '初中'),
    (74, '新吴区', '新吴实验中学', '初中'),
    (75, '新吴区', '新安中学', '初中'),
    (76, '新吴区', '硕放中学', '初中'),
    (77, '新吴区', '金鸿中学', '初中'),
    (78, '新吴区', '世新实验学校', '小学'),
    (79, '新吴区', '丽景实验学校', '小学'),
    (80, '新吴区', '南丰小学', '小学'),
    (81, '新吴区', '南星小学', '小学'),
    (82, '新吴区', '后宅中心小学', '小学'),
    (83, '新吴区', '坊前实验小学（一校区）', '小学'),
    (84, '新吴区', '坊前实验小学（二校区）', '小学'),
    (85, '新吴区', '太科城小学', '小学'),
    (86, '新吴区', '新吴实验小学', '小学'),
    (87, '新吴区', '新洲小学', '小学'),
    (88, '新吴区', '新苑小学', '小学'),
    (89, '新吴区', '新苑小学（西校区）', '小学'),
    (90, '新吴区', '旺庄实验小学', '小学'),
    (91, '新吴区', '春城实验小学（一校区）', '小学'),
    (92, '新吴区', '春城实验小学（二校区）', '小学'),
    (93, '新吴区', '春星小学', '小学'),
    (94, '新吴区', '梅村实验小学（一校区）', '小学'),
    (95, '新吴区', '梅村实验小学（二校区）', '小学'),
    (96, '新吴区', '梅里实验小学', '小学'),
    (97, '新吴区', '江溪小学', '小学'),
    (98, '新吴区', '泰伯实验学校', '小学'),
    (99, '新吴区', '泰山路实验小学', '小学'),
    (100, '新吴区', '硕放小学', '小学'),
    (101, '新吴区', '金鸿小学', '小学'),
    (102, '新吴区', '锡梅小学', '小学'),
    (103, '新吴区', '高浪小学', '小学'),
    (104, '新吴区', '鸿山实验小学', '小学'),
    (105, '新吴区', '扬名实验中学', '初中'),
    (106, '新吴区', '江南中学（新城校区）', '初中'),
    (107, '新吴区', '东降实验小学部', '小学'),
    (108, '新吴区', '华庄中心小学', '小学'),
    (109, '新吴区', '太湖实验小学', '小学'),
    (110, '新吴区', '尚贤万科小学', '小学'),
    (111, '新吴区', '尚贤融创校区', '小学'),
    (112, '新吴区', '扬名实验学校（侨谊校区）', '小学'),
    (113, '新吴区', '无锡市融成观顺实验小学', '小学'),
    (114, '新吴区', '江南实验小学', '小学'),
    (115, '新吴区', '育红山水校区（石塘）', '小学'),
    (116, '新吴区', '宋庆龄幼儿园', '幼儿园');

DROP TEMPORARY TABLE IF EXISTS tmp_school_batch_resolved;
CREATE TEMPORARY TABLE tmp_school_batch_resolved AS
SELECT
    s.row_no,
    s.area_name,
    s.school_name,
    s.stage_name,
    CASE s.area_name
        WHEN '市属' THEN 320200
        WHEN '滨湖区' THEN 320211
        WHEN '梁溪区' THEN 320213
        WHEN '新吴区' THEN 320214
        ELSE NULL
    END AS area_id,
    CASE s.area_name
        WHEN '市属' THEN '无锡市'
        WHEN '滨湖区' THEN '无锡市滨湖区'
        WHEN '梁溪区' THEN '无锡市梁溪区'
        WHEN '新吴区' THEN '无锡市新吴区'
        ELSE NULL
    END AS area_prefix,
    ROW_NUMBER() OVER (PARTITION BY s.area_name ORDER BY s.row_no) AS area_seq
FROM tmp_school_batch_stage s;

DROP TEMPORARY TABLE IF EXISTS tmp_school_batch_new;
CREATE TEMPORARY TABLE tmp_school_batch_new AS
SELECT r.*
FROM tmp_school_batch_resolved r
LEFT JOIN edu_school school
       ON school.school_name = r.school_name
      AND school.deleted = b'0'
WHERE school.id IS NULL;

INSERT INTO edu_school (
    school_name,
    area_id,
    school_address,
    code,
    creator,
    updater,
    deleted
)
SELECT
    n.school_name,
    n.area_id,
    CONCAT(
        n.area_prefix,
        CASE n.area_name
            WHEN '市属' THEN CASE MOD(n.area_seq - 1, 3)
                WHEN 0 THEN '解放东路'
                WHEN 1 THEN '清扬路'
                ELSE '太湖大道'
            END
            WHEN '滨湖区' THEN CASE MOD(n.area_seq - 1, 3)
                WHEN 0 THEN '观山路'
                WHEN 1 THEN '万顺道'
                ELSE '雪浪路'
            END
            WHEN '梁溪区' THEN CASE MOD(n.area_seq - 1, 3)
                WHEN 0 THEN '学前东路'
                WHEN 1 THEN '广益路'
                ELSE '通江大道'
            END
            WHEN '新吴区' THEN CASE MOD(n.area_seq - 1, 3)
                WHEN 0 THEN '震泽路'
                WHEN 1 THEN '菱湖大道'
                ELSE '新锡路'
            END
        END,
        100 + n.area_seq * 8,
        '号'
    ) AS school_address,
    NULL AS code,
    'codex' AS creator,
    'codex' AS updater,
    b'0' AS deleted
FROM tmp_school_batch_new n;

DROP TEMPORARY TABLE IF EXISTS tmp_stage_grade_map;
CREATE TEMPORARY TABLE tmp_stage_grade_map (
    stage_name VARCHAR(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    stage_order INT NOT NULL,
    grade_catalog_id BIGINT NOT NULL,
    class_count INT NOT NULL,
    PRIMARY KEY (stage_name, grade_catalog_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

INSERT INTO tmp_stage_grade_map (stage_name, stage_order, grade_catalog_id, class_count) VALUES
    ('幼儿园', 1, 1, 10),
    ('幼儿园', 2, 2, 10),
    ('幼儿园', 3, 3, 10),
    ('小学', 1, 4, 25),
    ('小学', 2, 5, 25),
    ('小学', 3, 6, 25),
    ('小学', 4, 7, 25),
    ('小学', 5, 8, 25),
    ('小学', 6, 9, 25),
    ('初中', 1, 10, 10),
    ('初中', 2, 11, 10),
    ('初中', 3, 12, 10);

INSERT INTO edu_school_grade (
    school_id,
    grade_catalog_id,
    creator,
    updater,
    deleted
)
SELECT
    school.id,
    grade_map.grade_catalog_id,
    'codex' AS creator,
    'codex' AS updater,
    b'0' AS deleted
FROM tmp_school_batch_new n
JOIN edu_school school
  ON school.school_name = n.school_name
 AND school.deleted = b'0'
JOIN tmp_stage_grade_map grade_map
  ON grade_map.stage_name = n.stage_name
LEFT JOIN edu_school_grade school_grade
       ON school_grade.school_id = school.id
      AND school_grade.grade_catalog_id = grade_map.grade_catalog_id
      AND school_grade.deleted = b'0'
WHERE school_grade.id IS NULL;

INSERT INTO edu_school_year (
    school_id,
    year_start,
    year_end,
    start_date,
    end_date,
    creator,
    updater,
    deleted
)
SELECT
    school.id,
    2026 AS year_start,
    2027 AS year_end,
    '2026-09-01' AS start_date,
    '2027-06-30' AS end_date,
    'codex' AS creator,
    'codex' AS updater,
    b'0' AS deleted
FROM tmp_school_batch_new n
JOIN edu_school school
  ON school.school_name = n.school_name
 AND school.deleted = b'0'
LEFT JOIN edu_school_year school_year
       ON school_year.school_id = school.id
      AND school_year.year_start = 2026
      AND school_year.deleted = b'0'
WHERE school_year.id IS NULL;

DROP TEMPORARY TABLE IF EXISTS tmp_class_no;
CREATE TEMPORARY TABLE tmp_class_no (
    class_no INT NOT NULL PRIMARY KEY
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

INSERT INTO tmp_class_no (class_no) VALUES
    (1),(2),(3),(4),(5),(6),(7),(8),(9),(10),
    (11),(12),(13),(14),(15),(16),(17),(18),(19),(20),
    (21),(22),(23),(24),(25);

INSERT INTO edu_school_class (
    school_id,
    entry_year,
    school_grade_id,
    school_year_id,
    class_no,
    class_name,
    creator,
    updater,
    deleted
)
SELECT
    school.id AS school_id,
    2026 - (grade_map.stage_order - 1) AS entry_year,
    school_grade.id AS school_grade_id,
    school_year.id AS school_year_id,
    class_no.class_no,
    CONCAT(2026 - (grade_map.stage_order - 1), '级', grade_catalog.grade_name, class_no.class_no, '班') AS class_name,
    'codex' AS creator,
    'codex' AS updater,
    b'0' AS deleted
FROM tmp_school_batch_new n
JOIN edu_school school
  ON school.school_name = n.school_name
 AND school.deleted = b'0'
JOIN tmp_stage_grade_map grade_map
  ON grade_map.stage_name = n.stage_name
JOIN edu_school_grade school_grade
  ON school_grade.school_id = school.id
 AND school_grade.grade_catalog_id = grade_map.grade_catalog_id
 AND school_grade.deleted = b'0'
JOIN edu_school_year school_year
  ON school_year.school_id = school.id
 AND school_year.year_start = 2026
 AND school_year.deleted = b'0'
JOIN edu_grade_catalog grade_catalog
  ON grade_catalog.id = grade_map.grade_catalog_id
 AND grade_catalog.deleted = b'0'
JOIN tmp_class_no class_no
  ON class_no.class_no <= grade_map.class_count
LEFT JOIN edu_school_class school_class
       ON school_class.school_id = school.id
      AND school_class.school_year_id = school_year.id
      AND school_class.school_grade_id = school_grade.id
      AND school_class.class_no = class_no.class_no
      AND school_class.deleted = b'0'
WHERE school_class.id IS NULL;

COMMIT;
