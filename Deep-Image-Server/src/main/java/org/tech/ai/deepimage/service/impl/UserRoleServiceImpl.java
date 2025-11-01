package org.tech.ai.deepimage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.tech.ai.deepimage.entity.UserRole;
import org.tech.ai.deepimage.mapper.UserRoleMapper;
import org.tech.ai.deepimage.service.UserRoleService;

/**
 * User-role association table service implementation class
 * 
 * @author zgq
 * @since 2025-09-29
 */
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {

}
