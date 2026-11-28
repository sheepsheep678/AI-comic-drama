package com.aicomic.watch.controller;

import com.aicomic.watch.domain.query.CommentAdminPageQuery;
import com.aicomic.watch.domain.vo.CommentAdminVO;
import com.aicomic.watch.service.ICommentService;
import com.aicomic.common.domain.dto.PageDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 一级评论管理端 前端控制器
 * </p>
 */
@RestController
@RequestMapping("/admin/comments")
@Api(tags = "一级评论管理接口")
@RequiredArgsConstructor
public class CommentAdminController {

    private final ICommentService commentService;

    @ApiOperation("管理端分页查询一级评论")
    @GetMapping("page")
    public PageDTO<CommentAdminVO> queryCommentPageAdmin(CommentAdminPageQuery query) {
        return commentService.queryCommentPageAdmin(query);
    }

    @ApiOperation("管理端根据id查询一级评论")
    @GetMapping("{id}")
    public CommentAdminVO queryCommentByIdAdmin(@PathVariable("id") Long id) {
        return commentService.queryCommentByIdAdmin(id);
    }

    @ApiOperation("隐藏或显示一级评论")
    @PutMapping("/{id}/hidden/{hidden}")
    public void hiddenComment(
            @ApiParam(value = "评论id", example = "1") @PathVariable("id") Long id,
            @ApiParam(value = "是否隐藏，true/false", example = "true") @PathVariable("hidden") Boolean hidden) {
        commentService.hiddenComment(id, hidden);
    }
}
