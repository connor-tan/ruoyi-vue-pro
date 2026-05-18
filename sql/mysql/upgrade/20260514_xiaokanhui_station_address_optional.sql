-- 校刊汇：站点地址改为非必填
-- MySQL 8 compatible, idempotent.

SET NAMES utf8mb4;

SET @station_address_required := (
    SELECT IF(COUNT(*) = 1, 1, 0)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'edu_station'
      AND COLUMN_NAME = 'station_address'
      AND IS_NULLABLE = 'NO'
);

SET @station_address_sql := IF(
    @station_address_required = 1,
    'ALTER TABLE `edu_station` MODIFY COLUMN `station_address` varchar(255) DEFAULT NULL COMMENT ''站点地址''',
    'SELECT ''edu_station.station_address already nullable'' AS message'
);

PREPARE stmt FROM @station_address_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
