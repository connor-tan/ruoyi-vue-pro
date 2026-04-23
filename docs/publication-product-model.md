# 刊物统一商品中心模型

## 核心约束

- 商品中心只保留一套 `product_spu` / `product_sku` 核心模型。
- 商品业务场景唯一来源是 `product_category.biz_scene`。
- `biz_scene` 当前固定为：
  - `NORMAL`
  - `PUBLICATION`
- SPU 表示一个刊物商品主体，不再按年级拆分 SPU。
- 刊物的年级、周期、版本、册别全部下沉到 SKU。

## 刊物建模

- 刊物主数据：
  - `product_publisher`
  - `product_publication_type`
- 刊物 SPU 扩展：
  - `product_publication_spu_ext`
  - 字段承载出版社、刊物类型、出刊周期、ISSN/CN/邮发代号、履约方式
- 刊物 SKU 扩展：
  - `product_publication_sku_ext`
  - 字段承载 `target_period`、册别、版本、ISBN、备注
- 刊物 SKU 适用年级：
  - `product_publication_sku_grade_rel`
  - 以 `sku_id + grade_catalog_id` 建模

## 规则边界

- 年级不再在 SPU 上维护。
- `target_period` 只允许使用结构化字段，不允许只存在于 `properties` JSON。
- 刊物默认履约方式是 `SCHOOL_STATION`，不映射为普通商品自提门店。
- `product_property` / `product_property_value` 仅服务普通商品规格，不作为刊物规则事实来源。

## 校验规则

- `PUBLICATION` 商品必须有：
  - 出版社
  - 刊物类型
  - 出刊周期
  - 至少一个 SKU
- 每个刊物 SKU 必须有：
  - `target_period`
  - 至少一个适用年级
- `product_publication_type.identifier_rule` 约束如下：
  - `NONE`：不要求额外标识
  - `SKU_ISBN_REQUIRED`：每个 SKU 必填 ISBN
  - `TITLE_PERIODICAL_IDENTIFIER_REQUIRED`：SPU 扩展至少填写 ISSN、CN 刊号、邮发代号之一

## 前端约束

- 管理后台只保留统一商品入口，不再保留独立刊物商品页。
- 刊物商品表单复用统一 SPU 表单，通过类目场景切换字段分区。
- 已废弃模型：
  - `domainType`
  - `publicationTitle`
  - 独立刊物商品 API / 页面

## 订刊域对接约定

- 后续订刊域只消费统一商品模型与刊物只读 API。
- 订刊窗口目标模型应为 `offer / offerSku`，不再回到 `windowSpu / windowSku` 的老语义。
