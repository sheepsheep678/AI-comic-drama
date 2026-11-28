package com.aicomic.watch.service.impl;

import com.aicomic.api.client.user.UserClient;
import com.aicomic.api.dto.user.UserDTO;
import com.aicomic.watch.domain.dto.CommentReplyFormDTO;
import com.aicomic.watch.domain.po.Comment;
import com.aicomic.watch.domain.po.CommentReply;
import com.aicomic.watch.domain.query.CommentReplyPageQuery;
import com.aicomic.watch.domain.vo.CommentReplyVO;
import com.aicomic.watch.enums.CommentStatus;
import com.aicomic.watch.mapper.CommentReplyMapper;
import com.aicomic.watch.service.ICommentReplyService;
import com.aicomic.watch.service.ICommentService;
import com.aicomic.watch.service.IHotReplyService;
import com.aicomic.api.client.liked.LikeClient;
import com.aicomic.common.autoconfigure.mq.RocketMqHelper;
import com.aicomic.common.constants.MqConstants;
import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.common.exceptions.BadRequestException;
import com.aicomic.common.exceptions.UnauthorizedException;
import com.aicomic.common.utils.BeanUtils;
import com.aicomic.common.utils.CollUtils;
import com.aicomic.common.utils.UserContext;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.aicomic.common.constants.Constant.DATA_FIELD_NAME_CREATE_TIME;
import static com.aicomic.common.constants.Constant.DATA_FIELD_NAME_LIKE_TIME;

