package com.aicomic.watch.controller;

import com.aicomic.watch.domain.query.CommentReplyPageQuery;
import com.aicomic.watch.domain.vo.CommentReplyVO;
import com.aicomic.watch.service.ICommentReplyService;
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

import javax.validation.Valid;

/**
 * <p>
 * 二级评论管理端 前端控制器
 * </p>
 */
@RestController
@RequestMapping("/admin/replies")
@Api(tags = "二级评论管理接口")
@RequiredArgsConstructor
public class CommentReplyAdminController {

    private final ICommentReplyService replyService;

    @ApiOperation("管理端分页查询一级评论下的二级评论")
    @GetMapping("page")
    public PageDTO<CommentReplyVO> queryReplyPage(@Valid CommentReplyPageQuery query) {
        return replyService.queryReplyPage(query, true);
    }

    @ApiOperation("管理端根据id查询二级评论")
    @GetMapping("{id}")
    public CommentReplyVO queryReplyById(@PathVariable("id") Long id) {
        return replyService.queryReplyById(id, true);
    }

    @ApiOperation("隐藏或显示二级评论")
    @PutMapping("/{id}/hidden/{hidden}")
    public void hiddenReply(
            @ApiParam(value = "二级评论id", example = "1") @PathVariable("id") Long id,
            @ApiParam(value = "是否隐藏，true/false", example = "true") @PathVariable("hidden") Boolean hidden) {
        replyService.hiddenReply(id, hidden);
    }
}
