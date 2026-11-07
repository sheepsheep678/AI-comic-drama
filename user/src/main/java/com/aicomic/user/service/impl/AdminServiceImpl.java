package com.aicomic.user.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aicomic.api.cache.RoleCache;
import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.common.enums.UserType;
import com.aicomic.common.utils.BeanUtils;
import com.aicomic.user.domain.po.UserDetail;
import com.aicomic.user.domain.query.UserPageQuery;
import com.aicomic.user.domain.vo.AdminVO;
import com.aicomic.user.service.IAdminService;
import com.aicomic.user.service.IUserDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 管理员详情表 服务实现类
 * </p>
 *
 * @since 2022-07-12
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements IAdminService {

    private final IUserDetailService detailService;
    private final RoleCache roleCache;
    @Override
    public PageDTO<AdminVO> queryStaffPage(UserPageQuery query) {
        // 1.搜索
        Page<UserDetail> p = detailService.queryUserDetailByPage(query, UserType.ADMIN);
        // 2.处理vo
        return PageDTO.of(p, u -> {
            AdminVO v = BeanUtils.toBean(u, AdminVO.class);
            v.setRoleName(roleCache.getRoleName(u.getRoleId()));
            return v;
        });
    }
}
