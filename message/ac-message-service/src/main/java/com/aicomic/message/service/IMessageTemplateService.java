package com.aicomic.message.service;

import com.aicomic.message.domain.dto.MessageTemplateDTO;
import com.aicomic.message.domain.dto.MessageTemplateFormDTO;
import com.aicomic.message.domain.query.MessageTemplatePageQuery;
import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.message.domain.po.MessageTemplate;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 第三方短信平台签名和模板信息 服务类
 * </p>
 *
 * @since 2022-08-19
 */
public interface IMessageTemplateService extends IService<MessageTemplate> {

    List<MessageTemplate> queryByNoticeTemplateId(Long id);

    Long saveMessageTemplate(MessageTemplateFormDTO messageTemplateDTO);

    void updateMessageTemplate(MessageTemplateFormDTO messageTemplateDTO);

    PageDTO<MessageTemplateDTO> queryMessageTemplates(MessageTemplatePageQuery pageQuery);

    MessageTemplateDTO queryMessageTemplate(Long id);
}
