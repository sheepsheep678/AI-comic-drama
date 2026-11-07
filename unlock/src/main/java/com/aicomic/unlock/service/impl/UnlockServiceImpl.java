package com.aicomic.unlock.service.impl;

import com.aicomic.unlock.domain.po.UnlockActivity;
import com.aicomic.unlock.mapper.UnlockMapper;
import com.aicomic.unlock.service.IUnlockService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 解锁活动，形式多种多样，例如：优惠券 服务实现类
 * </p>
 *
 * @since 2022-09-06
 */
@Service
public class UnlockServiceImpl extends ServiceImpl<UnlockMapper, UnlockActivity> implements IUnlockService {

}
