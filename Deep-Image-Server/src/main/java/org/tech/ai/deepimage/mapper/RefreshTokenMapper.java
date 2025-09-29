package org.tech.ai.deepimage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.tech.ai.deepimage.entity.RefreshToken;

/**
 * 刷新令牌表 Mapper 接口
 * 
 * @author zgq
 * @since 2025-09-29
 */
@Mapper
public interface RefreshTokenMapper extends BaseMapper<RefreshToken> {

}
