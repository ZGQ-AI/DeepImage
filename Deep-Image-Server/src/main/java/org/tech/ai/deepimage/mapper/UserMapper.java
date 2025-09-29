package org.tech.ai.deepimage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.tech.ai.deepimage.entity.User;

/**
 * 用户信息表 Mapper 接口
 * 
 * @author zgq
 * @since 2025-09-29
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
