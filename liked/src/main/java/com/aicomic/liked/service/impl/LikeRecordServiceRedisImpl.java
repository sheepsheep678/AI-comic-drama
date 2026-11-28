package com.aicomic.liked.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aicomic.api.dto.comment.LikeTimesDTO;
import com.aicomic.common.autoconfigure.mq.RocketMqHelper;
import com.aicomic.common.constants.LikeConstants;
import com.aicomic.common.exceptions.BadRequestException;
import com.aicomic.common.exceptions.UnauthorizedException;
import com.aicomic.common.utils.CollUtils;
import com.aicomic.common.utils.StringUtils;
import com.aicomic.common.utils.UserContext;
import com.aicomic.liked.constants.RedisConstants;
import com.aicomic.liked.domain.dto.LikeRecordFormDTO;
import com.aicomic.liked.domain.po.LikeRecord;
import com.aicomic.liked.mapper.LikeRecordMapper;
import com.aicomic.liked.service.ILikeRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.aicomic.common.constants.MqConstants.Topic.LIKE_RECORD_TOPIC;
import static com.aicomic.common.constants.MqConstants.Tag.LIKE_TIMES_TAG_TEMPLATE;

/**
 * <p>
 * 点赞记录表 服务实现类
 * </p>
 *
 */
@Service
@RequiredArgsConstructor
public class LikeRecordServiceRedisImpl extends ServiceImpl<LikeRecordMapper, LikeRecord> implements ILikeRecordService {

    private final RocketMqHelper mqHelper;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void addLikeRecord(LikeRecordFormDTO recordDTO) {
        // 1.校验业务类型，避免非法类型写入无人消费的 ZSet
        String bizType = recordDTO.getBizType();
        if (!LikeConstants.BIZ_TYPE_COMMENT.equals(bizType)
                && !LikeConstants.BIZ_TYPE_COMMENT_REPLY.equals(bizType)
                && !LikeConstants.BIZ_TYPE_NOTE.equals(bizType)) {
            throw new BadRequestException("不支持的点赞业务类型：" + bizType);
        }
        // 2.基于前端的参数，判断是执行点赞还是取消点赞
        boolean success = recordDTO.getLiked() ? like(recordDTO) : unlike(recordDTO);
        // 2.判断是否执行成功，如果失败，则直接结束
        if (!success) {
            return;
        }
        // 3.如果执行成功，统计点赞总数
        Long likedTimes = redisTemplate.opsForSet()
                .size(RedisConstants.LIKES_BIZ_KEY_PREFIX + recordDTO.getBizId());
        if (likedTimes == null) {
            return;
        }
        // 4.缓存点总数到Redis
        redisTemplate.opsForZSet().add(
                RedisConstants.LIKES_TIMES_KEY_PREFIX + recordDTO.getBizType(),
                recordDTO.getBizId().toString(),
                likedTimes
        );
    }

    @Override
    public Set<Long> isBizLiked(List<Long> bizIds) {
        // 1.获取登录用户id
        Long userId = UserContext.getUser();
        // 2.游客没有点赞状态，直接返回空集合
        if (userId == null || CollUtils.isEmpty(bizIds)) {
            return Collections.emptySet();
        }
        // 3.查询点赞状态
        List<Object> objects = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            StringRedisConnection src = (StringRedisConnection) connection;
            for (Long bizId : bizIds) {
                String key = RedisConstants.LIKES_BIZ_KEY_PREFIX + bizId;
                src.sIsMember(key, userId.toString());
            }
            return null;
        });
        // 4.返回结果
        return IntStream.range(0, objects.size())
                .filter(i -> (boolean) objects.get(i))
                .mapToObj(bizIds::get)
                .collect(Collectors.toSet());
    }

    @Override
    public void readLikedTimesAndSendMessage(String bizType, int maxBizSize) {
        // 1.读取并移除Redis中缓存的点赞总数
        String key = RedisConstants.LIKES_TIMES_KEY_PREFIX + bizType;
        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet().popMin(key, maxBizSize);
        if (CollUtils.isEmpty(tuples)) {
            return;
        }
        // 2.数据转换
        List<LikeTimesDTO> list = new ArrayList<>(tuples.size());
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            String bizId = tuple.getValue();
            Double likedTimes = tuple.getScore();
            if (bizId == null || likedTimes == null) {
                continue;
            }
            list.add(LikeTimesDTO.of(Long.valueOf(bizId), likedTimes.intValue()));
        }
        // 3.发送MQ消息
        mqHelper.send(
                LIKE_RECORD_TOPIC,
                StringUtils.format(LIKE_TIMES_TAG_TEMPLATE, bizType),
                list);
    }

    private boolean unlike(LikeRecordFormDTO recordDTO) {
        // 1.获取用户id
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new UnauthorizedException("未登录用户无法取消点赞");
        }
        // 2.获取Key
        String key = RedisConstants.LIKES_BIZ_KEY_PREFIX + recordDTO.getBizId();
        // 3.执行SREM命令
        Long result = redisTemplate.opsForSet().remove(key, userId.toString());
        return result != null && result > 0;
    }

    private boolean like(LikeRecordFormDTO recordDTO) {
        // 1.获取用户id
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new UnauthorizedException("未登录用户无法点赞");
        }
        // 2.获取Key
        String key = RedisConstants.LIKES_BIZ_KEY_PREFIX + recordDTO.getBizId();
        // 3.执行SADD命令
        Long result = redisTemplate.opsForSet().add(key, userId.toString());
        return result != null && result > 0;
    }
}
