package org.tech.ai.deepimage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.tech.ai.deepimage.entity.RolePermission;
import org.tech.ai.deepimage.mapper.RolePermissionMapper;
import org.tech.ai.deepimage.service.RolePermissionService;

/**
 * Role-permission association table service implementation class
 * 
 * @author zgq
 * @since 2025-09-29
 */
@Service
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission> implements RolePermissionService {

}
