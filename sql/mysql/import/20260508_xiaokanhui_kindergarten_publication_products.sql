-- 校刊汇幼儿园刊物商品导入
-- 数据来源：/Users/connor/项目/订刊系统/幼儿园报刊信息表（2026.5）.xlsx，工作表：幼儿园

SET NAMES utf8mb4;

DROP PROCEDURE IF EXISTS xiaokanhui_import_kindergarten_publications_20260508;

DELIMITER $$

CREATE PROCEDURE xiaokanhui_import_kindergarten_publications_20260508()
BEGIN
    DECLARE v_import_remark varchar(255) DEFAULT '幼儿园报刊信息表（2026.5）导入';
    DECLARE v_creator varchar(64) DEFAULT 'admin';
    DECLARE v_conflict_count int DEFAULT 0;
    DECLARE v_existing_import_count int DEFAULT 0;
    DECLARE v_missing_count int DEFAULT 0;
    DECLARE v_publication_type_id bigint DEFAULT NULL;
    DECLARE v_root_category_id bigint DEFAULT NULL;
    DECLARE v_root_pic_url varchar(255) DEFAULT NULL;
    DECLARE v_template_count int DEFAULT 0;
    DECLARE v_grade_count int DEFAULT 0;
    DECLARE v_spu_count int DEFAULT 0;
    DECLARE v_sku_count int DEFAULT 0;
    DECLARE v_spu_ext_count int DEFAULT 0;
    DECLARE v_sku_ext_count int DEFAULT 0;
    DECLARE v_grade_rel_count int DEFAULT 0;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    DROP TEMPORARY TABLE IF EXISTS tmp_kindergarten_publication_source;
    CREATE TEMPORARY TABLE tmp_kindergarten_publication_source (
        sort_no int NOT NULL PRIMARY KEY,
        title varchar(128) NOT NULL,
        issue_cycle varchar(64) NOT NULL,
        raw_identifier varchar(255) DEFAULT NULL,
        cn_code varchar(64) DEFAULT NULL,
        isbn varchar(64) DEFAULT NULL,
        post_distribution_code varchar(64) DEFAULT NULL,
        price_cent int NOT NULL,
        publisher_name varchar(255) NOT NULL,
        detail text NOT NULL
    );

    INSERT INTO tmp_kindergarten_publication_source (
        sort_no, title, issue_cycle, raw_identifier, cn_code, isbn, post_distribution_code,
        price_cent, publisher_name, detail
    ) VALUES
    (1, '嘟嘟熊画报', 'MONTHLY', 'CN11-5421/C', 'CN11-5421/C', NULL, NULL, 34200, '中国少年儿童新闻出版总社', '《嘟嘟熊画报》是中国少年儿童新闻出版总社全力推出的品牌形象，是由国际安徒生美术提名奖获得者、著名画家吴带生和《蓝皮鼠大脸猫》、《小糊涂神》之父，著名童话作家葛冰联袂创作的。《嘟嘟熊画报》是《婴儿画报》专刊，是国家教育部推荐的优秀幼儿刊物，每月一期。 “嘟嘟熊”的形象在社会上已经有了很大的知名度：中央电视台的婴幼儿的节目中有过“嘟嘟熊”的系列故事，玩具商、贝塔斯曼读者俱乐部都开始关注“嘟嘟熊”的形象。图文并茂，内容丰富。'),
    (2, '幼儿画报（益智综合刊）', 'MONTHLY', 'CN11-1063/C', 'CN11-1063/C', NULL, NULL, 6000, '中国少年儿童新闻出版总社', '《幼儿画报》的读者对象为3-7岁幼儿及其家长，汇集了一大批国内外优秀作家、画家和幼儿教育专家团队，为孩子精心打造高品质图画故事，让孩子的视野跨越国界，潜移默化地提高孩子的文学素养和高品位的美学修养。
益智综合刊包含了自我保护、五大领域游戏、趣味儿歌、美食屋、视觉大发现、恐龙幼儿园等栏目，并将“机器人”、“恐龙”等孩子喜爱的元素融入故事，激发孩子的无限潜能，在阅读中快乐成长。'),
    (3, '幼儿画报（人文科普刊）', 'MONTHLY', 'CN11-1063/C', 'CN11-1063/C', NULL, NULL, 6000, '中国少年儿童新闻出版总社', '《幼儿画报》的读者对象为3-7岁幼儿及其家长，汇集了一大批国内外优秀作家、画家和幼儿教育专家团队，为孩子精心打造高品质图画故事，让孩子的视野跨越国界，潜移默化地提高孩子的文学素养和高品位的美学修养。
人文科普刊每期均由院士主笔，为孩子讲解科学知识，带领孩子体验妙趣横生的探索之旅，激发他们探究科学的兴趣，提升创造力和创新力。'),
    (4, '幼儿画报（原创绘本刊）', 'MONTHLY', 'CN11-1063/C', 'CN11-1063/C', NULL, NULL, 6000, '中国少年儿童新闻出版总社', '《幼儿画报》的读者对象为3-7岁幼儿及其家长，汇集了一大批国内外优秀作家、画家和幼儿教育专家团队，为孩子精心打造高品质图画故事，让孩子的视野跨越国界，潜移默化地提高孩子的文学素养和高品位的美学修养。
原创绘本刊包含了科学认知、传统文化、人际交往、社会适应、情绪情感等丰富的主题，由中外名家精心创作，侧重文学、艺术的熏陶，培养孩子感受美、表现美的情趣和能力，收获智慧的启迪和情感体验。'),
    (5, '米老鼠', 'MONTHLY', 'CN11-3041/J', 'CN11-3041/J', NULL, NULL, 9000, '童趣出版社', '《米老鼠》是国际知名IP，杂志版于1932年出版，中文版于1993年创刊。杂志不仅仅是给孩子讲述发生在唐纳德和他的朋友们之间的故事，更将知识融入故事中，带孩子学习科普，国画知识、阅读英语故事。而且每一则故事中还蕴含了勇敢、真诚、智慧等优良品质，从而潜移默化地引导孩子成为更好的自己。'),
    (6, '小淑女漫画派', 'MONTHLY', 'ISBN978-7-5436-9310-0
邮发代号：24-917', NULL, 'ISBN978-7-5436-9310-0', '24-917', 9000, '青岛出版社少儿期刊中心', '《小淑女漫画派》是一本面向幼儿及小学生的少女综合漫画杂志，旨在为漫画迷们提供画风精致华美、情节奇趣梦幻、审美时尚健康、思想积极向上的优秀漫画作品，并且创新性地增加综合互动栏目。杂志包含清纯可爱的校园类漫画、神秘奇幻的魔法类漫画、搞怪逗趣的搞笑类漫画、温暖有爱的治愈类漫画等各种满足小读者的漫画。除了优秀原创漫画外，本刊还引进了法国高人气少女漫画，增加了时下风行的手账制作过程、时尚换装、公主涂色等互动栏目。本刊是一本不断在成长、更新的综合型漫画杂志。跟进最新漫画潮流和艺术形式，不断突破，力求始终保持新颖感、活泼感。坚守健康争取的价值观，用爱心、热情和责任感守护每位读者内心的漫画王国。'),
    (7, '小葵花·故事画刊', 'MONTHLY', 'CN37-1071/1', 'CN37-1071/1', NULL, NULL, 10200, '青岛出版社少儿期刊中心', '小葵花故事画刊创刊于1965年，前国家荣誉主席宋庆龄题词，荣获全国连环画报刊协会年度最高大奖“金环奖”，2016年、2017年全国优秀少儿报刊奖，连续入选国家新闻出版广电总局向少年儿童推荐的优秀少儿报刊名单。以清新唯美的童话故事、时尚精美的读图品质打动读者。'),
    (8, '漫趣·我会自己读', 'MONTHLY', 'CN11-5656/C', 'CN11-5656/C', NULL, NULL, 10800, '童趣出版社', '《漫趣-我会自己读》是一本帮助4-7岁有听说基础的孩子，轻松过度到独立阅读的儿童汉语分级读物的刊物。期刊拥有汉语分级领域专家团队，科学合理的、有规划的设定每月阅读识字量与故事内容。另外还有识字游戏、创意手工、儿童诗歌童谣欣赏、中华文化宣传、专家互动阅读指导等多个板块，内容丰富，形式多样，为亲自阅读到独立阅读架起一座桥梁。'),
    (9, '奇趣号', 'MONTHLY', '78-273', NULL, NULL, '78-273', 14400, '青岛出版社少儿期刊中心', '《奇趣号》——英国《OKIDO》原版引进，英国科学协会推荐的学前益智通识杂志，以“玩中学”为理念，内容以科学为主，融合文化、艺术、创新与实践，通过独特的艺术化视角呈现多元主题。精心设计的丰富板块，能全面培养孩子的感知力、实践力、想象力、审美力、思维力与创造力。'),
    (10, '小朋友·智趣手创', 'MONTHLY', 'CN31-1089/C', 'CN31-1089/C', NULL, NULL, 7500, '上海少年儿童出版社', '《小朋友》杂志创办于1922年，由著名文化人黎锦晖（聂耳的老师）任第一任主编，是我国第一本现代儿童杂志。建国后，宋庆龄为《小朋友》题写刊名并题词。本刊多次荣获全国及省、市大奖，董必武、郭沫若、胡乔木、林默涵、冰心等领导同志和知名人士都曾为它题词。本杂志一直以“陶冶儿童性情，增进儿童智慧，开阔儿童视野”为宗旨。它以生动有趣的故事、游戏、卡通、活动......让儿童养成积极乐观的人生态度、勇于探索的学习精神和互动合作的高尚情操。'),
    (11, '十万个为什么·科学启蒙', 'MONTHLY', 'CN31-2108/N', 'CN31-2108/N', NULL, NULL, 7500, '上海少年儿童出版社', '作为中国少儿科普经典 品牌下的系列产品，《十万个为什么》杂志连续入选国家新闻出版广电总局的全国优秀报刊推荐名单。这是一本按照STEM（科学、技术、工程、数学）科学教育理念建设的全媒体综合科普杂志，配有AR。杂志中众多DIY和互动栏目能引领小读者们在看看、想想、做做中，玩转科学，能让小读者们体验科学之魅、锻炼科学思维、培养创造力——这会让孩子终身收益！启蒙版字少图多，有大量的实拍图展示，内容丰富有趣，形式新颖多样。'),
    (12, '科学大众·小诺贝尔（低幼版）', 'MONTHLY', 'CN32/1427/N', 'CN32/1427/N', NULL, NULL, 6000, '科学大众杂志社', '《科学大众》是中国最早创办的科普期刊之一，1954年郭沫若先生亲自为杂志题写刊名。本刊创刊伊始就提出“科学大众化、大众科学化”的宗旨。近年来杂志社深入贯彻国务院《全民科学素质行动计划纲要》精神，依托杂志平台，开展各类青少年科普活动，普及科学知识，传播科学思想。低幼版是一本精心为低幼及小学低年级学生量身打造的趣味科普画刊。每期杂志为小读者介绍一个专题故事，解读生活现象背后的趣味科学知识。杂志注重科学性和故事性，以绘本的形式为读者呈现，内文文字采用大号字并且加注拼音，版式设计精美而活泼，让小读者们爱上科学、爱上阅读。');

    DROP TEMPORARY TABLE IF EXISTS tmp_kindergarten_publication_tag;
    CREATE TEMPORARY TABLE tmp_kindergarten_publication_tag (
        source_sort int NOT NULL,
        tag_sort int NOT NULL,
        tag_name varchar(255) NOT NULL,
        PRIMARY KEY (source_sort, tag_sort)
    );

    INSERT INTO tmp_kindergarten_publication_tag (source_sort, tag_sort, tag_name) VALUES
    (1, 1, '趣味认知'),
    (1, 2, '亲子共读'),
    (1, 3, '亲子互动'),
    (2, 1, '趣味认知'),
    (2, 2, '少儿绘画'),
    (2, 3, '智力开发'),
    (3, 1, '趣味认知'),
    (3, 2, '少儿绘画'),
    (3, 3, '智力开发'),
    (4, 1, '趣味认知'),
    (4, 2, '少儿绘画'),
    (4, 3, '智力开发'),
    (5, 1, '幼儿读物'),
    (5, 2, '漫画故事'),
    (6, 1, '少儿漫画知识'),
    (7, 1, '少儿绘画'),
    (8, 1, '幼儿阅读'),
    (8, 2, '礼貌学习'),
    (8, 3, '逻辑思维'),
    (8, 4, '智力开发'),
    (9, 1, '少儿阅读'),
    (9, 2, '少儿科普'),
    (10, 1, '少儿阅读'),
    (11, 1, '少儿阅读'),
    (11, 2, '少儿科普'),
    (12, 1, '少儿科普');

    DROP TEMPORARY TABLE IF EXISTS tmp_kindergarten_publication_pic;
    CREATE TEMPORARY TABLE tmp_kindergarten_publication_pic (
        source_sort int NOT NULL PRIMARY KEY,
        pic_url varchar(255) NOT NULL
    );

    SELECT id, pic_url
      INTO v_root_category_id, v_root_pic_url
      FROM product_category
     WHERE deleted = b'0'
       AND biz_scene = 'PUBLICATION'
       AND parent_id = 0
       AND name = '刊物'
     LIMIT 1;

    IF v_root_category_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '导入失败：缺少刊物根分类';
    END IF;

    SELECT id
      INTO v_publication_type_id
      FROM product_publication_type
     WHERE deleted = b'0'
       AND status = 0
       AND name = '杂志'
     LIMIT 1;

    IF v_publication_type_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '导入失败：缺少启用状态的刊物类型“杂志”';
    END IF;

    SELECT COUNT(*)
      INTO v_template_count
      FROM trade_delivery_express_template
     WHERE deleted = b'0'
       AND id = 1;

    IF v_template_count <> 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '导入失败：缺少快递模板 id=1';
    END IF;

    SELECT COUNT(*)
      INTO v_grade_count
      FROM edu_grade_catalog
     WHERE deleted = b'0'
       AND status = 0
       AND id IN (1, 2, 3);

    IF v_grade_count <> 3 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '导入失败：缺少启用状态的小班/中班/大班年级目录';
    END IF;

    SELECT COUNT(*)
      INTO v_conflict_count
      FROM product_spu spu
      JOIN tmp_kindergarten_publication_source source ON source.title = spu.name
     WHERE spu.deleted = b'0'
       AND spu.biz_scene = 'PUBLICATION'
       AND (spu.keyword IS NULL OR spu.keyword <> v_import_remark);

    IF v_conflict_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '导入失败：目标刊物名称已存在非本次导入商品';
    END IF;

    SELECT COUNT(*)
      INTO v_existing_import_count
      FROM product_spu spu
      JOIN tmp_kindergarten_publication_source source ON source.title = spu.name
     WHERE spu.deleted = b'0'
       AND spu.biz_scene = 'PUBLICATION'
       AND spu.keyword = v_import_remark;

    IF v_existing_import_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '导入失败：幼儿园刊物商品已导入，请勿重复执行';
    END IF;

    START TRANSACTION;

    INSERT INTO product_publisher (
        name, sort, status, remark, creator, create_time, updater, update_time, deleted, tenant_id
    )
    SELECT source.publisher_name,
           MIN(source.sort_no) * 10,
           0,
           v_import_remark,
           v_creator,
           NOW(),
           v_creator,
           NOW(),
           b'0',
           0
      FROM tmp_kindergarten_publication_source source
     GROUP BY source.publisher_name
    ON DUPLICATE KEY UPDATE
        status = 0,
        remark = IF(product_publisher.deleted = b'1' OR product_publisher.remark IS NULL OR product_publisher.remark = '',
                    VALUES(remark), product_publisher.remark),
        updater = VALUES(updater),
        update_time = VALUES(update_time),
        deleted = b'0';

    INSERT INTO product_category (
        parent_id, name, pic_url, sort, status, biz_scene, creator, create_time, updater, update_time, deleted, tenant_id
    )
    SELECT v_root_category_id,
           category.name,
           COALESCE(v_root_pic_url, ''),
           category.sort_no,
           0,
           'PUBLICATION',
           v_creator,
           NOW(),
           v_creator,
           NOW(),
           b'0',
           0
      FROM (
            SELECT '幼儿阅读' AS name, 140 AS sort_no
            UNION ALL
            SELECT '少儿阅读' AS name, 150 AS sort_no
           ) category
     WHERE NOT EXISTS (
            SELECT 1
              FROM product_category existing
             WHERE existing.deleted = b'0'
               AND existing.biz_scene = 'PUBLICATION'
               AND existing.name = category.name
     );

    SELECT COUNT(*)
      INTO v_missing_count
      FROM (
            SELECT DISTINCT tag.tag_name
              FROM tmp_kindergarten_publication_tag tag
              LEFT JOIN product_category category
                ON category.deleted = b'0'
               AND category.biz_scene = 'PUBLICATION'
               AND category.name = tag.tag_name
             WHERE category.id IS NULL
           ) missing_tag;

    IF v_missing_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '导入失败：存在未初始化的刊物标签分类';
    END IF;

    INSERT INTO tmp_kindergarten_publication_pic (source_sort, pic_url)
    SELECT source.sort_no,
           COALESCE(category.pic_url, v_root_pic_url, '')
      FROM tmp_kindergarten_publication_source source
      LEFT JOIN tmp_kindergarten_publication_tag tag
        ON tag.source_sort = source.sort_no
       AND tag.tag_sort = 1
      LEFT JOIN product_category category
        ON category.deleted = b'0'
       AND category.biz_scene = 'PUBLICATION'
       AND category.name = tag.tag_name;

    INSERT INTO product_spu (
        name, keyword, introduction, description, biz_scene, brand_id, pic_url, slider_pic_urls,
        sort, status, spec_type, price, market_price, cost_price, stock, delivery_types,
        delivery_template_id, give_integral, sub_commission_type, sales_count, virtual_sales_count,
        browse_count, creator, create_time, updater, update_time, deleted, tenant_id
    )
    SELECT source.title,
           v_import_remark,
           SUBSTRING(source.detail, 1, 200),
           source.detail,
           'PUBLICATION',
           NULL,
           pic.pic_url,
           JSON_ARRAY(pic.pic_url),
           source.sort_no * 10,
           1,
           b'0',
           source.price_cent,
           source.price_cent,
           source.price_cent,
           9999,
           '1,3',
           1,
           0,
           b'0',
           0,
           0,
           0,
           v_creator,
           NOW(),
           v_creator,
           NOW(),
           b'0',
           0
      FROM tmp_kindergarten_publication_source source
      JOIN tmp_kindergarten_publication_pic pic ON pic.source_sort = source.sort_no
     ORDER BY source.sort_no;

    INSERT INTO product_sku (
        spu_id, name, properties, price, market_price, cost_price, bar_code, pic_url, stock,
        weight, volume, first_brokerage_price, second_brokerage_price, sales_count, status,
        creator, create_time, updater, update_time, deleted, tenant_id
    )
    SELECT spu.id,
           CONCAT(source.title, '-全学年'),
           NULL,
           source.price_cent,
           source.price_cent,
           source.price_cent,
           NULL,
           spu.pic_url,
           9999,
           NULL,
           NULL,
           0,
           0,
           0,
           0,
           v_creator,
           NOW(),
           v_creator,
           NOW(),
           b'0',
           0
      FROM tmp_kindergarten_publication_source source
      JOIN product_spu spu
        ON spu.deleted = b'0'
       AND spu.biz_scene = 'PUBLICATION'
       AND spu.keyword = v_import_remark
       AND spu.name = source.title
     ORDER BY source.sort_no;

    INSERT INTO product_publication_spu_ext (
        spu_id, publisher_id, publication_type_id, issue_cycle, issn, cn_code, post_distribution_code,
        creator, create_time, updater, update_time, deleted, tenant_id
    )
    SELECT spu.id,
           publisher.id,
           v_publication_type_id,
           source.issue_cycle,
           NULL,
           source.cn_code,
           source.post_distribution_code,
           v_creator,
           NOW(),
           v_creator,
           NOW(),
           b'0',
           0
      FROM tmp_kindergarten_publication_source source
      JOIN product_spu spu
        ON spu.deleted = b'0'
       AND spu.biz_scene = 'PUBLICATION'
       AND spu.keyword = v_import_remark
       AND spu.name = source.title
      JOIN product_publisher publisher
        ON publisher.deleted = b'0'
       AND publisher.name = source.publisher_name
     ORDER BY source.sort_no;

    INSERT INTO product_publication_sku_ext (
        sku_id, isbn, remark,
        creator, create_time, updater, update_time, deleted, tenant_id
    )
    SELECT sku.id,
           source.isbn,
           source.raw_identifier,
           v_creator,
           NOW(),
           v_creator,
           NOW(),
           b'0',
           0
      FROM tmp_kindergarten_publication_source source
      JOIN product_spu spu
        ON spu.deleted = b'0'
       AND spu.biz_scene = 'PUBLICATION'
       AND spu.keyword = v_import_remark
       AND spu.name = source.title
      JOIN product_sku sku
        ON sku.deleted = b'0'
       AND sku.spu_id = spu.id
     ORDER BY source.sort_no;

    INSERT INTO product_publication_sku_grade_rel (
        sku_id, grade_catalog_id, creator, create_time, updater, update_time, deleted, tenant_id
    )
    SELECT sku.id,
           grade.grade_catalog_id,
           v_creator,
           NOW(),
           v_creator,
           NOW(),
           b'0',
           0
      FROM tmp_kindergarten_publication_source source
      JOIN product_spu spu
        ON spu.deleted = b'0'
       AND spu.biz_scene = 'PUBLICATION'
       AND spu.keyword = v_import_remark
       AND spu.name = source.title
      JOIN product_sku sku
        ON sku.deleted = b'0'
       AND sku.spu_id = spu.id
      JOIN (
            SELECT 1 AS grade_catalog_id
            UNION ALL SELECT 2
            UNION ALL SELECT 3
           ) grade
     ORDER BY source.sort_no, grade.grade_catalog_id;

    INSERT INTO product_spu_category_rel (
        spu_id, category_id, sort, creator, create_time, updater, update_time, deleted, tenant_id
    )
    SELECT spu.id,
           category.id,
           tag.tag_sort,
           v_creator,
           NOW(),
           v_creator,
           NOW(),
           b'0',
           0
      FROM tmp_kindergarten_publication_source source
      JOIN tmp_kindergarten_publication_tag tag ON tag.source_sort = source.sort_no
      JOIN product_spu spu
        ON spu.deleted = b'0'
       AND spu.biz_scene = 'PUBLICATION'
       AND spu.keyword = v_import_remark
       AND spu.name = source.title
      JOIN product_category category
        ON category.deleted = b'0'
       AND category.biz_scene = 'PUBLICATION'
       AND category.name = tag.tag_name
     ORDER BY source.sort_no, tag.tag_sort;

    SELECT COUNT(*)
      INTO v_spu_count
      FROM product_spu spu
      JOIN tmp_kindergarten_publication_source source ON source.title = spu.name
     WHERE spu.deleted = b'0'
       AND spu.biz_scene = 'PUBLICATION'
       AND spu.keyword = v_import_remark;

    SELECT COUNT(*)
      INTO v_sku_count
      FROM product_spu spu
      JOIN tmp_kindergarten_publication_source source ON source.title = spu.name
      JOIN product_sku sku ON sku.spu_id = spu.id AND sku.deleted = b'0'
     WHERE spu.deleted = b'0'
       AND spu.biz_scene = 'PUBLICATION'
       AND spu.keyword = v_import_remark;

    SELECT COUNT(*)
      INTO v_spu_ext_count
      FROM product_spu spu
      JOIN tmp_kindergarten_publication_source source ON source.title = spu.name
      JOIN product_publication_spu_ext ext ON ext.spu_id = spu.id AND ext.deleted = b'0'
     WHERE spu.deleted = b'0'
       AND spu.biz_scene = 'PUBLICATION'
       AND spu.keyword = v_import_remark;

    SELECT COUNT(*)
      INTO v_sku_ext_count
      FROM product_spu spu
      JOIN tmp_kindergarten_publication_source source ON source.title = spu.name
      JOIN product_sku sku ON sku.spu_id = spu.id AND sku.deleted = b'0'
      JOIN product_publication_sku_ext ext ON ext.sku_id = sku.id AND ext.deleted = b'0'
     WHERE spu.deleted = b'0'
       AND spu.biz_scene = 'PUBLICATION'
       AND spu.keyword = v_import_remark;

    SELECT COUNT(*)
      INTO v_grade_rel_count
      FROM product_spu spu
      JOIN tmp_kindergarten_publication_source source ON source.title = spu.name
      JOIN product_sku sku ON sku.spu_id = spu.id AND sku.deleted = b'0'
      JOIN product_publication_sku_grade_rel rel ON rel.sku_id = sku.id AND rel.deleted = b'0'
     WHERE spu.deleted = b'0'
       AND spu.biz_scene = 'PUBLICATION'
       AND spu.keyword = v_import_remark;

    IF v_spu_count <> 12 OR v_sku_count <> 12 OR v_spu_ext_count <> 12
        OR v_sku_ext_count <> 12 OR v_grade_rel_count <> 36 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '导入失败：导入后数据计数不符合预期';
    END IF;

    COMMIT;

    SELECT v_spu_count AS imported_spu_count,
           v_sku_count AS imported_sku_count,
           v_spu_ext_count AS imported_spu_ext_count,
           v_sku_ext_count AS imported_sku_ext_count,
           v_grade_rel_count AS imported_grade_rel_count;
END$$

DELIMITER ;

CALL xiaokanhui_import_kindergarten_publications_20260508();

DROP PROCEDURE IF EXISTS xiaokanhui_import_kindergarten_publications_20260508;
