package com.aicomic.watch.mapper;

import com.aicomic.watch.domain.po.InteractionQuestion;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.Collection;

/**
 * <p>
 * 互动提问的问题表 Mapper 接口
 * </p>
 *
 */
public interface InteractionQuestionMapper extends BaseMapper<InteractionQuestion> {

    /**
     * 将 Top4 回答字段置为 NULL，标记为待重算
     */
    @Update("<script>" +
            "UPDATE interaction_question SET top_reply_ids = NULL WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    int invalidateHotReplyIds(@Param("ids") Collection<Long> ids);
}
