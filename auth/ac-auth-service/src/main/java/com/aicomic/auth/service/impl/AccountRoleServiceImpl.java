package com.aicomic.auth.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aicomic.auth.domain.po.AccountRole;
import com.aicomic.auth.mapper.AccountRoleMapper;
import com.aicomic.auth.service.IAccountRoleService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 账户、角色关联表 服务实现类
 * </p>
 *
 * @since 2022-06-16
 */
@Service
public class AccountRoleServiceImpl extends ServiceImpl<AccountRoleMapper, AccountRole> implements IAccountRoleService {

}
