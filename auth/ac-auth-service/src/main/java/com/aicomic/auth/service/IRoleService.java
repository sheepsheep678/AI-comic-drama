package com.aicomic.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.aicomic.auth.domain.po.Role;

import java.util.List;

/**
 * <p>
 * 角色表 服务类
 * </p>
 *
 * @since 2022-06-16
 */
public interface IRoleService extends IService<Role> {

    boolean exists(Long roleId);
    boolean exists(List<Long> roleIds);

    void deleteRole(Long id);
}
