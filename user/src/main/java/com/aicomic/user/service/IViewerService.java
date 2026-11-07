package com.aicomic.user.service;

import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.user.domain.dto.ViewerFormDTO;
import com.aicomic.user.domain.query.UserPageQuery;
import com.aicomic.user.domain.vo.ViewerPageVo;

/**
 * <p>
 * 观众详情表 服务类
 * </p>
 *
 * @since 2022-07-12
 */
public interface IViewerService {

    void saveStudent(ViewerFormDTO viewerFormDTO);

    void updateMyPassword(ViewerFormDTO viewerFormDTO);

    PageDTO<ViewerPageVo> queryStudentPage(UserPageQuery pageQuery);
}
