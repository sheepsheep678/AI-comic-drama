package com.aicomic.message.service;

import com.aicomic.message.domain.dto.UserInboxDTO;
import com.aicomic.message.domain.dto.UserInboxFormDTO;
import com.aicomic.api.dto.user.UserDTO;
import com.aicomic.message.domain.query.UserInboxQuery;
import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.message.domain.po.NoticeTemplate;
import com.aicomic.message.domain.po.UserInbox;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 用户通知记录 服务类
 * </p>
 *
 * @since 2022-08-19
 */
public interface IUserInboxService extends IService<UserInbox> {

    void saveNoticeToInbox(NoticeTemplate noticeTemplate, List<UserDTO> users);

    PageDTO<UserInboxDTO> queryUserInBoxesPage(UserInboxQuery query);

    Long sentMessageToUser(UserInboxFormDTO userInboxFormDTO);
}
