package com.aicomic.watch.mapper;

import com.aicomic.watch.domain.dto.HotReplyCandidate;
import com.aicomic.watch.domain.po.CommentReply;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 二级评论 Mapper 接口
 * </p>
 */
public interface CommentReplyMapper extends BaseMapper<CommentReply> {

    /**
     * 查询一级评论下参与热度排名的二级评论候选；每个一级评论最多返回 limitPerComment 条
     */
    List<HotReplyCandidate> selectHotCandidates(
            @Param("commentIds") Collection<Long> commentIds,
            @Param("limitPerComment") int limitPerComment);
}
