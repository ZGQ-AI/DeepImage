-- ============================================
-- 文件管理模块数据库设计
-- ============================================

-- 文件记录主表
CREATE TABLE di_file_records (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    bucket_name VARCHAR(100) NOT NULL,
    object_name VARCHAR(500) UNIQUE NOT NULL,
    etag VARCHAR(100),
    original_filename VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100),
    file_extension VARCHAR(20),
    business_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'completed',
    visibility VARCHAR(20) NOT NULL DEFAULT 'private',
    file_url VARCHAR(1000) NOT NULL,
    thumbnail_url VARCHAR(1000),
    file_hash VARCHAR(64) NOT NULL,
    metadata JSONB,
    reference_count INT DEFAULT 0,
    delete_flag SMALLINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 表注释
COMMENT ON TABLE di_file_records IS '文件记录表,存储上传到MinIO的文件元数据信息';

-- 字段注释
COMMENT ON COLUMN di_file_records.id IS '文件记录唯一标识,主键';
COMMENT ON COLUMN di_file_records.user_id IS '文件所属用户ID,引用sys_users表,用于用户隔离';
COMMENT ON COLUMN di_file_records.bucket_name IS 'MinIO存储桶名称';
COMMENT ON COLUMN di_file_records.object_name IS 'MinIO对象名称(完整存储路径),全局唯一,格式: {user_id}/{business_type}/{date}/{uuid}.ext, 如: 1001/avatar/20251002/abc.jpg';
COMMENT ON COLUMN di_file_records.etag IS 'MinIO ETag,用于版本控制和文件校验';
COMMENT ON COLUMN di_file_records.original_filename IS '原始文件名,用户上传时的文件名';
COMMENT ON COLUMN di_file_records.file_size IS '文件大小(字节)';
COMMENT ON COLUMN di_file_records.content_type IS '文件MIME类型,如: image/jpeg, application/pdf';
COMMENT ON COLUMN di_file_records.file_extension IS '文件扩展名,如: jpg, pdf, png';
COMMENT ON COLUMN di_file_records.business_type IS '业务类型: avatar(头像), document(文档), image(图片), video(视频), temp(临时文件)等';
COMMENT ON COLUMN di_file_records.status IS '文件状态枚举: UPLOADING(上传中), COMPLETED(已完成), FAILED(失败), DELETED(已删除)';
COMMENT ON COLUMN di_file_records.visibility IS '访问权限枚举: PRIVATE(私有), PUBLIC(公开), SHARED(分享)';
COMMENT ON COLUMN di_file_records.file_url IS '文件访问URL';
COMMENT ON COLUMN di_file_records.thumbnail_url IS '缩略图URL(仅图片类型)';
COMMENT ON COLUMN di_file_records.file_hash IS '文件SHA256哈希值,用于去重、秒传和完整性校验';
COMMENT ON COLUMN di_file_records.metadata IS '扩展元数据(JSON格式),可存储图片宽高、视频时长等信息';
COMMENT ON COLUMN di_file_records.reference_count IS '引用计数,记录文件被引用次数,为0时可安全删除';
COMMENT ON COLUMN di_file_records.delete_flag IS '删除标志: 0=未删除, 1=已删除';
COMMENT ON COLUMN di_file_records.created_at IS '文件上传时间';
COMMENT ON COLUMN di_file_records.updated_at IS '记录最后更新时间';

-- 索引设计
CREATE INDEX idx_di_file_records_user_id ON di_file_records(user_id);
CREATE INDEX idx_di_file_records_business_type ON di_file_records(business_type);
CREATE INDEX idx_di_file_records_status ON di_file_records(status);
CREATE INDEX idx_di_file_records_object_name ON di_file_records(object_name);
CREATE INDEX idx_di_file_records_file_hash ON di_file_records(file_hash);
CREATE INDEX idx_di_file_records_created_at ON di_file_records(created_at);
CREATE INDEX idx_di_file_records_user_business ON di_file_records(user_id, business_type);

