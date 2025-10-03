# 文件管理模块 API 设计文档

> **版本**: v1.0  
> **更新日期**: 2025-10-02  
> **模块**: 文件管理（基于 MinIO）

---

## 目录

- [1. 概述](#1-概述)
- [2. 数据库设计](#2-数据库设计)
- [3. 接口总览](#3-接口总览)
- [4. 文件上传模块](#4-文件上传模块)
- [5. 文件查询模块](#5-文件查询模块)
- [6. 文件下载模块](#6-文件下载模块)
- [7. 文件管理模块](#7-文件管理模块)
- [8. 文件标签模块](#8-文件标签模块)
- [9. 文件分享模块](#9-文件分享模块)
- [10. 访问日志模块](#10-访问日志模块)
- [11. 技术实现要点](#11-技术实现要点)
- [12. 错误码定义](#12-错误码定义)

---

## 1. 概述

### 1.1 模块功能

文件管理模块基于 MinIO 对象存储实现，提供完整的文件生命周期管理功能：

- 📤 **文件上传**：单文件/批量上传、秒传、自动去重
- 🔍 **文件查询**：列表查询、详情查询、按标签/类型筛选
- 📥 **文件下载**：文件下载、预览 URL 生成
- 🛠️ **文件管理**：重命名、删除（软/硬删除）、批量操作
- 🏷️ **文件标签**：标签关联、标签移除、标签查询
- 🤝 **文件分享**：用户间分享、权限控制、分享统计
- 📊 **访问日志**：操作记录、访问统计

### 1.2 核心特性

- ✅ **文件去重**：基于 SHA256 哈希值实现秒传
- ✅ **权限控制**：私有/公开/分享三种访问权限
- ✅ **用户隔离**：所有操作基于用户维度隔离
- ✅ **引用计数**：防止误删，支持安全删除
- ✅ **完整审计**：记录所有文件操作行为
- ✅ **扩展元数据**：支持存储图片宽高、视频时长等信息

### 1.3 技术栈

- **对象存储**：MinIO
- **数据库**：PostgreSQL
- **ORM**：MyBatis-Plus
- **文件处理**：Apache Commons IO、Thumbnailator（缩略图）
- **哈希计算**：SHA-256

---

## 2. 数据库设计

### 2.1 核心表结构

#### **di_file_records** - 文件记录表

存储上传到 MinIO 的文件元数据信息。

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGSERIAL | 主键 |
| user_id | BIGINT | 文件所属用户ID |
| bucket_name | VARCHAR(100) | MinIO 存储桶名称 |
| object_name | VARCHAR(500) | MinIO 对象名称（全局唯一） |
| etag | VARCHAR(100) | MinIO ETag |
| original_filename | VARCHAR(255) | 原始文件名 |
| file_size | BIGINT | 文件大小（字节） |
| content_type | VARCHAR(100) | MIME 类型 |
| file_extension | VARCHAR(20) | 文件扩展名 |
| business_type | VARCHAR(50) | 业务类型（avatar/document/image/video/temp） |
| status | VARCHAR(20) | 文件状态（UPLOADING/COMPLETED/FAILED/DELETED） |
| visibility | VARCHAR(20) | 访问权限（PRIVATE/PUBLIC/SHARED） |
| file_url | VARCHAR(1000) | 文件访问 URL |
| thumbnail_url | VARCHAR(1000) | 缩略图 URL |
| file_hash | VARCHAR(64) | SHA256 哈希值 |
| metadata | JSONB | 扩展元数据 |
| reference_count | INT | 引用计数 |
| delete_flag | SMALLINT | 删除标志 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

#### **di_file_shares** - 文件分享表

管理用户间的文件分享关系。

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGSERIAL | 主键 |
| file_id | BIGINT | 被分享的文件ID |
| share_from_user_id | BIGINT | 分享者用户ID |
| share_to_user_id | BIGINT | 接收者用户ID |
| share_type | VARCHAR(20) | 分享类型（PERMANENT/TEMPORARY） |
| expires_at | TIMESTAMP | 过期时间 |
| permission_level | VARCHAR(20) | 权限级别（VIEW/DOWNLOAD/EDIT） |
| revoked | SMALLINT | 撤销状态 |
| message | TEXT | 分享留言 |
| view_count | INT | 查看次数 |
| download_count | INT | 下载次数 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

#### **di_file_tags** - 文件-标签关联表

实现文件和标签的多对多关系。

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGSERIAL | 主键 |
| file_id | BIGINT | 文件ID |
| tag_id | BIGINT | 标签ID |
| created_at | TIMESTAMP | 创建时间 |

#### **di_file_access_logs** - 文件访问日志表

记录文件的所有访问行为。

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGSERIAL | 主键 |
| file_id | BIGINT | 文件ID |
| user_id | BIGINT | 用户ID |
| access_type | VARCHAR(20) | 访问类型（DOWNLOAD/PREVIEW/UPLOAD） |
| ip_address | VARCHAR(45) | IP 地址 |
| user_agent | TEXT | User Agent |
| share_id | BIGINT | 分享ID（如果通过分享访问） |
| created_at | TIMESTAMP | 访问时间 |

---

## 3. 接口总览

### 3.1 接口列表

| 模块 | 接口名称 | HTTP 方法 | 路径 |
|------|----------|-----------|------|
| **文件上传** | 单文件上传 | POST | `/api/files/upload` |
| | 批量上传 | POST | `/api/files/upload/batch` |
| | 秒传检测 | POST | `/api/files/check-hash` |
| **文件查询** | 文件列表（分页） | GET | `/api/files` |
| | 文件详情 | GET | `/api/files/{fileId}` |
| | 按标签查询 | GET | `/api/files/by-tag/{tagId}` |
| | 按业务类型查询 | GET | `/api/files/by-type/{businessType}` |
| **文件下载** | 下载文件 | GET | `/api/files/{fileId}/download` |
| | 预览文件（临时URL） | GET | `/api/files/{fileId}/preview` |
| **文件管理** | 重命名文件 | PUT | `/api/files/{fileId}/rename` |
| | 删除文件（软删除） | DELETE | `/api/files/{fileId}` |
| | 批量删除 | DELETE | `/api/files/batch` |
| | 彻底删除 | DELETE | `/api/files/{fileId}/permanent` |
| **文件标签** | 添加标签 | POST | `/api/files/{fileId}/tags` |
| | 移除标签 | DELETE | `/api/files/{fileId}/tags/{tagId}` |
| | 查询文件的标签 | GET | `/api/files/{fileId}/tags` |
| **文件分享** | 创建分享 | POST | `/api/files/{fileId}/share` |
| | 取消分享 | DELETE | `/api/files/share/{shareId}` |
| | 我的分享列表 | GET | `/api/files/shares/outgoing` |
| | 收到的分享 | GET | `/api/files/shares/incoming` |
| | 分享详情 | GET | `/api/files/shares/{shareId}` |
| **访问日志** | 文件访问日志 | GET | `/api/files/{fileId}/logs` |
| | 用户文件统计 | GET | `/api/files/statistics` |

---

## 4. 文件上传模块

### 4.1 单文件上传

#### 接口信息

```
POST /api/files/upload
Content-Type: multipart/form-data
```

#### 请求参数

**UploadFileRequest**

```java
@Data
public class UploadFileRequest {
    
    /**
     * 上传的文件（必填）
     */
    @NotNull(message = "文件不能为空")
    private MultipartFile file;
    
    /**
     * 业务类型（必填）
     * 可选值：avatar, document, image, video, temp
     */
    @NotBlank(message = "业务类型不能为空")
    @Pattern(regexp = "avatar|document|image|video|temp", message = "业务类型不合法")
    private String businessType;
    
    /**
     * 文件描述（可选）
     */
    @Size(max = 500, message = "文件描述不能超过500字符")
    private String description;
    
    /**
     * 标签ID列表（可选）
     * 上传时直接关联标签
     */
    private List<Long> tagIds;
}
```

#### 返回值

**FileUploadResponse**

```java
@Data
@Builder
public class FileUploadResponse {
    
    /**
     * 文件ID
     */
    private Long fileId;
    
    /**
     * 原始文件名
     */
    private String originalFilename;
    
    /**
     * 文件访问 URL
     */
    private String fileUrl;
    
    /**
     * 缩略图 URL（仅图片类型）
     */
    private String thumbnailUrl;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 内容类型
     */
    private String contentType;
    
    /**
     * 文件哈希值
     */
    private String fileHash;
    
    /**
     * 是否秒传
     */
    private Boolean quickUpload;
    
    /**
     * 上传时间
     */
    private LocalDateTime uploadedAt;
}
```

#### 响应示例

```json
{
    "code": 200,
    "message": "Success",
    "data": {
        "fileId": 10001,
        "originalFilename": "example.jpg",
        "fileUrl": "http://minio.example.com/deepimage/1001/image/20251002/abc123.jpg",
        "thumbnailUrl": "http://minio.example.com/deepimage/1001/image/20251002/abc123_thumb.jpg",
        "fileSize": 1048576,
        "contentType": "image/jpeg",
        "fileHash": "a1b2c3d4e5f6...",
        "quickUpload": false,
        "uploadedAt": "2025-10-02T10:30:00"
    }
}
```

#### 业务逻辑

1. **参数校验**：校验文件大小、类型、业务类型
2. **计算哈希**：计算文件 SHA256 哈希值
3. **秒传检测**：检查 `file_hash` 是否已存在
   - 存在：增加引用计数，返回已有文件信息
   - 不存在：继续上传流程
4. **生成 ObjectName**：格式 `{userId}/{businessType}/{date}/{uuid}.{ext}`
   - 示例：`1001/image/20251002/abc123.jpg`
5. **上传到 MinIO**：调用 `minioClient.putObject()`
6. **保存记录**：插入 `di_file_records` 表
7. **生成缩略图**：如果是图片，异步生成缩略图
8. **关联标签**：如果提供了 `tagIds`，插入 `di_file_tags`
9. **记录日志**：插入 `di_file_access_logs`（access_type=UPLOAD）
10. **返回响应**：返回文件信息

#### 错误处理

| 错误码 | 说明 |
|--------|------|
| 400 | 文件为空、文件过大、类型不支持 |
| 500 | MinIO 上传失败、数据库异常 |

---

### 4.2 秒传检测

#### 接口信息

```
POST /api/files/check-hash
```

#### 请求参数

**CheckFileHashRequest**

```java
@Data
public class CheckFileHashRequest {
    
    /**
     * 文件 SHA256 哈希值
     */
    @NotBlank(message = "文件哈希不能为空")
    @Size(min = 64, max = 64, message = "哈希值长度必须为64位")
    private String fileHash;
    
    /**
     * 原始文件名
     */
    @NotBlank(message = "原始文件名不能为空")
    private String originalFilename;
    
    /**
     * 业务类型
     */
    @NotBlank(message = "业务类型不能为空")
    private String businessType;
}
```

#### 返回值

**CheckHashResponse**

```java
@Data
@Builder
public class CheckHashResponse {
    
    /**
     * 文件是否已存在
     */
    private Boolean exists;
    
    /**
     * 如果存在，返回文件ID
     */
    private Long fileId;
    
    /**
     * 如果存在，返回文件URL
     */
    private String fileUrl;
    
    /**
     * 提示信息
     */
    private String message;
}
```

#### 响应示例

```json
{
    "code": 200,
    "message": "Success",
    "data": {
        "exists": true,
        "fileId": 10001,
        "fileUrl": "http://minio.example.com/...",
        "message": "文件已存在，可秒传"
    }
}
```

#### 业务逻辑

1. 根据 `file_hash` 和 `user_id` 查询 `di_file_records`
2. 如果找到：
   - 返回 `exists=true`，返回文件信息
   - 前端可直接调用"秒传"接口完成上传
3. 如果未找到：
   - 返回 `exists=false`
   - 前端需要完整上传文件

---

### 4.3 批量上传

#### 接口信息

```
POST /api/files/upload/batch
```

#### 请求参数

**BatchUploadFileRequest**

```java
@Data
public class BatchUploadFileRequest {
    
    /**
     * 文件列表
     */
    @NotNull(message = "文件列表不能为空")
    @Size(min = 1, max = 10, message = "一次最多上传10个文件")
    private List<MultipartFile> files;
    
    /**
     * 业务类型
     */
    @NotBlank(message = "业务类型不能为空")
    private String businessType;
}
```

#### 返回值

**BatchUploadResponse**

```java
@Data
@Builder
public class BatchUploadResponse {
    
    /**
     * 成功数量
     */
    private Integer successCount;
    
    /**
     * 失败数量
     */
    private Integer failedCount;
    
    /**
     * 成功的文件列表
     */
    private List<FileUploadResponse> successFiles;
    
    /**
     * 失败的文件列表
     */
    private List<UploadErrorInfo> failedFiles;
}

@Data
@Builder
class UploadErrorInfo {
    private String filename;
    private String errorMessage;
}
```

#### 业务逻辑

1. 循环处理每个文件
2. 每个文件独立执行上传流程
3. 失败的文件记录错误信息，不影响其他文件
4. 返回成功和失败的统计信息

---

## 5. 文件查询模块

### 5.1 文件列表（分页）

#### 接口信息

```
GET /api/files?page=1&size=20&businessType=image&keyword=关键词&sortBy=createdAt&sortOrder=desc
```

#### 请求参数

**ListFilesRequest** (extends PageRequest)

```java
@Data
public class ListFilesRequest extends PageRequest {
    
    /**
     * 业务类型（可选）
     */
    private String businessType;
    
    /**
     * 关键词搜索（可选）
     * 搜索原始文件名
     */
    private String keyword;
    
    /**
     * 排序字段（可选）
     * 可选值：createdAt, fileSize, originalFilename
     */
    private String sortBy = "createdAt";
    
    /**
     * 排序方向（可选）
     * 可选值：asc, desc
     */
    private String sortOrder = "desc";
}
```

#### 返回值

**PageResponse\<FileInfoResponse\>**

```java
@Data
@Builder
public class FileInfoResponse {
    
    private Long fileId;
    private String originalFilename;
    private String fileUrl;
    private String thumbnailUrl;
    private Long fileSize;
    private String contentType;
    private String fileExtension;
    private String businessType;
    private String status;
    private String visibility;
    
    /**
     * 文件关联的标签
     */
    private List<TagResponse> tags;
    
    private Integer referenceCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### 响应示例

```json
{
    "code": 200,
    "message": "Success",
    "data": {
        "total": 100,
        "page": 1,
        "size": 20,
        "records": [
            {
                "fileId": 10001,
                "originalFilename": "example.jpg",
                "fileUrl": "http://...",
                "thumbnailUrl": "http://...",
                "fileSize": 1048576,
                "contentType": "image/jpeg",
                "fileExtension": "jpg",
                "businessType": "image",
                "status": "COMPLETED",
                "visibility": "PRIVATE",
                "tags": [
                    {"id": 1, "tagName": "工作", "color": "#FF5733"}
                ],
                "referenceCount": 1,
                "createdAt": "2025-10-02T10:30:00"
            }
        ]
    }
}
```

#### 业务逻辑

1. 获取当前用户ID
2. 构建查询条件：
   - `user_id = 当前用户ID`
   - `delete_flag = 0`
   - 如果有 `businessType`，添加条件
   - 如果有 `keyword`，使用 `LIKE` 搜索 `original_filename`
3. 分页查询
4. 关联查询标签信息（LEFT JOIN `di_file_tags` 和 `di_tags`）
5. 返回分页结果

---

### 5.2 文件详情

#### 接口信息

```
GET /api/files/{fileId}
```

#### 返回值

**FileDetailResponse** (extends FileInfoResponse)

```java
@Data
@Builder
public class FileDetailResponse extends FileInfoResponse {
    
    /**
     * 文件哈希值
     */
    private String fileHash;
    
    /**
     * 扩展元数据（JSON）
     */
    private String metadata;
    
    /**
     * 总查看次数
     */
    private Integer viewCount;
    
    /**
     * 总下载次数
     */
    private Integer downloadCount;
    
    /**
     * 最后访问时间
     */
    private LocalDateTime lastAccessedAt;
    
    /**
     * 分享信息
     */
    private List<FileShareInfo> shares;
}

@Data
@Builder
class FileShareInfo {
    private Long shareId;
    private String shareToUsername;
    private String permissionLevel;
    private LocalDateTime createdAt;
}
```

#### 业务逻辑

1. 根据 `fileId` 查询文件记录
2. 校验权限（文件所有者 或 被分享者）
3. 查询访问日志统计（viewCount, downloadCount）
4. 查询分享信息
5. 返回完整详情

---

### 5.3 按标签查询

#### 接口信息

```
GET /api/files/by-tag/{tagId}?page=1&size=20
```

#### 业务逻辑

1. 校验标签是否属于当前用户
2. 通过 `di_file_tags` 关联查询文件列表
3. 返回分页结果

---

### 5.4 按业务类型查询

#### 接口信息

```
GET /api/files/by-type/{businessType}?page=1&size=20
```

#### 业务逻辑

1. 查询条件：`user_id = 当前用户ID AND business_type = {businessType}`
2. 返回分页结果

---

## 6. 文件下载模块

### 6.1 下载文件

#### 接口信息

```
GET /api/files/{fileId}/download
```

#### 返回值

文件流（`ResponseEntity<InputStreamResource>`）

#### 响应头

```
Content-Type: application/octet-stream
Content-Disposition: attachment; filename="example.jpg"
Content-Length: 1048576
```

#### 业务逻辑

1. 根据 `fileId` 查询文件记录
2. 校验权限：
   - 文件所有者：直接允许
   - 被分享者：检查分享关系（未撤销、未过期、权限包含 DOWNLOAD）
3. 从 MinIO 获取文件流：`minioClient.getObject()`
4. 设置响应头（Content-Disposition, Content-Type）
5. 记录访问日志（access_type=DOWNLOAD）
6. 增加下载计数
7. 返回文件流

---

### 6.2 预览文件（获取临时 URL）

#### 接口信息

```
GET /api/files/{fileId}/preview?expirySeconds=3600
```

#### 请求参数

| 参数 | 类型 | 说明 | 默认值 |
|------|------|------|--------|
| expirySeconds | Integer | 有效期（秒） | 3600 |

#### 返回值

**FilePreviewResponse**

```java
@Data
@Builder
public class FilePreviewResponse {
    
    /**
     * 临时访问 URL（MinIO presigned URL）
     */
    private String previewUrl;
    
    /**
     * 有效期（秒）
     */
    private Integer expirySeconds;
    
    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;
}
```

#### 响应示例

```json
{
    "code": 200,
    "message": "Success",
    "data": {
        "previewUrl": "http://minio.example.com/deepimage/...?X-Amz-Expires=3600&...",
        "expirySeconds": 3600,
        "expiresAt": "2025-10-02T11:30:00"
    }
}
```

#### 业务逻辑

1. 校验权限（同下载接口）
2. 调用 MinIO API 生成 presigned URL：
   ```java
   String url = minioClient.getPresignedObjectUrl(
       GetPresignedObjectUrlArgs.builder()
           .method(Method.GET)
           .bucket(bucketName)
           .object(objectName)
           .expiry(expirySeconds, TimeUnit.SECONDS)
           .build()
   );
   ```
3. 记录访问日志（access_type=PREVIEW）
4. 返回临时 URL

---

## 7. 文件管理模块

### 7.1 重命名文件

#### 接口信息

```
PUT /api/files/{fileId}/rename
```

#### 请求参数

**RenameFileRequest**

```java
@Data
public class RenameFileRequest {
    
    /**
     * 新文件名
     */
    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名不能超过255字符")
    private String newFilename;
}
```

#### 返回值

```java
ApiResponse<FileInfoResponse>
```

#### 业务逻辑

1. 查询文件记录
2. 校验权限（必须是文件所有者）
3. 更新 `original_filename` 字段
4. 更新 `updated_at`
5. 返回更新后的文件信息

**注意**：仅修改数据库中的 `original_filename`，不修改 MinIO 中的 `object_name`。

---

### 7.2 删除文件（软删除）

#### 接口信息

```
DELETE /api/files/{fileId}
```

#### 返回值

```java
ApiResponse<Boolean>
```

#### 业务逻辑

1. 查询文件记录
2. 校验权限（必须是文件所有者）
3. 软删除：
   - 设置 `delete_flag = 1`
   - 设置 `status = 'DELETED'`
   - 更新 `updated_at`
4. **不删除 MinIO 中的实际文件**
5. 返回成功

---

### 7.3 批量删除

#### 接口信息

```
DELETE /api/files/batch
```

#### 请求参数

**BatchDeleteFilesRequest**

```java
@Data
public class BatchDeleteFilesRequest {
    
    /**
     * 文件ID列表
     */
    @NotEmpty(message = "文件ID列表不能为空")
    @Size(max = 100, message = "一次最多删除100个文件")
    private List<Long> fileIds;
}
```

#### 返回值

**BatchDeleteResponse**

```java
@Data
@Builder
public class BatchDeleteResponse {
    
    /**
     * 成功数量
     */
    private Integer successCount;
    
    /**
     * 失败数量
     */
    private Integer failedCount;
    
    /**
     * 失败的文件ID列表
     */
    private List<Long> failedFileIds;
}
```

---

### 7.4 彻底删除（从 MinIO 删除）

#### 接口信息

```
DELETE /api/files/{fileId}/permanent
```

#### 返回值

```java
ApiResponse<Boolean>
```

#### 业务逻辑

1. 查询文件记录
2. 校验权限（必须是文件所有者）
3. 检查引用计数：
   - 如果 `reference_count > 0`，抛出异常："文件正在被引用，无法删除"
4. 从 MinIO 删除文件：
   ```java
   minioClient.removeObject(
       RemoveObjectArgs.builder()
           .bucket(bucketName)
           .object(objectName)
           .build()
   );
   ```
5. 从数据库删除记录（物理删除）
6. 删除关联数据：
   - `di_file_tags`（标签关联）
   - `di_file_shares`（分享记录）
   - `di_file_access_logs`（访问日志）
7. 返回成功

---

## 8. 文件标签模块

### 8.1 为文件添加标签

#### 接口信息

```
POST /api/files/{fileId}/tags
```

#### 请求参数

**AddFileTagsRequest**

```java
@Data
public class AddFileTagsRequest {
    
    /**
     * 标签ID列表
     */
    @NotEmpty(message = "标签ID列表不能为空")
    @Size(max = 10, message = "一次最多添加10个标签")
    private List<Long> tagIds;
}
```

#### 返回值

```java
ApiResponse<List<TagResponse>>
```

#### 业务逻辑

1. 校验文件权限（必须是文件所有者）
2. 校验标签权限（标签必须属于当前用户）
3. 批量插入 `di_file_tags`（忽略已存在的关联）
4. 更新标签的 `usage_count`
5. 返回文件的所有标签

---

### 8.2 移除文件标签

#### 接口信息

```
DELETE /api/files/{fileId}/tags/{tagId}
```

#### 返回值

```java
ApiResponse<Boolean>
```

#### 业务逻辑

1. 校验权限
2. 删除 `di_file_tags` 中的关联记录
3. 减少标签的 `usage_count`
4. 返回成功

---

### 8.3 查询文件的所有标签

#### 接口信息

```
GET /api/files/{fileId}/tags
```

#### 返回值

```java
ApiResponse<List<TagResponse>>
```

#### 业务逻辑

1. 校验文件访问权限
2. 通过 `di_file_tags` 关联查询标签
3. 返回标签列表

---

## 9. 文件分享模块

### 9.1 创建分享

#### 接口信息

```
POST /api/files/{fileId}/share
```

#### 请求参数

**CreateFileShareRequest**

```java
@Data
public class CreateFileShareRequest {
    
    /**
     * 分享目标用户ID
     */
    @NotNull(message = "分享目标用户ID不能为空")
    private Long shareToUserId;
    
    /**
     * 分享类型
     * 可选值：PERMANENT, TEMPORARY
     */
    @NotBlank(message = "分享类型不能为空")
    @Pattern(regexp = "PERMANENT|TEMPORARY", message = "分享类型不合法")
    private String shareType;
    
    /**
     * 过期时间（TEMPORARY 类型必填）
     */
    private LocalDateTime expiresAt;
    
    /**
     * 权限级别
     * 可选值：VIEW, DOWNLOAD, EDIT
     */
    @NotBlank(message = "权限级别不能为空")
    @Pattern(regexp = "VIEW|DOWNLOAD|EDIT", message = "权限级别不合法")
    private String permissionLevel;
    
    /**
     * 分享留言（可选）
     */
    @Size(max = 500, message = "留言不能超过500字符")
    private String message;
}
```

#### 返回值

**FileShareResponse**

```java
@Data
@Builder
public class FileShareResponse {
    
    private Long shareId;
    private Long fileId;
    private String originalFilename;
    private String fileUrl;
    private String thumbnailUrl;
    
    private Long shareFromUserId;
    private String shareFromUsername;
    
    private Long shareToUserId;
    private String shareToUsername;
    
    private String shareType;
    private LocalDateTime expiresAt;
    private String permissionLevel;
    private String message;
    
    private Boolean revoked;
    private Integer viewCount;
    private Integer downloadCount;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### 业务逻辑

1. 校验权限（必须是文件所有者）
2. 校验目标用户是否存在
3. 检查是否已存在分享关系（避免重复分享）
4. 插入 `di_file_shares` 记录
5. 更新文件的 `visibility = 'SHARED'`
6. 返回分享信息

---

### 9.2 取消分享

#### 接口信息

```
DELETE /api/files/share/{shareId}
```

#### 返回值

```java
ApiResponse<Boolean>
```

#### 业务逻辑

1. 查询分享记录
2. 校验权限（必须是分享者）
3. 软删除：设置 `revoked = 1`
4. 更新 `updated_at`
5. 如果文件没有其他有效分享，更新 `visibility = 'PRIVATE'`
6. 返回成功

---

### 9.3 我的分享列表

#### 接口信息

```
GET /api/files/shares/outgoing?page=1&size=20
```

#### 返回值

```java
ApiResponse<PageResponse<FileShareResponse>>
```

#### 业务逻辑

1. 查询条件：`share_from_user_id = 当前用户ID AND revoked = 0`
2. 分页查询
3. 关联查询文件信息和目标用户信息
4. 返回分页结果

---

### 9.4 收到的分享

#### 接口信息

```
GET /api/files/shares/incoming?page=1&size=20
```

#### 返回值

```java
ApiResponse<PageResponse<FileShareResponse>>
```

#### 业务逻辑

1. 查询条件：`share_to_user_id = 当前用户ID AND revoked = 0 AND (expires_at IS NULL OR expires_at > NOW())`
2. 分页查询
3. 关联查询文件信息和分享者信息
4. 返回分页结果

---

### 9.5 分享详情

#### 接口信息

```
GET /api/files/shares/{shareId}
```

#### 返回值

```java
ApiResponse<FileShareResponse>
```

#### 业务逻辑

1. 查询分享记录
2. 校验权限（必须是分享者或接收者）
3. 关联查询完整信息
4. 返回分享详情

---

## 10. 访问日志模块

### 10.1 文件访问日志

#### 接口信息

```
GET /api/files/{fileId}/logs?page=1&size=20
```

#### 返回值

**PageResponse\<FileAccessLogResponse\>**

```java
@Data
@Builder
public class FileAccessLogResponse {
    
    private Long logId;
    private String accessType;  // DOWNLOAD, PREVIEW, UPLOAD
    private String username;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime accessedAt;
}
```

#### 业务逻辑

1. 校验权限（必须是文件所有者）
2. 查询 `di_file_access_logs`
3. 关联查询用户信息
4. 返回分页结果

---

### 10.2 用户文件统计

#### 接口信息

```
GET /api/files/statistics
```

#### 返回值

**FileStatisticsResponse**

```java
@Data
@Builder
public class FileStatisticsResponse {
    
    /**
     * 总文件数
     */
    private Long totalFiles;
    
    /**
     * 总存储大小（字节）
     */
    private Long totalSize;
    
    /**
     * 按业务类型统计
     */
    private Long imageCount;
    private Long documentCount;
    private Long videoCount;
    private Long avatarCount;
    private Long tempCount;
    
    /**
     * 分享统计
     */
    private Long shareOutCount;  // 分享出去的文件数
    private Long shareInCount;   // 收到的分享数
    
    /**
     * 访问统计
     */
    private Long totalDownloads;  // 总下载次数
    private Long totalViews;      // 总查看次数
    private Long totalUploads;    // 总上传次数
    
    /**
     * 最近上传
     */
    private LocalDateTime lastUploadedAt;
}
```

#### 业务逻辑

1. 统计当前用户的文件数据：
   - 文件总数：`SELECT COUNT(*) FROM di_file_records WHERE user_id = ? AND delete_flag = 0`
   - 总大小：`SELECT SUM(file_size) FROM ...`
   - 按业务类型分组统计：`GROUP BY business_type`
2. 统计分享数据：
   - 分享出去：`SELECT COUNT(DISTINCT file_id) FROM di_file_shares WHERE share_from_user_id = ?`
   - 收到分享：`SELECT COUNT(*) FROM di_file_shares WHERE share_to_user_id = ?`
3. 统计访问数据：
   - 从 `di_file_access_logs` 按 `access_type` 分组统计
4. 返回统计结果

---

## 11. 技术实现要点

### 11.1 MinIO 集成

#### 上传文件

```java
minioClient.putObject(
    PutObjectArgs.builder()
        .bucket(bucketName)
        .object(objectName)
        .stream(inputStream, fileSize, -1)
        .contentType(contentType)
        .build()
);
```

#### 下载文件

```java
InputStream stream = minioClient.getObject(
    GetObjectArgs.builder()
        .bucket(bucketName)
        .object(objectName)
        .build()
);
```

#### 生成临时访问 URL

```java
String url = minioClient.getPresignedObjectUrl(
    GetPresignedObjectUrlArgs.builder()
        .method(Method.GET)
        .bucket(bucketName)
        .object(objectName)
        .expiry(3600, TimeUnit.SECONDS)
        .build()
);
```

#### 删除文件

```java
minioClient.removeObject(
    RemoveObjectArgs.builder()
        .bucket(bucketName)
        .object(objectName)
        .build()
);
```

### 11.2 文件哈希计算

使用 SHA-256 算法计算文件哈希：

```java
public static String calculateFileHash(InputStream inputStream) throws Exception {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] buffer = new byte[8192];
    int read;
    while ((read = inputStream.read(buffer)) > 0) {
        digest.update(buffer, 0, read);
    }
    byte[] hashBytes = digest.digest();
    
    // 转换为十六进制字符串
    StringBuilder hexString = new StringBuilder();
    for (byte b : hashBytes) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) hexString.append('0');
        hexString.append(hex);
    }
    return hexString.toString();
}
```

### 11.3 缩略图生成

使用 Thumbnailator 库生成图片缩略图：

```java
Thumbnails.of(originalFile)
    .size(200, 200)
    .keepAspectRatio(true)
    .toFile(thumbnailFile);
```

### 11.4 秒传实现

1. 前端计算文件哈希（浏览器端使用 Web Crypto API）
2. 调用"秒传检测"接口
3. 如果文件已存在：
   - 增加 `reference_count`
   - 创建新的文件记录（指向同一个 MinIO 对象）
4. 如果文件不存在：
   - 完整上传文件

### 11.5 权限校验

```java
private void checkFilePermission(Long fileId, Long userId) {
    FileRecord file = fileRecordMapper.selectById(fileId);
    
    if (file == null) {
        throw BusinessException.notFound("文件不存在");
    }
    
    // 检查是否为文件所有者
    if (file.getUserId().equals(userId)) {
        return;
    }
    
    // 检查是否被分享
    LambdaQueryWrapper<FileShare> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(FileShare::getFileId, fileId)
           .eq(FileShare::getShareToUserId, userId)
           .eq(FileShare::getRevoked, 0)
           .and(w -> w.isNull(FileShare::getExpiresAt)
                      .or()
                      .gt(FileShare::getExpiresAt, LocalDateTime.now()));
    
    FileShare share = fileShareMapper.selectOne(wrapper);
    if (share == null) {
        throw BusinessException.forbidden("无权访问该文件");
    }
}
```

### 11.6 访问日志记录

```java
@Async
public void logFileAccess(Long fileId, Long userId, String accessType, 
                          String ipAddress, String userAgent, Long shareId) {
    FileAccessLog log = new FileAccessLog();
    log.setFileId(fileId);
    log.setUserId(userId);
    log.setAccessType(accessType);
    log.setIpAddress(ipAddress);
    log.setUserAgent(userAgent);
    log.setShareId(shareId);
    log.setCreatedAt(LocalDateTime.now());
    
    fileAccessLogMapper.insert(log);
}
```

---

## 12. 错误码定义

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误、业务异常 |
| 401 | 未登录 |
| 403 | 无权访问 |
| 404 | 资源不存在 |
| 500 | 系统错误 |

### 业务错误码

| 错误码 | 说明 |
|--------|------|
| 40001 | 文件不存在 |
| 40002 | 文件过大 |
| 40003 | 文件类型不支持 |
| 40004 | 文件哈希值不匹配 |
| 40005 | 文件正在被引用，无法删除 |
| 40006 | 分享已撤销或已过期 |
| 40007 | 无权访问该文件 |
| 40008 | 标签不存在或无权访问 |
| 40009 | 目标用户不存在 |
| 40010 | 已存在相同的分享关系 |

---

## 附录：完整示例

### 示例：文件上传流程

#### 1. Controller

```java
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    
    private final FileService fileService;
    
    @PostMapping("/upload")
    public ApiResponse<FileUploadResponse> uploadFile(@Valid UploadFileRequest request) {
        FileUploadResponse response = fileService.uploadFile(request);
        return ApiResponse.success(response);
    }
}
```

#### 2. Service

```java
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    
    private final MinioClient minioClient;
    private final FileRecordMapper fileRecordMapper;
    private final MinioProperties minioProperties;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileUploadResponse uploadFile(UploadFileRequest request) {
        // 1. 获取当前用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 2. 校验文件
        MultipartFile file = request.getFile();
        if (file.getSize() > MAX_FILE_SIZE) {
            throw BusinessException.badRequest("文件大小超过限制");
        }
        
        // 3. 计算文件哈希
        String fileHash = calculateFileHash(file.getInputStream());
        
        // 4. 检查秒传
        FileRecord existingFile = fileRecordMapper.selectOne(
            new LambdaQueryWrapper<FileRecord>()
                .eq(FileRecord::getFileHash, fileHash)
                .eq(FileRecord::getUserId, userId)
                .last("LIMIT 1")
        );
        
        if (existingFile != null) {
            // 秒传：增加引用计数
            existingFile.setReferenceCount(existingFile.getReferenceCount() + 1);
            fileRecordMapper.updateById(existingFile);
            
            return buildResponse(existingFile, true);
        }
        
        // 5. 生成 ObjectName
        String objectName = generateObjectName(userId, request.getBusinessType(), 
                                              file.getOriginalFilename());
        
        // 6. 上传到 MinIO
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(minioProperties.getBucket())
                .object(objectName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build()
        );
        
        // 7. 保存记录
        FileRecord record = buildFileRecord(userId, file, objectName, fileHash, request);
        fileRecordMapper.insert(record);
        
        // 8. 异步生成缩略图
        if (isImage(file.getContentType())) {
            asyncGenerateThumbnail(record);
        }
        
        // 9. 关联标签
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            addFileTags(record.getId(), request.getTagIds());
        }
        
        // 10. 记录日志
        logFileAccess(record.getId(), userId, "UPLOAD", getIpAddress(), getUserAgent());
        
        return buildResponse(record, false);
    }
}
```

---

**文档结束**

如有疑问，请联系项目负责人。

