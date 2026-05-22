-- 校刊汇小学低年级刊物商品导入
-- 数据来源：/Users/connor/项目/订刊系统/小学低年级/低年级汇总.xlsx，工作表：Sheet1
-- 业务口径：小学低年级 = 一年级、二年级；已存在同名刊物跳过；本脚本只导入商品中心基础刊物数据。

SET NAMES utf8mb4;

DROP PROCEDURE IF EXISTS xiaokanhui_import_primary_low_publications_20260521;

DELIMITER $$

CREATE PROCEDURE xiaokanhui_import_primary_low_publications_20260521()
BEGIN
    DECLARE v_import_remark varchar(255) DEFAULT '小学低年级报刊信息导入（2026.5）';
    DECLARE v_creator varchar(64) DEFAULT 'admin';
    DECLARE v_root_category_id bigint DEFAULT NULL;
    DECLARE v_root_pic_url varchar(255) DEFAULT NULL;
    DECLARE v_conflict_count int DEFAULT 0;
    DECLARE v_existing_import_count int DEFAULT 0;
    DECLARE v_missing_count int DEFAULT 0;
    DECLARE v_template_count int DEFAULT 0;
    DECLARE v_grade_count int DEFAULT 0;
    DECLARE v_spu_count int DEFAULT 0;
    DECLARE v_sku_count int DEFAULT 0;
    DECLARE v_spu_ext_count int DEFAULT 0;
    DECLARE v_sku_ext_count int DEFAULT 0;
    DECLARE v_grade_rel_count int DEFAULT 0;
    DECLARE v_category_rel_count int DEFAULT 0;
    DECLARE v_issue_template_count int DEFAULT 0;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    DROP TEMPORARY TABLE IF EXISTS tmp_primary_low_publication_source;
    CREATE TEMPORARY TABLE tmp_primary_low_publication_source (
        sort_no int NOT NULL PRIMARY KEY,
        title varchar(128) NOT NULL,
        cycle_raw varchar(32) NOT NULL,
        issue_cycle varchar(64) NOT NULL,
        issue_count int NOT NULL,
        interval_days int NOT NULL,
        publication_type_name varchar(64) NOT NULL,
        raw_identifier varchar(255) DEFAULT NULL,
        cn_code varchar(64) DEFAULT NULL,
        isbn varchar(64) DEFAULT NULL,
        post_distribution_code varchar(64) DEFAULT NULL,
        price_cent int NOT NULL,
        publisher_name varchar(255) NOT NULL,
        detail text NOT NULL,
        pic_url varchar(1024) NOT NULL,
        slider_pic_urls varchar(2000) NOT NULL
    );

    INSERT INTO tmp_primary_low_publication_source (
        sort_no, title, cycle_raw, issue_cycle, issue_count, interval_days, publication_type_name,
        raw_identifier, cn_code, isbn, post_distribution_code, price_cent, publisher_name, detail,
        pic_url, slider_pic_urls
    ) VALUES
    (1, '中国少年报（低年级）', '周刊', 'WEEKLY', 26, 7, '报纸', 'CN11-0063', 'CN11-0063', NULL, NULL, 6500, '中国少年儿童新闻出版总社', '中国少年报低年级版面向6-10岁的儿童，是唯一面向全国发行的中国少年先锋队低年级少先队队报，由中国共产主义青年团中央委员会主管，中国少年儿童新闻出版总社主办，中国少年报社出版，属于综合类新闻报刊。中国少年报低年级以开展队前教育，推出优秀少先队活动、优秀少先队员典型为主题，以习惯的养成教育为己任，“受众最大化、道理故事化、栏目形象化、知识游戏化、版面多变化、语言口语化、活动普及化”是本报的特色，坚持“说儿童话、为儿童说话、让儿童说话”的办报理念和“好人生从好习惯开始，好习惯从《中国少年报》开始”的办报宗旨，帮助儿童了解天下事，解除学习生活中的烦恼，培养科学兴趣，提高审美能力，注重素质教育，活动丰富多彩，是班主任和家长的得力助手，被誉为“人生第一报”。', 'http://61.160.108.46:29000/connor/20260521/primary_low/中国少年报（儿童版）.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/中国少年报（儿童版）.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/中国少年报（儿童版）4月1.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/中国少年报（儿童版）4月2.jpg"]'),
    (2, '小学生数学报', '周报', 'WEEKLY', 26, 7, '报纸', 'CN32-0701/F', 'CN32-0701/F', NULL, NULL, 4000, '江苏教育报刊总社', '《小学生数学报》是江苏省教育厅主管、江苏教育报刊总社主办的全国首份小学数学专业报纸，1985年4月创刊，著名数学家苏步青亲笔题词！本报的办报宗旨是为小学生学好数学、提升全面素质服务，核心是传播数学文化、激发学习兴趣、开拓知识视野、提供学习资源、培养数学素养。内容特色：“同步辅导”紧扣新课标，同步教材知识点，讲解重难点、易错点；“思维拓展”有名师大讲坛、每日思维操等栏目，训练逻辑、空间与推理能力；“趣味阅读”里有数学故事、生活数学、数学史趣谈、游戏与漫画，寓教于乐；“素养提升”里渗透数学思想方法，培养科学探究精神与解决实际问题能力。', 'http://61.160.108.46:29000/connor/20260521/primary_low/小学生数学报（一年级）.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/小学生数学报（一年级）.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/小学生数学报（二年级）.jpg"]'),
    (3, '阅读（低年级）', '月刊', 'MONTHLY', 6, 30, '杂志', 'CN32-1729/G4', 'CN32-1729/G4', NULL, NULL, 4800, '江苏教育报刊总社', '《阅读》低年级是江苏省教育厅主管、江苏教育报刊总社主办的少儿阅读期刊。本刊的办刊宗旨是我阅读、我快乐、我成长，提供优质精神食量，激发阅读兴趣，提升语文素养。内容特色：“全文拼音”适配低年级识字量，实现无障碍独立阅读；“图文并茂”短小故事、童话、国学、科普，搭配童趣彩图，寓教于乐；“同步助学”衔接课堂，字词积累、写话启蒙，助力读写能力进阶。王牌栏目：“名著直通车”精选绘本与经典儿童文学，培养文学审美；“字词城堡”汉字演化、趣味识字，轻松掌握字词规律；“国学讲堂”启蒙国学经典、传承文化底蕴；“我的乐园”刊登学生原创儿歌、童谣与短写话，鼓励创作；“动漫俱乐部”趣味动漫故事，贴合孩子兴趣。', 'http://61.160.108.46:29000/connor/20260521/primary_low/阅读低年级.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/阅读低年级.jpg"]'),
    (4, '语文报（小学版）', '半月刊', 'SEMI_MONTHLY', 12, 15, '杂志', 'CN14-0703/F', 'CN14-0703/F', NULL, NULL, 4800, '语文报出版社', '《语文报》是由山西师大教育科技传媒集团主办，语文报出版社出版的全国性小学语文专业报纸，1981年创刊，本报的办报理念是坚持大语文，立足课内、拓展课外，激发兴趣’夯实基础、提升素养。内容特色：低年级全文注音，手绘插图，趣味故事+识字启蒙；同步辅导，紧扣新课标与统编教材，单元知识点解析、易错点拨、语文要素专项训练；阅读拓展，名家美文、国学经典、科普故事、跨学科阅读，积累素材、开拓视野；写作指导，技法讲解、范文赏析、学生习作刊登，助力从写话到作文进阶。王牌栏目：“课文放大镜”深度解读教材课文，延伸知识与文化背景；“阅读训练营”分级阅读练习，提升理解与答题能力；“作文百花园”优秀习作展示，配名师点评与写作技巧；“国学小讲堂”三字经、弟子规、古诗词等启蒙内容；“字词游乐园”趣味识字、词语积累、病句修改等基础训练。', 'http://61.160.108.46:29000/connor/20260521/primary_low/语文报一年级4月.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/语文报一年级4月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/语文报一年级5月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/语文报二年级4月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/语文报二年级5月.jpg"]'),
    (5, '时代英语报', '周报', 'WEEKLY', 26, 7, '报纸', 'CN32-0097', 'CN32-0097', NULL, NULL, 4500, '江苏凤凰教育出版社', '《时代英语报》是江苏省第一家英语学习报，由江苏教育出版社主办，隶属于全国规模、实力都有一定影响的大型新闻出版传媒产业集团——凤凰传媒集团，面向全国发行。《时代英语报》以“传播优秀文化、推进课程改革、指导英语学习、提高人文素养”为宗旨，紧跟课程改革步伐，紧密结合教学实践，以丰富多彩的阅读内容，生动活泼的版面形式，全力帮助广大青少年读者学好英语、提高素质。各版内容既源于教材，配合考试，帮助读者提高学习成绩，又高于教材，适当拓展，为读者的终生学习和素质发展打好基础。', 'http://61.160.108.46:29000/connor/20260521/primary_low/时代英语报一年级.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/时代英语报一年级.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/时代英语报二年级.jpg"]'),
    (6, '时代语文周刊', '周报', 'WEEKLY', 26, 7, '报纸', 'CN32-0078', 'CN32-0078', NULL, NULL, 4500, '江苏凤凰教育出版社', '《时代学习报·语文周刊》是江苏凤凰出版传媒股份有限公司主管、江苏凤凰报刊出版传媒有限公司主办的小学语文报，荣获中国少儿报刊金奖。办报宗旨是：与时代同步，与课改同行，全面配合教学改革，提升学生语文素养。内容特色：“同步教材”，紧扣新课标与统编教材，解析重难点、易错点，配单元练习与测试；“读写并重”，名家美文、经典古诗词、时鲜素材；写作技法、范文点评、学生习作展示；“趣味拓展”，国学启蒙、文史百科、趣味阅读，激发兴趣，开阔视野；“名师打造”，苏教版教材编委、特级教师领衔，省市教研员参与撰稿，质量权威。王牌栏目：“原创天地”名家首发美文，提升阅读审美；“课文放大镜”教材重难点精讲，趣味知识补充；“阅读训练营”分级阅读练习，强化理解能力；“作文百花园”学生佳作+名师点评，助力写作进阶；“国学小讲堂”古诗词、传统文化启蒙，涵养底蕴', 'http://61.160.108.46:29000/connor/20260521/primary_low/时代语文报二年级.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/时代语文报二年级.jpg"]'),
    (7, '时代数学周刊', '周报', 'WEEKLY', 26, 7, '报纸', 'CN32-0078', 'CN32-0078', NULL, NULL, 4500, '江苏凤凰教育出版社', '《时代学习报·数学周刊》是江苏凤凰出版传媒主管、江苏凤凰报刊出版传媒主办的小学数学专业报纸，2006年创刊，荣获中国少儿报刊金奖。办刊宗旨：激发兴趣、指导方法、启迪思维、开拓视野，全面提升数学素养与综合能力。内容特色：“同步精讲”紧扣新课标与教材，解析重难点、易错点，配单元练习与测试，夯实课内基础；“思维拓展”名师原创、生活数学、数学史话、趣味游戏，训练逻辑思维与解题能力；“分层辅导”基础巩固、解题技巧、错例分析，适配不同水平学生，兼顾提优与补差；“权威编写”教材编委、特级教师、省市教研员撰稿，内容专业、严谨、实用。王牌栏目：“善学乐思”原创趣味数学，链接生活，激发兴趣；“直通课堂”同步教材重难点，精讲核心知识；“小试身手”分层练习题，巩固每周所学；“快乐地带”数学故事、游戏、漫画，寓教于乐；“名师讲坛”特级教师讲解解题方法与思维技巧。', 'http://61.160.108.46:29000/connor/20260521/primary_low/时代数学报二年级.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/时代数学报二年级.jpg"]'),
    (8, '快乐作文（低年级）', '月刊', 'MONTHLY', 6, 30, '杂志', 'CN13-1357/G4', 'CN13-1357/G4', NULL, NULL, 4800, '河北阅读传媒有限责任公司', '快乐作文一二年级版全部内容注有汉语拼音，其中“快乐课堂”板块由北京市特级教师编写组及全国全国名师编写，内容以“人教版”为主，紧密配合小学低年级说写教学，与说写教学同步。“快乐宝盒”由名师主持，教会说写方法，拓展想象空间。', 'http://61.160.108.46:29000/connor/20260521/primary_low/快乐作文低年级3月.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/快乐作文低年级3月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/快乐作文低年级4月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/快乐作文低年级5月.jpg"]'),
    (9, '我的语文我的数学（一年级/二年级）', '半年刊', 'SEMI_ANNUAL', 1, 180, '杂志', 'CN32-1878/G4', 'CN32-1878/G4', NULL, NULL, 7200, '江苏凤凰教育出版社', '《我的语文我的数学》是由江苏凤凰教育出版社出版，面向小学一二年级的注音版同步期刊，分语文和数学两册，适配人教版和苏教版教材。刊物核心定位：同步课本、夯实字词、句子、阅读与写话基础。主要内容：课文同步解析、生字注音与笔顺、词句练习、看图写话、趣味阅读与国学启蒙。', 'http://61.160.108.46:29000/connor/20260521/primary_low/我的语文我的数学6月.png', '["http://61.160.108.46:29000/connor/20260521/primary_low/我的语文我的数学6月.png", "http://61.160.108.46:29000/connor/20260521/primary_low/我的语文我的数学7月.png"]'),
    (10, '全国优秀作文选（低年级）', '月刊', 'MONTHLY', 6, 30, '杂志', 'CN32-174B/G4', 'CN32-174B/G4', NULL, NULL, 7200, '江苏凤凰教育出版社', '《全国优秀作文选》（小学低年级）好看实用、图文并茂，让孩子充分享受写作文的快乐。彩色插图赏心悦目，让孩子主动阅读。关注新课程，配合新教材关注小学生的生活、思想、情感，帮助孩子提高写作能力。杂志有文曲星、作文T台秀、连环画、日记本、同体话匣子、写话童子功、阅读坊、涂鸦坊等栏目，内容丰富多彩，不枯燥很有趣，可以让孩子有兴趣阅读下去。我们不仅仅在这本杂志中读到很多文笔优美、笔触细腻的作文及点评，还可以接受作文写作指导，比如“刘老师教写话“和”何老师魔法课堂，通过片段训练、多图写话来锻炼孩子的写作能力，孩子写作文不容易写偏。对于小学低年级的孩子来说，阅读一些长篇幅没有标注汉语拼音的文章是很困难的，本杂志很贴心，因为它为低年级的小学生标注了汉语拼音，而且文字部分排版也很舒适，还配有童趣的绘画，让孩子轻轻松松阅读杂志，轻轻松松写作文。', 'http://61.160.108.46:29000/connor/20260521/primary_low/全国优秀作文选（低年级）3月.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/全国优秀作文选（低年级）3月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/全国优秀作文选（低年级）4月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/全国优秀作文选（低年级）5月.jpg"]'),
    (11, '少儿科学周刊（儿童版）', '月刊', 'MONTHLY', 6, 30, '杂志', 'CN13-1412/N', 'CN13-1412/N', NULL, NULL, 6000, '河北阅读传媒有限公司', '《少儿科学周刊》引进美国《科学周刊》版权，为了激发孩子学习科学的兴趣，让孩子更好的学习科学，《少儿科学周刊》每期结合当前科学发展议题介绍一个主题，根据这一主题将内容分为四大板块：知识介绍板块，有浅入深对主题知识进行介绍。充满童趣的语言让科学学习不再枯燥。开拓视野板块，包括探索发现、大开眼界、科学童话、最科学等栏目，开拓学生视野，激发学习兴趣。动手实践板块，列举一些与主题相关的动手小实验，让孩子在亲自操作的过程的过程中思考、解决问题。语言学习板块，中英对照，快乐学英语。严谨的科技英语词汇、活泼有趣的形式，让孩子轻松学英语。《少儿科学周刊》还配合小学《科学》教材，进行适度扩展，以增加学生学科的兴趣。', 'http://61.160.108.46:29000/connor/20260521/primary_low/少儿科学周刊（儿童版）3月.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/少儿科学周刊（儿童版）3月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/少儿科学周刊（儿童版）4月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/少儿科学周刊（儿童版）5月.jpg"]'),
    (12, '轻松学语数（注音版）', '月刊', 'MONTHLY', 6, 30, '杂志', 'CN/33-1392/G4', 'CN/33-1392/G4', NULL, NULL, 7500, '浙江省期刊总社有限公司', '《轻松学语数》是为孩子量身打造的学习类课外读物，通过生动的表现形式，将语文和数学知识表现得活泼有趣，使小读者爱上学习，培养良好的学习习惯，培养想象力和思维力，全方位提高自身素质。', 'http://61.160.108.46:29000/connor/20260521/primary_low/轻松学语数3月.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/轻松学语数3月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/轻松学语数4月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/轻松学语数5月.jpg"]'),
    (13, '天天爱学习', '月刊', 'MONTHLY', 6, 30, '杂志', 'CN12-1414/G4', 'CN12-1414/G4', NULL, NULL, 9000, '天津电子出版社', '《天天爱学习》是面向小学1-6年级学生的月刊，由天津出版传媒集团主管、天津电子出版社与天津市期刊协会主办，2007年创刊，是中国教育学会小学教育专业委员会指导的课题成果期刊。本刊物精准分级。1-6年级每年级独立刊；趣味学习，以故事、漫画、游戏承载知识点，主打“玩中学”激发兴趣、降低学习难度；同步培优，紧扣课本，解析重难点，训练解题方法，兼顾基础巩固+思维拓展。', 'http://61.160.108.46:29000/connor/20260521/primary_low/天天爱学习一年级.png', '["http://61.160.108.46:29000/connor/20260521/primary_low/天天爱学习一年级.png", "http://61.160.108.46:29000/connor/20260521/primary_low/天天爱学习二年级.png"]'),
    (14, '海洋探秘', '月刊', 'MONTHLY', 6, 30, '杂志', 'ISBN978-7-5552-5404-1', NULL, 'ISBN978-7-5552-5404-1', NULL, 6000, '青岛出版社少儿期刊中心', '《海洋探秘》是一本精彩、震撼、时尚的海洋宝典。杂志每一期主打介绍一个海洋知识，随后关于科学、文化、环境、历史、生态等各个方面的海洋知识都会用多样的形式和多彩的知识结构进行科普。既有追溯历史的远古探奇，又有精彩纷呈的奇趣动物之旅，既有博闻多识的科学家介绍，又有引人入胜的自然传奇故事。这本杂志并不是简单、机械地介绍海洋知识，而是充分贴合了少年儿童的生理和心理需求，用图文并茂、浅显易懂的方式，把科学家们海洋的探索历程讲述出来。用拟人化的语言搭配趣味故事、漫画把人与自然彼此依存、休戚相关的关系诠释出来，培养孩子热爱海洋、热爱大自然的情怀。杂志的图片都是摄影师实地拍摄的高清、高质量图片，真实再现海洋世界的奇妙景观及专业海洋生物知识。用生动的语言、精美的图片以及孩子喜爱且容易接受的方式带孩子探索奇妙的海洋世界，揭示海洋生态的奥秘，能够一下子把孩子的目光拉到千里之外的海洋，激发孩子的好奇心。', 'http://61.160.108.46:29000/connor/20260521/primary_low/海洋探秘3月.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/海洋探秘3月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/海洋探秘4月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/海洋探秘5月.jpg"]'),
    (15, '动物奇迹', '月刊', 'MONTHLY', 6, 30, '杂志', 'ISBN978-7-5436-8631-9', NULL, 'ISBN978-7-5436-8631-9', NULL, 7000, '青岛出版社少儿期刊中心', '《动物奇迹》是由青岛出版社出版，是一本专为小学生打造的趣味动物科普月刊。杂志内容生动有趣，汇集了世界各地珍稀动物的真实故事、高清实拍画面与趣味科普知识。刊物兼具知识性与趣味性，不仅能让孩子开阔眼界、认识奇妙的动物世界，，还能培养热爱自然、尊重生命的美好品质，是非常适合孩子课外阅读、拓宽自然视野的优质科普读物。', 'http://61.160.108.46:29000/connor/20260521/primary_low/动物奇迹3月.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/动物奇迹3月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/动物奇迹4月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/动物奇迹5月.jpg"]'),
    (16, '爆笑王', '月刊', 'MONTHLY', 6, 30, '杂志', 'CN37-1584/G', 'CN37-1584/G', NULL, NULL, 6000, '青岛出版社少儿期刊中心', '幽默是一种智慧，是一种才华，是一种力量！《爆笑王》创刊于2014年，致力于为孩子们打造优质的趣味漫画科普，让读者在轻松愉悦的阅读氛围中，培养乐观心态，积极面对学习。', 'http://61.160.108.46:29000/connor/20260521/primary_low/爆笑王3月.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/爆笑王3月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/爆笑王4月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/爆笑王5月.jpg"]'),
    (17, '脑力大挑战', '月刊', 'MONTHLY', 6, 30, '杂志', 'ISBN978-7-5552-0066-6', NULL, 'ISBN978-7-5552-0066-6', NULL, 7000, '青岛出版社少儿期刊中心', '这本杂志很“怪异”，需要从中间开始向左右两边翻阅；这本杂志很“有料”，特邀智力作家编写趣味智力题，提升你的逻辑推理能力、语言分析能力、好玩不重样；这本杂志很“没主见”，一个故事，N种结局，一切由你决定！每个转折点，都是对你观察力、分析力、记忆力以及其他能力的体验；这本杂志不准只用眼睛看，还要动手做，国际创意大师的奇思妙想、疯狂科学家的趣味实验离我们宾不遥远，帅气又博学的N博士会带你逐一体验，灵活你的双手，活跃你的大脑，总而言之，面侦破、荒岛求生、地心探奇......智力冒险缺你不可！疯狂实验、神秘魔术、百变创意.......智力奇迹由你创造', 'http://61.160.108.46:29000/connor/20260521/primary_low/脑力大挑战3月.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/脑力大挑战3月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/脑力大挑战4月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/脑力大挑战5月.jpg"]'),
    (18, '环球探索', '月刊', 'MONTHLY', 6, 30, '杂志', 'CN10-1425/J', 'CN10-1425/J', NULL, NULL, 9600, '童趣出版有限公司', '环球探索是美国探索频道（Discovery）独家授权、中国科学院老科技工作者协会推荐。读者对象是6-14岁的青少年。全球220个国家和地区、45中语言、20亿用户所能看到的缤纷世界，自然科学、人文历史、科技发展和科学实验四大领域火热呈现，带给孩子“大科普”的阅读感受，真正体验到环球探索的魅力，国家自然科学最高学术机构——中国科学院普讲师团专家的鼎力支持，专业科普人士的亲笔著述，紧跟国内外最新发生的科学事件，展现全球优秀摄影师的倾力之作，为小读者呈现人生第一本科普学习类杂志，奉献精彩绝伦的科普视觉盛宴。', 'http://61.160.108.46:29000/connor/20260521/primary_low/环球探索3月.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/环球探索3月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/环球探索4月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/环球探索5月.jpg"]'),
    (19, '天才小画家', '月刊', 'MONTHLY', 6, 30, '杂志', '24-795', NULL, NULL, '24-795', 8000, '青岛出版社少儿期刊中心', '本刊以培养绘画兴趣、激发创意想象和挖掘无限才华为目标，用精彩的故事带动新奇有趣的艺术游戏形式，配合实用技巧，让不爱画画的孩子爱上画画，让热爱画画的孩子画得更妙。用具有张力的故事情节赋予游戏特殊含义，孩子的每一笔起落都又足够的动因。', 'http://61.160.108.46:29000/connor/20260521/primary_low/天才小画家3月.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/天才小画家3月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/天才小画家4月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/天才小画家5月.jpg"]'),
    (20, '博物', '月刊', 'MONTHLY', 6, 30, '杂志', 'CN11-5716/P', 'CN11-5716/P', NULL, NULL, 15000, '《中国国家地理杂志社》有限公司', '博物是《中国国家地理》青春版，是在CNG同一品牌下，依据读者定位不同而产生的杂志，是《中国国家地理》杂志有益的补充。以青少年为主要读者对象，引导学生走进自然、勇于实践、博学广纳、探索求知。它集知识性、趣味性、互动性于一身，图文并茂，紧跟时代。内容广泛涉猎天文、地理、生物、历史等诸多领域，具有科学性、权威性、趣味性，对青少年的健康成长起到良性的引导作用。', 'http://61.160.108.46:29000/connor/20260521/primary_low/博物3月.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/博物3月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/博物4月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/博物5月.jpg"]'),
    (21, '故事大王', '月刊', 'MONTHLY', 6, 30, '杂志', 'CN31-1084/C', 'CN31-1084/C', NULL, NULL, 5600, '上海少年儿童出版社', '《故事大王》创办于1983年1月，是一本以小学生级读者为对象的儿童故事读物。自创办开始就以“高起点、高质量的少儿故事读物”为目标。编辑部聘请了“刘兰芳、姜昆、秦文君、鞠萍等知名艺术家担任顾问，将内容编辑的生动有趣，又有时代感，收到了广大小读者的喜爱。本刊曾创造中国少儿期刊的销售记录，至今未被打破，目前仍是中国畅销的少儿期刊之一。幽默、有趣的故事永远能抓住孩子的心', 'http://61.160.108.46:29000/connor/20260521/primary_low/故事大王3月.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/故事大王3月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/故事大王4月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/故事大王5月.jpg"]'),
    (22, '小哥白尼-趣味科学', '月刊', 'MONTHLY', 6, 30, '杂志', 'CN61-1286/N', 'CN61-1286/N', NULL, NULL, 8000, '陕西小哥白尼杂志社有限公司', '《小哥白尼-趣味科学》是一本图文并茂的少儿科普画刊。信息量大、参与性强、儿童趣味浓郁等特点，主要反映宇宙、海洋、物理、环境和生命五大主题，设有地球巡逻队，、科技大盘、生物大观、神秘大自然、趣味科学馆、军事长镜头、科学卡通、科学奇闻60秒等栏目。它就像是一个知识的大宝库，用生动的语言、精美的图片把你带进一个又一个知识的殿堂。科技大转盘讲的是最新的科技进展，你可以坐上火星探测号，也可以看看微波炉到底怎样八十五做熟；地球巡逻队带领你穿梭时空，同恐龙对话，到大峡谷做客；宇宙观测站让你一直遥望到深邃的太空；当然也有生物大观于海底动物园里的动物朋友。', 'http://61.160.108.46:29000/connor/20260521/primary_low/小哥白尼趣味科学3月.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/小哥白尼趣味科学3月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/小哥白尼趣味科学4月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/小哥白尼趣味科学5月.jpg"]'),
    (23, '小哥白尼-野生动物', '月刊', 'MONTHLY', 6, 30, '杂志', 'CN61-1286/N', 'CN61-1286/N', NULL, NULL, 8000, '陕西小哥白尼杂志社有限公司', '《小哥白尼-野生动物》是1997年创刊，面向6-15岁小读者的自然科普月刊，由陕西省出版印刷公司主管主办，小哥白尼杂志社出版。以“自然探秘、动物寻奇”为主题，用实拍大图+趣味故事+权威知识，带孩子认识野生动物、探索自然奥秘，培养爱护动物、保护环境的意识。主要栏目有“野性大地/大洋觅踪/天空游侠”，陆地、海洋、空中动物的生存故事；“动物星档案”动物习性、冷知识，专业又好懂；“森林故事会/神秘故事城”，冒险故事，融入生态知识；“真相实验室/新奇研究所”拆解动物奇妙行为，趣味科普；“超级冒险家/动物园的故事”，科考纪实、动物保育故事。国际国内野生动物专家供稿，知识准确靠谱；大量高清实拍图，身临其境刊世界珍奇动物，给读者带来视觉震撼；故事化叙事、语言浅显，低年级也能自主阅读；传递生命教育与环保理念，激发探索自然的好奇心。', 'http://61.160.108.46:29000/connor/20260515/image_1778823803768.png', '["http://61.160.108.46:29000/connor/20260515/image_1778823803768.png"]'),
    (24, '小哥白尼-军事科学', '月刊', 'MONTHLY', 6, 30, '杂志', 'CN61-1286/N', 'CN61-1286/N', NULL, NULL, 8000, '陕西小哥白尼杂志社有限公司', '《小哥白尼-军事科学》是2006年创刊，面向6-15岁小读者的军事科普月刊，由陕西省出版印刷公司主管主办，小哥白尼杂志社出版，是国内少有的少儿国防教育期刊，是全国唯一与数百位解放军合作、专注国防教育的少儿杂志。以“解析武器装备，领略军队风采”为主题，用高清实拍+趣味故事+权威知识，带孩子认识军事装备、了解军营生活、培养爱国情怀与国防意识。主要栏目有“军营大揭秘/小鬼进军营”，解放军训练、演习、日常故事，零距离看军营；“王牌战神/战场传奇档案”，古今名将、经典战役，还原战争智慧；“名枪Club/铁甲陆战王/海上钢铁侠/空中格斗场”，陆、海、空武器装备全解析，坦克、军舰、战机、枪械一网打尽；“疯狂军械师/重返古战场”，武器原理、古代战争趣史，趣味科普军事知识。解放军专家供稿，装备数据、军营知识准确可靠；大量高清实景图、装备解析图，直观感受军事力量；故事化叙事、语言浅显，低年级也能自主阅读；传递爱国精神与国防理念，激发探索军事科技的好奇心。', 'http://61.160.108.46:29000/connor/20260521/primary_low/小哥白尼军事科学5月.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/小哥白尼军事科学5月.jpg"]'),
    (25, '小哥白尼-漫画科学', '月刊', 'MONTHLY', 6, 30, '杂志', 'CN61-1286/N', 'CN61-1286/N', NULL, NULL, 8000, '陕西小哥白尼杂志社有限公司', '《小哥白尼-漫画科学》是面向6-12岁小读者的漫画科普月刊，属于《小哥白尼》系列，由陕西省出版社印刷公司主管，小哥白尼杂志社出版。用原创漫画+趣味故事+前沿知识，覆盖宇宙、地球、生命、文明、科幻五大领域，把抽象科学变直观，激发好奇心，培养阅读力。主要栏目“科学漫画”《科学探险队》《偷星记》等连载，剧情+知识点双在线。“宇宙观察站”星空、黑洞、航天器等太空奥秘；“地球巡逻队”地理奇观、自然现象、环境科学；“生命研究所”动植物冷知识、人体科学、趣味仿生；“文明大冒险”历史趣闻、古文明探秘、科技史话；“科幻空间站”未来科技、脑洞实验、科幻故事。全彩漫画分镜，语言浅白，低年级可自主阅读；天文、地理、生物、历史、科幻一网打尽；动手实验、谜题闯关、读者投稿、边玩边学。延续《小哥白尼》专业编审，知识准确、价值观正向。', 'http://61.160.108.46:29000/connor/20260521/primary_low/小哥白尼漫画科学3月.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/小哥白尼漫画科学3月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/小哥白尼漫画科学4月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/小哥白尼漫画科学5月.jpg"]'),
    (26, '发现号趣味百科', '月刊', 'MONTHLY', 6, 30, '杂志', 'CN32-1056/C', 'CN32-1056/C', NULL, NULL, 7500, '江苏凤凰少年儿童出版社有限公司', '《发现号-趣味百科》大型全彩少儿百科、科普期刊，荣获“江苏省教育厅推荐的优秀科普读物”、“国家新闻出版广电总局向全国少年儿童推荐的优秀少儿期刊”。以神奇、趣味、感悟为主题，包括科技、自然、历史、军事、天文、艺术、生物等多领域趣味知识，为少年儿童打开一扇展望世界、启智感悟、发现探索的窗口。', 'http://61.160.108.46:29000/connor/20260521/primary_low/发现号3月.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/发现号3月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/发现号4月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/发现号5月.jpg"]'),
    (27, '自然探秘', '月刊', 'MONTHLY', 6, 30, '杂志', '97-8015-791-1', NULL, NULL, '97-8015-791-1', 7500, '江苏凤凰少年儿童出版社有限公司', '《自然探秘》是一本专为学生打造的大型科普画刊，又江苏少年儿童出版社创办，是江苏省教育厅推荐优秀科普读物，入选2014年国家新闻出版广电总局向全国少年儿童推荐哟羞少儿报刊。本刊设计地球、海洋、宇宙、环境、生命等领域，将漫画、图片和知识融为一体，尽显大自然神奇，以酷炫的自然世界和丰富的百科知识，为孩子打开探索未知世界的大门。', 'http://61.160.108.46:29000/connor/20260521/primary_low/自然探秘3月.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/自然探秘3月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/自然探秘4月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/自然探秘5月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/自然探秘6月.jpg"]'),
    (28, '智力大王', '月刊', 'MONTHLY', 6, 30, '杂志', 'CN32-1056/C', 'CN32-1056/C', NULL, NULL, 7500, '江苏凤凰少年儿童出版社有限公司', '《智力大王》是一本集智力开发、知识拓展、互动游艺为一体的益智画刊，它具有认知、启智、互动的亮点，大部分栏目都融入了一定的趣味知识，包括军事、地理、恐龙、理财等方面，通过知识与游艺的结合。开启智慧，激发潜能，有效锻炼观察力、记忆力、理解力和推理力。在互动上强调参与性，尽可能地利用故事的形式来营造真实的问题情景，吸用孩子主动开动脑筋，完成一系列的智力训练。', 'http://61.160.108.46:29000/connor/20260521/primary_low/智力大王3月.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/智力大王3月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/智力大王4月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/智力大王5月.jpg"]'),
    (29, '神探大揭秘', '月刊', 'MONTHLY', 6, 30, '杂志', 'CN64-1054/N', 'CN64-1054/N', NULL, NULL, 7500, '陕西童话世界杂志社有限公司', '《神探大揭秘》是一本经典与时尚相结合的少年侦探冒险杂志，让你从阅读中得到乐趣，从乐趣中引发思考。我们相信作为新时代的男孩女孩，智慧、勇气、想象力、不屈不挠的精神都是不可或缺的。而这本杂志，将会是你最好的成长伙伴和助手。', 'http://61.160.108.46:29000/connor/20260521/primary_low/神探大揭秘3月.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/神探大揭秘3月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/神探大揭秘4月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/神探大揭秘5月.jpg"]'),
    (30, '神探迈克狐', '月刊', 'MONTHLY', 6, 30, '杂志', 'CN44-1515/Z', 'CN44-1515/Z', NULL, NULL, 9000, '《孩子》编辑部', '在一个以动物为主导的世界，神探迈克狐和助理啾飒凭借着科学与智慧，在格兰岛上破获了一个个神秘离奇的案件。你会发现，每个案件背后都隐藏着神奇的科学知识，比如：蝙蝠是用超声波定位，杜鹃鸟“雀占鸠巢”，生石灰遇水放热.......还等什么，快来和迈克狐一起，激活大脑，用科学与智慧，破解谜案吧！', 'http://61.160.108.46:29000/connor/20260521/primary_low/神探迈克狐3月.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/神探迈克狐3月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/神探迈克狐4月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/神探迈克狐5月.jpg"]'),
    (31, '趣味数学', '月刊', 'MONTHLY', 6, 30, '杂志', 'CN51-1802G', 'CN51-1802G', NULL, NULL, 7500, '四川少年儿童出版社有限公司', '一道道智力题变成了妙趣横生的故事，一个个大侦探带你去数字追踪，一位位大数学家把他们的成长经历娓娓道来......如果说兴趣是最好的老师，这本《趣味数学》里就装满了最好的“老师”！凭借其革命性的教育理念，以及编辑的不断创新，提供给孩子前所未有的阅读体验，让孩子在爱不释手的同时，找回夜袭乐趣。依托卓越品质，创刊初期月销量突破万册，跻身中国最优秀快乐学习类期刊行列。', 'http://61.160.108.46:29000/connor/20260521/primary_low/趣味数学3月.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/趣味数学3月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/趣味数学4月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/趣味数学5月.jpg"]'),
    (32, '恐龙密码', '月刊', 'MONTHLY', 6, 30, '杂志', NULL, NULL, NULL, NULL, 7500, '江苏凤凰少年儿童出版社有限公司', '《恐龙密码》是一本时尚新锐的、以恐龙为知识、古生物知识为内容的少儿科普刊物，它依托江苏少年儿童出版社优秀的编辑力量和丰富的资源，带领孩子们穿越史前时期，进入神秘的恐龙时代，全方案解析、呈现精彩神奇的古生物知识世界。', 'http://61.160.108.46:29000/connor/20260521/primary_low/恐龙密码3月.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/恐龙密码3月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/恐龙密码4月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/恐龙密码5月.jpg"]'),
    (33, '幽默派对', '月刊', 'MONTHLY', 6, 30, '杂志', 'CN2-1269/J', 'CN2-1269/J', NULL, NULL, 8000, '天津人民美术出版社主办', '《幽默派对》是一本旨在为学生减轻学习压力、帮助学生快乐成长的幽默类刊物，本刊不仅传递着干净纯粹的快乐，更是用轻松有趣的文字来传递积极向上的心态。贴近校园、贴近学生的幽默段子和高效图片，助力学生在紧张的学习之余放松心情。', 'http://61.160.108.46:29000/connor/20260521/primary_low/幽默派对3月.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/幽默派对3月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/幽默派对4月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/幽默派对5月.jpg"]'),
    (34, '创意手工与美术', '月刊', 'MONTHLY', 6, 30, '杂志', 'CN32-1776/G4', 'CN32-1776/G4', NULL, NULL, 7500, '江苏凤凰少年儿童出版社有限公司', '《创意美术与手工》注重对小读者的艺术熏陶，创意激发，及动手能力的培养。刊物栏目设置包括艺术欣赏类、手工类、美术技巧教学类、创意鉴赏类等多方面素质培养内容。作为一本充实小读者们课余生活的刊物，《创意美术与手工》在栏目选题上尊重孩子们的喜好，把艺术和创意带入孩子们的日常生活，注重互动，重视培养孩子们的动手能力。', 'http://61.160.108.46:29000/connor/20260521/primary_low/创意美术与手工3月.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/创意美术与手工3月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/创意美术与手工4月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/创意美术与手工5月.jpg"]'),
    (35, '爱上看图写话', '月刊', 'MONTHLY', 6, 30, '杂志', 'CN61-1476/C', 'CN61-1476/C', NULL, NULL, 7500, '启迪杂志社', '《爱上看图写话》内容精彩、文字幽默、插图精美，旨在通过精彩故事的分享‘好玩漫画的欣赏，让孩子在愉悦的阅读中，不知不觉中感受经典、提高鉴赏、学会结构，在潜移默化中轻松学会作文。达到：一分钟看图，二分钟思考，三分钟想象，四分钟成文。', 'http://61.160.108.46:29000/connor/20260521/primary_low/爱上看图写话3月.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/爱上看图写话3月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/爱上看图写话4月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/爱上看图写话5月.jpg"]'),
    (36, '我是不白吃', '月刊', 'MONTHLY', 6, 30, '杂志', 'CN12-1171/J', 'CN12-1171/J', NULL, NULL, 9000, '天津人民美术出版社', '《我是不白吃》是一本集趣味性与知识性于一体的漫画百科杂志，以生动、有趣的漫画故事，讲述科技、美食、历史、文化、名人轶事、成语故事、儿童心理等。用孩子感兴趣、能读懂的方式，拆解尖端科技背后的学科知识；呈现山海异兽的奇幻魅力。以独特的视角和幽默的语言，令枯燥的百科知识变得简单、易懂，让孩子在充满笑声的阅读中开眼界、长知识。', 'http://61.160.108.46:29000/connor/20260521/primary_low/我是不白吃3月.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/我是不白吃3月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/我是不白吃4月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/我是不白吃5月.jpg"]'),
    (37, '迷你世界', '月刊', 'MONTHLY', 6, 30, '杂志', 'CN51-1802/G', 'CN51-1802/G', NULL, NULL, 9000, '四川少年儿童出版社有限公司', '《迷你世界》是四川少年儿童出版社有限公司主管主办，是一本益智又好玩的创意编程及思维挑战杂志。由迷你世界里的明星卡通人物——迷斯拉、兔美美、潘达、熊孩子......带领读者在充满想象力的世界里，玩思维游戏，学创意编程，读冒险故事，感受自由探索的魅力，体会快乐创造的乐趣，提升逻辑思维，读完每一期，孩子们会觉得世界奇妙，充满了探索欲、灵感多多。', 'http://61.160.108.46:29000/connor/20260521/primary_low/迷你世界3月.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/迷你世界3月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/迷你世界4月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/迷你世界5月.jpg"]'),
    (38, '我是大侦探', '月刊', 'MONTHLY', 6, 30, '杂志', 'CN36-1057/G0', 'CN36-1057/G0', NULL, NULL, 8000, '江西美术出版社
《小猕猴智力画刊》编辑部', '《我是大侦探》是一本科学推理探索的少年侦探杂志，杂志主张轻松阅读、快乐阅读，让读者从积极阳光、正能量的侦探故事中得到乐趣，从乐趣中引发思考，提高思辨能力。对少年儿童开发智力、学习科学知识非常有益。大咖云集、精美原创插图，让小读者真正享受阅读的快乐。本刊主要栏目有侦探大本营、神探小侦探、非常推理、故事万花筒、历史谜案等。另外还有一些有趣益智的小栏目，每个栏目都有各自的特点，充分照顾了不同的阅读需要。', 'http://61.160.108.46:29000/connor/20260521/primary_low/我是大侦探3月.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/我是大侦探3月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/我是大侦探4月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/我是大侦探5月.jpg"]'),
    (39, '自然密码', '月刊', 'MONTHLY', 6, 30, '杂志', 'CN-12-1022/J', 'CN-12-1022/J', NULL, NULL, 8000, '南腔北调杂志社', '《自然密码》将带你拥抱自然，为你讲解自然，向你展现大自然的无穷魅力。杂志栏目有自然大策划、奇妙动物园、远古穿越记等。让你足不出户也能领略世界各地的自然风光，见证千奇百怪的生命历程，上至在雪山上翱翔的雄鹰，下至深海中遨游的鲨鱼，每一种生物都有它出彩的地方。我们用浅显有趣的话语为孩子科普自然界的奇闻异事，开拓视野，增长知识，在不知不觉中培养孩子探索与学习能力。我们用通俗易懂的文字，搭配生动有趣的漫画，回答来自小读者的提问，满足小读者的好奇心。我们可以教你制作叶脉书签，变科学的趣味魔术，同你一起解密科学原理。', 'http://61.160.108.46:29000/connor/20260521/primary_low/自然密码3月.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/自然密码3月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/自然密码4月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/自然密码5月.jpg"]'),
    (40, '少年国学·古典文学常识', '月刊', 'MONTHLY', 6, 30, '杂志', 'CN/32-174B/G4', 'CN/32-174B/G4', NULL, NULL, 7200, '江苏凤凰教育出版社', '《少年国学》旨在用通俗、活泼、有趣的方式，赋予国学“厚重”之外的一份“轻盈”，为小读者奉献与时俱进的“新国学”，帮助大家在快乐学习中，亲近传统文化，为初高中的古代诗歌、文言文学习打下扎实的基础，让“中华民族做根本的文化基因”深植于心，同时提高语文素养，做个“地道”的中国人。刊物采用故事、漫画、小剧场等趣味化的形式呈现古代优秀文学、历史、哲学、政治、军事、音乐、书法、绘画及生活方面的内容。读者还可以通过扫描二维码提交古诗文朗诵音频，跟全国小朋友一起比拼，编读互动非常丰富。', 'http://61.160.108.46:29000/connor/20260521/primary_low/少年国学3月.jpg', '["http://61.160.108.46:29000/connor/20260521/primary_low/少年国学3月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/少年国学4月.jpg", "http://61.160.108.46:29000/connor/20260521/primary_low/少年国学5月.jpg"]');

    DROP TEMPORARY TABLE IF EXISTS tmp_primary_low_publication_tag;
    CREATE TEMPORARY TABLE tmp_primary_low_publication_tag (
        source_sort int NOT NULL,
        tag_sort int NOT NULL,
        tag_name varchar(255) NOT NULL,
        PRIMARY KEY (source_sort, tag_sort)
    );

    INSERT INTO tmp_primary_low_publication_tag (source_sort, tag_sort, tag_name) VALUES
    (1, 1, '队报队刊'),
    (2, 1, '学习资料'),
    (3, 1, '少儿阅读'),
    (3, 2, '素材积累'),
    (4, 1, '学习资料'),
    (5, 1, '学习资料'),
    (6, 1, '学习资料'),
    (7, 1, '学习资料'),
    (8, 1, '学习资料'),
    (8, 2, '文学读物'),
    (9, 1, '学习资料'),
    (10, 1, '学习辅导'),
    (10, 2, '文学读物'),
    (11, 1, '少儿科普'),
    (12, 1, '少儿阅读'),
    (12, 2, '素材积累'),
    (13, 1, '少儿阅读'),
    (13, 2, '素材积累'),
    (14, 1, '自然科学'),
    (14, 2, '人文地理'),
    (14, 3, '少儿科普'),
    (15, 1, '自然科学'),
    (15, 2, '人文地理'),
    (15, 3, '少儿科普'),
    (16, 1, '幽默笑话'),
    (16, 2, '儿童文学'),
    (16, 3, '少儿漫画知识'),
    (17, 1, '智力开发'),
    (17, 2, '趣味认知'),
    (18, 1, '少儿科普'),
    (19, 1, '少儿绘画'),
    (20, 1, '趣味认知'),
    (20, 2, '智力开发'),
    (20, 3, '少儿科普'),
    (21, 1, '少儿阅读'),
    (21, 2, '素材积累'),
    (22, 1, '趣味认知'),
    (22, 2, '智力开发'),
    (22, 3, '少儿科普'),
    (23, 1, '趣味认知'),
    (23, 2, '智力开发'),
    (23, 3, '少儿科普'),
    (24, 1, '趣味认知'),
    (24, 2, '智力开发'),
    (24, 3, '少儿科普'),
    (25, 1, '趣味认知'),
    (25, 2, '智力开发'),
    (25, 3, '少儿科普'),
    (26, 1, '智力开发'),
    (26, 2, '自然探索'),
    (26, 3, '少儿地理'),
    (27, 1, '兴趣阅读'),
    (28, 1, '逻辑思维'),
    (28, 2, '儿童读物'),
    (28, 3, '智力开发'),
    (29, 1, '推理悬疑'),
    (29, 2, '智力开发'),
    (29, 3, '少年侦探'),
    (30, 1, '推理悬疑'),
    (30, 2, '智力开发'),
    (30, 3, '少年侦探'),
    (31, 1, '逻辑思维'),
    (31, 2, '学习辅导'),
    (32, 1, '少儿科普'),
    (32, 2, '自然科学'),
    (33, 1, '幽默笑话'),
    (34, 1, '新手工'),
    (34, 2, '逻辑思维'),
    (34, 3, '兴趣培养'),
    (35, 1, '学习资料'),
    (35, 2, '素材积累'),
    (36, 1, '漫画故事'),
    (36, 2, '少儿阅读'),
    (37, 1, '逻辑思维'),
    (37, 2, '儿童读物'),
    (37, 3, '智力开发'),
    (38, 1, '逻辑思维'),
    (38, 2, '少儿侦探'),
    (39, 1, '少儿科普'),
    (40, 1, '少儿阅读');

    DROP TEMPORARY TABLE IF EXISTS tmp_primary_low_issue_no;
    CREATE TEMPORARY TABLE tmp_primary_low_issue_no (
        issue_no int NOT NULL PRIMARY KEY
    );

    INSERT INTO tmp_primary_low_issue_no (issue_no) VALUES
    (1),
    (2),
    (3),
    (4),
    (5),
    (6),
    (7),
    (8),
    (9),
    (10),
    (11),
    (12),
    (13),
    (14),
    (15),
    (16),
    (17),
    (18),
    (19),
    (20),
    (21),
    (22),
    (23),
    (24),
    (25),
    (26);

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

    SELECT COUNT(*)
      INTO v_missing_count
      FROM product_publication_type
     WHERE deleted = b'0'
       AND status = 0
       AND name IN ('杂志', '报纸');

    IF v_missing_count <> 2 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '导入失败：缺少启用状态的刊物类型“杂志/报纸”';
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
       AND id IN (4, 5);

    IF v_grade_count <> 2 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '导入失败：缺少启用状态的一年级/二年级目录';
    END IF;

    SELECT COUNT(*)
      INTO v_conflict_count
      FROM product_spu spu
      JOIN tmp_primary_low_publication_source source ON source.title = spu.name
     WHERE spu.deleted = b'0'
       AND spu.biz_scene = 'PUBLICATION';

    IF v_conflict_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '导入失败：目标刊物名称已存在，请检查跳过名单';
    END IF;

    SELECT COUNT(*)
      INTO v_existing_import_count
      FROM product_spu spu
     WHERE spu.deleted = b'0'
       AND spu.biz_scene = 'PUBLICATION'
       AND spu.keyword = v_import_remark;

    IF v_existing_import_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '导入失败：小学低年级刊物商品已导入，请勿重复执行';
    END IF;

    START TRANSACTION;

    INSERT INTO system_dict_data (
        sort, label, value, dict_type, status, color_type, css_class, remark,
        creator, create_time, updater, update_time, deleted
    )
    SELECT 30, '半月刊', 'SEMI_MONTHLY', 'edu_cycle', 0, '', '', v_import_remark,
           v_creator, NOW(), v_creator, NOW(), b'0'
      FROM DUAL
     WHERE NOT EXISTS (
           SELECT 1 FROM system_dict_data
            WHERE deleted = b'0' AND dict_type = 'edu_cycle' AND value = 'SEMI_MONTHLY'
     );

    UPDATE system_dict_data
       SET label = '半月刊', sort = 30, status = 0, updater = v_creator, update_time = NOW()
     WHERE deleted = b'0' AND dict_type = 'edu_cycle' AND value = 'SEMI_MONTHLY';

    INSERT INTO system_dict_data (
        sort, label, value, dict_type, status, color_type, css_class, remark,
        creator, create_time, updater, update_time, deleted
    )
    SELECT 40, '半年刊', 'SEMI_ANNUAL', 'edu_cycle', 0, '', '', v_import_remark,
           v_creator, NOW(), v_creator, NOW(), b'0'
      FROM DUAL
     WHERE NOT EXISTS (
           SELECT 1 FROM system_dict_data
            WHERE deleted = b'0' AND dict_type = 'edu_cycle' AND value = 'SEMI_ANNUAL'
     );

    UPDATE system_dict_data
       SET label = '半年刊', sort = 40, status = 0, updater = v_creator, update_time = NOW()
     WHERE deleted = b'0' AND dict_type = 'edu_cycle' AND value = 'SEMI_ANNUAL';

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
      FROM tmp_primary_low_publication_source source
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
           missing.tag_name,
           v_root_pic_url,
           200 + missing.sort_no,
           0,
           'PUBLICATION',
           v_creator,
           NOW(),
           v_creator,
           NOW(),
           b'0',
           0
      FROM (
            SELECT tag.tag_name, MIN(source.sort_no * 10 + tag.tag_sort) AS sort_no
              FROM tmp_primary_low_publication_tag tag
              JOIN tmp_primary_low_publication_source source ON source.sort_no = tag.source_sort
             GROUP BY tag.tag_name
           ) missing
     WHERE NOT EXISTS (
            SELECT 1
              FROM product_category existing
             WHERE existing.deleted = b'0'
               AND existing.biz_scene = 'PUBLICATION'
               AND existing.name = missing.tag_name
     );

    SELECT COUNT(*)
      INTO v_missing_count
      FROM (
            SELECT DISTINCT tag.tag_name
              FROM tmp_primary_low_publication_tag tag
              LEFT JOIN product_category category
                ON category.deleted = b'0'
               AND category.biz_scene = 'PUBLICATION'
               AND category.name = tag.tag_name
             WHERE category.id IS NULL
           ) missing_tag;

    IF v_missing_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '导入失败：存在未初始化的刊物标签分类';
    END IF;

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
           source.pic_url,
           source.slider_pic_urls,
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
      FROM tmp_primary_low_publication_source source
     ORDER BY source.sort_no;

    INSERT INTO product_sku (
        spu_id, name, properties, price, market_price, cost_price, bar_code, pic_url, stock,
        weight, volume, first_brokerage_price, second_brokerage_price, sales_count, status,
        creator, create_time, updater, update_time, deleted, tenant_id
    )
    SELECT spu.id,
           CONCAT(source.title, '-6个月'),
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
      FROM tmp_primary_low_publication_source source
      JOIN product_spu spu
        ON spu.deleted = b'0'
       AND spu.biz_scene = 'PUBLICATION'
       AND spu.keyword = v_import_remark
       AND spu.name = source.title
     ORDER BY source.sort_no;

    INSERT INTO product_publication_spu_ext (
        spu_id, publisher_id, publication_type_id, issue_mode, issue_cycle, issn, cn_code, post_distribution_code,
        creator, create_time, updater, update_time, deleted, tenant_id
    )
    SELECT spu.id,
           publisher.id,
           type.id,
           'PERIODICAL',
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
      FROM tmp_primary_low_publication_source source
      JOIN product_spu spu
        ON spu.deleted = b'0'
       AND spu.biz_scene = 'PUBLICATION'
       AND spu.keyword = v_import_remark
       AND spu.name = source.title
      JOIN product_publisher publisher
        ON publisher.deleted = b'0'
       AND publisher.name = source.publisher_name
      JOIN product_publication_type type
        ON type.deleted = b'0'
       AND type.status = 0
       AND type.name = source.publication_type_name
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
      FROM tmp_primary_low_publication_source source
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
      FROM tmp_primary_low_publication_source source
      JOIN product_spu spu
        ON spu.deleted = b'0'
       AND spu.biz_scene = 'PUBLICATION'
       AND spu.keyword = v_import_remark
       AND spu.name = source.title
      JOIN product_sku sku
        ON sku.deleted = b'0'
       AND sku.spu_id = spu.id
      JOIN (
            SELECT 4 AS grade_catalog_id
            UNION ALL SELECT 5
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
      FROM tmp_primary_low_publication_source source
      JOIN tmp_primary_low_publication_tag tag ON tag.source_sort = source.sort_no
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

    INSERT INTO product_publication_sku_issue_template (
        sku_id, issue_no, issue_name, publish_offset_days, delivery_offset_days, sort, status, remark,
        creator, create_time, updater, update_time, deleted
    )
    SELECT sku.id,
           issue.issue_no,
           CONCAT('第', issue.issue_no, '期'),
           (issue.issue_no - 1) * source.interval_days,
           (issue.issue_no - 1) * source.interval_days,
           issue.issue_no,
           0,
           CONCAT(source.cycle_raw, '默认期次模板'),
           v_creator,
           NOW(),
           v_creator,
           NOW(),
           b'0'
      FROM tmp_primary_low_publication_source source
      JOIN product_spu spu
        ON spu.deleted = b'0'
       AND spu.biz_scene = 'PUBLICATION'
       AND spu.keyword = v_import_remark
       AND spu.name = source.title
      JOIN product_sku sku
        ON sku.deleted = b'0'
       AND sku.spu_id = spu.id
      JOIN tmp_primary_low_issue_no issue
        ON issue.issue_no <= source.issue_count
     ORDER BY source.sort_no, issue.issue_no;

    SELECT COUNT(*)
      INTO v_spu_count
      FROM product_spu spu
      JOIN tmp_primary_low_publication_source source ON source.title = spu.name
     WHERE spu.deleted = b'0'
       AND spu.biz_scene = 'PUBLICATION'
       AND spu.keyword = v_import_remark;

    SELECT COUNT(*)
      INTO v_sku_count
      FROM product_spu spu
      JOIN tmp_primary_low_publication_source source ON source.title = spu.name
      JOIN product_sku sku ON sku.spu_id = spu.id AND sku.deleted = b'0'
     WHERE spu.deleted = b'0'
       AND spu.biz_scene = 'PUBLICATION'
       AND spu.keyword = v_import_remark;

    SELECT COUNT(*)
      INTO v_spu_ext_count
      FROM product_spu spu
      JOIN tmp_primary_low_publication_source source ON source.title = spu.name
      JOIN product_publication_spu_ext ext ON ext.spu_id = spu.id AND ext.deleted = b'0'
     WHERE spu.deleted = b'0'
       AND spu.biz_scene = 'PUBLICATION'
       AND spu.keyword = v_import_remark;

    SELECT COUNT(*)
      INTO v_sku_ext_count
      FROM product_spu spu
      JOIN tmp_primary_low_publication_source source ON source.title = spu.name
      JOIN product_sku sku ON sku.spu_id = spu.id AND sku.deleted = b'0'
      JOIN product_publication_sku_ext ext ON ext.sku_id = sku.id AND ext.deleted = b'0'
     WHERE spu.deleted = b'0'
       AND spu.biz_scene = 'PUBLICATION'
       AND spu.keyword = v_import_remark;

    SELECT COUNT(*)
      INTO v_grade_rel_count
      FROM product_spu spu
      JOIN tmp_primary_low_publication_source source ON source.title = spu.name
      JOIN product_sku sku ON sku.spu_id = spu.id AND sku.deleted = b'0'
      JOIN product_publication_sku_grade_rel rel ON rel.sku_id = sku.id AND rel.deleted = b'0'
     WHERE spu.deleted = b'0'
       AND spu.biz_scene = 'PUBLICATION'
       AND spu.keyword = v_import_remark;

    SELECT COUNT(*)
      INTO v_category_rel_count
      FROM product_spu spu
      JOIN tmp_primary_low_publication_source source ON source.title = spu.name
      JOIN product_spu_category_rel rel ON rel.spu_id = spu.id AND rel.deleted = b'0'
     WHERE spu.deleted = b'0'
       AND spu.biz_scene = 'PUBLICATION'
       AND spu.keyword = v_import_remark;

    SELECT COUNT(*)
      INTO v_issue_template_count
      FROM product_spu spu
      JOIN tmp_primary_low_publication_source source ON source.title = spu.name
      JOIN product_sku sku ON sku.spu_id = spu.id AND sku.deleted = b'0'
      JOIN product_publication_sku_issue_template template ON template.sku_id = sku.id AND template.deleted = b'0'
     WHERE spu.deleted = b'0'
       AND spu.biz_scene = 'PUBLICATION'
       AND spu.keyword = v_import_remark;

    IF v_spu_count <> 40 OR v_sku_count <> 40 OR v_spu_ext_count <> 40 OR v_sku_ext_count <> 40
        OR v_grade_rel_count <> 80 OR v_category_rel_count <> 80 OR v_issue_template_count <> 341 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '导入失败：导入后数据计数不符合预期';
    END IF;

    COMMIT;

    SELECT v_spu_count AS imported_spu_count,
           v_sku_count AS imported_sku_count,
           v_spu_ext_count AS imported_spu_ext_count,
           v_sku_ext_count AS imported_sku_ext_count,
           v_grade_rel_count AS imported_grade_rel_count,
           v_category_rel_count AS imported_category_rel_count,
           v_issue_template_count AS imported_issue_template_count;
END$$

DELIMITER ;

CALL xiaokanhui_import_primary_low_publications_20260521();

DROP PROCEDURE IF EXISTS xiaokanhui_import_primary_low_publications_20260521;
