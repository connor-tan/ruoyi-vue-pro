-- xiaokanhui EDU 逻辑删除唯一键占用清理
-- 当前项目处于开发期，不保留历史兼容；这些表删除后允许重建同一业务键。

SELECT 'before' AS phase, 'edu_student_class' AS table_name, COUNT(*) AS deleted_count FROM edu_student_class WHERE deleted = b'1'
UNION ALL SELECT 'before', 'edu_school_class', COUNT(*) FROM edu_school_class WHERE deleted = b'1'
UNION ALL SELECT 'before', 'edu_school_year', COUNT(*) FROM edu_school_year WHERE deleted = b'1'
UNION ALL SELECT 'before', 'edu_school_grade', COUNT(*) FROM edu_school_grade WHERE deleted = b'1'
UNION ALL SELECT 'before', 'edu_station', COUNT(*) FROM edu_station WHERE deleted = b'1'
UNION ALL SELECT 'before', 'edu_year_catalog', COUNT(*) FROM edu_year_catalog WHERE deleted = b'1'
UNION ALL SELECT 'before', 'product_publisher', COUNT(*) FROM product_publisher WHERE deleted = b'1'
UNION ALL SELECT 'before', 'product_publication_type', COUNT(*) FROM product_publication_type WHERE deleted = b'1';

DELETE FROM edu_student_class WHERE deleted = b'1';
DELETE FROM edu_school_class WHERE deleted = b'1';
DELETE FROM edu_school_year WHERE deleted = b'1';
DELETE FROM edu_school_grade WHERE deleted = b'1';
DELETE FROM edu_station WHERE deleted = b'1';
DELETE FROM edu_year_catalog WHERE deleted = b'1';
DELETE FROM product_publisher WHERE deleted = b'1';
DELETE FROM product_publication_type WHERE deleted = b'1';

SELECT 'after' AS phase, 'edu_student_class' AS table_name, COUNT(*) AS deleted_count FROM edu_student_class WHERE deleted = b'1'
UNION ALL SELECT 'after', 'edu_school_class', COUNT(*) FROM edu_school_class WHERE deleted = b'1'
UNION ALL SELECT 'after', 'edu_school_year', COUNT(*) FROM edu_school_year WHERE deleted = b'1'
UNION ALL SELECT 'after', 'edu_school_grade', COUNT(*) FROM edu_school_grade WHERE deleted = b'1'
UNION ALL SELECT 'after', 'edu_station', COUNT(*) FROM edu_station WHERE deleted = b'1'
UNION ALL SELECT 'after', 'edu_year_catalog', COUNT(*) FROM edu_year_catalog WHERE deleted = b'1'
UNION ALL SELECT 'after', 'product_publisher', COUNT(*) FROM product_publisher WHERE deleted = b'1'
UNION ALL SELECT 'after', 'product_publication_type', COUNT(*) FROM product_publication_type WHERE deleted = b'1';
