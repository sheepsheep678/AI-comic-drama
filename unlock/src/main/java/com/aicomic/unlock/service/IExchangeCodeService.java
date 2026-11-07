package com.aicomic.unlock.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.unlock.domain.po.Coupon;
import com.aicomic.unlock.domain.po.ExchangeCode;
import com.aicomic.unlock.domain.query.CodeQuery;
import com.aicomic.unlock.domain.vo.ExchangeCodeVO;

/**
 * <p>
 * 兑换码 服务类
 * </p>
 *
 */
public interface IExchangeCodeService extends IService<ExchangeCode> {
    void asyncGenerateCode(Coupon coupon);

    boolean updateExchangeMark(long serialNum, boolean mark);

    PageDTO<ExchangeCodeVO> queryCodePage(CodeQuery query);

    Long exchangeTargetId(long serialNum);
}
