-- 点赞中心（aicomic_like 库）建表脚本，手工执行
CREATE TABLE IF NOT EXISTS `like_record` (
  `id`          bigint      NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id`     bigint      NOT NULL COMMENT '用户id',
  `biz_id`      bigint      NOT NULL COMMENT '点赞业务id',
  `biz_type`    varchar(32) NOT NULL COMMENT '点赞业务类型：COMMENT-一级评论，COMMENT_REPLY-二级评论，NOTE-笔记',
  `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_biz` (`user_id`, `biz_type`, `biz_id`),
  KEY `idx_biz_type_biz` (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='点赞记录表';
