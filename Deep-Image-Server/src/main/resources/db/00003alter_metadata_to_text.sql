-- ============================================
-- 修改 metadata 字段类型从 JSONB 改为 TEXT
-- 原因: 简化类型处理，避免 MyBatis Plus 与 PostgreSQL JSONB 类型转换问题
-- ============================================

-- 修改字段类型
ALTER TABLE di_file_records 
    ALTER COLUMN metadata TYPE TEXT;

-- 更新字段注释
COMMENT ON COLUMN di_file_records.metadata IS '扩展元数据(JSON格式),可存储图片宽高、视频时长等信息,存储为TEXT类型';

