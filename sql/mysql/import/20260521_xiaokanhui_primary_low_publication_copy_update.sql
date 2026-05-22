-- 校刊汇小学低年级刊物商品文案校正
-- 目标：将低年级导入刊物的商品关键字从导入批次标识改为刊物相关词，并缩短商品简介。

SET NAMES utf8mb4;

DROP PROCEDURE IF EXISTS xiaokanhui_update_primary_low_publication_copy_20260521;

DELIMITER $$

CREATE PROCEDURE xiaokanhui_update_primary_low_publication_copy_20260521()
BEGIN
    DECLARE v_creator varchar(64) DEFAULT 'admin';
    DECLARE v_old_keyword varchar(255) DEFAULT '小学低年级报刊信息导入（2026.5）';
    DECLARE v_target_count int DEFAULT 0;
    DECLARE v_old_keyword_count int DEFAULT 0;
    DECLARE v_max_keyword_length int DEFAULT 0;
    DECLARE v_max_intro_length int DEFAULT 0;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    DROP TEMPORARY TABLE IF EXISTS tmp_primary_low_publication_title;
    CREATE TEMPORARY TABLE tmp_primary_low_publication_title (
        title varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL PRIMARY KEY
    );

    INSERT INTO tmp_primary_low_publication_title (title) VALUES
    ('中国少年报（低年级）'),
    ('小学生数学报'),
    ('阅读（低年级）'),
    ('语文报（小学版）'),
    ('时代英语报'),
    ('时代语文周刊'),
    ('时代数学周刊'),
    ('快乐作文（低年级）'),
    ('我的语文我的数学（一年级/二年级）'),
    ('全国优秀作文选（低年级）'),
    ('少儿科学周刊（儿童版）'),
    ('轻松学语数（注音版）'),
    ('天天爱学习'),
    ('海洋探秘'),
    ('动物奇迹'),
    ('爆笑王'),
    ('脑力大挑战'),
    ('环球探索'),
    ('天才小画家'),
    ('博物'),
    ('故事大王'),
    ('小哥白尼-趣味科学'),
    ('小哥白尼-野生动物'),
    ('小哥白尼-军事科学'),
    ('小哥白尼-漫画科学'),
    ('发现号趣味百科'),
    ('自然探秘'),
    ('智力大王'),
    ('神探大揭秘'),
    ('神探迈克狐'),
    ('趣味数学'),
    ('恐龙密码'),
    ('幽默派对'),
    ('创意手工与美术'),
    ('爱上看图写话'),
    ('我是不白吃'),
    ('迷你世界'),
    ('我是大侦探'),
    ('自然密码'),
    ('少年国学·古典文学常识');

    SELECT COUNT(*)
      INTO v_target_count
      FROM product_spu spu
      JOIN tmp_primary_low_publication_title target
        ON target.title = spu.name COLLATE utf8mb4_unicode_ci
     WHERE spu.deleted = b'0'
       AND spu.biz_scene = 'PUBLICATION';

    IF v_target_count <> 40 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '更新失败：小学低年级目标刊物数量不是 40';
    END IF;

    START TRANSACTION;

    DROP TEMPORARY TABLE IF EXISTS tmp_primary_low_publication_category_keyword;
    CREATE TEMPORARY TABLE tmp_primary_low_publication_category_keyword AS
    SELECT rel.spu_id,
           GROUP_CONCAT(category.name ORDER BY rel.sort, category.id SEPARATOR ',') AS category_names
      FROM product_spu_category_rel rel
      JOIN product_category category
        ON category.id = rel.category_id
       AND category.deleted = b'0'
     WHERE rel.deleted = b'0'
     GROUP BY rel.spu_id;

    DROP TEMPORARY TABLE IF EXISTS tmp_primary_low_publication_copy;
    CREATE TEMPORARY TABLE tmp_primary_low_publication_copy (
        spu_id bigint NOT NULL PRIMARY KEY,
        new_keyword varchar(256) NOT NULL,
        clean_description text NOT NULL,
        p1_pos int DEFAULT NULL,
        p2_pos int DEFAULT NULL,
        new_introduction varchar(256) DEFAULT NULL
    );

    INSERT INTO tmp_primary_low_publication_copy (spu_id, new_keyword, clean_description)
    SELECT spu.id,
           LEFT(CONCAT(spu.name, ',一年级,二年级,', keyword.category_names), 256),
           REPLACE(REPLACE(REPLACE(TRIM(spu.description), CHAR(13), ''), CHAR(10), ''), CHAR(9), '')
      FROM product_spu spu
      JOIN tmp_primary_low_publication_title target
        ON target.title = spu.name COLLATE utf8mb4_unicode_ci
      LEFT JOIN tmp_primary_low_publication_category_keyword keyword
        ON keyword.spu_id = spu.id
     WHERE spu.deleted = b'0'
       AND spu.biz_scene = 'PUBLICATION';

    UPDATE tmp_primary_low_publication_copy
       SET p1_pos = NULLIF(LEAST(
               IF(LOCATE('。', clean_description) = 0, 9999, LOCATE('。', clean_description)),
               IF(LOCATE('！', clean_description) = 0, 9999, LOCATE('！', clean_description)),
               IF(LOCATE('？', clean_description) = 0, 9999, LOCATE('？', clean_description)),
               IF(LOCATE('!', clean_description) = 0, 9999, LOCATE('!', clean_description)),
               IF(LOCATE('?', clean_description) = 0, 9999, LOCATE('?', clean_description))
           ), 9999);

    UPDATE tmp_primary_low_publication_copy
       SET p2_pos = CASE
               WHEN p1_pos IS NULL THEN NULL
               ELSE p1_pos + NULLIF(LEAST(
                    IF(LOCATE('。', SUBSTRING(clean_description, p1_pos + 1)) = 0, 9999,
                       LOCATE('。', SUBSTRING(clean_description, p1_pos + 1))),
                    IF(LOCATE('！', SUBSTRING(clean_description, p1_pos + 1)) = 0, 9999,
                       LOCATE('！', SUBSTRING(clean_description, p1_pos + 1))),
                    IF(LOCATE('？', SUBSTRING(clean_description, p1_pos + 1)) = 0, 9999,
                       LOCATE('？', SUBSTRING(clean_description, p1_pos + 1))),
                    IF(LOCATE('!', SUBSTRING(clean_description, p1_pos + 1)) = 0, 9999,
                       LOCATE('!', SUBSTRING(clean_description, p1_pos + 1))),
                    IF(LOCATE('?', SUBSTRING(clean_description, p1_pos + 1)) = 0, 9999,
                       LOCATE('?', SUBSTRING(clean_description, p1_pos + 1)))
               ), 9999)
           END;

    UPDATE tmp_primary_low_publication_copy
       SET new_introduction = CASE
               WHEN CHAR_LENGTH(clean_description) <= 130 THEN clean_description
               WHEN p1_pos BETWEEN 70 AND 140 THEN SUBSTRING(clean_description, 1, p1_pos)
               WHEN p1_pos BETWEEN 1 AND 69 AND p2_pos BETWEEN 80 AND 145 THEN SUBSTRING(clean_description, 1, p2_pos)
               WHEN p1_pos BETWEEN 50 AND 69 THEN SUBSTRING(clean_description, 1, p1_pos)
               ELSE CONCAT(SUBSTRING(clean_description, 1, 130), '…')
           END;

    UPDATE product_spu spu
      JOIN tmp_primary_low_publication_copy copy ON copy.spu_id = spu.id
       SET spu.keyword = copy.new_keyword,
           spu.introduction = copy.new_introduction,
           spu.updater = v_creator,
           spu.update_time = NOW();

    SELECT COUNT(*)
      INTO v_old_keyword_count
      FROM product_spu spu
      JOIN tmp_primary_low_publication_title target
        ON target.title = spu.name COLLATE utf8mb4_unicode_ci
     WHERE spu.deleted = b'0'
       AND spu.biz_scene = 'PUBLICATION'
       AND spu.keyword = v_old_keyword;

    IF v_old_keyword_count <> 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '更新失败：仍存在旧导入批次关键字';
    END IF;

    SELECT MAX(CHAR_LENGTH(spu.keyword)), MAX(CHAR_LENGTH(spu.introduction))
      INTO v_max_keyword_length, v_max_intro_length
      FROM product_spu spu
      JOIN tmp_primary_low_publication_title target
        ON target.title = spu.name COLLATE utf8mb4_unicode_ci
     WHERE spu.deleted = b'0'
       AND spu.biz_scene = 'PUBLICATION';

    IF v_max_keyword_length > 256 OR v_max_intro_length > 145 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '更新失败：关键字或简介长度超出预期';
    END IF;

    COMMIT;

    SELECT v_target_count AS updated_spu_count,
           v_old_keyword_count AS old_keyword_count,
           v_max_keyword_length AS max_keyword_length,
           v_max_intro_length AS max_introduction_length;
END$$

DELIMITER ;

CALL xiaokanhui_update_primary_low_publication_copy_20260521();

DROP PROCEDURE IF EXISTS xiaokanhui_update_primary_low_publication_copy_20260521;
