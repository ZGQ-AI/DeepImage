# DeepImage 项目开发规范文档

## 1. 概述

### 1.1 设计原则

本项目遵循以下核心设计原则：

1. **入参/出参封装标准**：所有接口的方法参数必须封装为 Request 类，返回值必须封装为 Response 类
2. **禁止魔法值**：所有常量必须用枚举或常量类封装
3. **统一常量管理**：多处重复使用的值必须定义为常量
4. **配置信息外部化**：可变配置使用 `application.yml` 配合 `@ConfigurationProperties`
5. **请求参数合法性验证**：使用 Jakarta Validation 注解进行参数校验
6. **层次分离/单一职责**：Controller 只做接收请求和响应转换，业务逻辑全部在 Service 层
7. **包结构规范**：Request、Response、常量、枚举分别放在约定目录
8. **严禁通过前端传递 UserId 参数**：使用 Sa-Token 从会话中获取当前用户 ID

### 1.2 技术栈

- **框架**: Spring Boot 3.x
- **ORM**: MyBatis-Plus
- **数据库**: PostgreSQL
- **认证**: Sa-Token
- **对象存储**: MinIO
- **校验**: Jakarta Validation
- **日志**: SLF4J + Logback

### 1.3 包结构

```
org.tech.ai.deepimage
├── config/              # 配置类
├── constant/            # 常量类
├── controller/          # 控制器
├── entity/              # 实体类
├── enums/               # 枚举类
├── exception/           # 异常类
├── mapper/              # Mapper 接口
├── model/
│   └── dto/
│       ├── request/     # 请求 DTO
│       └── response/    # 响应 DTO
├── service/             # Service 接口
│   └── impl/            # Service 实现类
└── util/                # 工具类
```

---

## 2. 数据库设计规范

### 2.1 设计哲学

数据库设计是业务逻辑的抽象表达，核心目标是**在保证数据完整性的前提下，最大化查询性能和维护效率**。

### 2.2 六大设计原则

#### 2.2.1 一致性原则

**核心思想**：统一的命名、类型和结构规范，降低认知成本

**关键要点**：
- 命名规范：主键统一为 `id`，外键为 `{table}_id`，时间字段为 `created_at/updated_at`
- 类型规范：主键用 `BIGSERIAL`，状态字段用 `SMALLINT`，时间用 `TIMESTAMP` (或 `TIMESTAMP WITH TIME ZONE`)
- 结构规范：所有表包含 `delete_flag`、`created_at`、`updated_at` 等标准字段

**决策指导**：当面临命名或类型选择时，优先考虑与现有规范的统一性

#### 2.2.2 完整性原则

**核心思想**：通过约束和业务逻辑保证数据的准确性和一致性

**关键要点**：
- 数据完整性：使用 `UNIQUE`、`NOT NULL` 等约束防止无效数据
- 业务完整性：通过复合约束防止业务逻辑冲突
- 引用完整性：通过应用层而非物理外键维护关联关系

**决策指导**：优先在数据库层面保证数据完整性，复杂业务逻辑在应用层处理

#### 2.2.3 性能原则

**核心思想**：在查询效率和存储效率之间找到最佳平衡点

**关键要点**：
- 索引策略：为查询字段建索引，避免低基数字段单独索引
- 存储优化：选择合适的数据类型，避免空间浪费
- 查询优化：通过复合索引和部分索引优化常用查询

**决策指导**：基于实际查询场景设计索引，定期分析慢查询并优化

#### 2.2.4 安全原则

**核心思想**：保护敏感数据，记录操作轨迹，支持数据恢复

**关键要点**：
- 数据保护：敏感信息加密存储，密码使用哈希值
- 操作审计：记录创建者、更新者、操作时间等审计信息
- 数据恢复：支持软删除，便于数据恢复和回滚

**决策指导**：敏感数据必须加密，重要操作必须可追溯

#### 2.2.5 可维护原则

**核心思想**：通过文档化和标准化提高代码可读性和维护效率

**关键要点**：
- 文档化：所有表和字段都有清晰的注释
- 标准化：遵循行业最佳实践和团队约定
- 模块化：职责分离，便于独立维护

**决策指导**：代码应该自解释，注释说明"为什么"而不是"做什么"

#### 2.2.6 扩展原则

**核心思想**：设计灵活的数据结构，支持业务变化和规模增长

