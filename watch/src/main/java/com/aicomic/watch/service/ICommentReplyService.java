package com.aicomic.watch.service;

import com.aicomic.watch.domain.dto.CommentReplyFormDTO;
import com.aicomic.watch.domain.po.CommentReply;
import com.aicomic.watch.domain.query.CommentReplyPageQuery;
import com.aicomic.watch.domain.vo.CommentReplyVO;
import com.aicomic.common.domain.dto.PageDTO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 二级评论服务类
 * </p>
 */
public interface ICommentReplyService extends IService<CommentReply> {

    /**
     * 发表二级评论
     */
    void saveReply(CommentReplyFormDTO replyDTO);

    /**
     * 分页查询一级评论下的二级评论
     */
    PageDTO<CommentReplyVO> queryReplyPage(CommentReplyPageQuery query, boolean forAdmin);

    /**
     * 根据id查询二级评论
     */
    CommentReplyVO queryReplyById(Long id, boolean forAdmin);

    /**
     * 管理端隐藏或显示二级评论
     */
    void hiddenReply(Long id, Boolean hidden);
}
