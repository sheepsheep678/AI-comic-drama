package com.aicomic.watch.service;

import com.aicomic.watch.domain.dto.CommentFormDTO;
import com.aicomic.watch.domain.dto.CommentUpdateDTO;
import com.aicomic.watch.domain.po.Comment;
import com.aicomic.watch.domain.query.CommentAdminPageQuery;
import com.aicomic.watch.domain.query.CommentPageQuery;
import com.aicomic.watch.domain.vo.CommentAdminVO;
import com.aicomic.watch.domain.vo.CommentVO;
import com.aicomic.common.domain.dto.PageDTO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 一级评论服务类
 * </p>
 */
public interface ICommentService extends IService<Comment> {

    /**
     * 发表一级评论
     */
    void saveComment(CommentFormDTO commentDTO);

    /**
     * 修改自己的一级评论
     */
    void updateComment(Long id, CommentUpdateDTO commentDTO);

    /**
     * 删除自己的一级评论及其二级评论
     */
    void deleteComment(Long id);

    /**
     * 用户端分页查询一级评论
     */
    PageDTO<CommentVO> queryCommentPage(CommentPageQuery query);

    /**
     * 用户端根据id查询一级评论
     */
    CommentVO queryCommentById(Long id);

    /**
     * 管理端分页查询一级评论
     */
    PageDTO<CommentAdminVO> queryCommentPageAdmin(CommentAdminPageQuery query);

    /**
     * 管理端根据id查询一级评论
     */
    CommentAdminVO queryCommentByIdAdmin(Long id);

    /**
     * 管理端隐藏或显示一级评论
     */
    void hiddenComment(Long id, Boolean hidden);
}
