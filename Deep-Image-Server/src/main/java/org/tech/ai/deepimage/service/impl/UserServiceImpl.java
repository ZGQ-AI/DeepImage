package org.tech.ai.deepimage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.tech.ai.deepimage.entity.User;
import org.tech.ai.deepimage.mapper.UserMapper;
import org.tech.ai.deepimage.service.UserService;

/**
 * 用户信息表 服务实现类
 * 
 * @author zgq
 * @since 2025-09-29
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}
