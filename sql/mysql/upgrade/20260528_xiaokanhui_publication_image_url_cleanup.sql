-- 校刊汇刊物商品旧图片域名清理
-- 目标：替换商品中心仍引用的 http://test.yudao.iocoder.cn 刊物图片地址。

SET NAMES utf8mb4;

DROP PROCEDURE IF EXISTS xiaokanhui_cleanup_publication_image_url_20260528;

DELIMITER $$

CREATE PROCEDURE xiaokanhui_cleanup_publication_image_url_20260528()
BEGIN
    DECLARE v_old_url varchar(255) DEFAULT 'http://test.yudao.iocoder.cn/20260423/xsb24_1776948956903.jpg';
    DECLARE v_missing_count int DEFAULT 0;
    DECLARE v_remaining_count int DEFAULT 0;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    DROP TEMPORARY TABLE IF EXISTS tmp_publication_image_url_fix;
    CREATE TEMPORARY TABLE tmp_publication_image_url_fix (
        spu_id bigint NOT NULL PRIMARY KEY,
        title varchar(128) NOT NULL,
        category_name varchar(128) DEFAULT NULL,
        use_spu_cover bit(1) NOT NULL DEFAULT b'0',
        replacement_url varchar(1024) DEFAULT NULL
    );

    INSERT INTO tmp_publication_image_url_fix (spu_id, title, category_name, use_spu_cover) VALUES
    (10, '漫趣·我会自己读', '幼儿阅读', b'0'),
    (11, '奇趣号', '少儿阅读', b'0'),
    (12, '小朋友·智趣手创', '少儿阅读', b'0'),
    (13, '十万个为什么·科学启蒙', NULL, b'1');

    SELECT COUNT(*)
      INTO v_missing_count
      FROM tmp_publication_image_url_fix fix
      LEFT JOIN product_spu spu
        ON spu.id = fix.spu_id
       AND spu.name = fix.title
       AND spu.biz_scene = 'PUBLICATION'
       AND spu.deleted = b'0'
     WHERE spu.id IS NULL;

    IF v_missing_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '刊物旧图片替换失败：目标刊物商品不存在或名称不匹配';
    END IF;

    UPDATE tmp_publication_image_url_fix fix
      JOIN product_category category
        ON category.deleted = b'0'
       AND category.biz_scene = 'PUBLICATION'
       AND category.name = fix.category_name
       SET fix.replacement_url = category.pic_url
     WHERE fix.use_spu_cover = b'0';

    UPDATE tmp_publication_image_url_fix fix
      JOIN product_spu spu
        ON spu.id = fix.spu_id
       AND spu.deleted = b'0'
       SET fix.replacement_url = spu.pic_url
     WHERE fix.use_spu_cover = b'1';

    SELECT COUNT(*)
      INTO v_missing_count
      FROM tmp_publication_image_url_fix
     WHERE replacement_url IS NULL
        OR replacement_url = ''
        OR replacement_url = v_old_url
        OR replacement_url LIKE 'http://test.yudao.iocoder.cn/%';

    IF v_missing_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '刊物旧图片替换失败：替换目标图片为空或仍为旧域名';
    END IF;

    UPDATE product_spu spu
      JOIN tmp_publication_image_url_fix fix ON fix.spu_id = spu.id
       SET spu.pic_url = fix.replacement_url,
           spu.slider_pic_urls = JSON_ARRAY(fix.replacement_url),
           spu.updater = 'admin',
           spu.update_time = NOW()
     WHERE fix.use_spu_cover = b'0'
       AND spu.biz_scene = 'PUBLICATION'
       AND spu.deleted = b'0'
       AND (
            spu.pic_url = v_old_url
            OR spu.slider_pic_urls LIKE CONCAT('%', v_old_url, '%')
       );

    UPDATE product_sku sku
      JOIN tmp_publication_image_url_fix fix ON fix.spu_id = sku.spu_id
       SET sku.pic_url = fix.replacement_url,
           sku.updater = 'admin',
           sku.update_time = NOW()
     WHERE sku.deleted = b'0'
       AND sku.pic_url = v_old_url;

    SELECT COUNT(*)
      INTO v_remaining_count
      FROM (
            SELECT CAST(spu.pic_url AS CHAR CHARACTER SET utf8mb4) COLLATE utf8mb4_unicode_ci AS url
              FROM product_spu spu
             WHERE spu.biz_scene = 'PUBLICATION'
               AND spu.deleted = b'0'
               AND spu.pic_url LIKE 'http://test.yudao.iocoder.cn/%'
            UNION ALL
            SELECT CAST(slider.url AS CHAR CHARACTER SET utf8mb4) COLLATE utf8mb4_unicode_ci AS url
              FROM product_spu spu
              JOIN JSON_TABLE(spu.slider_pic_urls, '$[*]' COLUMNS(url varchar(1024) PATH '$')) slider
             WHERE spu.biz_scene = 'PUBLICATION'
               AND spu.deleted = b'0'
               AND slider.url LIKE 'http://test.yudao.iocoder.cn/%'
            UNION ALL
            SELECT CAST(sku.pic_url AS CHAR CHARACTER SET utf8mb4) COLLATE utf8mb4_unicode_ci AS url
              FROM product_sku sku
              JOIN product_spu spu ON spu.id = sku.spu_id
             WHERE spu.biz_scene = 'PUBLICATION'
               AND spu.deleted = b'0'
               AND sku.deleted = b'0'
               AND sku.pic_url LIKE 'http://test.yudao.iocoder.cn/%'
           ) remaining;

    IF v_remaining_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '刊物旧图片替换失败：商品中心仍存在旧域名图片';
    END IF;

    COMMIT;

    SELECT 'publication_image_url_cleanup_done' AS result;
END $$

DELIMITER ;

CALL xiaokanhui_cleanup_publication_image_url_20260528();

DROP PROCEDURE IF EXISTS xiaokanhui_cleanup_publication_image_url_20260528;
