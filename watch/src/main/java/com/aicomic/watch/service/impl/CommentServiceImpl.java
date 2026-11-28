package com.aicomic.watch.service.impl;

import com.aicomic.api.cache.CategoryCache;
import com.aicomic.api.client.comic.ComicClient;
import com.aicomic.api.client.comic.EpisodeClient;
import com.aicomic.api.client.search.SearchClient;
import com.aicomic.api.client.user.UserClient;
import com.aicomic.api.dto.comic.ComicFullInfoDTO;
import com.aicomic.api.dto.comic.ComicSimpleInfoDTO;
import com.aicomic.api.dto.comic.EpisodeSimpleInfoDTO;
import com.aicomic.api.dto.user.UserDTO;
import com.aicomic.watch.constants.HotReplyConstants;
import com.aicomic.watch.domain.dto.CommentFormDTO;
import com.aicomic.watch.domain.dto.CommentUpdateDTO;
import com.aicomic.watch.domain.dto.HotReplyCandidate;
import com.aicomic.watch.domain.po.Comment;
import com.aicomic.watch.domain.po.CommentReply;
import com.aicomic.watch.domain.query.CommentAdminPageQuery;
import com.aicomic.watch.domain.query.CommentPageQuery;
import com.aicomic.watch.domain.vo.CommentAdminVO;
import com.aicomic.watch.domain.vo.CommentVO;
import com.aicomic.watch.domain.vo.HotReplyVO;
import com.aicomic.watch.enums.CommentStatus;
import com.aicomic.watch.mapper.CommentMapper;
import com.aicomic.watch.mapper.CommentReplyMapper;
import com.aicomic.watch.service.ICommentService;
import com.aicomic.watch.service.IHotReplyService;
import com.aicomic.api.client.liked.LikeClient;
import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.common.exceptions.BadRequestException;
import com.aicomic.common.exceptions.UnauthorizedException;
import com.aicomic.common.utils.BeanUtils;
import com.aicomic.common.utils.CollUtils;
import com.aicomic.common.utils.StringUtils;
import com.aicomic.common.utils.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 一级评论服务实现类
 * </p>
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    private final IHotReplyService hotReplyService;
    private final LikeClient likeClient;
    private final CommentReplyMapper replyMapper;
    private final UserClient userClient;
    private final ComicClient comicClient;
    private final SearchClient searchClient;
    private final EpisodeClient episodeClient;
    private final CategoryCache categoryCache;

    @Override
    public void saveComment(CommentFormDTO commentDTO) {
        // 1.获取当前登录的用户id
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new UnauthorizedException("未登录用户无法评论");
        }
        // 2.数据封装
        Comment comment = BeanUtils.copyBean(commentDTO, Comment.class);
        comment.setUserId(userId);
        comment.setReplyTimes(0);
        comment.setLikedTimes(0);
        comment.setHidden(false);
        comment.setStatus(CommentStatus.UN_CHECK);
        // 新评论还没有二级评论，直接标记为已计算但无结果
        comment.setTopReplyIds("");
        // 3.写入数据库
        save(comment);
    }

    @Override
    public PageDTO<CommentVO> queryCommentPage(CommentPageQuery query) {
        // 1.参数校验，漫剧id和片段id不能都为空
        Long courseId = query.getCourseId();
        Long sectionId = query.getSectionId();
        if (courseId == null && sectionId == null) {
            throw new BadRequestException("漫剧id和片段id不能都为空");
        }
        Long userId = UserContext.getUser();
        boolean onlyMine = Boolean.TRUE.equals(query.getOnlyMine());
        if (onlyMine && userId == null) {
            return PageDTO.empty(0L, 0L);
        }
        // 2.分页查询
        Page<Comment> page = lambdaQuery()
                .eq(onlyMine, Comment::getUserId, userId)
                .eq(courseId != null, Comment::getCourseId, courseId)
                .eq(sectionId != null, Comment::getSectionId, sectionId)
                .eq(Comment::getHidden, false)
                .page(query.toMpPageDefaultSortByCreateTimeDesc());
        List<Comment> records = page.getRecords();
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(page);
        }
        // 3.批量加载热度二级评论
        Map<Long, List<HotReplyCandidate>> hotReplyMap =
                hotReplyService.loadHotReplies(records, HotReplyConstants.TOP_REPLY_SIZE);

        // 4.批量查询用户信息（评论者 + 非匿名二级评论者）
        Map<Long, UserDTO> userMap = queryUserMap(collectUserIds(records, hotReplyMap, false));
        // 5.批量查询当前用户对一级评论的点赞状态
        Set<Long> commentIds = records.stream()
                .map(Comment::getId)
                .collect(Collectors.toSet());
        Set<Long> likedIds = likeClient.isBizLiked(commentIds);

        // 6.封装VO
        List<CommentVO> voList = new ArrayList<>(records.size());
        for (Comment record : records) {
            CommentVO vo = BeanUtils.copyBean(record, CommentVO.class);
            voList.add(vo);
            fillCommentUser(vo, record, userMap, false);
            vo.setLiked(likedIds.contains(record.getId()));
            vo.setHotReplies(buildHotReplyVOList(
                    hotReplyMap.getOrDefault(record.getId(), Collections.emptyList()), userMap));
        }

        return PageDTO.of(page, voList);
    }

    @Override
    public CommentVO queryCommentById(Long id) {
        // 1.根据id查询数据
        Comment comment = getById(id);
        // 2.数据校验
        if (comment == null || Boolean.TRUE.equals(comment.getHidden())) {
            return null;
        }
        // 3.加载热度二级评论
        Map<Long, List<HotReplyCandidate>> hotReplyMap =
                hotReplyService.loadHotReplies(Collections.singletonList(comment),
                        HotReplyConstants.TOP_REPLY_SIZE);
        List<HotReplyCandidate> hotReplies =
                hotReplyMap.getOrDefault(id, Collections.emptyList());
        // 4.批量查询评论者与二级评论者信息
        Map<Long, UserDTO> userMap = queryUserMap(
                collectUserIds(Collections.singletonList(comment),
                        Collections.singletonMap(id, hotReplies), false));
        // 5.当前用户点赞状态
        Set<Long> likedIds = likeClient.isBizLiked(Collections.singletonList(id));
        // 6.封装VO
        CommentVO vo = BeanUtils.copyBean(comment, CommentVO.class);
        fillCommentUser(vo, comment, userMap, false);
        vo.setLiked(likedIds.contains(id));
        vo.setHotReplies(buildHotReplyVOList(hotReplies, userMap));
        return vo;
    }

    @Override
    public PageDTO<CommentAdminVO> queryCommentPageAdmin(CommentAdminPageQuery query) {
        // 1.处理漫剧名称，得到漫剧id
        List<Long> courseIds = null;
        if (StringUtils.isNotBlank(query.getCourseName())) {
            courseIds = searchClient.queryCoursesIdByName(query.getCourseName());
            if (CollUtils.isEmpty(courseIds)) {
                return PageDTO.empty(0L, 0L);
            }
        }
        // 2.分页查询
        CommentStatus status = null;
        if (query.getStatus() != null) {
            status = CommentStatus.of(query.getStatus());
            if (status == null) {
                return PageDTO.empty(0L, 0L);
            }
        }
        LocalDateTime begin = query.getBeginTime();
        LocalDateTime end = query.getEndTime();
        Page<Comment> page = lambdaQuery()
                .in(courseIds != null, Comment::getCourseId, courseIds)
                .eq(status != null, Comment::getStatus, status)
                .gt(begin != null, Comment::getCreateTime, begin)
                .lt(end != null, Comment::getCreateTime, end)
                .page(query.toMpPageDefaultSortByCreateTimeDesc());
        List<Comment> records = page.getRecords();
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(page);
        }

        // 3.准备VO需要的数据：用户数据、漫剧数据、章节数据
        Map<Long, List<HotReplyCandidate>> hotReplyMap =
                hotReplyService.loadHotReplies(records, HotReplyConstants.TOP_REPLY_SIZE);
        Set<Long> userIds = new HashSet<>();
        Set<Long> cIds = new HashSet<>();
        Set<Long> cataIds = new HashSet<>();
        for (Comment comment : records) {
            userIds.add(comment.getUserId());
            cIds.add(comment.getCourseId());
            cataIds.add(comment.getChapterId());
            cataIds.add(comment.getSectionId());
            for (HotReplyCandidate reply : hotReplyMap.getOrDefault(comment.getId(), Collections.emptyList())) {
                if (!Boolean.TRUE.equals(reply.getAnonymity())) {
                    userIds.add(reply.getUserId());
                }
            }
        }
        Map<Long, UserDTO> userMap = queryUserMap(userIds);

        List<ComicSimpleInfoDTO> cInfos = comicClient.getSimpleInfoList(cIds);
        Map<Long, ComicSimpleInfoDTO> cInfoMap = new HashMap<>(cInfos.size());
        if (CollUtils.isNotEmpty(cInfos)) {
            cInfoMap = cInfos.stream().collect(Collectors.toMap(ComicSimpleInfoDTO::getId, c -> c));
        }

        List<EpisodeSimpleInfoDTO> catas = episodeClient.batchQueryCatalogue(cataIds);
        Map<Long, String> cataMap = new HashMap<>(catas.size());
        if (CollUtils.isNotEmpty(catas)) {
            cataMap = catas.stream()
                    .collect(Collectors.toMap(EpisodeSimpleInfoDTO::getId, EpisodeSimpleInfoDTO::getName));
        }

        // 4.封装VO
        List<CommentAdminVO> voList = new ArrayList<>(records.size());
        for (Comment comment : records) {
            CommentAdminVO vo = BeanUtils.copyBean(comment, CommentAdminVO.class);
            vo.setStatus(comment.getStatus() == null ? null : comment.getStatus().getValue());
            voList.add(vo);
            fillCommentUser(vo, comment, userMap, true);

            ComicSimpleInfoDTO cInfo = cInfoMap.get(comment.getCourseId());
            if (cInfo != null) {
                vo.setCourseName(cInfo.getName());
                vo.setCategoryName(categoryCache.getCategoryNames(cInfo.getCategoryIds()));
            }
            vo.setChapterName(cataMap.getOrDefault(comment.getChapterId(), ""));
            vo.setSectionName(cataMap.getOrDefault(comment.getSectionId(), ""));
            vo.setHotReplies(buildHotReplyVOList(
                    hotReplyMap.getOrDefault(comment.getId(), Collections.emptyList()), userMap));
        }
        return PageDTO.of(page, voList);
    }

    @Override
    public CommentAdminVO queryCommentByIdAdmin(Long id) {
        // 1.根据id查询评论
        Comment comment = getById(id);
        if (comment == null) {
            return null;
        }
        // 2.转PO为VO
        CommentAdminVO vo = BeanUtils.copyBean(comment, CommentAdminVO.class);
        vo.setStatus(comment.getStatus() == null ? null : comment.getStatus().getValue());
        // 3.加载热度二级评论，并批量查询评论者与二级评论者信息
        Map<Long, List<HotReplyCandidate>> hotReplyMap =
                hotReplyService.loadHotReplies(Collections.singletonList(comment),
                        HotReplyConstants.TOP_REPLY_SIZE);
        List<HotReplyCandidate> hotReplies = hotReplyMap.getOrDefault(id, Collections.emptyList());
        Set<Long> userIds = new HashSet<>();
        userIds.add(comment.getUserId());
        for (HotReplyCandidate reply : hotReplies) {
            if (!Boolean.TRUE.equals(reply.getAnonymity())) {
                userIds.add(reply.getUserId());
            }
        }
        Map<Long, UserDTO> userMap = queryUserMap(userIds);
        fillCommentUser(vo, comment, userMap, true);
        // 4.查询漫剧信息
        ComicFullInfoDTO cInfo = comicClient.getCourseInfoById(comment.getCourseId(), false, true);
        if (cInfo != null) {
            vo.setCourseName(cInfo.getName());
            vo.setCategoryName(categoryCache.getCategoryNames(cInfo.getCategoryIds()));
            List<Long> creatorIds = cInfo.getCreatorIds();
            List<UserDTO> creators = userClient.queryUserByIds(creatorIds);
            if (CollUtils.isNotEmpty(creators)) {
                vo.setCreatorName(creators.stream()
                        .map(UserDTO::getName).collect(Collectors.joining("/")));
            }
        }
        // 5.查询章节信息
        List<EpisodeSimpleInfoDTO> catas = episodeClient.batchQueryCatalogue(
                List.of(comment.getChapterId(), comment.getSectionId()));
        Map<Long, String> cataMap = new HashMap<>(catas.size());
        if (CollUtils.isNotEmpty(catas)) {
            cataMap = catas.stream()
                    .collect(Collectors.toMap(EpisodeSimpleInfoDTO::getId, EpisodeSimpleInfoDTO::getName));
        }
        vo.setChapterName(cataMap.getOrDefault(comment.getChapterId(), ""));
        vo.setSectionName(cataMap.getOrDefault(comment.getSectionId(), ""));
        // 6.封装热度二级评论
        vo.setHotReplies(buildHotReplyVOList(hotReplies, userMap));
        return vo;
    }

    @Override
    public void hiddenComment(Long id, Boolean hidden) {
        // 1.更新评论
        Comment comment = new Comment();
        comment.setId(id);
        comment.setHidden(hidden);
        updateById(comment);
        // 2.隐藏状态变化会影响热度榜，立即失效并等待任务重算
        hotReplyService.invalidate(Collections.singleton(id));
        hotReplyService.markDirty(Collections.singleton(id));
    }

    @Override
    public void updateComment(Long id, CommentUpdateDTO commentDTO) {
        // 1.获取当前登录用户
        Long userId = UserContext.getUser();
        // 2.查询当前评论
        Comment old = getById(id);
        if (old == null) {
            throw new BadRequestException("评论不存在");
        }
        // 3.判断是否是当前用户的评论
        if (!old.getUserId().equals(userId)) {
            throw new BadRequestException("无权修改他人的评论");
        }
        // 4.只允许修改内容与匿名状态
        Comment comment = new Comment();
        comment.setId(id);
        comment.setContent(commentDTO.getContent());
        comment.setAnonymity(commentDTO.getAnonymity());
        updateById(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long id) {
        // 1.获取当前登录用户
        Long userId = UserContext.getUser();
        // 2.查询当前评论
        Comment comment = getById(id);
        if (comment == null) {
            return;
        }
        // 3.判断是否是当前用户的评论
        if (!comment.getUserId().equals(userId)) {
            throw new BadRequestException("无权删除他人的评论");
        }
        // 4.删除一级评论
        removeById(id);
        // 5.删除二级评论
        replyMapper.delete(
                new QueryWrapper<CommentReply>().lambda().eq(CommentReply::getCommentId, id)
        );
        // 6.清理热度缓存
        hotReplyService.cleanup(id);
    }

    /**
     * 收集需要查询用户信息的用户id
     */
    private Set<Long> collectUserIds(List<Comment> comments,
                                     Map<Long, List<HotReplyCandidate>> hotReplyMap,
                                     boolean ignoreAnonymity) {
        Set<Long> userIds = new HashSet<>();
        for (Comment comment : comments) {
            if (ignoreAnonymity || !Boolean.TRUE.equals(comment.getAnonymity())) {
                userIds.add(comment.getUserId());
            }
            for (HotReplyCandidate reply : hotReplyMap.getOrDefault(comment.getId(), Collections.emptyList())) {
                if (ignoreAnonymity || !Boolean.TRUE.equals(reply.getAnonymity())) {
                    userIds.add(reply.getUserId());
                }
            }
        }
        userIds.remove(null);
        return userIds;
    }

    /**
     * 批量查询用户信息
     */
    private Map<Long, UserDTO> queryUserMap(Collection<Long> userIds) {
        if (CollUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        Set<Long> ids = userIds.stream()
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        List<UserDTO> users = userClient.queryUserByIds(ids);
        if (CollUtils.isEmpty(users)) {
            return Collections.emptyMap();
        }
        return users.stream().collect(Collectors.toMap(UserDTO::getId, u -> u));
    }

    /**
     * 填充评论者信息；匿名评论对普通用户不暴露身份
     */
    private void fillCommentUser(CommentVO vo, Comment comment, Map<Long, UserDTO> userMap, boolean admin) {
        if (!admin && Boolean.TRUE.equals(comment.getAnonymity())) {
            vo.setUserId(null);
            vo.setUserName(null);
            vo.setUserIcon(null);
            return;
        }
        UserDTO user = userMap.get(comment.getUserId());
        if (user != null) {
            vo.setUserName(user.getName());
            vo.setUserIcon(user.getIcon());
        }
    }

    private void fillCommentUser(CommentAdminVO vo, Comment comment, Map<Long, UserDTO> userMap, boolean admin) {
        if (!admin && Boolean.TRUE.equals(comment.getAnonymity())) {
            vo.setUserName(null);
            vo.setUserIcon(null);
            return;
        }
        UserDTO user = userMap.get(comment.getUserId());
        if (user != null) {
            vo.setUserName(user.getName());
            vo.setUserIcon(user.getIcon());
        }
    }

    /**
     * 组装热度二级评论 VO；匿名评论不暴露用户身份
     */
    private List<HotReplyVO> buildHotReplyVOList(List<HotReplyCandidate> candidates,
                                                 Map<Long, UserDTO> userMap) {
        if (CollUtils.isEmpty(candidates)) {
            return Collections.emptyList();
        }
        Map<Long, UserDTO> users = userMap == null ? Collections.emptyMap() : userMap;
        List<HotReplyVO> result = new ArrayList<>(candidates.size());
        for (HotReplyCandidate candidate : candidates) {
            HotReplyVO vo = new HotReplyVO();
            vo.setId(candidate.getReplyId());
            vo.setContent(candidate.getContent());
            vo.setAnonymity(candidate.getAnonymity());
            vo.setLikedTimes(candidate.getLikedTimes());
            vo.setCreateTime(candidate.getCreateTime());
            vo.setHidden(candidate.getHidden());
            if (Boolean.TRUE.equals(candidate.getAnonymity())) {
                vo.setUserId(null);
                vo.setUserName(null);
                vo.setUserIcon(null);
            } else {
                vo.setUserId(candidate.getUserId());
                UserDTO user = users.get(candidate.getUserId());
                if (user != null) {
                    vo.setUserName(user.getName());
                    vo.setUserIcon(user.getIcon());
                }
            }
            result.add(vo);
        }
        return result;
    }
}
