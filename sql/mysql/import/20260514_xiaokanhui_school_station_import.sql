-- 校刊汇：学校与站点基础数据导入
-- 数据源：用户提供的学校/站点截图，共 115 行。
-- 核验来源优先使用 2025 年无锡市教育局学校名录；无法唯一确认的学校名称追加“（待确定）”。

SET NAMES utf8mb4;

DROP PROCEDURE IF EXISTS xiaokanhui_import_school_station_20260514;

DELIMITER $$

CREATE PROCEDURE xiaokanhui_import_school_station_20260514()
BEGIN
    DECLARE v_expected_source_count INT DEFAULT 115;
    DECLARE v_source_count INT DEFAULT 0;
    DECLARE v_conflict_count INT DEFAULT 0;
    DECLARE v_warehouse_id BIGINT DEFAULT 1;
    DECLARE v_target_schools INT DEFAULT 0;
    DECLARE v_created_schools INT DEFAULT 0;
    DECLARE v_updated_schools INT DEFAULT 0;
    DECLARE v_stations_bound INT DEFAULT 0;
    DECLARE v_stage_rows INT DEFAULT 0;
    DECLARE v_need_review_rows INT DEFAULT 0;
    DECLARE v_error_message VARCHAR(255);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    SET SESSION group_concat_max_len = 65535;

    DROP TEMPORARY TABLE IF EXISTS tmp_xkh_school_station_source;
    DROP TEMPORARY TABLE IF EXISTS tmp_xkh_station_expected;
    DROP TEMPORARY TABLE IF EXISTS tmp_xkh_station_resolved;
    DROP TEMPORARY TABLE IF EXISTS tmp_xkh_school_source_enriched;
    DROP TEMPORARY TABLE IF EXISTS tmp_xkh_school_target;
    DROP TEMPORARY TABLE IF EXISTS tmp_xkh_school_existing_match;
    DROP TEMPORARY TABLE IF EXISTS tmp_xkh_school_match_count;
    DROP TEMPORARY TABLE IF EXISTS tmp_xkh_school_resolved;
    DROP TEMPORARY TABLE IF EXISTS tmp_xkh_stage_target;
    DROP TEMPORARY TABLE IF EXISTS tmp_xkh_stage_removal;

    CREATE TEMPORARY TABLE tmp_xkh_school_station_source (
        row_no INT NOT NULL PRIMARY KEY,
        source_area VARCHAR(32) NOT NULL,
        raw_school_name VARCHAR(128) NOT NULL,
        stage_code VARCHAR(32) NOT NULL,
        station_name VARCHAR(64) NOT NULL,
        final_school_name VARCHAR(128) NOT NULL,
        school_address VARCHAR(255) NULL,
        source_url VARCHAR(512) NULL,
        confirmation_status VARCHAR(32) NOT NULL
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

    INSERT INTO tmp_xkh_school_station_source (row_no, source_area, raw_school_name, stage_code, station_name, final_school_name, school_address, source_url, confirmation_status) VALUES
        (1, '市局', '学前总部', 'primary', '发行部', '江苏省无锡师范学校附属小学（学前校区）（待确定）', '梁溪区教育路6号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'NEEDS_REVIEW'),
        (2, '市局', '新城小学', 'primary', '发行部', '江苏省无锡师范学校附属太湖新城小学', '无锡市滨湖区万兴路8号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (3, '市局', '阳光小学', 'primary', '发行部', '江苏省无锡师范学校附属小学（阳光校区）（待确定）', '梁溪区阳光城市花园C区17号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'NEEDS_REVIEW'),
        (4, '滨湖区', '峰影小学', 'primary', '河埒站', '无锡市峰影小学', '无锡市马山启帆路1号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (5, '滨湖区', '梅园小学', 'primary', '河埒站', '无锡市梅园小学（待确定）', '无锡市梅园徐巷450号', NULL, 'NEEDS_REVIEW'),
        (6, '滨湖区', '河埒中心小学', 'primary', '河埒站', '无锡市河埒中心小学', '无锡市滨湖区稻香路801号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (7, '滨湖区', '稻香小学', 'primary', '河埒站', '无锡市稻香实验小学', '无锡市稻香新村5号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (8, '滨湖区', '立人小学', 'primary', '河埒站', '无锡市立人小学', '无锡市胡埭环镇南路2号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (9, '滨湖区', '育红小学', 'primary', '河埒站', '无锡市育红小学', '无锡市梁溪路788号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (10, '滨湖区', '育英实验小学（华晶校区）', 'primary', '河埒站', '无锡市育英胜利小学（含华晶）', '无锡市惠河路170号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (11, '滨湖区', '育英小学胜利校区', 'primary', '河埒站', '无锡市育英胜利小学（含华晶）', '无锡市惠河路170号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (12, '滨湖区', '育英文旅实验小学', 'primary', '河埒站', '无锡市育英文旅实验小学', '无锡市蠡湖大道2108-384号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (13, '滨湖区', '育英锦园小学', 'primary', '河埒站', '无锡市育英锦园实验小学', '无锡市渔景路2号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (14, '滨湖区', '滨湖区胡埭小学', 'primary', '河埒站', '无锡市胡埭中心小学', '无锡市胡埭振胡路48号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (15, '滨湖区', '雪浪中心小学', 'primary', '河埒站', '无锡市雪浪中心小学', '无锡市雪浪街道育才路1号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (16, '滨湖区', '滨湖实验幼儿园', 'kindergarten', '河埒站', '无锡市滨湖实验幼儿园', '无锡市滨湖区青祁路26号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (17, '滨湖区', '滨湖实验幼儿园万达园区', 'kindergarten', '河埒站', '无锡市滨湖实验幼儿园万达分园', '无锡市滨湖区万达广场D区26号', 'https://www.wxbh.gov.cn/doc/2023/05/26/4116867.shtml', 'CONFIRMED'),
        (18, '滨湖区', '滨湖实验幼儿园誉品园区', 'kindergarten', '河埒站', '无锡市滨湖实验幼儿园誉品分园', '无锡市滨湖区河埒口誉品华府7号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (19, '梁溪区', '侨谊中学', 'middle', '清扬站', '无锡市侨谊实验中学', '无锡市梁溪区南下塘213号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (20, '梁溪区', '侨谊古运河中学', 'middle', '清扬站', '无锡市侨谊古运河中学', '无锡市梁溪区南长街730号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (21, '梁溪区', '侨谊明德中学', 'middle', '清扬站', '无锡市梁溪区明德实验学校', '无锡市梁溪区动力路168号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (22, '梁溪区', '凤翔中学', 'middle', '清扬站', '无锡市凤翔实验学校', '无锡市梁溪区民丰路188号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (23, '梁溪区', '刘潭中学', 'middle', '清扬站', '无锡市刘潭实验学校', '无锡市梁溪区石澄路139号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (24, '梁溪区', '南长实验中学', 'middle', '清扬站', '无锡市南长实验中学', '无锡市梁溪区水沟头45号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (25, '梁溪区', '塔影中学', 'middle', '清扬站', '无锡市塔影中学', '无锡市梁溪区上马墩169号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (26, '梁溪区', '东林古运河小学', 'primary', '清扬站', '无锡市古运河实验小学', '无锡市梁溪区南长街716号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (27, '梁溪区', '东林小学', 'primary', '清扬站', '无锡市东林小学', '无锡市梁溪区解放东路818号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (28, '梁溪区', '东林惠畅实验学校', 'primary', '清扬站', '无锡市梁溪区东林惠畅实验学校（小学部）', '无锡市梁溪区金山三支路30号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (29, '梁溪区', '五河小学', 'primary', '清扬站', '无锡市五河新村小学', '无锡市梁溪区五河新村311号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (30, '梁溪区', '五爱小学', 'primary', '清扬站', '无锡市五爱小学', '无锡市梁溪区人民西路37号（人民路校区）；无锡市梁溪区蓉湖南路202号（蓉湖校区）', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (31, '梁溪区', '亭子桥小学', 'primary', '清扬站', '无锡市亭子桥中心小学', '无锡市梁溪区东门唐巷73号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (32, '梁溪区', '兰亭小学', 'primary', '清扬站', '江苏省无锡兰亭小学', '无锡市梁溪区广源路199号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (33, '梁溪区', '凤翔小学', 'primary', '清扬站', '无锡市凤翔实验学校', '无锡市梁溪区民丰路188号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (34, '梁溪区', '刘潭二村小学', 'primary', '清扬站', '无锡市刘潭实验小学', '无锡市梁溪区刘潭二村100号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (35, '梁溪区', '刘潭实验学校小学部', 'primary', '清扬站', '无锡市刘潭实验学校', '无锡市梁溪区石澄路139号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (36, '梁溪区', '南湖小学', 'primary', '清扬站', '无锡市南湖小学', '无锡市梁溪区德才路5号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (37, '梁溪区', '南长街小学', 'primary', '清扬站', '无锡市南长街小学', '无锡市梁溪区扬名路12号；无锡市梁溪区塘南新村88号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (38, '梁溪区', '刘潭小学', 'primary', '清扬站', '无锡市刘潭实验小学', '无锡市梁溪区刘潭二村100号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (39, '梁溪区', '吴桥实验小学', 'primary', '清扬站', '无锡市吴桥实验小学', '无锡市梁溪区五河支路五河苑2号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (40, '梁溪区', '塔影中心小学', 'primary', '清扬站', '无锡市塔影中心小学', '无锡市梁溪区塔影二村65号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (41, '梁溪区', '夹城里中心小学', 'primary', '清扬站', '无锡市夹城里中心小学', '无锡市清扬新村196号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (42, '梁溪区', '山北中心小学', 'primary', '清扬站', '无锡市山北中心小学', '无锡市梁溪区惠龙新村258号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (43, '梁溪区', '广勤中学', 'middle', '城中站', '无锡市广勤中学', '无锡市梁溪区周山浜俞巷96号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (44, '梁溪区', '江南中学（通扬校区）', 'middle', '城中站', '无锡市江南中学（通扬校区）（待确定）', '梁溪区通扬路15号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'NEEDS_REVIEW'),
        (45, '梁溪区', '江南中学（阳光校区）', 'middle', '城中站', '无锡市江南中学（阳光校区）（待确定）', '梁溪区阳光直街18号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'NEEDS_REVIEW'),
        (46, '梁溪区', '清名桥中学', 'middle', '城中站', '无锡市清名桥中学', '无锡市梁溪区界泾桥弄173-1', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (47, '梁溪区', '积余中学', 'middle', '城中站', '无锡市积余实验学校', '无锡市梁溪区春申路164号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (48, '梁溪区', '崇宁路小学', 'primary', '城中站', '江苏省无锡崇宁路实验小学', '无锡市梁溪区崇宁路4号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (49, '梁溪区', '广瑞小学', 'primary', '城中站', '无锡市广瑞实验小学', '无锡市梁溪区广瑞三村62号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (50, '梁溪区', '广益中心小学', 'primary', '城中站', '无锡市广益中心小学', '无锡市梁溪区广益街道广益路186号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (51, '梁溪区', '惠山小学', 'primary', '城中站', '江苏省无锡惠山小学', '无锡市梁溪区盛岸二村113号（盛岸校区）', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (52, '梁溪区', '扬名中心小学', 'primary', '城中站', '无锡市扬名中心小学', '无锡市梁溪区曹张新村曹婆桥124号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (53, '梁溪区', '新开河小学', 'primary', '城中站', '无锡市新开河小学', '无锡市梁溪区毛湾家园1号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (54, '梁溪区', '明德实验学校（小学部）', 'primary', '城中站', '无锡市梁溪区明德实验学校（小学部）', '无锡市梁溪区动力路168号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (55, '梁溪区', '梁庄实验小学', 'primary', '城中站', '无锡市梁庄实验小学（待确定）', '', NULL, 'NEEDS_REVIEW'),
        (56, '梁溪区', '沁园实验小学', 'primary', '城中站', '江苏省无锡沁园实验小学', '无锡市梁溪区沁园新村58号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (57, '梁溪区', '沁园实验小学（五星校区）', 'primary', '城中站', '无锡市梁溪区沁园实验小学五星分校', '无锡市梁溪区五星家园329号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (58, '梁溪区', '滨河实验小学', 'primary', '城中站', '无锡市滨河实验小学', '无锡市梁溪区芦中路299号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (59, '梁溪区', '积余小学', 'primary', '城中站', '无锡市积余实验学校', '无锡市梁溪区春申路164号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (60, '梁溪区', '积余小学运河分校', 'primary', '城中站', '无锡市梁溪区积余实验学校运河分校', '无锡市梁溪区通惠中路133号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (61, '梁溪区', '芦庄二小', 'primary', '城中站', '无锡市芦庄第二小学', '无锡市梁溪区扬名街道芦庄五区64-2', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (62, '梁溪区', '芦庄实验小学', 'primary', '城中站', '无锡市芦庄实验小学', '无锡市梁溪区芦庄一区126-1（芦庄校区）；无锡市梁溪区芦庄六区100号（英才校区）', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (63, '梁溪区', '花园实验小学', 'primary', '城中站', '无锡市花园实验小学', '无锡市梁溪区通扬南路135号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (64, '梁溪区', '连元街锡山小学', 'primary', '城中站', '无锡市连元街小学锡山分校（待确定）', '', NULL, 'NEEDS_REVIEW'),
        (65, '梁溪区', '连元街小学', 'primary', '城中站', '无锡市连元街小学', '无锡市梁溪区连元街28号；无锡市梁溪区保利广场12号（保利校区）；无锡市梁溪区新开河8-1号（新开河校区）', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (66, '梁溪区', '连元街小学惠芬分校', 'primary', '城中站', '无锡市连元街小学惠芬分校（待确定）', '', NULL, 'NEEDS_REVIEW'),
        (67, '梁溪区', '通德桥实验小学', 'primary', '城中站', '江苏省无锡通德桥实验小学', '无锡市梁溪区五爱路55号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (68, '梁溪区', '通江实验小学', 'primary', '城中站', '江苏省无锡通江实验小学', '无锡市梁溪区庆丰路18号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (69, '梁溪区', '崇海小学', 'primary', '城中站', '无锡市崇海小学（待确定）', '', NULL, 'NEEDS_REVIEW'),
        (70, '新吴区', '丽景中学', 'middle', '新吴站', '无锡市新吴区丽景实验学校（初中部）', '无锡市新吴区硕放街道新农路12号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (71, '新吴区', '太科城中学', 'middle', '新吴站', '无锡高新区（新吴区）太科城实验学校（初中部）', '无锡市新吴区净慧西道9号（暂借）', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (72, '新吴区', '文博中学', 'middle', '新吴站', '无锡市新吴区文博实验中学', '无锡市新吴区坊泰路（泰山路实验小学东侧）', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (73, '新吴区', '新吴实验中学', 'middle', '新吴站', '无锡市新吴实验中学', '无锡市新吴区江学路6号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (74, '新吴区', '新安中学', 'middle', '新吴站', '无锡市新安中学', '无锡新吴区新安街道宁乐路1号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (75, '新吴区', '硕放中学', 'middle', '新吴站', '无锡市硕放中学', '无锡市新吴区硕放街道通祥路111号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (76, '新吴区', '金鸿中学', 'middle', '新吴站', '无锡市新吴区金鸿实验学校（初中部）', '无锡市新吴区鸿山街道中庆路80号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (77, '新吴区', '旺庄实验学校', 'primary', '新吴站', '无锡市新吴区第一实验学校（待确定）', '无锡市旺庄西路18号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'NEEDS_REVIEW'),
        (78, '新吴区', '丽景实验学校', 'primary', '新吴站', '无锡市新吴区丽景实验学校（小学部）', '无锡市新吴区硕放街道新农路12号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (79, '新吴区', '南丰小学', 'primary', '新吴站', '无锡市新吴区南丰小学', '无锡市新吴区缇香路100号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (80, '新吴区', '南星小学', 'primary', '新吴站', '无锡市新吴区南星小学', '无锡市新吴区硕放街道南星苑二路123号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (81, '新吴区', '后宅中心小学', 'primary', '新吴站', '无锡市后宅中心小学', '无锡市新吴区鸿山街道鸿山路201号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (82, '新吴区', '坊前实验小学（一校区）', 'primary', '新吴站', '无锡市新吴区坊前实验小学（一校区）（待确定）', '无锡市新吴区江溪街道坊明路10号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'NEEDS_REVIEW'),
        (83, '新吴区', '坊前实验小学（二校区）', 'primary', '新吴站', '无锡市新吴区坊前实验小学（二校区）（待确定）', '无锡市新吴区江溪街道坊明路10号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'NEEDS_REVIEW'),
        (84, '新吴区', '太科城小学', 'primary', '新吴站', '无锡高新区（新吴区）太科城实验学校(小学部）', '无锡市新吴区净慧西道9号（暂借）', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (85, '新吴区', '新吴实验小学', 'primary', '新吴站', '无锡市新吴实验小学', '无锡市新吴区清源路49号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (86, '新吴区', '新洲小学', 'primary', '新吴站', '无锡市新洲小学', '无锡市新吴区旺庄街道联心嘉园151号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (87, '新吴区', '新苑小学', 'primary', '新吴站', '无锡市新吴区新苑实验小学（东校区）（待确定）', '新吴区江溪街道长江北路88号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'NEEDS_REVIEW'),
        (88, '新吴区', '新苑小学（西校区）', 'primary', '新吴站', '无锡市新吴区新苑实验小学（西校区）（待确定）', '新吴区江溪街道太湖花园一期87号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'NEEDS_REVIEW'),
        (89, '新吴区', '旺庄实验小学', 'primary', '新吴站', '无锡市新吴区旺庄实验小学', '无锡市新吴区旺庄街道湘江北路8号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (90, '新吴区', '春城实验小学（一校区）', 'primary', '新吴站', '无锡市春城实验小学（一校区）（待确定）', '无锡市新吴区金城东路77号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'NEEDS_REVIEW'),
        (91, '新吴区', '春城实验小学（二校区）', 'primary', '新吴站', '无锡市春城实验小学（二校区）（待确定）', '无锡市新吴区金城东路77号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'NEEDS_REVIEW'),
        (92, '新吴区', '春星小学', 'primary', '新吴站', '无锡市新吴区春星小学', '无锡市新吴区旺庄街道旺庄东路8号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (93, '新吴区', '梅村实验小学（一校区）', 'primary', '新吴站', '无锡市梅村实验小学（一校区）（待确定）', '无锡新吴区梅村街道梅育路58号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'NEEDS_REVIEW'),
        (94, '新吴区', '梅村实验小学（二校区）', 'primary', '新吴站', '无锡市梅村实验小学（二校区）（待确定）', '无锡新吴区梅村街道梅育路58号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'NEEDS_REVIEW'),
        (95, '新吴区', '梅里实验小学', 'primary', '新吴站', '无锡市新吴区梅里实验小学', '无锡市新吴区梅村街道锡贤路161号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (96, '新吴区', '江溪小学', 'primary', '新吴站', '无锡市新吴区江溪小学', '无锡市新吴区叙康里300号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (97, '新吴区', '泰伯实验学校', 'primary', '新吴站', '无锡市新吴区泰伯实验小学', '无锡市新吴区鸿山街道鸿山路659号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (98, '新吴区', '泰山路实验小学', 'primary', '新吴站', '无锡市新吴区泰山路实验小学', '无锡市新吴区泰山路189号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (99, '新吴区', '硕放小学', 'primary', '新吴站', '无锡市新吴区硕放实验小学', '无锡市新吴区硕放街道通祥北路8号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (100, '新吴区', '金鸿小学', 'primary', '新吴站', '无锡市新吴区金鸿实验学校(小学部）', '无锡市新吴区鸿山街道中庆路80号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (101, '新吴区', '锡梅小学', 'primary', '新吴站', '无锡市新吴区锡梅实验小学', '无锡新吴区梅村街道锡义路521号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (102, '新吴区', '高浪小学', 'primary', '新吴站', '无锡市新吴区高浪小学', '无锡市新吴区旺庄街道高浪嘉园1-2号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (103, '新吴区', '鸿山实验小学', 'primary', '新吴站', '无锡市新吴区鸿山实验小学', '无锡市新吴区鸿山街道锡协路300号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (104, '新吴区', '扬名实验中学', 'middle', '新城站', '无锡市梁溪区扬名实验学校', '无锡市梁溪区梁东路188号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (105, '新吴区', '江南中学（新城校区）', 'middle', '新城站', '无锡市江南新城实验中学', '无锡市西凤道7号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (106, '新吴区', '东绛实验小学部', 'primary', '新城站', '无锡市东绛实验学校（小学部）', '无锡经开区太湖街道盛园路1号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (107, '新吴区', '华庄中心小学', 'primary', '新城站', '无锡市华庄中心小学', '无锡经开区华庄街道育才路78号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (108, '新吴区', '太湖实验小学', 'primary', '新城站', '无锡市太湖实验小学', '无锡经开区华庄街道水乡南苑60号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (109, '新吴区', '尚贤万科小学', 'primary', '新城站', '无锡市尚贤万科小学', '无锡市经开区万科城市花园二期163号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (110, '新吴区', '尚贤融创校区', 'primary', '新城站', '无锡市尚贤融创小学', '无锡经开区观山路100号天鹅湖花园B区118号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (111, '新吴区', '扬名实验学校（侨谊校区）', 'primary', '新城站', '无锡市扬名实验学校（侨谊校区）（待确定）', '', NULL, 'NEEDS_REVIEW'),
        (112, '新吴区', '无锡市融成观顺实验小学', 'primary', '新城站', '无锡市融成观顺实验小学', '无锡经开区观顺道366号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (113, '新吴区', '江南实验小学', 'primary', '新城站', '无锡市江南实验小学', '无锡经开区华庄街道落霞苑社区', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'CONFIRMED'),
        (114, '新吴区', '育红山水校区（石塘）', 'primary', '新城站', '无锡市育红山水小学（石塘校区）（待确定）', '无锡市滨湖区大通路632号', 'https://jy.wuxi.gov.cn/doc/2025/08/14/4629204.shtml', 'NEEDS_REVIEW'),
        (115, '新吴区', '朱庆路幼儿园', 'kindergarten', '新城站', '朱庆路幼儿园（待确定）', '', NULL, 'NEEDS_REVIEW');

    CREATE TEMPORARY TABLE tmp_xkh_station_expected (
        station_name VARCHAR(64) NOT NULL PRIMARY KEY,
        area_id BIGINT NOT NULL,
        sort INT NOT NULL
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

    INSERT INTO tmp_xkh_station_expected (station_name, area_id, sort) VALUES
        ('发行部', 320200, 10),
        ('河埒站', 320211, 20),
        ('清扬站', 320211, 30),
        ('城中站', 320213, 40),
        ('新吴站', 320214, 50),
        ('新城站', 320214, 60);

    SELECT COUNT(*) INTO v_source_count FROM tmp_xkh_school_station_source;
    IF v_source_count <> v_expected_source_count THEN
        SET v_error_message = CONCAT('学校站点源数据行数异常，expected=', v_expected_source_count, ', actual=', v_source_count);
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = v_error_message;
    END IF;

    START TRANSACTION;

    SELECT COUNT(*) INTO v_conflict_count
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'edu_station'
      AND COLUMN_NAME = 'station_address'
      AND IS_NULLABLE = 'YES';
    IF v_conflict_count <> 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '请先执行 20260514_xiaokanhui_station_address_optional.sql';
    END IF;

    SELECT COUNT(*) INTO v_conflict_count
    FROM repo_warehouse
    WHERE deleted = b'0'
      AND status = 0;
    IF v_conflict_count <> 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '启用仓库不是唯一，学校默认仓库无法确定';
    END IF;

    SELECT COUNT(*) INTO v_conflict_count
    FROM repo_warehouse
    WHERE id = 1
      AND name = '无锡仓'
      AND deleted = b'0'
      AND status = 0;
    IF v_conflict_count <> 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '默认仓库无锡仓(id=1)不存在或未启用';
    END IF;

    SELECT COUNT(*) INTO v_conflict_count
    FROM (
        SELECT s.station_name
        FROM edu_station s
        JOIN tmp_xkh_station_expected e ON e.station_name = s.station_name COLLATE utf8mb4_unicode_ci
        WHERE s.deleted = b'0'
        GROUP BY s.station_name
        HAVING COUNT(*) > 1
    ) duplicated_station;
    IF v_conflict_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '存在同名站点多条记录，导入中止';
    END IF;

    INSERT INTO edu_station (
        station_name, area_id, contact_name, contact_mobile, station_address,
        sort, status, remark, creator, create_time, updater, update_time, deleted
    )
    SELECT e.station_name, e.area_id, NULL, NULL, NULL,
           e.sort, 0, '学校站点导入自动创建', 'system', NOW(), 'system', NOW(), b'0'
    FROM tmp_xkh_station_expected e
    LEFT JOIN edu_station s ON s.deleted = b'0'
        AND s.station_name COLLATE utf8mb4_unicode_ci = e.station_name
    WHERE s.id IS NULL;

    CREATE TEMPORARY TABLE tmp_xkh_station_resolved AS
    SELECT e.station_name, MIN(s.id) AS station_id, COUNT(*) AS match_count
    FROM tmp_xkh_station_expected e
    JOIN edu_station s ON s.deleted = b'0'
        AND s.station_name COLLATE utf8mb4_unicode_ci = e.station_name
    GROUP BY e.station_name;

    SELECT COUNT(*) INTO v_conflict_count
    FROM tmp_xkh_station_resolved
    WHERE match_count <> 1;
    IF v_conflict_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '站点匹配失败，导入中止';
    END IF;

    CREATE TEMPORARY TABLE tmp_xkh_school_source_enriched AS
    SELECT src.*,
           CASE src.source_area
               WHEN '市局' THEN 320200
               WHEN '滨湖区' THEN 320211
               WHEN '梁溪区' THEN 320213
               WHEN '新吴区' THEN 320214
           END AS area_id,
           st.station_id
    FROM tmp_xkh_school_station_source src
    LEFT JOIN tmp_xkh_station_resolved st ON st.station_name = src.station_name;

    SELECT COUNT(*) INTO v_conflict_count
    FROM tmp_xkh_school_source_enriched
    WHERE area_id IS NULL OR station_id IS NULL
       OR stage_code NOT IN ('kindergarten', 'primary', 'middle');
    IF v_conflict_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '源数据存在无法映射的区域、学段或站点';
    END IF;

    SELECT COUNT(*) INTO v_conflict_count
    FROM (
        SELECT area_id, final_school_name
        FROM tmp_xkh_school_source_enriched
        GROUP BY area_id, final_school_name
        HAVING COUNT(DISTINCT station_id) > 1
    ) station_conflict;
    IF v_conflict_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '同一学校目标被分配到多个站点，导入中止';
    END IF;

    CREATE TEMPORARY TABLE tmp_xkh_school_target AS
    SELECT area_id,
           final_school_name,
           MIN(station_id) AS station_id,
           NULLIF(SUBSTRING_INDEX(GROUP_CONCAT(NULLIF(school_address, '') ORDER BY row_no SEPARATOR '|||'), '|||', 1), '') AS school_address,
           GROUP_CONCAT(raw_school_name ORDER BY row_no SEPARATOR '、') AS raw_school_names,
           IF(SUM(confirmation_status = 'NEEDS_REVIEW') > 0, 'NEEDS_REVIEW', 'CONFIRMED') AS confirmation_status,
           CAST(NULL AS SIGNED) AS existing_school_id
    FROM tmp_xkh_school_source_enriched
    GROUP BY area_id, final_school_name;

    CREATE TEMPORARY TABLE tmp_xkh_school_existing_match AS
    SELECT t.area_id, t.final_school_name, s.id AS school_id
    FROM tmp_xkh_school_target t
    JOIN tmp_xkh_school_source_enriched e ON e.area_id = t.area_id
        AND e.final_school_name = t.final_school_name
    JOIN edu_school s ON s.deleted = b'0'
        AND s.area_id = t.area_id
        AND s.school_name IN (e.raw_school_name, e.final_school_name)
    GROUP BY t.area_id, t.final_school_name, s.id;

    CREATE TEMPORARY TABLE tmp_xkh_school_match_count AS
    SELECT area_id, final_school_name, COUNT(*) AS match_count, MIN(school_id) AS school_id
    FROM tmp_xkh_school_existing_match
    GROUP BY area_id, final_school_name;

    SELECT COUNT(*) INTO v_conflict_count
    FROM tmp_xkh_school_match_count
    WHERE match_count > 1;
    IF v_conflict_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '同一区域同名学校匹配到多条记录，导入中止';
    END IF;

    UPDATE tmp_xkh_school_target t
    JOIN tmp_xkh_school_match_count m ON m.area_id = t.area_id
        AND m.final_school_name = t.final_school_name
        AND m.match_count = 1
    SET t.existing_school_id = m.school_id;

    INSERT INTO edu_school (
        school_name, area_id, school_address, station_id, warehouse_id, code,
        creator, create_time, updater, update_time, deleted
    )
    SELECT t.final_school_name, t.area_id, COALESCE(t.school_address, ''), t.station_id, v_warehouse_id, NULL,
           'system', NOW(), 'system', NOW(), b'0'
    FROM tmp_xkh_school_target t
    WHERE t.existing_school_id IS NULL;

    UPDATE edu_school s
    JOIN tmp_xkh_school_target t ON t.existing_school_id = s.id
    SET s.school_name = t.final_school_name,
        s.station_id = t.station_id,
        s.warehouse_id = v_warehouse_id,
        s.school_address = CASE
            WHEN t.school_address IS NOT NULL AND t.school_address <> '' THEN t.school_address
            ELSE s.school_address
        END,
        s.updater = 'system',
        s.update_time = NOW();

    CREATE TEMPORARY TABLE tmp_xkh_school_resolved AS
    SELECT t.*, s.id AS school_id
    FROM tmp_xkh_school_target t
    JOIN edu_school s ON s.deleted = b'0'
        AND s.area_id = t.area_id
        AND s.school_name = t.final_school_name;

    SELECT COUNT(*) INTO v_conflict_count
    FROM (
        SELECT area_id, final_school_name
        FROM tmp_xkh_school_resolved
        GROUP BY area_id, final_school_name
        HAVING COUNT(*) > 1
    ) duplicated_resolved_school;
    IF v_conflict_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '导入后学校解析出现重复记录，导入中止';
    END IF;

    CREATE TEMPORARY TABLE tmp_xkh_stage_target AS
    SELECT DISTINCT r.school_id, e.stage_code AS stage
    FROM tmp_xkh_school_source_enriched e
    JOIN tmp_xkh_school_resolved r ON r.area_id = e.area_id
        AND r.final_school_name = e.final_school_name;

    CREATE TEMPORARY TABLE tmp_xkh_stage_removal AS
    SELECT ss.school_id, ss.stage
    FROM edu_school_stage ss
    JOIN tmp_xkh_school_resolved r ON r.school_id = ss.school_id
    LEFT JOIN tmp_xkh_stage_target st ON st.school_id = ss.school_id
        AND st.stage = ss.stage COLLATE utf8mb4_unicode_ci
    WHERE ss.deleted = b'0'
      AND st.school_id IS NULL;

    SELECT COUNT(*) INTO v_conflict_count
    FROM tmp_xkh_stage_removal rem
    LEFT JOIN edu_grade_catalog gc ON gc.stage COLLATE utf8mb4_unicode_ci = rem.stage
        AND gc.deleted = b'0'
    LEFT JOIN edu_school_grade sg ON sg.school_id = rem.school_id
        AND sg.grade_catalog_id = gc.id
        AND sg.deleted = b'0'
    LEFT JOIN edu_school_class sc ON sc.school_id = rem.school_id
        AND sc.school_grade_id = sg.id
        AND sc.deleted = b'0'
    LEFT JOIN edu_student_class stc ON stc.class_id = sc.id
        AND stc.deleted = b'0'
    LEFT JOIN edu_student stu ON stu.current_school_id = rem.school_id
        AND stu.deleted = b'0'
    WHERE sg.id IS NOT NULL
       OR sc.id IS NOT NULL
       OR stc.id IS NOT NULL
       OR stu.id IS NOT NULL;
    IF v_conflict_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '学段移除会影响已有年级、班级或学生链路，导入中止';
    END IF;

    UPDATE edu_school_stage ss
    JOIN tmp_xkh_stage_removal rem ON rem.school_id = ss.school_id
        AND rem.stage = ss.stage COLLATE utf8mb4_unicode_ci
    SET ss.deleted = b'1',
        ss.updater = 'system',
        ss.update_time = NOW();

    INSERT INTO edu_school_stage (
        school_id, stage, creator, create_time, updater, update_time, deleted
    )
    SELECT st.school_id, st.stage, 'system', NOW(), 'system', NOW(), b'0'
    FROM tmp_xkh_stage_target st
    ON DUPLICATE KEY UPDATE
        deleted = b'0',
        updater = 'system',
        update_time = NOW();

    SELECT COUNT(*) INTO v_target_schools FROM tmp_xkh_school_target;
    SELECT COUNT(*) INTO v_created_schools FROM tmp_xkh_school_target WHERE existing_school_id IS NULL;
    SELECT COUNT(*) INTO v_updated_schools FROM tmp_xkh_school_target WHERE existing_school_id IS NOT NULL;
    SELECT COUNT(DISTINCT station_id) INTO v_stations_bound FROM tmp_xkh_school_target;
    SELECT COUNT(*) INTO v_stage_rows FROM tmp_xkh_stage_target;
    SELECT COUNT(*) INTO v_need_review_rows FROM tmp_xkh_school_station_source WHERE confirmation_status = 'NEEDS_REVIEW';

    COMMIT;

    SELECT 'source_rows' AS metric, v_source_count AS value
    UNION ALL
    SELECT 'target_schools', v_target_schools
    UNION ALL
    SELECT 'created_schools', v_created_schools
    UNION ALL
    SELECT 'updated_schools', v_updated_schools
    UNION ALL
    SELECT 'stations_bound', v_stations_bound
    UNION ALL
    SELECT 'stage_rows', v_stage_rows
    UNION ALL
    SELECT 'need_review_rows', v_need_review_rows;

    SELECT e.stage_code AS stage, COUNT(DISTINCT r.school_id) AS school_count
    FROM tmp_xkh_school_source_enriched e
    JOIN tmp_xkh_school_resolved r ON r.area_id = e.area_id
        AND r.final_school_name = e.final_school_name
    GROUP BY e.stage_code
    ORDER BY FIELD(e.stage_code, 'kindergarten', 'primary', 'middle');

    SELECT row_no, source_area, raw_school_name, final_school_name, school_address, source_url
    FROM tmp_xkh_school_station_source
    WHERE confirmation_status = 'NEEDS_REVIEW'
    ORDER BY row_no;
END $$

DELIMITER ;

CALL xiaokanhui_import_school_station_20260514();

DROP PROCEDURE IF EXISTS xiaokanhui_import_school_station_20260514;