**关键要点**：
- 灵活性：预留扩展字段（如 JSONB metadata），支持业务需求变化
- 可扩展性：支持数据量增长和性能扩展
- 兼容性：保持向后兼容，支持平滑升级

**决策指导**：在满足当前需求的前提下，为未来变化预留空间

### 2.3 设计流程

1. **需求分析**：理解业务需求和数据关系
2. **概念设计**：抽象出实体和关系
3. **逻辑设计**：设计表结构和约束
4. **物理设计**：优化索引和存储
5. **验证测试**：验证性能和完整性

### 2.4 核心决策框架

当面临设计选择时，按以下优先级考虑：

1. **数据完整性** > 性能优化
2. **一致性规范** > 个性化需求
3. **安全性** > 便利性
4. **可维护性** > 短期效率
5. **扩展性** > 当前完美

### 2.5 命名规范

- **表名**：使用小写字母，单词间使用下划线分隔，统一前缀（如 `di_` 表示 DeepImage，`sys_` 表示系统表）
- **字段名**：使用小写字母，单词间使用下划线分隔
- **主键**：统一命名为 `id`，类型为 `BIGSERIAL`
- **外键**：命名为 `{table}_id`（如 `user_id`、`file_id`）
- **时间字段**：统一使用 `created_at`、`updated_at`

### 2.6 必备字段

所有业务表必须包含以下字段：

```sql
id BIGSERIAL PRIMARY KEY,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
```

需要软删除的表还需包含：

```sql
delete_flag SMALLINT DEFAULT 0  -- 0=未删除, 1=已删除
```

### 2.7 注释规范

- 每个表必须有表注释：`COMMENT ON TABLE 表名 IS '表说明'`
- 每个字段必须有字段注释：`COMMENT ON COLUMN 表名.字段名 IS '字段说明'`

### 2.8 索引规范

- 主键自动创建索引
- 外键字段必须创建索引
- 经常用于查询条件的字段创建索引
- 联合索引遵循最左前缀原则
- 索引命名规范：`idx_表名_字段名`

**示例**：

```sql
CREATE INDEX idx_di_file_records_user_id ON di_file_records(user_id);
CREATE INDEX idx_di_file_records_user_business ON di_file_records(user_id, business_type);
```

### 2.9 质量保证

- **代码审查**：检查是否遵循设计原则
- **性能测试**：验证查询性能是否满足要求
- **数据验证**：确保数据完整性和一致性
- **文档更新**：保持设计文档的同步更新

---

## 3. Entity 层规范

### 3.1 基本结构

```java
package org.tech.ai.deepimage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 实体类说明
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Data
@TableName("表名")
public class EntityName {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 字段说明
     */
    @TableField("字段名")
    private Type fieldName;

    /**
     * 删除标志：0=未删除，1=已删除
     */
    @TableLogic
    @TableField("delete_flag")
    private Integer deleteFlag;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
```

### 3.2 规范要点

| 项目 | 规范 |
|------|------|
| **类注解** | `@Data`（Lombok）、`@TableName("表名")` |
| **主键** | `@TableId(value = "id", type = IdType.AUTO)` |
| **字段映射** | `@TableField("字段名")` |
| **软删除** | `@TableLogic`（MyBatis-Plus 会自动处理） |
| **时间类型** | 统一使用 `LocalDateTime` |
| **时间字段赋值** | 在 Service 层手动设置 `LocalDateTime.now()` |
| **字段命名** | 驼峰命名法（Java）对应下划线命名（数据库） |
| **注释** | 每个字段必须有 JavaDoc 注释 |

### 3.3 字段类型映射

| 数据库类型 | Java类型 | 注解 | 说明 |
|-----------|---------|------|------|
| BIGSERIAL | Long | @TableId | 主键字段 |
| VARCHAR | String | @TableField | 字符串字段 |
| TEXT | String | @TableField | 长文本字段 |
| BOOLEAN | Boolean | @TableField | 布尔字段 |
| SMALLINT | Integer | @TableField | 小整数（如状态、标志） |
| INT | Integer | @TableField | 整数字段 |
| BIGINT | Long | @TableField | 长整数字段 |
| TIMESTAMP | LocalDateTime | @TableField | 时间字段（在 Service 层手动设置） |
| JSONB | String | @TableField | JSON 数据（需要手动序列化/反序列化） |

### 3.4 禁止事项

