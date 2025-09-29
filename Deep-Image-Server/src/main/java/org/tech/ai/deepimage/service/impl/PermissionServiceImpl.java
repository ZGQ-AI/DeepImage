package org.tech.ai.deepimage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.tech.ai.deepimage.entity.Permission;
import org.tech.ai.deepimage.mapper.PermissionMapper;
import org.tech.ai.deepimage.service.PermissionService;

/**
 * 权限信息表 服务实现类
 * 
 * @author zgq
 * @since 2025-09-29
 */
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

}
