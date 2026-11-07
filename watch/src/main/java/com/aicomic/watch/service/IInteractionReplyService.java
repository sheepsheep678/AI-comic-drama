package com.aicomic.watch.service;

import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.watch.domain.dto.ReplyDTO;
import com.aicomic.watch.domain.po.InteractionReply;
import com.baomidou.mybatisplus.extension.service.IService;
import com.aicomic.watch.domain.query.ReplyPageQuery;
import com.aicomic.watch.domain.vo.ReplyVO;

/**
 * <p>
 * 互动问题的回答或评论 服务类
 * </p>
 *
 */
public interface IInteractionReplyService extends IService<InteractionReply> {

    void saveReply(ReplyDTO replyDTO);

    PageDTO<ReplyVO> queryReplyPage(ReplyPageQuery pageQuery, boolean isViewer);

    void hiddenReply(Long id, Boolean hidden);

    ReplyVO queryReplyById(Long id);
}
