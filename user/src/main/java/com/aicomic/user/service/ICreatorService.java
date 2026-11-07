package com.aicomic.user.service;

import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.user.domain.query.UserPageQuery;
import com.aicomic.user.domain.vo.CreatorPageVO;

/**
 * <p>
 * 创作者详情表 服务类
 * </p>
 *
 * @since 2022-07-12
 */
public interface ICreatorService{
    PageDTO<CreatorPageVO> queryTeacherPage(UserPageQuery pageQuery);

}
