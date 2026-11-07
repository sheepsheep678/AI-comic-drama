package com.aicomic.pay.service.impl;

import com.aicomic.common.utils.BeanUtils;
import com.aicomic.pay.sdk.dto.PayChannelDTO;
import com.aicomic.pay.domain.po.PayChannel;
import com.aicomic.pay.mapper.PayChannelMapper;
import com.aicomic.pay.service.IPayChannelService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 支付渠道 服务实现类
 * </p>
 *
 * @since 2022-08-26
 */
@Service
public class PayChannelServiceImpl extends ServiceImpl<PayChannelMapper, PayChannel> implements IPayChannelService {

    @Override
    public Long addPayChannel(PayChannelDTO channelDTO) {
        // 1.属性转换
        PayChannel payChannel = BeanUtils.toBean(channelDTO, PayChannel.class);
        // 2.保存
        save(payChannel);
        return payChannel.getId();
    }

    @Override
    public void updatePayChannel(PayChannelDTO channelDTO) {
        // 1.属性转换
        PayChannel payChannel = BeanUtils.toBean(channelDTO, PayChannel.class);
        // 2.保存
        updateById(payChannel);
    }
}