- ❌ **不要使用 `@EqualsAndHashCode` 注解**（可能导致性能问题和无限递归）
- ❌ **不要使用 `FieldFill` 自动填充**（如 `fill = FieldFill.INSERT`），时间字段在 Service 层手动设置
- ❌ 不要在 Entity 中写业务逻辑方法
- ❌ 不要在 Entity 中引用其他 Entity（避免循环依赖）
- ❌ 不要使用 `Date` 类型，统一使用 `LocalDateTime`
- ❌ 逻辑删除字段统一使用 `deleteFlag`，不要使用其他命名

---

## 4. Mapper 层规范

### 4.1 基本结构

```java
package org.tech.ai.deepimage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.tech.ai.deepimage.entity.EntityName;

/**
 * Mapper 接口说明
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Mapper
public interface EntityNameMapper extends BaseMapper<EntityName> {
    
    // 简单 CRUD 使用 MyBatis-Plus 提供的方法
    // 复杂查询在此定义方法，在 XML 中实现
}
```

### 4.2 XML 配置

**文件位置**：`src/main/resources/mapper/EntityNameMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" 
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="org.tech.ai.deepimage.mapper.EntityNameMapper">

    <!-- 初始时保持空文件，MyBatis-Plus 自动处理 CRUD -->
    <!-- 需要自定义 SQL 时再添加具体查询 -->
    
    <!-- 复杂查询 SQL 示例 -->
    <select id="methodName" resultType="返回类型">
        SELECT * FROM table_name WHERE ...
    </select>
    
</mapper>
```

**规范要点**：
- **初始时保持空文件**，MyBatis-Plus 会自动处理基础 CRUD 操作
- 只有在需要自定义 SQL（如复杂联表查询）时才添加具体查询
- 简单的条件查询使用 `LambdaQueryWrapper`，无需编写 XML

### 4.3 使用建议

| 场景 | 推荐方式 |
|------|----------|
| **简单 CRUD** | 使用 MyBatis-Plus 提供的 `insert/update/delete/selectById` 等方法 |
| **简单条件查询** | 使用 `LambdaQueryWrapper` |
| **复杂联表查询** | 在 XML 中编写 SQL |
| **批量操作** | 使用 MyBatis-Plus 的 `saveBatch/updateBatchById` |

**示例**：

```java
// 使用 LambdaQueryWrapper
LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(Tag::getUserId, userId)
       .orderByDesc(Tag::getUsageCount);
List<Tag> tags = tagMapper.selectList(wrapper);
```

---

## 5. Service 层规范

### 5.1 接口定义

```java
package org.tech.ai.deepimage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.tech.ai.deepimage.entity.EntityName;
import org.tech.ai.deepimage.model.dto.request.*;
import org.tech.ai.deepimage.model.dto.response.*;

/**
 * Service 接口说明
 * 
 * @author zgq
 * @since 2025-10-01
 */
public interface EntityNameService extends IService<EntityName> {
    
    /**
     * 创建资源
     * 
     * @param request 创建请求
     * @return 资源响应
     */
    EntityNameResponse create(CreateEntityNameRequest request);
    
    /**
     * 查询列表
     * 
     * @return 资源列表
     */
    List<EntityNameResponse> list();
    
    /**
     * 更新资源
     * 
     * @param request 更新请求
     * @return 资源响应
     */
    EntityNameResponse update(UpdateEntityNameRequest request);
    
    /**
     * 删除资源
     * 
     * @param request 删除请求
     */
    void delete(DeleteEntityNameRequest request);
}
```

### 5.2 实现类结构

```java
package org.tech.ai.deepimage.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tech.ai.deepimage.entity.EntityName;
import org.tech.ai.deepimage.exception.BusinessException;
import org.tech.ai.deepimage.mapper.EntityNameMapper;
import org.tech.ai.deepimage.model.dto.request.*;
import org.tech.ai.deepimage.model.dto.response.*;
import org.tech.ai.deepimage.service.EntityNameService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service 实现类说明
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EntityNameServiceImpl extends ServiceImpl<EntityNameMapper, EntityName> 
        implements EntityNameService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntityNameResponse create(CreateEntityNameRequest request) {
        // 1. 获取当前用户ID（禁止从前端传递）
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("创建资源: userId={}, request={}", userId, request);
        
        // 2. 参数校验和业务逻辑处理
        // ...
        
        // 3. 构建实体
        EntityName entity = new EntityName();
        entity.setUserId(userId);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        // ... 设置其他字段
        
        // 4. 保存到数据库
        save(entity);
        
        // 5. 返回 Response
        log.info("资源创建成功: entityId={}", entity.getId());
        return EntityNameResponse.from(entity);
    }
    
    // 其他方法实现...
}
```

