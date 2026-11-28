package com.aicomic.watch.mapper;

import com.aicomic.watch.domain.po.Comment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.Collection;

/**
 * <p>
 * 一级评论 Mapper 接口
 * </p>
 */
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 将热门二级评论字段置为 NULL，标记为待重算
     */
    @Update("<script>" +
            "UPDATE `comment` SET top_reply_ids = NULL WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    int invalidateHotReplyIds(@Param("ids") Collection<Long> ids);
}
