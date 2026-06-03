-- 刊物商品取消库存：刊物 SPU/SKU 库存仅作为技术保留字段，统一归零。
-- 普通商品库存不受影响。

UPDATE product_sku sku
    INNER JOIN product_spu spu ON spu.id = sku.spu_id
SET sku.stock = 0
WHERE spu.biz_scene = 'PUBLICATION'
  AND sku.deleted = b'0'
  AND spu.deleted = b'0'
  AND sku.stock <> 0;

UPDATE product_spu
SET stock = 0
WHERE biz_scene = 'PUBLICATION'
  AND deleted = b'0'
  AND stock <> 0;
