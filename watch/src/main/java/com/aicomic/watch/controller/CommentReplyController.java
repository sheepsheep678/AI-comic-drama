package com.aicomic.watch.controller;

import com.aicomic.watch.domain.dto.CommentReplyFormDTO;
import com.aicomic.watch.domain.query.CommentReplyPageQuery;
import com.aicomic.watch.domain.vo.CommentReplyVO;
import com.aicomic.watch.service.ICommentReplyService;
import com.aicomic.common.domain.dto.PageDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * <p>
 * 二级评论 前端控制器
 * </p>
 */
@RestController
@RequestMapping("/replies")
@Api(tags = "二级评论相关接口")
@RequiredArgsConstructor
public class CommentReplyController {

    private final ICommentReplyService replyService;

    @ApiOperation("发表二级评论")
    @PostMapping
    public void saveReply(@Valid @RequestBody CommentReplyFormDTO replyDTO) {
        replyService.saveReply(replyDTO);
    }

    @ApiOperation("分页查询一级评论下的二级评论")
    @GetMapping("page")
    public PageDTO<CommentReplyVO> queryReplyPage(@Valid CommentReplyPageQuery query) {
        return replyService.queryReplyPage(query, false);
    }
}
