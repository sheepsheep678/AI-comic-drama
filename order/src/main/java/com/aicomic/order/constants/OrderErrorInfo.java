package com.aicomic.order.constants;

public interface OrderErrorInfo {

    String CARTS_FULL = "用户购物车漫剧不能超过{}";
    String COMIC_NOT_EXISTS = "漫剧不存在";
    String COMIC_EXPIRED = "漫剧已过期";
    String COMIC_NOT_FOR_SALE = "漫剧无法购买";
    String COMIC_NOT_FREE = "漫剧不是免费漫剧";

    String PLACE_ORDER_FAILED = "下单失败";
    String ORDER_NOT_EXISTS = "订单不存在";
    String ORDER_ALREADY_FINISH = "订单已经支付或退款";
    String ORDER_OVER_TIME = "订单已经超时";
    String ORDER_CANNOT_REFUND = "订单未支付或已关闭";

    String NO_AUTH_REFUND = "无权申请退款";
    String REFUND_TOO_MANY_TIMES = "退款次数太多";
    String REFUND_IN_PROGRESS = "有其它未完成的退款进程";
    String REFUND_NOT_EXISTS = "退款记录不存在";
    String REFUND_APPROVED = "退款申请已经处理过了";
    String FREE_COMIC_CANNOT_REFUND = "免费漫剧不能退款";

}
