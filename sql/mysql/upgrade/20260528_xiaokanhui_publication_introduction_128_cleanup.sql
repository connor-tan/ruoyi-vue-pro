-- 校刊汇刊物商品简介 128 字以内清理
-- 目标：重写刊物商品简介，避免超出 128 字和生硬截断。

SET NAMES utf8mb4;

DROP PROCEDURE IF EXISTS xiaokanhui_cleanup_publication_introduction_128_20260528;

DELIMITER $$

CREATE PROCEDURE xiaokanhui_cleanup_publication_introduction_128_20260528()
BEGIN
    DECLARE v_expected_count int DEFAULT 53;
    DECLARE v_missing_count int DEFAULT 0;
    DECLARE v_too_long_count int DEFAULT 0;
    DECLARE v_intro_match_count int DEFAULT 0;
    DECLARE v_over_limit_count int DEFAULT 0;
    DECLARE v_bad_ending_count int DEFAULT 0;
    DECLARE v_min_intro_length int DEFAULT 0;
    DECLARE v_max_intro_length int DEFAULT 0;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    DROP TEMPORARY TABLE IF EXISTS tmp_publication_introduction_fix;
    CREATE TEMPORARY TABLE tmp_publication_introduction_fix (
        spu_id bigint NOT NULL PRIMARY KEY,
        title varchar(128) NOT NULL,
        new_introduction varchar(128) NOT NULL
    );

    INSERT INTO tmp_publication_introduction_fix (spu_id, title, new_introduction) VALUES
    (2, 'API测试刊物商品', '用于交易链 REST 测试，验证刊物订购、结算与订单链路。'),
    (3, '嘟嘟熊画报', '《嘟嘟熊画报》面向幼儿家庭，以经典图画故事和亲子阅读为主，内容图文并茂，适合启蒙认知与亲子互动。'),
    (4, '幼儿画报（益智综合刊）', '《幼儿画报（益智综合刊）》面向3-7岁儿童，融合图画故事、益智游戏和自我保护内容，帮助孩子在阅读中快乐成长。'),
    (5, '幼儿画报（人文科普刊）', '《幼儿画报（人文科普刊）》面向3-7岁儿童，由院士主笔讲解科学知识，带领孩子体验探索乐趣，激发创造力。'),
    (6, '幼儿画报（原创绘本刊）', '《幼儿画报（原创绘本刊）》汇集原创图画故事，涵盖科学认知、传统文化和情绪情感，重在艺术启蒙与心灵滋养。'),
    (7, '米老鼠', '《米老鼠》以经典IP故事承载科普、国画、英语阅读和品格教育，让孩子在趣味漫画中收获知识与勇气。'),
    (8, '小淑女漫画派', '《小淑女漫画派》面向幼儿及小学生，精选校园、奇幻、搞笑等少女漫画，并加入互动栏目，兼具审美与阅读乐趣。'),
    (9, '小葵花·故事画刊', '《小葵花·故事画刊》以清新童话故事和精美图画打动读者，曾获少儿报刊奖项，适合孩子进行故事阅读和美育启蒙。'),
    (10, '漫趣·我会自己读', '《漫趣·我会自己读》面向4-7岁儿童，通过汉语分级阅读、识字游戏和创意栏目，帮助孩子从亲子阅读走向独立阅读。'),
    (11, '奇趣号', '《奇趣号》引进英国《OKIDO》，以“玩中学”为理念，融合科学、文化、艺术和实践，培养孩子的通识兴趣与创造力。'),
    (12, '小朋友·智趣手创', '《小朋友·智趣手创》创刊历史悠久，以故事、游戏、卡通和活动启迪儿童智慧，帮助孩子拓展视野、保持探索精神。'),
    (13, '十万个为什么·科学启蒙', '《十万个为什么·科学启蒙》以STEM理念组织科普内容，通过DIY和互动栏目，让孩子在动手体验中培养科学兴趣。'),
    (14, '科学大众·小诺贝尔（低幼版）', '《科学大众·小诺贝尔（低幼版）》面向低幼及小学低年级学生，以绘本化科普故事讲解生活现象，培养科学阅读兴趣。'),
    (81, '中国少年报（低年级）', '《中国少年报（低年级）》面向6-10岁儿童，是全国发行的少先队低年级队报，关注新闻、习惯养成和综合素质启蒙。'),
    (82, '小学生数学报', '《小学生数学报》服务小学数学学习，讲解同步知识、数学文化和思维方法，帮助孩子激发兴趣、提升数学素养。'),
    (83, '阅读（低年级）', '《阅读（低年级）》以全文拼音、图文故事、童话国学和写话启蒙为特色，帮助低年级学生独立阅读、积累语文素材。'),
    (84, '语文报（小学版）', '《语文报（小学版）》围绕小学语文学习，立足课内、拓展课外，通过阅读、字词和写作训练提升语文素养。'),
    (85, '时代英语报', '《时代英语报》面向青少年英语学习，结合教材与课外阅读，帮助学生积累语言知识、拓展视野并提升人文素养。'),
    (86, '时代语文周刊', '《时代语文周刊》配合小学语文课改，提供教材同步、读写训练和趣味拓展内容，帮助学生稳步提升语文素养。'),
    (87, '时代数学周刊', '《时代数学周刊》面向小学数学学习，强调兴趣激发、方法指导和思维启迪，帮助学生拓宽视野、提升数学素养。'),
    (88, '快乐作文（低年级）', '《快乐作文（低年级）》采用注音内容和名师编写栏目，配合低年级说写教学，帮助孩子学习写话方法、拓展想象。'),
    (89, '我的语文我的数学（一年级/二年级）', '《我的语文我的数学》面向小学一二年级，分语文和数学两册，围绕同步课本、字词句、阅读与看图写话夯实基础。'),
    (90, '全国优秀作文选（低年级）', '《全国优秀作文选（低年级）》图文并茂，关注新课程和孩子生活体验，通过范文、日记和写话栏目提升写作能力。'),
    (91, '少儿科学周刊（儿童版）', '《少儿科学周刊（儿童版）》引进《科学周刊》版权，每期围绕科学主题展开知识介绍，帮助孩子培养科学兴趣。'),
    (92, '轻松学语数（注音版）', '《轻松学语数（注音版）》以生动形式呈现语文和数学知识，帮助孩子爱上学习，培养习惯、想象力和思维力。'),
    (93, '天天爱学习', '《天天爱学习》面向小学1-6年级学生，围绕学习兴趣、阅读素材和综合能力提升，提供适合日常积累的月刊内容。'),
    (94, '海洋探秘', '《海洋探秘》聚焦海洋知识，融合科学、文化、环境、历史和生态内容，用多样形式带孩子探索奇妙海洋世界。'),
    (95, '动物奇迹', '《动物奇迹》是面向小学生的趣味动物科普月刊，以珍稀动物故事、实拍画面和科普知识，培养自然兴趣与生命意识。'),
    (96, '爆笑王', '《爆笑王》用幽默漫画和趣味科普营造轻松阅读氛围，帮助孩子释放压力、培养乐观心态和积极学习状态。'),
    (97, '脑力大挑战', '《脑力大挑战》通过趣味智力题、逻辑推理和多结局故事训练观察、分析与表达能力，让孩子在互动中锻炼思维。'),
    (98, '环球探索', '《环球探索》引入Discovery视野，面向6-14岁青少年，覆盖自然科学、人文历史、科技发展和实验探索。'),
    (99, '天才小画家', '《天才小画家》以故事带动艺术游戏和实用技巧，培养绘画兴趣、创意想象与审美能力，让孩子更愿意动手表达。'),
    (100, '博物', '《博物》是《中国国家地理》青春版，面向青少年介绍自然、地理和科学知识，引导学生走进自然、探索求知。'),
    (101, '故事大王', '《故事大王》面向小学生读者，精选生动有趣、富有时代感的儿童故事，帮助孩子在轻松阅读中积累表达素材。'),
    (102, '小哥白尼-趣味科学', '《小哥白尼-趣味科学》是少儿科普画刊，围绕宇宙、海洋、物理、环境和生命主题，用图文栏目激发科学兴趣。'),
    (103, '小哥白尼-野生动物', '《小哥白尼-野生动物》面向6-15岁读者，以自然探秘和动物寻奇为主题，用实拍图片、趣味故事和权威知识介绍野生动物。'),
    (104, '小哥白尼-军事科学', '《小哥白尼-军事科学》面向6-15岁读者，讲解军事装备、军营生活和国防知识，培养军事科技兴趣与国防意识。'),
    (105, '小哥白尼-漫画科学', '《小哥白尼-漫画科学》面向6-12岁读者，用原创漫画、趣味故事和前沿知识呈现宇宙、地球、生命等科学主题。'),
    (106, '发现号趣味百科', '《发现号-趣味百科》是全彩少儿百科科普期刊，覆盖科技、自然、历史、天文等领域，帮助孩子发现世界。'),
    (107, '自然探秘', '《自然探秘》是面向学生的大型科普画刊，覆盖地球、海洋、宇宙、环境和生命主题，帮助孩子探索自然奥秘。'),
    (108, '智力大王', '《智力大王》集智力开发、知识拓展和互动游戏于一体，用军事、地理、恐龙、理财等趣味内容训练综合思维。'),
    (109, '神探大揭秘', '《神探大揭秘》融合少年侦探冒险和推理思考，让孩子在阅读中获得乐趣，培养智慧、勇气和想象力。'),
    (110, '神探迈克狐', '《神探迈克狐》以科学侦探故事串联案件推理和科普知识，让孩子跟随主角动脑破案，激发观察力与科学兴趣。'),
    (111, '趣味数学', '《趣味数学》把智力题、数字追踪和数学家故事变成趣味阅读，让孩子在故事中激发数学兴趣与逻辑思维。'),
    (112, '恐龙密码', '《恐龙密码》以恐龙和古生物知识为主题，带孩子穿越史前世界，系统认识神奇的古生物知识。'),
    (113, '幽默派对', '《幽默派对》以校园幽默、轻松故事和趣味图片传递积极心态，帮助学生在紧张学习之余放松心情。'),
    (114, '创意手工与美术', '《创意手工与美术》围绕艺术欣赏、手工制作、美术技巧和创意鉴赏，培养孩子的审美兴趣、创意表达和动手能力。'),
    (115, '爱上看图写话', '《爱上看图写话》通过精彩故事、趣味漫画和精美插图，引导孩子观察、思考、想象和表达，轻松学习看图写话。'),
    (116, '我是不白吃', '《我是不白吃》以生动漫画讲述科技、美食、历史、文化和成语故事，让孩子在笑声中开阔眼界、增长知识。'),
    (117, '迷你世界', '《迷你世界》以创意编程、思维挑战和冒险故事为主，带孩子在想象世界中玩思维游戏，感受自由探索与创造乐趣。'),
    (118, '我是大侦探', '《我是大侦探》主张轻松阅读和快乐阅读，用科学推理故事训练思辨能力，让孩子在侦探阅读中获得乐趣。'),
    (119, '自然密码', '《自然密码》用浅显文字和趣味栏目讲解自然风光、动物生命和科学现象，帮助孩子开阔视野、保持探索好奇心。'),
    (120, '少年国学·古典文学常识', '《少年国学·古典文学常识》用故事、漫画和小剧场讲解传统文化与古诗文知识，帮助孩子亲近国学、提升语文素养。');

    SELECT COUNT(*)
      INTO v_missing_count
      FROM tmp_publication_introduction_fix fix
      LEFT JOIN product_spu spu
        ON spu.id = fix.spu_id
       AND spu.name = fix.title
       AND spu.biz_scene = 'PUBLICATION'
       AND spu.deleted = b'0'
     WHERE spu.id IS NULL;

    IF v_missing_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '刊物简介清理失败：目标刊物商品不存在或名称不匹配';
    END IF;

    SELECT COUNT(*)
      INTO v_too_long_count
      FROM tmp_publication_introduction_fix
     WHERE CHAR_LENGTH(new_introduction) > 128;

    IF v_too_long_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '刊物简介清理失败：存在超过 128 字的简介';
    END IF;

    SELECT COUNT(*)
      INTO v_missing_count
      FROM tmp_publication_introduction_fix;

    IF v_missing_count <> v_expected_count THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '刊物简介清理失败：简介映射数量不符合预期';
    END IF;

    SELECT COUNT(*)
      INTO v_bad_ending_count
      FROM tmp_publication_introduction_fix
     WHERE new_introduction NOT REGEXP '[。！？]$'
        OR new_introduction LIKE '%…%'
        OR new_introduction LIKE '%<%';

    IF v_bad_ending_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '刊物简介清理失败：存在非完整收束、HTML 或省略号简介';
    END IF;

    UPDATE product_spu spu
      JOIN tmp_publication_introduction_fix fix ON fix.spu_id = spu.id
       SET spu.introduction = fix.new_introduction,
           spu.updater = 'admin',
           spu.update_time = NOW()
     WHERE spu.biz_scene = 'PUBLICATION'
       AND spu.deleted = b'0'
       AND NOT (spu.introduction <=> fix.new_introduction);

    SELECT COUNT(*)
      INTO v_intro_match_count
      FROM product_spu spu
      JOIN tmp_publication_introduction_fix fix ON fix.spu_id = spu.id
     WHERE spu.biz_scene = 'PUBLICATION'
       AND spu.deleted = b'0'
       AND spu.introduction = fix.new_introduction;

    IF v_intro_match_count <> v_expected_count THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '刊物简介清理失败：落库后简介与映射不一致';
    END IF;

    SELECT COUNT(*)
      INTO v_over_limit_count
      FROM product_spu
     WHERE biz_scene = 'PUBLICATION'
       AND deleted = b'0'
       AND CHAR_LENGTH(introduction) > 128;

    IF v_over_limit_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '刊物简介清理失败：商品中心仍存在超过 128 字简介';
    END IF;

    SELECT COUNT(*)
      INTO v_bad_ending_count
      FROM product_spu
     WHERE biz_scene = 'PUBLICATION'
       AND deleted = b'0'
       AND (
            introduction NOT REGEXP '[。！？]$'
            OR introduction LIKE '%…%'
            OR introduction LIKE '%<%'
       );

    IF v_bad_ending_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '刊物简介清理失败：商品中心仍存在非完整收束、HTML 或省略号简介';
    END IF;

    SELECT MIN(CHAR_LENGTH(new_introduction)),
           MAX(CHAR_LENGTH(new_introduction))
      INTO v_min_intro_length,
           v_max_intro_length
      FROM tmp_publication_introduction_fix;

    COMMIT;

    SELECT v_expected_count AS target_spu_count,
           v_intro_match_count AS introduction_match_count,
           v_over_limit_count AS over_limit_count,
           v_bad_ending_count AS bad_ending_count,
           v_min_intro_length AS min_introduction_length,
           v_max_intro_length AS max_introduction_length;
END $$

DELIMITER ;

CALL xiaokanhui_cleanup_publication_introduction_128_20260528();

DROP PROCEDURE IF EXISTS xiaokanhui_cleanup_publication_introduction_128_20260528;
