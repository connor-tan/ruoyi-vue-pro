-- 校刊汇：导入学校学年与学校年级基础数据
-- 口径：
-- 1. 范围限定为当前学校导入目标：默认仓库为无锡仓(id=1)，且绑定到发行部、河埒站、清扬站、城中站、新吴站、新城站。
-- 2. 学年覆盖 2025-2026、2026-2027，日期统一为 09-01 至次年 06-30。
-- 3. 不再按固定班号预铺班级；班级由后台维护、学生绑定或升班流程按需创建。
-- 4. 学校年级容量按学段默认写入 max_class_no；本脚本不创建实体班级。

SET NAMES utf8mb4;

DROP PROCEDURE IF EXISTS xiaokanhui_seed_school_grade_class_20260515;

DELIMITER $$

CREATE PROCEDURE xiaokanhui_seed_school_grade_class_20260515()
BEGIN
    DECLARE v_school_count INT DEFAULT 0;
    DECLARE v_school_stage_count INT DEFAULT 0;
    DECLARE v_school_year_target_count INT DEFAULT 0;
    DECLARE v_school_grade_target_count INT DEFAULT 0;
    DECLARE v_class_target_count INT DEFAULT 0;
    DECLARE v_existing_active_class_count INT DEFAULT 0;
    DECLARE v_error_message VARCHAR(255);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    DROP TEMPORARY TABLE IF EXISTS tmp_xkh_target_year;
    DROP TEMPORARY TABLE IF EXISTS tmp_xkh_stage_class_count;
    DROP TEMPORARY TABLE IF EXISTS tmp_xkh_class_no;
    DROP TEMPORARY TABLE IF EXISTS tmp_xkh_target_school;
    DROP TEMPORARY TABLE IF EXISTS tmp_xkh_year_catalog;
    DROP TEMPORARY TABLE IF EXISTS tmp_xkh_school_year_target;
    DROP TEMPORARY TABLE IF EXISTS tmp_xkh_school_grade_target;
    DROP TEMPORARY TABLE IF EXISTS tmp_xkh_school_year_resolved;
    DROP TEMPORARY TABLE IF EXISTS tmp_xkh_school_grade_resolved;
    DROP TEMPORARY TABLE IF EXISTS tmp_xkh_class_target;

    CREATE TEMPORARY TABLE tmp_xkh_target_year (
        year_start INT NOT NULL PRIMARY KEY,
        year_end INT NOT NULL,
        start_date DATE NOT NULL,
        end_date DATE NOT NULL
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

    INSERT INTO tmp_xkh_target_year (year_start, year_end, start_date, end_date) VALUES
        (2025, 2026, '2025-09-01', '2026-06-30'),
        (2026, 2027, '2026-09-01', '2027-06-30');

    CREATE TEMPORARY TABLE tmp_xkh_stage_class_count (
        stage VARCHAR(32) NOT NULL PRIMARY KEY,
        class_count INT NOT NULL
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

    INSERT INTO tmp_xkh_stage_class_count (stage, class_count) VALUES
        ('kindergarten', 10),
        ('primary', 25),
        ('middle', 10);

    CREATE TEMPORARY TABLE tmp_xkh_class_no (
        class_no INT NOT NULL PRIMARY KEY
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

    INSERT INTO tmp_xkh_class_no (class_no) VALUES
        (1), (2), (3), (4), (5),
        (6), (7), (8), (9), (10),
        (11), (12), (13), (14), (15),
        (16), (17), (18), (19), (20),
        (21), (22), (23), (24), (25);

    START TRANSACTION;

    CREATE TEMPORARY TABLE tmp_xkh_target_school AS
    SELECT DISTINCT s.id AS school_id, s.school_name
    FROM edu_school s
    JOIN edu_station st ON st.id = s.station_id
        AND st.deleted = b'0'
        AND st.station_name IN ('发行部', '河埒站', '清扬站', '城中站', '新吴站', '新城站')
    WHERE s.deleted = b'0'
      AND s.warehouse_id = 1;

    SELECT COUNT(*) INTO v_school_count FROM tmp_xkh_target_school;
    IF v_school_count = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '未找到本次导入目标学校，班级初始化中止';
    END IF;

    SELECT COUNT(*) INTO v_school_stage_count
    FROM edu_school_stage ss
    JOIN tmp_xkh_target_school ts ON ts.school_id = ss.school_id
    WHERE ss.deleted = b'0'
      AND ss.stage IN ('kindergarten', 'primary', 'middle');
    IF v_school_stage_count = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '目标学校没有有效学段，班级初始化中止';
    END IF;

    INSERT INTO edu_year_catalog (
        year_start, year_end, creator, create_time, updater, update_time, deleted
    )
    SELECT ty.year_start, ty.year_end, 'system', NOW(), 'system', NOW(), b'0'
    FROM tmp_xkh_target_year ty
    ON DUPLICATE KEY UPDATE
        deleted = b'0',
        updater = 'system',
        update_time = NOW();

    CREATE TEMPORARY TABLE tmp_xkh_year_catalog AS
    SELECT yc.id AS year_catalog_id, ty.year_start, ty.year_end, ty.start_date, ty.end_date
    FROM tmp_xkh_target_year ty
    JOIN edu_year_catalog yc ON yc.year_start = ty.year_start
        AND yc.year_end = ty.year_end
        AND yc.deleted = b'0';

    SELECT COUNT(*) INTO v_school_year_target_count
    FROM tmp_xkh_target_school ts
    JOIN tmp_xkh_year_catalog yc;

    CREATE TEMPORARY TABLE tmp_xkh_school_year_target AS
    SELECT ts.school_id, yc.year_catalog_id, yc.year_start, yc.year_end, yc.start_date, yc.end_date
    FROM tmp_xkh_target_school ts
    JOIN tmp_xkh_year_catalog yc;

    INSERT INTO edu_school_year (
        school_id, year_catalog_id, year_start, year_end, start_date, end_date,
        creator, create_time, updater, update_time, deleted
    )
    SELECT school_id, year_catalog_id, year_start, year_end, start_date, end_date,
           'system', NOW(), 'system', NOW(), b'0'
    FROM tmp_xkh_school_year_target
    ON DUPLICATE KEY UPDATE
        year_catalog_id = VALUES(year_catalog_id),
        year_end = VALUES(year_end),
        start_date = VALUES(start_date),
        end_date = VALUES(end_date),
        deleted = b'0',
        updater = 'system',
        update_time = NOW();

    CREATE TEMPORARY TABLE tmp_xkh_school_grade_target AS
    SELECT DISTINCT ts.school_id, gc.id AS grade_catalog_id, cc.class_count AS max_class_no
    FROM tmp_xkh_target_school ts
    JOIN edu_school_stage ss ON ss.school_id = ts.school_id
        AND ss.deleted = b'0'
    JOIN edu_grade_catalog gc ON gc.stage = ss.stage
        AND gc.deleted = b'0'
        AND gc.status = 0
    JOIN tmp_xkh_stage_class_count cc ON cc.stage = gc.stage
    WHERE ss.stage IN ('kindergarten', 'primary', 'middle');

    SELECT COUNT(*) INTO v_school_grade_target_count FROM tmp_xkh_school_grade_target;
    IF v_school_grade_target_count = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '没有可创建的学校年级，班级初始化中止';
    END IF;

    INSERT INTO edu_school_grade (
        school_id, grade_catalog_id, max_class_no, creator, create_time, updater, update_time, deleted
    )
    SELECT school_id, grade_catalog_id, max_class_no, 'system', NOW(), 'system', NOW(), b'0'
    FROM tmp_xkh_school_grade_target
    ON DUPLICATE KEY UPDATE
        deleted = b'0',
        max_class_no = IF(max_class_no IS NULL OR max_class_no = 0, VALUES(max_class_no), max_class_no),
        updater = 'system',
        update_time = NOW();

    CREATE TEMPORARY TABLE tmp_xkh_school_year_resolved AS
    SELECT sy.id AS school_year_id, sy.school_id, sy.year_catalog_id, sy.year_start, sy.year_end
    FROM edu_school_year sy
    JOIN tmp_xkh_school_year_target t ON t.school_id = sy.school_id
        AND t.year_catalog_id = sy.year_catalog_id
    WHERE sy.deleted = b'0';

    CREATE TEMPORARY TABLE tmp_xkh_school_grade_resolved AS
    SELECT sg.id AS school_grade_id, sg.school_id, sg.grade_catalog_id
    FROM edu_school_grade sg
    JOIN tmp_xkh_school_grade_target t ON t.school_id = sg.school_id
        AND t.grade_catalog_id = sg.grade_catalog_id
    WHERE sg.deleted = b'0';

    CREATE TEMPORARY TABLE tmp_xkh_class_target AS
    SELECT ts.school_id,
           sy.year_start - (CAST(SUBSTRING(gc.grade_no, 2) AS UNSIGNED) - 1) AS entry_year,
           sy.school_year_id,
           sg.school_grade_id,
           n.class_no,
           CONCAT(sy.year_start - (CAST(SUBSTRING(gc.grade_no, 2) AS UNSIGNED) - 1),
                  '级', gc.grade_name, n.class_no, '班') AS class_name
    FROM tmp_xkh_target_school ts
    JOIN edu_school_stage ss ON ss.school_id = ts.school_id
        AND ss.deleted = b'0'
        AND ss.stage IN ('kindergarten', 'primary', 'middle')
    JOIN edu_grade_catalog gc ON gc.stage = ss.stage
        AND gc.deleted = b'0'
        AND gc.status = 0
    JOIN tmp_xkh_stage_class_count cc ON cc.stage = gc.stage
    JOIN tmp_xkh_school_grade_resolved sg ON sg.school_id = ts.school_id
        AND sg.grade_catalog_id = gc.id
    JOIN tmp_xkh_school_year_resolved sy ON sy.school_id = ts.school_id
    JOIN tmp_xkh_class_no n ON n.class_no <= cc.class_count;
    DELETE FROM tmp_xkh_class_target;

    SELECT COUNT(*) INTO v_class_target_count FROM tmp_xkh_class_target;

    SELECT COUNT(*) INTO v_existing_active_class_count
    FROM edu_school_class c
    JOIN tmp_xkh_class_target t ON t.school_id = c.school_id
        AND t.entry_year = c.entry_year
        AND t.school_year_id = c.school_year_id
        AND t.school_grade_id = c.school_grade_id
        AND t.class_no = c.class_no
    WHERE c.deleted = b'0';

    INSERT INTO edu_school_class (
        school_id, entry_year, school_grade_id, school_year_id, class_no, class_name,
        creator, create_time, updater, update_time, deleted
    )
    SELECT school_id, entry_year, school_grade_id, school_year_id, class_no, class_name,
           'system', NOW(), 'system', NOW(), b'0'
    FROM tmp_xkh_class_target
    ON DUPLICATE KEY UPDATE
        class_name = VALUES(class_name),
        deleted = b'0',
        updater = 'system',
        update_time = NOW();

    COMMIT;

    SELECT 'target_schools' AS metric, v_school_count AS value
    UNION ALL
    SELECT 'target_school_stage_rows', v_school_stage_count
    UNION ALL
    SELECT 'target_school_year_rows', v_school_year_target_count
    UNION ALL
    SELECT 'target_school_grade_rows', v_school_grade_target_count
    UNION ALL
    SELECT 'target_class_rows', v_class_target_count
    UNION ALL
    SELECT 'existing_active_class_rows_before', v_existing_active_class_count
    UNION ALL
    SELECT 'created_or_reactivated_class_rows', v_class_target_count - v_existing_active_class_count;

    SELECT gc.stage, COUNT(DISTINCT ts.school_id) AS school_count,
           COUNT(DISTINCT gc.id) AS grade_count,
           MAX(cc.class_count) AS class_count_per_grade,
           COUNT(*) * MAX(cc.class_count) * (SELECT COUNT(*) FROM tmp_xkh_year_catalog) AS class_rows_for_two_years
    FROM tmp_xkh_target_school ts
    JOIN edu_school_stage ss ON ss.school_id = ts.school_id
        AND ss.deleted = b'0'
        AND ss.stage IN ('kindergarten', 'primary', 'middle')
    JOIN edu_grade_catalog gc ON gc.stage = ss.stage
        AND gc.deleted = b'0'
        AND gc.status = 0
    JOIN tmp_xkh_stage_class_count cc ON cc.stage = gc.stage
    GROUP BY gc.stage
    ORDER BY FIELD(gc.stage, 'kindergarten', 'primary', 'middle');
END $$

DELIMITER ;

CALL xiaokanhui_seed_school_grade_class_20260515();

DROP PROCEDURE IF EXISTS xiaokanhui_seed_school_grade_class_20260515;