-- ============================================
-- 文件分享表 - 支持用户间文件分享
-- ============================================
CREATE TABLE di_file_shares (
    -- 基础字段
    id BIGSERIAL PRIMARY KEY,
    file_id BIGINT NOT NULL,
    share_from_user_id BIGINT NOT NULL,
    share_to_user_id BIGINT NOT NULL,
    share_type VARCHAR(20) NOT NULL DEFAULT 'permanent',
    expires_at TIMESTAMP,
    permission_level VARCHAR(20) NOT NULL DEFAULT 'view',
    revoked SMALLINT DEFAULT 0,
    message TEXT,    
    view_count INT DEFAULT 0,
    download_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(file_id, share_to_user_id, revoked)
);

-- 表注释
COMMENT ON TABLE di_file_shares IS '文件分享表,管理用户间的文件分享关系';

-- 字段注释
COMMENT ON COLUMN di_file_shares.id IS '分享记录唯一标识,主键';
COMMENT ON COLUMN di_file_shares.file_id IS '被分享的文件ID,引用di_file_records表';
COMMENT ON COLUMN di_file_shares.share_from_user_id IS '分享者用户ID(谁分享的),引用sys_users表';
COMMENT ON COLUMN di_file_shares.share_to_user_id IS '接收者用户ID(分享给谁),引用sys_users表';
COMMENT ON COLUMN di_file_shares.share_type IS '分享类型枚举: PERMANENT(永久分享), TEMPORARY(临时分享)';
COMMENT ON COLUMN di_file_shares.expires_at IS '过期时间,仅TEMPORARY类型需要,NULL表示永久有效';
COMMENT ON COLUMN di_file_shares.permission_level IS '分享权限级别枚举: VIEW(仅查看), DOWNLOAD(可下载), EDIT(可编辑-预留)';
COMMENT ON COLUMN di_file_shares.revoked IS '撤销状态: 0=有效, 1=已撤销(软删除,保留分享历史)';
COMMENT ON COLUMN di_file_shares.message IS '分享留言,分享者给接收者的消息';
COMMENT ON COLUMN di_file_shares.view_count IS '查看次数统计';
COMMENT ON COLUMN di_file_shares.download_count IS '下载次数统计';
COMMENT ON COLUMN di_file_shares.created_at IS '分享创建时间';
COMMENT ON COLUMN di_file_shares.updated_at IS '分享信息最后更新时间';

CREATE INDEX idx_di_file_shares_from_user ON di_file_shares(share_from_user_id, created_at DESC);

CREATE INDEX idx_di_file_shares_to_user ON di_file_shares(share_to_user_id, created_at DESC);

CREATE INDEX idx_di_file_shares_access_check ON di_file_shares(file_id, share_to_user_id, revoked, expires_at);

CREATE INDEX idx_di_file_shares_file_id ON di_file_shares(file_id);

CREATE INDEX idx_di_file_shares_expires ON di_file_shares(expires_at);

-- 文件访问日志表(可选,如果需要统计文件访问情况)
CREATE TABLE di_file_access_logs (
    id BIGSERIAL PRIMARY KEY,
    file_id BIGINT NOT NULL,
    user_id BIGINT,
    access_type VARCHAR(20) NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    share_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE di_file_access_logs IS '文件访问日志表,记录文件访问行为';
COMMENT ON COLUMN di_file_access_logs.id IS '日志记录唯一标识';
COMMENT ON COLUMN di_file_access_logs.file_id IS '被访问的文件ID';
COMMENT ON COLUMN di_file_access_logs.user_id IS '访问用户ID,未登录用户为NULL';
COMMENT ON COLUMN di_file_access_logs.access_type IS '访问类型枚举: DOWNLOAD(下载), PREVIEW(预览), UPLOAD(上传)';
COMMENT ON COLUMN di_file_access_logs.ip_address IS '访问IP地址';
COMMENT ON COLUMN di_file_access_logs.user_agent IS '用户代理信息';
COMMENT ON COLUMN di_file_access_logs.share_id IS '分享ID,如果是通过分享访问';
COMMENT ON COLUMN di_file_access_logs.created_at IS '访问时间';

CREATE INDEX idx_di_file_access_logs_file_id ON di_file_access_logs(file_id);
CREATE INDEX idx_di_file_access_logs_user_id ON di_file_access_logs(user_id);
CREATE INDEX idx_di_file_access_logs_created_at ON di_file_access_logs(created_at);