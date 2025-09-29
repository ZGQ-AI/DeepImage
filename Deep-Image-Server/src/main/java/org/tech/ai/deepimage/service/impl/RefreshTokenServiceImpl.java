package org.tech.ai.deepimage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.tech.ai.deepimage.entity.RefreshToken;
import org.tech.ai.deepimage.mapper.RefreshTokenMapper;
import org.tech.ai.deepimage.service.RefreshTokenService;

/**
 * 刷新令牌表 服务实现类
 * 
 * @author zgq
 * @since 2025-09-29
 */
@Service
public class RefreshTokenServiceImpl extends ServiceImpl<RefreshTokenMapper, RefreshToken> implements RefreshTokenService {

}
