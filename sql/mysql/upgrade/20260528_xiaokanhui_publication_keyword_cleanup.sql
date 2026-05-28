-- 校刊汇刊物商品关键字清理
-- 目标：根据当前刊物商品简介，替换导入批次标识和机械分类关键字。

SET NAMES utf8mb4;

DROP PROCEDURE IF EXISTS xiaokanhui_cleanup_publication_keyword_20260528;

DELIMITER $$

CREATE PROCEDURE xiaokanhui_cleanup_publication_keyword_20260528()
BEGIN
    DECLARE v_expected_count int DEFAULT 53;
    DECLARE v_missing_count int DEFAULT 0;
    DECLARE v_too_long_count int DEFAULT 0;
    DECLARE v_keyword_match_count int DEFAULT 0;
    DECLARE v_old_keyword_count int DEFAULT 0;
    DECLARE v_min_keyword_length int DEFAULT 0;
    DECLARE v_max_keyword_length int DEFAULT 0;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    DROP TEMPORARY TABLE IF EXISTS tmp_publication_keyword_fix;
    CREATE TEMPORARY TABLE tmp_publication_keyword_fix (
        spu_id bigint NOT NULL PRIMARY KEY,
        title varchar(128) NOT NULL,
        new_keyword varchar(256) NOT NULL
    );

    INSERT INTO tmp_publication_keyword_fix (spu_id, title, new_keyword) VALUES
    (2, 'API测试刊物商品', 'API测试刊物商品,交易链测试,REST测试,订刊测试'),
    (3, '嘟嘟熊画报', '嘟嘟熊画报,幼儿画报,3-7岁,图画故事,亲子共读,趣味认知,亲子互动'),
    (4, '幼儿画报（益智综合刊）', '幼儿画报益智综合刊,3-7岁,图画故事,益智启蒙,文学素养,美学修养'),
    (5, '幼儿画报（人文科普刊）', '幼儿画报人文科普刊,3-7岁,人文科普,院士科普,科学探索,创造力启蒙'),
    (6, '幼儿画报（原创绘本刊）', '幼儿画报原创绘本刊,3-7岁,原创绘本,科学认知,传统文化,情绪情感,艺术启蒙'),
    (7, '米老鼠', '米老鼠,国际IP,少儿漫画,科普知识,英语阅读,品格培养'),
    (8, '小淑女漫画派', '小淑女漫画派,少女漫画,校园漫画,奇幻漫画,搞笑漫画,综合互动'),
    (9, '小葵花·故事画刊', '小葵花故事画刊,童话故事,图画阅读,少儿故事,美育阅读,优秀少儿报刊'),
    (10, '漫趣·我会自己读', '漫趣我会自己读,4-7岁,汉语分级阅读,独立阅读,识字游戏,亲子阅读'),
    (11, '奇趣号', '奇趣号,学前益智,OKIDO,玩中学,科学通识,艺术创新,实践探索'),
    (12, '小朋友·智趣手创', '小朋友智趣手创,少儿阅读,故事游戏,卡通活动,智趣手创,开阔视野'),
    (13, '十万个为什么·科学启蒙', '十万个为什么科学启蒙,少儿科普,STEM启蒙,科学兴趣,优秀少儿报刊'),
    (14, '科学大众·小诺贝尔（低幼版）', '科学大众小诺贝尔低幼版,低幼科普,趣味科普,科学知识,科普画刊,科学思维'),
    (81, '中国少年报（低年级）', '中国少年报低年级,6-10岁,少先队队报,综合新闻,儿童报刊'),
    (82, '小学生数学报', '小学生数学报,小学数学,数学文化,学习兴趣,数学素养,学习资源'),
    (83, '阅读（低年级）', '阅读低年级,全文拼音,独立阅读,语文素养,童话国学,素材积累'),
    (84, '语文报（小学版）', '语文报小学版,小学语文,大语文,课内拓展,基础夯实,语文素养'),
    (85, '时代英语报', '时代英语报,小学英语,英语学习报,凤凰传媒,英语阅读,学习资料'),
    (86, '时代语文周刊', '时代语文周刊,小学语文,课改同步,语文素养,少儿报刊金奖'),
    (87, '时代数学周刊', '时代数学周刊,小学数学,数学思维,方法指导,开拓视野,数学素养'),
    (88, '快乐作文（低年级）', '快乐作文低年级,注音作文,低年级写话,名师课堂,想象表达,作文启蒙'),
    (89, '我的语文我的数学（一年级/二年级）', '我的语文我的数学,一二年级,同步课本,语文数学,看图写话,趣味阅读'),
    (90, '全国优秀作文选（低年级）', '全国优秀作文选低年级,低年级作文,图文阅读,写作能力,日记写话,阅读坊'),
    (91, '少儿科学周刊（儿童版）', '少儿科学周刊儿童版,科学周刊,少儿科普,科学议题,知识介绍,科学兴趣'),
    (92, '轻松学语数（注音版）', '轻松学语数注音版,语文数学,课外读物,学习习惯,想象力,思维力'),
    (93, '天天爱学习', '天天爱学习,小学月刊,学习兴趣,课题成果,少儿阅读,素材积累'),
    (94, '海洋探秘', '海洋探秘,海洋科普,海洋知识,生态环境,科学文化,自然探索'),
    (95, '动物奇迹', '动物奇迹,动物科普,珍稀动物,实拍画面,自然生命,趣味知识'),
    (96, '爆笑王', '爆笑王,趣味漫画,幽默笑话,漫画科普,乐观心态,轻松阅读'),
    (97, '脑力大挑战', '脑力大挑战,智力题,逻辑推理,语言分析,互动故事,思维训练'),
    (98, '环球探索', '环球探索,Discovery,大科普,自然科学,人文历史,科学实验'),
    (99, '天才小画家', '天才小画家,绘画兴趣,创意想象,艺术游戏,美术技巧,少儿绘画'),
    (100, '博物', '博物,中国国家地理,青少年科普,自然探索,实践求知,博物知识'),
    (101, '故事大王', '故事大王,儿童故事,少儿阅读,高质量故事,时代感,素材积累'),
    (102, '小哥白尼-趣味科学', '小哥白尼趣味科学,少儿科普,宇宙海洋,物理生命,趣味科学,科学卡通'),
    (103, '小哥白尼-野生动物', '小哥白尼野生动物,自然科普,野生动物,自然探秘,动物寻奇,环境保护'),
    (104, '小哥白尼-军事科学', '小哥白尼军事科学,军事科普,国防教育,少儿军事,军事知识,科学启蒙'),
    (105, '小哥白尼-漫画科学', '小哥白尼漫画科学,漫画科普,宇宙地球,生命文明,前沿知识,好奇心'),
    (106, '发现号趣味百科', '发现号趣味百科,少儿百科,趣味科普,优秀科普读物,自然探索,少儿地理'),
    (107, '自然探秘', '自然探秘,科普画刊,自然探索,学生科普,优秀少儿报刊,兴趣阅读'),
    (108, '智力大王', '智力大王,智力开发,知识拓展,互动游艺,趣味知识,益智画刊'),
    (109, '神探大揭秘', '神探大揭秘,少年侦探,侦探冒险,推理思考,智慧勇气,成长伙伴'),
    (110, '神探迈克狐', '神探迈克狐,科学侦探,推理悬疑,科学知识,智慧破案,脑力挑战'),
    (111, '趣味数学', '趣味数学,数学故事,智力题,数字追踪,数学兴趣,逻辑思维'),
    (112, '恐龙密码', '恐龙密码,恐龙科普,古生物知识,史前世界,自然科学,少儿科普'),
    (113, '幽默派对', '幽默派对,幽默笑话,校园幽默,减压阅读,快乐成长,积极心态'),
    (114, '创意手工与美术', '创意手工与美术,创意美术,手工制作,艺术熏陶,动手能力,兴趣培养'),
    (115, '爱上看图写话', '爱上看图写话,看图写话,作文启蒙,漫画欣赏,结构表达,想象成文'),
    (116, '我是不白吃', '我是不白吃,漫画百科,趣味知识,科技美食,历史文化,成语故事'),
    (117, '迷你世界', '迷你世界,创意编程,思维挑战,冒险故事,自由探索,益智阅读'),
    (118, '我是大侦探', '我是大侦探,科学推理,少年侦探,思辨能力,快乐阅读,逻辑思维'),
    (119, '自然密码', '自然密码,自然科普,自然风光,动物生命,自然策划,奇妙动物'),
    (120, '少年国学·古典文学常识', '少年国学古典文学常识,少年国学,传统文化,古诗文基础,语文素养,国学启蒙');

    SELECT COUNT(*)
      INTO v_missing_count
      FROM tmp_publication_keyword_fix fix
      LEFT JOIN product_spu spu
        ON spu.id = fix.spu_id
       AND spu.name = fix.title
       AND spu.biz_scene = 'PUBLICATION'
       AND spu.deleted = b'0'
     WHERE spu.id IS NULL;

    IF v_missing_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '刊物关键字清理失败：目标刊物商品不存在或名称不匹配';
    END IF;

    SELECT COUNT(*)
      INTO v_too_long_count
      FROM tmp_publication_keyword_fix
     WHERE CHAR_LENGTH(new_keyword) > 256;

    IF v_too_long_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '刊物关键字清理失败：存在超过 256 字符的关键字';
    END IF;

    SELECT COUNT(*)
      INTO v_missing_count
      FROM tmp_publication_keyword_fix;

    IF v_missing_count <> v_expected_count THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '刊物关键字清理失败：关键字映射数量不符合预期';
    END IF;

    UPDATE product_spu spu
      JOIN tmp_publication_keyword_fix fix ON fix.spu_id = spu.id
       SET spu.keyword = fix.new_keyword,
           spu.updater = 'admin',
           spu.update_time = NOW()
     WHERE spu.biz_scene = 'PUBLICATION'
       AND spu.deleted = b'0'
       AND NOT (spu.keyword <=> fix.new_keyword);

    SELECT COUNT(*)
      INTO v_keyword_match_count
      FROM product_spu spu
      JOIN tmp_publication_keyword_fix fix ON fix.spu_id = spu.id
     WHERE spu.biz_scene = 'PUBLICATION'
       AND spu.deleted = b'0'
       AND spu.keyword = fix.new_keyword;

    IF v_keyword_match_count <> v_expected_count THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '刊物关键字清理失败：落库后关键字与映射不一致';
    END IF;

    SELECT COUNT(*)
      INTO v_old_keyword_count
      FROM product_spu
     WHERE biz_scene = 'PUBLICATION'
       AND deleted = b'0'
       AND (
            keyword LIKE '%报刊信息表（2026.5）导入%'
            OR keyword LIKE '%报刊信息导入（2026.5）%'
            OR keyword LIKE '%,一年级,二年级,%'
       );

    IF v_old_keyword_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '刊物关键字清理失败：仍存在导入标识或旧年级拼接关键字';
    END IF;

    SELECT MIN(CHAR_LENGTH(new_keyword)),
           MAX(CHAR_LENGTH(new_keyword))
      INTO v_min_keyword_length,
           v_max_keyword_length
      FROM tmp_publication_keyword_fix;

    COMMIT;

    SELECT v_expected_count AS target_spu_count,
           v_keyword_match_count AS keyword_match_count,
           v_old_keyword_count AS old_keyword_count,
           v_min_keyword_length AS min_keyword_length,
           v_max_keyword_length AS max_keyword_length;
END $$

DELIMITER ;

CALL xiaokanhui_cleanup_publication_keyword_20260528();

DROP PROCEDURE IF EXISTS xiaokanhui_cleanup_publication_keyword_20260528;
