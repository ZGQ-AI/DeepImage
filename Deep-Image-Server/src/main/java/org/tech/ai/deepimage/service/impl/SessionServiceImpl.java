package org.tech.ai.deepimage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.tech.ai.deepimage.entity.Session;
import org.tech.ai.deepimage.mapper.SessionMapper;
import org.tech.ai.deepimage.service.SessionService;

/**
 * 用户会话表 服务实现类
 * 
 * @author zgq
 * @since 2025-09-29
 */
@Service
public class SessionServiceImpl extends ServiceImpl<SessionMapper, Session> implements SessionService {

}
