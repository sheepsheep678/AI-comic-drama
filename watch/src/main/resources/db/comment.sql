-- 观看互动中心（aicomic_watch 库）评论建表脚本，手工执行
-- 一级评论 + 严格两级评论模型：comment -> comment_reply，不再有三级

CREATE TABLE IF NOT EXISTS `comment` (
  `id`              bigint        NOT NULL COMMENT '一级评论id',
  `content`         varchar(1000) NOT NULL COMMENT '评论正文',
  `course_id`       bigint        NOT NULL COMMENT '漫剧id',
  `chapter_id`      bigint        NOT NULL COMMENT '章id',
  `section_id`      bigint        NOT NULL COMMENT '片段id',
  `user_id`         bigint        NOT NULL COMMENT '评论用户id',
  `latest_reply_id` bigint                 DEFAULT NULL COMMENT '最新二级评论id',
  `top_reply_ids`   varchar(128)           DEFAULT NULL COMMENT '点赞最高的前4个二级评论id，逗号分隔；NULL-未计算，空串-已计算但无可见回复',
  `reply_times`     int           NOT NULL DEFAULT 0 COMMENT '二级评论数量（历史总数，隐藏不扣减）',
  `liked_times`     int           NOT NULL DEFAULT 0 COMMENT '一级评论点赞数',
  `anonymity`       tinyint(1)    NOT NULL DEFAULT 0 COMMENT '是否匿名：0-否，1-是',
  `hidden`          tinyint(1)    NOT NULL DEFAULT 0 COMMENT '是否隐藏：0-否，1-是',
  `status`          tinyint       NOT NULL DEFAULT 0 COMMENT '管理端状态：0-未查看，1-已查看',
  `create_time`     datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`     datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_course_section_time` (`course_id`, `section_id`, `create_time`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='一级评论表';

CREATE TABLE IF NOT EXISTS `comment_reply` (
  `id`              bigint        NOT NULL COMMENT '二级评论id',
  `comment_id`      bigint        NOT NULL COMMENT '所属一级评论id',
  `user_id`         bigint        NOT NULL COMMENT '评论用户id',
  `content`         varchar(1000) NOT NULL COMMENT '评论正文',
  `target_user_id`  bigint                 DEFAULT NULL COMMENT '被@的用户id',
  `target_reply_id` bigint                 DEFAULT NULL COMMENT '被回复的二级评论id；NULL表示直接回复一级评论',
  `liked_times`     int           NOT NULL DEFAULT 0 COMMENT '点赞数',
  `anonymity`       tinyint(1)    NOT NULL DEFAULT 0 COMMENT '是否匿名：0-否，1-是',
  `hidden`          tinyint(1)    NOT NULL DEFAULT 0 COMMENT '是否隐藏：0-否，1-是',
  `create_time`     datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`     datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_comment_time` (`comment_id`, `create_time`),
  KEY `idx_comment_liked` (`comment_id`, `liked_times`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='二级评论表';