### 5.3 规范要点

| 项目 | 规范 |
|------|------|
| **类注解** | `@Slf4j`, `@Service`, `@RequiredArgsConstructor` |
| **继承** | `extends ServiceImpl<Mapper, Entity> implements Service` |
| **事务** | 需要事务的方法添加 `@Transactional(rollbackFor = Exception.class)` |
| **用户ID** | 使用 `StpUtil.getLoginIdAsLong()` 获取，禁止从参数传递 |
| **日志** | 记录关键操作（创建、更新、删除、异常） |
| **返回值** | 禁止返回 `Map`，必须封装为 Response 类 |
| **权限校验** | 在操作前校验用户是否有权限（是否为资源所有者） |

### 5.4 禁止事项

- ❌ 禁止在 Service 中直接操作 HttpServletRequest/Response
- ❌ 禁止返回 Entity 实体类给 Controller，必须转换为 Response
- ❌ 禁止接收前端传递的 `userId` 参数
- ❌ 禁止返回 `Map<String, Object>` 类型

---

## 6. Controller 层规范

### 6.1 基本结构

```java
package org.tech.ai.deepimage.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.tech.ai.deepimage.model.dto.request.*;
import org.tech.ai.deepimage.model.dto.response.*;
import org.tech.ai.deepimage.service.EntityNameService;

import java.util.List;

/**
 * Controller 说明
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/api/模块名")
@RequiredArgsConstructor
public class EntityNameController {
    
    private final EntityNameService entityNameService;
    
    /**
     * 创建资源
     * 
     * @param request 创建请求
     * @return 资源响应
     */
    @PostMapping
    public ApiResponse<EntityNameResponse> create(@Valid @RequestBody CreateEntityNameRequest request) {
        EntityNameResponse response = entityNameService.create(request);
        return ApiResponse.success(response);
    }
    
    /**
     * 查询列表
     * 
     * @return 资源列表
     */
    @GetMapping
    public ApiResponse<List<EntityNameResponse>> list() {
        List<EntityNameResponse> list = entityNameService.list();
        return ApiResponse.success(list);
    }
    
    /**
     * 查询详情
     * 
     * @param id 资源ID
     * @return 资源详情
     */
    @GetMapping("/{id}")
    public ApiResponse<EntityNameResponse> getById(@PathVariable Long id) {
        EntityNameResponse response = entityNameService.getById(id);
        return ApiResponse.success(response);
    }
    
    /**
     * 更新资源
     * 
     * @param request 更新请求
     * @return 资源响应
     */
    @PutMapping
    public ApiResponse<EntityNameResponse> update(@Valid @RequestBody UpdateEntityNameRequest request) {
        EntityNameResponse response = entityNameService.update(request);
        return ApiResponse.success(response);
    }
    
    /**
     * 删除资源
     * 
     * @param request 删除请求
     * @return 成功响应
     */
    @DeleteMapping
    public ApiResponse<Void> delete(@Valid @RequestBody DeleteEntityNameRequest request) {
        entityNameService.delete(request);
        return ApiResponse.success(null);
    }
}
```

### 6.2 规范要点

| 项目 | 规范 |
|------|------|
| **类注解** | `@Slf4j`, `@RestController`, `@Validated`, `@RequestMapping`, `@RequiredArgsConstructor` |
| **路径前缀** | 统一使用 `/api` 开头 |
| **入参** | 使用 `@Valid @RequestBody XxxRequest`，开启参数校验 |
| **出参** | 统一返回 `ApiResponse<T>` |
| **HTTP 方法** | GET 查询、POST 创建、PUT 更新、DELETE 删除 |
| **路径参数** | 使用 `@PathVariable`，如 `/api/files/{fileId}` |
| **查询参数** | 使用 `@RequestParam`，如 `/api/files?page=1&size=20` |
| **JavaDoc** | 每个方法必须有注释说明 |

### 6.3 RESTful 风格

| 操作 | HTTP 方法 | 路径示例 | 说明 |
|------|-----------|----------|------|
| 创建资源 | POST | `/api/tags` | 请求体包含资源信息 |
| 查询列表 | GET | `/api/tags` | 可带查询参数 |
| 查询详情 | GET | `/api/tags/{id}` | 路径参数传递 ID |
| 更新资源 | PUT | `/api/tags` | 请求体包含更新信息 |
| 删除资源 | DELETE | `/api/tags` | 请求体或路径参数传递 ID |

