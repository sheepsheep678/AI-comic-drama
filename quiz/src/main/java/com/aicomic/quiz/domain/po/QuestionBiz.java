package com.aicomic.quiz.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 问题和业务关联表，例如把片段id和问题id关联，一个片段下可以有多个问题
 * </p>
 *
 * @since 2022-09-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("quiz_question_biz")
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class QuestionBiz implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 业务id，要关联问题的某业务id，例如片段id
     */
    private Long bizId;

    /**
     * 问题id
     */
    private Long questionId;


}
