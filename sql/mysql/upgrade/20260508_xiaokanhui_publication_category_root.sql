-- 校刊汇：刊物商品分类唯一根节点初始化与数据检查
-- 业务口径：biz_scene = 'PUBLICATION' 且 parent_id = 0 的分类是唯一刊物根。

DROP PROCEDURE IF EXISTS xkh_publication_category_root_guard;

DELIMITER //
CREATE PROCEDURE xkh_publication_category_root_guard()
BEGIN
    DECLARE root_count INT DEFAULT 0;

    SELECT COUNT(*)
    INTO root_count
    FROM product_category
    WHERE biz_scene = 'PUBLICATION'
      AND parent_id = 0
      AND deleted = b'0';

    IF root_count > 1 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '存在多个刊物顶级分类，请先保留唯一 PUBLICATION + parent_id=0 根分类';
    END IF;

    IF root_count = 0 THEN
        INSERT INTO product_category (
            parent_id, name, pic_url, sort, status, biz_scene,
            creator, create_time, updater, update_time, deleted, tenant_id
        )
        VALUES (
            0, '刊物', '', 0, 0, 'PUBLICATION',
            '1', NOW(), '1', NOW(), b'0', 0
        );
    END IF;
END//
DELIMITER ;

CALL xkh_publication_category_root_guard();

DROP PROCEDURE IF EXISTS xkh_publication_category_root_guard;
