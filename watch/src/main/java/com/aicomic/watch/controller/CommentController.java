package com.aicomic.watch.controller;

import com.aicomic.watch.domain.dto.CommentFormDTO;
import com.aicomic.watch.domain.dto.CommentUpdateDTO;
import com.aicomic.watch.domain.query.CommentPageQuery;
import com.aicomic.watch.domain.vo.CommentVO;
import com.aicomic.watch.service.ICommentService;
import com.aicomic.common.domain.dto.PageDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * <p>
 * 一级评论 前端控制器
 * </p>
 */
@RestController
@RequestMapping("/comments")
@Api(tags = "一级评论相关接口")
@RequiredArgsConstructor
public class CommentController {

    private final ICommentService commentService;

    @ApiOperation("发表一级评论")
    @PostMapping
    public void saveComment(@Valid @RequestBody CommentFormDTO commentDTO) {
        commentService.saveComment(commentDTO);
    }

    @ApiOperation("修改自己的一级评论")
    @PutMapping("/{id}")
    public void updateComment(
            @ApiParam("要修改的评论id") @PathVariable("id") Long id,
            @Valid @RequestBody CommentUpdateDTO commentDTO) {
        commentService.updateComment(id, commentDTO);
    }

    @ApiOperation("分页查询一级评论")
    @GetMapping("page")
    public PageDTO<CommentVO> queryCommentPage(CommentPageQuery query) {
        return commentService.queryCommentPage(query);
    }

    @ApiOperation("根据id查询一级评论")
    @GetMapping("{id}")
    public CommentVO queryCommentById(@PathVariable("id") Long id) {
        return commentService.queryCommentById(id);
    }

    @ApiOperation("删除自己的一级评论及其二级评论")
    @DeleteMapping("/{id}")
    public void deleteComment(
            @ApiParam(value = "评论id", example = "1") @PathVariable("id") Long id) {
        commentService.deleteComment(id);
    }
}
