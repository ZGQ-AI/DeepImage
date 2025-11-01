package org.tech.ai.deepimage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.tech.ai.deepimage.entity.Permission;

/**
 * Permission information table Mapper interface
 * 
 * @author zgq
 * @since 2025-09-29
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

}
