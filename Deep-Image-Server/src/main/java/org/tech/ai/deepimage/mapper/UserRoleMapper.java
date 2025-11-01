package org.tech.ai.deepimage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.tech.ai.deepimage.entity.UserRole;

/**
 * User-role association table Mapper interface
 * 
 * @author zgq
 * @since 2025-09-29
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

}
