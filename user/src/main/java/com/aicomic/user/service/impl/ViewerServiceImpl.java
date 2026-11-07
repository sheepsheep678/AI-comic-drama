package com.aicomic.user.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aicomic.api.client.order.OrderClient;
import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.common.enums.UserType;
import com.aicomic.common.utils.BeanUtils;
import com.aicomic.common.utils.CollUtils;
import com.aicomic.common.utils.RandomUtils;
import com.aicomic.user.constants.UserConstants;
import com.aicomic.user.domain.dto.ViewerFormDTO;
import com.aicomic.user.domain.po.User;
import com.aicomic.user.domain.po.UserDetail;
import com.aicomic.user.domain.query.UserPageQuery;
import com.aicomic.user.domain.vo.ViewerPageVo;
import com.aicomic.user.service.IViewerService;
import com.aicomic.user.service.IUserDetailService;
import com.aicomic.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 观众详情表 服务实现类
 * </p>
 *
 * @since 2022-07-12
 */
@Service
@RequiredArgsConstructor
public class ViewerServiceImpl implements IViewerService {

    private final IUserService userService;
    private final IUserDetailService detailService;
    private final OrderClient orderClient;

    @Override
    @Transactional
    public void saveStudent(ViewerFormDTO viewerFormDTO) {
        // 1.新增用户账号
        User user = new User();
        user.setCellPhone(viewerFormDTO.getCellPhone());
        user.setPassword(viewerFormDTO.getPassword());
        user.setType(UserType.VIEWER);
        userService.addUserByPhone(user, viewerFormDTO.getCode());

        // 2.新增观众详情
        UserDetail viewer = new UserDetail();
        viewer.setId(user.getId());
        viewer.setName(RandomUtils.randomString(8));
        viewer.setRoleId(UserConstants.VIEWER_ROLE_ID);
        detailService.save(viewer);
    }

    @Override
    public void updateMyPassword(ViewerFormDTO viewerFormDTO) {
        userService.updatePasswordByPhone(
                viewerFormDTO.getCellPhone(), viewerFormDTO.getCode(), viewerFormDTO.getPassword()
        );
    }

    @Override
    public PageDTO<ViewerPageVo> queryStudentPage(UserPageQuery query) {
        // 1.分页条件
        Page<UserDetail> page  =  detailService.queryUserDetailByPage(query, UserType.VIEWER);
        List<UserDetail> records = page.getRecords();
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(page);
        }

        // 2.查询购买的漫剧数量
        List<Long> stuIds = records.stream().map(UserDetail::getId).collect(Collectors.toList());
        Map<Long, Integer> numMap = orderClient.countPurchaseComicOfViewer(stuIds);

        // 3.处理vo
        List<ViewerPageVo> list = new ArrayList<>(records.size());
        for (UserDetail r : records) {
            ViewerPageVo v = BeanUtils.toBean(r, ViewerPageVo.class);
            list.add(v);
            v.setCourseAmount(numMap.get(r.getId()));
        }
        return new PageDTO<>(page.getTotal(), page.getPages(), list);
    }
}