### 6.4 禁止事项

- ❌ 不要在 Controller 中写业务逻辑
- ❌ 不要在 Controller 中直接操作数据库
- ❌ 不要返回 Entity 实体类
- ❌ 不要接收前端传递的 `userId` 参数
- ❌ 不要使用 `@RequestMapping` 的 `method` 属性，使用专用注解（`@GetMapping` 等）

---

## 7. DTO 层规范

### 7.1 Request 规范

**文件位置**：`org.tech.ai.deepimage.model.dto.request`

```java
package org.tech.ai.deepimage.model.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 请求说明
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Data
public class XxxRequest {
    
    /**
     * 字段说明
     */
    @NotBlank(message = "字段不能为空")
    @Size(max = 50, message = "字段不能超过50个字符")
    private String fieldName;
    
    /**
     * ID
     */
    @NotNull(message = "ID不能为空")
    @Min(value = 1, message = "ID必须大于0")
    private Long id;
    
    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    /**
     * 正则校验示例
     */
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "颜色格式不正确，请使用#RRGGBB格式")
    private String color;
}
```

#### 常用校验注解

| 注解 | 说明 | 示例 |
|------|------|------|
| `@NotNull` | 不能为 null | `@NotNull(message = "ID不能为空")` |
| `@NotBlank` | 字符串不能为空或空白 | `@NotBlank(message = "名称不能为空")` |
| `@NotEmpty` | 集合/数组不能为空 | `@NotEmpty(message = "列表不能为空")` |
| `@Size` | 字符串/集合长度限制 | `@Size(min = 1, max = 50)` |
| `@Min/@Max` | 数值范围限制 | `@Min(value = 1)` |
| `@Email` | 邮箱格式校验 | `@Email(message = "邮箱格式不正确")` |
| `@Pattern` | 正则表达式校验 | `@Pattern(regexp = "正则")` |

### 7.2 Response 规范

**文件位置**：`org.tech.ai.deepimage.model.dto.response`

```java
package org.tech.ai.deepimage.model.dto.response;

import lombok.Builder;
import lombok.Data;
import org.tech.ai.deepimage.entity.EntityName;

import java.time.LocalDateTime;

/**
 * 响应说明
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Data
@Builder
public class XxxResponse {
    
    /**
     * ID
     */
    private Long id;
    
    /**
     * 字段说明
     */
    private String fieldName;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 从 Entity 转换为 Response
     * 
     * @param entity 实体对象
     * @return Response 对象
     */
    public static XxxResponse from(EntityName entity) {
        return XxxResponse.builder()
                .id(entity.getId())
                .fieldName(entity.getFieldName())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
```

#### 规范要点

- 使用 `@Builder` 模式方便构建对象
- 提供 `from(Entity)` 静态方法用于实体转换
- 只包含需要返回给前端的字段（敏感信息不返回）
- 时间类型使用 `LocalDateTime`（Jackson 自动序列化为 ISO 8601 格式）

### 7.3 ApiResponse 统一响应

```java
package org.tech.ai.deepimage.model.dto.response;

import lombok.Data;

/**
 * 统一响应包装类
 */
@Data
public class ApiResponse<T> {
    
    private final int code;
    private final String message;
    private final T data;
    
    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "Success", data);
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(500, message, null);
    }
    
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
```

**响应格式**：

```json
{
    "code": 200,
    "message": "Success",
    "data": {
        // 具体数据
    }
}
```

---

## 8. 枚举规范

**文件位置**：`org.tech.ai.deepimage.enums`

### 8.1 基本结构

```java
package org.tech.ai.deepimage.enums;

import lombok.Getter;

/**
 * 枚举说明
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Getter
public enum XxxEnum {
    
    VALUE1("CODE1", "描述1"),
    VALUE2("CODE2", "描述2"),
    VALUE3("CODE3", "描述3");
    
    private final String code;
    private final String description;
    
    XxxEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 根据 code 获取枚举
     * 
     * @param code 代码
     * @return 枚举值
     */
    public static XxxEnum fromCode(String code) {
        for (XxxEnum value : XxxEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("未知的枚举代码: " + code);
    }
}
```

### 8.2 规范要点

