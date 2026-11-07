package com.aicomic.common.constants;

public interface MqConstants {
    interface Exchange{
        /*漫剧有关的交换机*/
        String COMIC_EXCHANGE = "comic.topic";

        /*订单有关的交换机*/
        String ORDER_EXCHANGE = "order.topic";

        /*观看有关的交换机*/
        String WATCH_EXCHANGE = "watch.topic";

        /*信息中心短信相关的交换机*/
        String SMS_EXCHANGE = "sms.direct";

        /*异常信息的交换机*/
        String ERROR_EXCHANGE = "error.topic";

        /*支付有关的交换机*/
        String PAY_EXCHANGE = "pay.topic";
        /*交易服务延迟任务交换机*/
        String ORDER_DELAY_EXCHANGE = "order.delay.topic";

         /*点赞记录有关的交换机*/
        String LIKE_RECORD_EXCHANGE = "like.record.topic";

        /*优惠解锁有关的交换机*/
        String UNLOCK_EXCHANGE = "unlock.topic";
    }
    interface Queue {
        String ERROR_QUEUE_TEMPLATE = "error.{}.queue";
    }
    interface Key{
        /*漫剧有关的 RoutingKey*/
        String COMIC_NEW_KEY = "comic.new";
        String COMIC_UP_KEY = "comic.up";
        String COMIC_DOWN_KEY = "comic.down";
        String COMIC_EXPIRE_KEY = "comic.expire";
        String COMIC_DELETE_KEY = "comic.delete";

        /*订单有关的RoutingKey*/
        String ORDER_PAY_KEY = "order.pay";
        String ORDER_REFUND_KEY = "order.refund";

        /*积分相关RoutingKey*/
        /* 写回答 */
        String WRITE_REPLY = "reply.new";
        /* 签到 */
        String SIGN_IN = "sign.in";
        /* 观看视频 */
        String WATCH_CLIP = "clip.learned";
        /* 写笔记 */
        String WRITE_NOTE = "note.new";
        /* 笔记被采集 */
        String NOTE_GATHERED = "note.gathered";

        /*点赞的RoutingKey*/
        String LIKE_TIMES_KEY_TEMPLATE = "{}.times.changed";
        /*问答*/
        String QA_LIKE_TIMES_KEY = "QA.times.changed";
        /*笔记*/
        String NOTE_LIKE_TIMES_KEY = "NOTE.times.changed";

        /*短信系统发送短信*/
        String SMS_MESSAGE = "sms.message";

        /*异常RoutingKey的前缀*/
        String ERROR_KEY_PREFIX = "error.";
        String DEFAULT_ERROR_KEY = "error.#";

        /*支付有关的key*/
        String PAY_SUCCESS = "pay.success";
        String REFUND_CHANGE = "refund.status.change";

        String ORDER_DELAY_KEY = "delay.order.query";

        /*领取优惠券的key*/
        String COUPON_RECEIVE = "coupon.receive";
    }
}
