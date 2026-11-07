package com.aicomic.message.service;

import com.aicomic.message.domain.dto.NoticeTemplateDTO;
import com.aicomic.message.domain.dto.NoticeTemplateFormDTO;
import com.aicomic.message.domain.query.NoticeTemplatePageQuery;
import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.message.domain.po.NoticeTemplate;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 通知模板 服务类
 * </p>
 *
 * @since 2022-08-19
 */
public interface INoticeTemplateService extends IService<NoticeTemplate> {

    Long saveNoticeTemplate(NoticeTemplateFormDTO noticeTemplateFormDTO);

    void updateNoticeTemplate(NoticeTemplateFormDTO noticeTemplateFormDTO);

    PageDTO<NoticeTemplateDTO> queryNoticeTemplates(NoticeTemplatePageQuery pageQuery);

    NoticeTemplateDTO queryNoticeTemplate(Long id);

    NoticeTemplate queryByCode(String code);
}