- 必须包含 `description` 字段说明含义
- 使用 `@Getter` 注解
- 提供 `fromCode` 或 `fromValue` 方法用于反向查找
- 禁止在代码中使用魔法值（硬编码的字符串/数字）

### 8.3 示例

```java
@Getter
public enum FileStatusEnum {
    
    UPLOADING("上传中"),
    COMPLETED("已完成"),
    FAILED("失败"),
    DELETED("已删除");
    
    private final String description;
    
    FileStatusEnum(String description) {
        this.description = description;
    }
}
```

**使用方式**：

```java
// ✅ 正确
entity.setStatus(FileStatusEnum.COMPLETED.name());

// ❌ 错误（魔法值）
entity.setStatus("COMPLETED");
```

---

## 9. 常量规范

**文件位置**：`org.tech.ai.deepimage.constant`

### 9.1 基本结构

```java
package org.tech.ai.deepimage.constant;

/**
 * 常量类说明
 * 
 * @author zgq
 * @since 2025-10-01
 */
public class XxxConstant {
    
    private XxxConstant() {
        // 私有构造函数，防止实例化
    }
    
    /**
     * 常量说明
     */
    public static final String CONSTANT_NAME = "value";
    
    /**
     * 数值常量
     */
    public static final int MAX_SIZE = 100;
}
```

### 9.2 示例

```java
public class ResponseConstant {
    
    private ResponseConstant() {}
    
    // 响应码
    public static final int SUCCESS = 200;
    public static final int SYSTEM_ERROR = 500;
    public static final int PARAM_ERROR = 400;
    
    // 响应消息
    public static final String SUCCESS_MESSAGE = "Success";
    public static final String SYSTEM_ERROR_MESSAGE = "System Error";
}
```

---

## 10. 配置管理规范

### 10.1 配置文件

**文件位置**：`src/main/resources/application.yml`

```yaml
# MinIO 配置
minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket: deepimage

# 刷新令牌配置
refresh-token:
  expiry-days: 30
  max-tokens-per-user: 5
```

### 10.2 配置类

**文件位置**：`org.tech.ai.deepimage.config`

```java
package org.tech.ai.deepimage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MinIO 配置属性
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
    
    /**
     * MinIO 服务地址
     */
    private String endpoint;
    
    /**
     * 访问密钥
     */
    private String accessKey;
    
    /**
     * 秘密密钥
     */
    private String secretKey;
    
    /**
     * 默认存储桶
     */
    private String bucket;
}
```

### 10.3 规范要点

- 可变配置必须外部化到 `application.yml`
- 使用 `@ConfigurationProperties` 映射配置项
- 配置类添加 `@Component` 或 `@Configuration` 注解
- 提供完整的字段注释

---

## 11. 异常处理规范

### 11.1 自定义异常

```java
package org.tech.ai.deepimage.exception;

import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final int code;
    
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
    
    public static BusinessException badRequest(String message) {
        return new BusinessException(400, message);
    }
    
    public static BusinessException notFound(String message) {
        return new BusinessException(404, message);
    }
    
    public static BusinessException forbidden(String message) {
        return new BusinessException(403, message);
    }
}
```

### 11.2 全局异常处理器

```java
package org.tech.ai.deepimage.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.tech.ai.deepimage.exception.BusinessException;
import org.tech.ai.deepimage.model.dto.response.ApiResponse;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return ApiResponse.error(e.getCode(), e.getMessage());
    }
    
    /**
     * 参数校验异常
     */
    @ExceptionHandler(BindException.class)
    public ApiResponse<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("参数校验失败: {}", message);
        return ApiResponse.error(400, message);
    }
    
    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return ApiResponse.error("系统异常，请联系管理员");
    }
}
```

---

## 12. 日志规范

### 12.1 日志级别

| 级别 | 使用场景 |
|------|----------|
| **ERROR** | 系统错误、异常情况 |
| **WARN** | 警告信息、业务异常 |
| **INFO** | 关键业务操作、系统启动信息 |
| **DEBUG** | 调试信息（生产环境关闭） |

### 12.2 日志规范

```java
@Slf4j
@Service
public class XxxServiceImpl {
    
    public void method() {
        // ✅ 正确：记录关键操作
        log.info("创建标签: userId={}, tagName={}", userId, tagName);
        
        try {
            // 业务逻辑
            log.info("标签创建成功: tagId={}", tagId);
        } catch (Exception e) {
            // ✅ 正确：记录异常堆栈
            log.error("标签创建失败", e);
        }
        
        // ✅ 正确：记录警告
        log.warn("标签名已存在: userId={}, tagName={}", userId, tagName);
        
        // ❌ 错误：不要使用 System.out.println
        System.out.println("这是错误的日志方式");
    }
}
```

