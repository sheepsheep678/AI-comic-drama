package com.aicomic.common.constants;

/**
 * RocketMQ 常量定义
 * <p>
 * 原 RabbitMQ 中的交换机（Exchange）对应 RocketMQ 的 Topic，
 * 路由键（RoutingKey）对应 RocketMQ 的 Tag。
 * 名称只能包含字母、数字、下划线和中划线，因此统一用下划线分隔。
 */
public interface MqConstants {
    /**
     * RocketMQ Topic，对应原来的 Exchange
     */
    interface Topic {
        /*漫剧有关的Topic*/
        String COMIC_TOPIC = "comic_topic";

        /*订单有关的Topic*/
        String ORDER_TOPIC = "order_topic";

        /*观看有关的Topic*/
        String WATCH_TOPIC = "watch_topic";

        /*信息中心短信相关的Topic*/
        String SMS_TOPIC = "sms_topic";

        /*异常信息的Topic*/
        String ERROR_TOPIC = "error_topic";

        /*支付有关的Topic*/
        String PAY_TOPIC = "pay_topic";
        /*交易服务延迟任务Topic*/
        String ORDER_DELAY_TOPIC = "order_delay_topic";

        /*点赞记录有关的Topic*/
        String LIKE_RECORD_TOPIC = "like_record_topic";

        /*优惠解锁有关的Topic*/
        String UNLOCK_TOPIC = "unlock_topic";
    }

    /**
     * RocketMQ Tag，对应原来的 RoutingKey
     */
    interface Tag {
        /*漫剧有关的Tag*/
        String COMIC_NEW_TAG = "comic_new";
        String COMIC_UP_TAG = "comic_up";
        String COMIC_DOWN_TAG = "comic_down";
        String COMIC_EXPIRE_TAG = "comic_expire";
        String COMIC_DELETE_TAG = "comic_delete";

        /*订单有关的Tag*/
        String ORDER_PAY_TAG = "order_pay";
        String ORDER_REFUND_TAG = "order_refund";

        /*积分相关Tag*/
        /* 写评论 */
        String WRITE_REPLY_TAG = "reply_new";
        /* 签到 */
        String SIGN_IN_TAG = "sign_in";
        /* 观看视频 */
        String WATCH_CLIP_TAG = "clip_learned";
        /* 写笔记 */
        String WRITE_NOTE_TAG = "note_new";
        /* 笔记被采集 */
        String NOTE_GATHERED_TAG = "note_gathered";

        /*点赞的Tag*/
        String LIKE_TIMES_TAG_TEMPLATE = "{}_times_changed";
        /*一级评论*/
        String COMMENT_LIKE_TIMES_TAG = "COMMENT_times_changed";
        /*二级评论*/
        String COMMENT_REPLY_LIKE_TIMES_TAG = "COMMENT_REPLY_times_changed";
        /*笔记*/
        String NOTE_LIKE_TIMES_TAG = "NOTE_times_changed";

        /*短信系统发送短信*/
        String SMS_MESSAGE_TAG = "sms_message";

        /*支付有关的Tag*/
        String PAY_SUCCESS_TAG = "pay_success";
        String REFUND_CHANGE_TAG = "refund_status_change";

        String ORDER_DELAY_TAG = "delay_order_query";

        /*领取优惠券的Tag*/
        String COUPON_RECEIVE_TAG = "coupon_receive";
    }

    /**
     * RocketMQ 消费者组，每个监听逻辑使用独立消费者组，保证互不影响
     */
    interface ConsumerGroup {
        /*观看服务：积分变更*/
        String WATCH_POINTS_GROUP = "watch_points_consumer_group";
        /*评论服务：评论点赞数变更*/
        String COMMENT_LIKE_TIMES_CHANGE_GROUP = "comment_like_times_change_consumer_group";
        /*观看服务：订单支付/退款*/
        String WATCH_ORDER_CHANGE_GROUP = "watch_order_change_consumer_group";

        /*订单服务：支付/退款结果*/
        String ORDER_PAY_RESULT_GROUP = "order_pay_result_consumer_group";
        /*订单服务：订单延迟查询*/
        String ORDER_DELAY_QUERY_GROUP = "order_delay_query_consumer_group";

        /*消息服务：短信*/
        String SMS_MESSAGE_GROUP = "sms_message_consumer_group";

        /*优惠券服务：领取优惠券*/
        String COUPON_RECEIVE_GROUP = "coupon_receive_consumer_group";

        /*搜索服务：订单*/
        String SEARCH_ORDER_GROUP = "search_order_consumer_group";
        /*搜索服务：漫剧*/
        String SEARCH_COMIC_GROUP = "search_comic_consumer_group";
    }
}
