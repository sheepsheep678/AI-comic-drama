package com.aicomic.watch.mapper;

import com.aicomic.watch.domain.dto.HotReplyCandidate;
import com.aicomic.watch.domain.po.InteractionReply;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 互动问题的回答或评论 Mapper 接口
 * </p>
 *
 */
public interface InteractionReplyMapper extends BaseMapper<InteractionReply> {

    /**
     * 查询问题下参与热度排名的顶层回答候选；每个问题最多返回 limitPerQuestion 条
     */
    List<HotReplyCandidate> selectHotCandidates(
            @Param("questionIds") Collection<Long> questionIds,
            @Param("limitPerQuestion") int limitPerQuestion);
}