---

## 13. 安全规范

### 13.1 用户认证

- ✅ 使用 Sa-Token 管理用户会话
- ✅ 使用 `StpUtil.getLoginIdAsLong()` 获取当前用户 ID
- ❌ **严禁通过前端传递 userId 参数**

### 13.2 权限校验

```java
public void deleteFile(Long fileId) {
    // 1. 获取当前用户ID
    Long userId = StpUtil.getLoginIdAsLong();
    
    // 2. 查询文件
    FileRecord file = fileRecordMapper.selectById(fileId);
    if (file == null) {
        throw BusinessException.notFound("文件不存在");
    }
    
    // 3. 校验权限
    if (!file.getUserId().equals(userId)) {
        throw BusinessException.forbidden("无权操作该文件");
    }
    
    // 4. 执行删除
    fileRecordMapper.deleteById(fileId);
}
```

### 13.3 敏感信息保护

- ❌ 不要在 Response 中返回密码、密钥等敏感信息
- ✅ 密码使用哈希加密存储
- ✅ 日志中不要记录敏感信息

---

## 14. 代码质量要求

### 14.1 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| **类名** | 大驼峰 | `UserService`, `FileController` |
| **方法名** | 小驼峰 | `createUser`, `listFiles` |
| **变量名** | 小驼峰 | `userId`, `fileName` |
| **常量** | 全大写下划线分隔 | `MAX_SIZE`, `DEFAULT_PAGE_SIZE` |
| **包名** | 全小写 | `org.tech.ai.deepimage.service` |

### 14.2 注释规范

- 每个类必须有 JavaDoc 注释（说明、作者、日期）
- 每个公共方法必须有 JavaDoc 注释（说明、参数、返回值）
- 复杂逻辑必须有行内注释

### 14.3 代码风格

- 使用 IDE 自动格式化（推荐 Google Java Style）
- 代码行宽不超过 120 字符
- 使用空行分隔逻辑块
- 导入语句不使用通配符（`import xxx.*`）

### 14.4 禁止事项

- ❌ 禁止使用魔法值
- ❌ 禁止大段代码注释（应该删除）
- ❌ 禁止空的 catch 块
- ❌ 禁止未使用的导入和变量
- ❌ 禁止过长的方法（超过 100 行应拆分）

---

## 附录：完整示例

### 示例：创建标签功能

#### 1. Entity

```java
@Data
@TableName("di_tags")
public class Tag {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("tag_name")
    private String tagName;
    
    @TableField("color")
    private String color;
    
    @TableField("usage_count")
    private Integer usageCount;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
```

#### 2. Request

```java
@Data
public class CreateTagRequest {
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称不能超过50个字符")
    private String tagName;
    
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "颜色格式不正确")
    private String color;
}
```

#### 3. Response

```java
@Data
@Builder
public class TagResponse {
    private Long id;
    private String tagName;
    private String color;
    private Integer usageCount;
    private LocalDateTime createdAt;
    
    public static TagResponse from(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .tagName(tag.getTagName())
                .color(tag.getColor())
                .usageCount(tag.getUsageCount())
                .createdAt(tag.getCreatedAt())
                .build();
    }
}
```

#### 4. Service

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TagResponse createTag(CreateTagRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("创建标签: userId={}, tagName={}", userId, request.getTagName());
        
        Tag tag = new Tag();
        tag.setUserId(userId);
        tag.setTagName(request.getTagName().trim());
        tag.setColor(request.getColor());
        tag.setUsageCount(0);
        tag.setCreatedAt(LocalDateTime.now());
        tag.setUpdatedAt(LocalDateTime.now());
        
        save(tag);
        log.info("标签创建成功: tagId={}", tag.getId());
        return TagResponse.from(tag);
    }
}
```

#### 5. Controller

```java
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {
    
    private final TagService tagService;
    
    @PostMapping
    public ApiResponse<TagResponse> createTag(@Valid @RequestBody CreateTagRequest request) {
        TagResponse response = tagService.createTag(request);
        return ApiResponse.success(response);
    }
}
```

---

**文档结束**

如有疑问，请联系项目负责人。