/**
 * <p>
 * 二级评论服务实现类
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentReplyServiceImpl extends ServiceImpl<CommentReplyMapper, CommentReply> implements ICommentReplyService {

    private final ICommentService commentService;
    private final UserClient userClient;
    private final LikeClient likeClient;
    private final RocketMqHelper mqHelper;
    private final IHotReplyService hotReplyService;

    @Override
    @Transactional
    public void saveReply(CommentReplyFormDTO replyDTO) {
        // 1.获取登录用户
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new UnauthorizedException("未登录用户无法评论");
        }
        // 2.校验一级评论存在且未被隐藏
        Comment comment = commentService.getById(replyDTO.getCommentId());
        if (comment == null || Boolean.TRUE.equals(comment.getHidden())) {
            throw new BadRequestException("评论不存在或已隐藏");
        }
        // 3.严格两级：被回复的二级评论必须属于同一条一级评论
        Long targetUserId = null;
        if (replyDTO.getTargetReplyId() != null) {
            CommentReply target = getById(replyDTO.getTargetReplyId());
            if (target == null
                    || !replyDTO.getCommentId().equals(target.getCommentId())
                    || Boolean.TRUE.equals(target.getHidden())) {
                throw new BadRequestException("被回复的评论不存在或不可回复");
            }
            targetUserId = target.getUserId();
        }
        // 4.保存二级评论
        CommentReply reply = BeanUtils.toBean(replyDTO, CommentReply.class);
        reply.setId(null);
        reply.setUserId(userId);
        reply.setTargetUserId(targetUserId);
        reply.setLikedTimes(0);
        reply.setHidden(false);
        save(reply);
        // 5.原子累加二级评论数量，并更新最新回复
        boolean isViewer = Boolean.TRUE.equals(replyDTO.getIsViewer());
        commentService.lambdaUpdate()
                .setSql("reply_times = reply_times + 1")
                .set(Comment::getLatestReplyId, reply.getId())
                .set(isViewer, Comment::getStatus, CommentStatus.UN_CHECK.getValue())
                .eq(Comment::getId, replyDTO.getCommentId())
                .update();
        // 6.事务提交后再失效热门缓存、打脏标记并发送积分消息，避免回滚造成脏数据/误加积分
        Long commentId = replyDTO.getCommentId();
        runAfterCommit(() -> {
            hotReplyService.invalidate(Collections.singleton(commentId));
            hotReplyService.markDirty(Collections.singleton(commentId));
            if (isViewer) {
                try {
                    mqHelper.send(MqConstants.Topic.WATCH_TOPIC,
                            MqConstants.Tag.WRITE_REPLY_TAG, userId);
                } catch (Exception e) {
                    log.warn("发送评论积分消息失败，userId：{}", userId, e);
                }
            }
        });
    }

    @Override
    public PageDTO<CommentReplyVO> queryReplyPage(CommentReplyPageQuery query, boolean forAdmin) {
        // 1.一级评论id不能为空
        Long commentId = query.getCommentId();
        if (commentId == null) {
            throw new BadRequestException("一级评论id不能为空");
        }
        // 2.普通用户不能查看已隐藏一级评论下的二级评论
        if (!forAdmin) {
            Comment parent = commentService.getById(commentId);
            if (parent == null || Boolean.TRUE.equals(parent.getHidden())) {
                return PageDTO.empty(0L, 0L);
            }
        }
        // 3.分页查询二级评论，按点赞数倒序，点赞数相同按创建时间升序
        Page<CommentReply> page = lambdaQuery()
                .eq(CommentReply::getCommentId, commentId)
                .eq(!forAdmin, CommentReply::getHidden, false)
                .page(query.toMpPage(
                        new OrderItem(DATA_FIELD_NAME_LIKE_TIME, false),
                        new OrderItem(DATA_FIELD_NAME_CREATE_TIME, true)
                ));
        List<CommentReply> records = page.getRecords();
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(page);
        }
        // 3.批量查询评论者、目标评论、目标用户信息与点赞状态
        Set<Long> targetReplyIds = records.stream()
                .map(CommentReply::getTargetReplyId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, CommentReply> targetMap = loadReplies(targetReplyIds);
        Set<Long> userIds = collectUserIds(records, targetMap, forAdmin);
        Map<Long, UserDTO> userMap = queryUserMap(userIds);
        Set<Long> replyIds = records.stream().map(CommentReply::getId).collect(Collectors.toSet());
        Set<Long> likedIds = likeClient.isBizLiked(replyIds);
        // 4.处理VO
        List<CommentReplyVO> list = new ArrayList<>(records.size());
        for (CommentReply reply : records) {
            CommentReplyVO vo = BeanUtils.toBean(reply, CommentReplyVO.class);
            list.add(vo);
            fillReplyUser(vo, reply, userMap, forAdmin);
            fillTargetUser(vo, reply, targetMap, userMap, forAdmin);
            vo.setLiked(likedIds.contains(reply.getId()));
        }
        return new PageDTO<>(page.getTotal(), page.getPages(), list);
    }

    @Override
    public CommentReplyVO queryReplyById(Long id, boolean forAdmin) {
        // 1.根据id查询
        CommentReply reply = getById(id);
        if (reply == null || (!forAdmin && Boolean.TRUE.equals(reply.getHidden()))) {
            return null;
        }
        // 2.查询目标评论与用户信息
        Map<Long, CommentReply> targetMap = reply.getTargetReplyId() == null
                ? Collections.emptyMap()
                : loadReplies(Collections.singleton(reply.getTargetReplyId()));
        Map<Long, UserDTO> userMap = queryUserMap(
                collectUserIds(Collections.singletonList(reply), targetMap, forAdmin));
        // 3.查询当前用户点赞状态
        Set<Long> likedIds = likeClient.isBizLiked(Collections.singletonList(id));
        // 4.处理VO
        CommentReplyVO vo = BeanUtils.toBean(reply, CommentReplyVO.class);
        fillReplyUser(vo, reply, userMap, forAdmin);
        fillTargetUser(vo, reply, targetMap, userMap, forAdmin);
        vo.setLiked(likedIds.contains(id));
        return vo;
    }

    @Override
    public void hiddenReply(Long id, Boolean hidden) {
        // 1.查询
        CommentReply old = getById(id);
        if (old == null) {
            return;
        }
        // 2.隐藏二级评论（按约定不扣减一级评论的 reply_times）
        CommentReply reply = new CommentReply();
        reply.setId(id);
        reply.setHidden(hidden);
        updateById(reply);
        // 3.可见性变化影响热门 Top4，立即失效并等待任务重算
        hotReplyService.invalidate(Collections.singleton(old.getCommentId()));
        hotReplyService.markDirty(Collections.singleton(old.getCommentId()));
    }

    /**
     * 查询目标二级评论
     */
    private Map<Long, CommentReply> loadReplies(Collection<Long> replyIds) {
        if (CollUtils.isEmpty(replyIds)) {
            return Collections.emptyMap();
        }
        List<CommentReply> replies = listByIds(replyIds);
        if (CollUtils.isEmpty(replies)) {
            return Collections.emptyMap();
        }
        return replies.stream().collect(Collectors.toMap(CommentReply::getId, Function.identity()));
    }

    /**
     * 收集评论者与目标用户id；普通用户不收集匿名用户
     */
    private Set<Long> collectUserIds(List<CommentReply> replies,
                                     Map<Long, CommentReply> targetMap,
                                     boolean forAdmin) {
        Set<Long> userIds = new HashSet<>();
        for (CommentReply reply : replies) {
            if (forAdmin || !Boolean.TRUE.equals(reply.getAnonymity())) {
                userIds.add(reply.getUserId());
            }
            if (reply.getTargetReplyId() != null) {
                CommentReply target = targetMap.get(reply.getTargetReplyId());
                if (target != null) {
                    if (forAdmin || (!Boolean.TRUE.equals(target.getAnonymity())
                            && !Boolean.TRUE.equals(target.getHidden()))) {
                        userIds.add(target.getUserId());
                    }
                } else if (forAdmin) {
                    userIds.add(reply.getTargetUserId());
                }
            }
        }
        userIds.remove(null);
        return userIds;
    }

    private Map<Long, UserDTO> queryUserMap(Collection<Long> userIds) {
        if (CollUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        List<UserDTO> users = userClient.queryUserByIds(userIds);
        if (CollUtils.isEmpty(users)) {
            return Collections.emptyMap();
        }
        return users.stream().collect(Collectors.toMap(UserDTO::getId, u -> u));
    }

    /**
     * 填充评论者信息；匿名评论对普通用户不暴露身份
     */
    private void fillReplyUser(CommentReplyVO vo, CommentReply reply,
                               Map<Long, UserDTO> userMap, boolean forAdmin) {
        if (!forAdmin && Boolean.TRUE.equals(reply.getAnonymity())) {
            vo.setUserId(null);
            vo.setUserName(null);
            vo.setUserIcon(null);
            vo.setUserType(null);
            return;
        }
        UserDTO user = userMap.get(reply.getUserId());
        if (user != null) {
            vo.setUserIcon(user.getIcon());
            vo.setUserName(user.getName());
            vo.setUserType(user.getType());
        }
    }

    /**
     * 填充被@目标用户信息；匿名目标对普通用户不暴露身份
     */
    private void fillTargetUser(CommentReplyVO vo, CommentReply reply,
                                Map<Long, CommentReply> targetMap,
                                Map<Long, UserDTO> userMap, boolean forAdmin) {
        if (reply.getTargetReplyId() == null) {
            return;
        }
        CommentReply target = targetMap.get(reply.getTargetReplyId());
        if (target == null || (!forAdmin && (Boolean.TRUE.equals(target.getAnonymity())
                || Boolean.TRUE.equals(target.getHidden())))) {
            vo.setTargetUserId(null);
            vo.setTargetUserName(null);
            return;
        }
        vo.setTargetUserId(target.getUserId());
        UserDTO targetUser = userMap.get(target.getUserId());
        if (targetUser != null) {
            vo.setTargetUserName(targetUser.getName());
        }
    }

    /**
     * 事务提交后再执行缓存失效和消息发送，避免事务回滚导致脏数据
     */
    private void runAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            task.run();
        }
    }
}
