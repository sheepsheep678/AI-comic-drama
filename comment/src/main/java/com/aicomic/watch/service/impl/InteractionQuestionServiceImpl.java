package com.aicomic.watch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aicomic.api.cache.CategoryCache;
import com.aicomic.api.client.comic.EpisodeClient;
import com.aicomic.api.client.comic.ComicClient;
import com.aicomic.api.client.search.SearchClient;
import com.aicomic.api.client.user.UserClient;
import com.aicomic.api.dto.comic.EpisodeSimpleInfoDTO;
import com.aicomic.api.dto.comic.ComicFullInfoDTO;
import com.aicomic.api.dto.comic.ComicSimpleInfoDTO;
import com.aicomic.api.dto.user.UserDTO;
import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.common.exceptions.BadRequestException;
import com.aicomic.common.utils.BeanUtils;
import com.aicomic.common.utils.CollUtils;
import com.aicomic.common.utils.StringUtils;
import com.aicomic.common.utils.UserContext;
import com.aicomic.watch.constants.HotReplyConstants;
import com.aicomic.watch.domain.dto.HotReplyCandidate;
import com.aicomic.watch.domain.dto.QuestionFormDTO;
import com.aicomic.watch.domain.po.InteractionQuestion;
import com.aicomic.watch.domain.po.InteractionReply;
import com.aicomic.watch.domain.query.QuestionAdminPageQuery;
import com.aicomic.watch.domain.query.QuestionPageQuery;
import com.aicomic.watch.domain.vo.HotReplyVO;
import com.aicomic.watch.domain.vo.QuestionAdminVO;
import com.aicomic.watch.domain.vo.QuestionVO;
import com.aicomic.watch.mapper.InteractionQuestionMapper;
import com.aicomic.watch.mapper.InteractionReplyMapper;
import com.aicomic.watch.service.IHotReplyService;
import com.aicomic.watch.service.IInteractionQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 互动提问的问题表 服务实现类
 * </p>
 *
 */
@Service
@RequiredArgsConstructor
public class InteractionQuestionServiceImpl extends ServiceImpl<InteractionQuestionMapper, InteractionQuestion> implements IInteractionQuestionService {

    private final IHotReplyService hotReplyService;
    private final InteractionReplyMapper replyMapper;
    private final UserClient userClient;
    private final ComicClient courseClient;
    private final SearchClient searchClient;
    private final EpisodeClient catalogueClient;
    private final CategoryCache categoryCache;

    @Override
    public void saveQuestion(QuestionFormDTO questionDTO) {
        // 1.获取当前登录的用户id
        Long userId = UserContext.getUser();
        // 2.数据封装
        InteractionQuestion question = BeanUtils.copyBean(questionDTO, InteractionQuestion.class);
        question.setUserId(userId);
        // 新问题还没有回答，直接标记为已计算但无结果
        question.setTopReplyIds("");
        // 3.写入数据库
        save(question);
    }

    @Override
    public PageDTO<QuestionVO> queryQuestionPage(QuestionPageQuery query) {
        // 1.参数校验，漫剧id和片段id不能都为空
        Long courseId = query.getCourseId();
        Long sectionId = query.getSectionId();
        if (courseId == null && sectionId == null) {
            throw new BadRequestException("漫剧id和片段id不能都为空");
        }
        // 2.分页查询
        Page<InteractionQuestion> page = lambdaQuery()
                .select(InteractionQuestion.class, info -> !info.getProperty().equals("description"))
                .eq(query.getOnlyMine(), InteractionQuestion::getUserId, UserContext.getUser())
                .eq(courseId != null, InteractionQuestion::getCourseId, courseId)
                .eq(sectionId != null, InteractionQuestion::getSectionId, sectionId)
                .eq(InteractionQuestion::getHidden, false)
                .page(query.toMpPageDefaultSortByCreateTimeDesc());
        List<InteractionQuestion> records = page.getRecords();
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(page);
        }
        // 3.批量加载热度回答
        Map<Long, List<HotReplyCandidate>> hotReplyMap =
                hotReplyService.loadHotReplies(records, HotReplyConstants.TOP_REPLY_SIZE);

        // 4.批量查询用户信息（提问者 + 非匿名回答者）
        Set<Long> userIds = new HashSet<>();
        for (InteractionQuestion q : records) {
            if (!Boolean.TRUE.equals(q.getAnonymity())) {
                userIds.add(q.getUserId());
            }
            for (HotReplyCandidate reply : hotReplyMap.getOrDefault(q.getId(), Collections.emptyList())) {
                if (!Boolean.TRUE.equals(reply.getAnonymity())) {
                    userIds.add(reply.getUserId());
                }
            }
        }
        userIds.remove(null);
        Map<Long, UserDTO> userMap = new HashMap<>(userIds.size());
        if (CollUtils.isNotEmpty(userIds)) {
            List<UserDTO> users = userClient.queryUserByIds(userIds);
            userMap = users.stream()
                    .collect(Collectors.toMap(UserDTO::getId, u -> u));
        }

        // 5.封装VO
        List<QuestionVO> voList = new ArrayList<>(records.size());
        for (InteractionQuestion r : records) {
            QuestionVO vo = BeanUtils.copyBean(r, QuestionVO.class);
            voList.add(vo);
            if (!Boolean.TRUE.equals(r.getAnonymity())) {
                UserDTO userDTO = userMap.get(r.getUserId());
                if (userDTO != null) {
                    vo.setUserName(userDTO.getName());
                    vo.setUserIcon(userDTO.getIcon());
                }
            }
            vo.setHotReplies(buildHotReplyVOList(
                    hotReplyMap.getOrDefault(r.getId(), Collections.emptyList()), userMap));
        }

        return PageDTO.of(page, voList);
    }

    @Override
    public QuestionVO queryQuestionById(Long id) {
        // 1.根据id查询数据
        InteractionQuestion question = getById(id);
        // 2.数据校验
        if (question == null || Boolean.TRUE.equals(question.getHidden())) {
            // 没有数据或者是被隐藏了
            return null;
        }
        // 3.加载热度回答
        Map<Long, List<HotReplyCandidate>> hotReplyMap =
                hotReplyService.loadHotReplies(Collections.singletonList(question),
                        HotReplyConstants.TOP_REPLY_SIZE);
        List<HotReplyCandidate> hotReplies =
                hotReplyMap.getOrDefault(id, Collections.emptyList());
        // 4.批量查询提问者与回答者信息
        Set<Long> userIds = new HashSet<>();
        if (!Boolean.TRUE.equals(question.getAnonymity())) {
            userIds.add(question.getUserId());
        }
        for (HotReplyCandidate reply : hotReplies) {
            if (!Boolean.TRUE.equals(reply.getAnonymity())) {
                userIds.add(reply.getUserId());
            }
        }
        userIds.remove(null);
        Map<Long, UserDTO> userMap = queryUserMap(userIds);
        // 5.封装VO
        QuestionVO vo = BeanUtils.copyBean(question, QuestionVO.class);
        if (!Boolean.TRUE.equals(question.getAnonymity())) {
            UserDTO user = userMap.get(question.getUserId());
            if (user != null) {
                vo.setUserName(user.getName());
                vo.setUserIcon(user.getIcon());
            }
        }
        vo.setHotReplies(buildHotReplyVOList(hotReplies, userMap));
        return vo;
    }

    @Override
    public PageDTO<QuestionAdminVO> queryQuestionPageAdmin(QuestionAdminPageQuery query) {
        // 1.处理漫剧名称，得到漫剧id
        List<Long> courseIds = null;
        if (StringUtils.isNotBlank(query.getCourseName())) {
            courseIds = searchClient.queryCoursesIdByName(query.getCourseName());
            if (CollUtils.isEmpty(courseIds)) {
                return PageDTO.empty(0L, 0L);
            }
        }
        // 2.分页查询
        Integer status = query.getStatus();
        LocalDateTime begin = query.getBeginTime();
        LocalDateTime end = query.getEndTime();
        Page<InteractionQuestion> page = lambdaQuery()
                .in(courseIds != null, InteractionQuestion::getCourseId, courseIds)
                .eq(status != null, InteractionQuestion::getStatus, status)
                .gt(begin != null, InteractionQuestion::getCreateTime, begin)
                .lt(end != null, InteractionQuestion::getCreateTime, end)
                .page(query.toMpPageDefaultSortByCreateTimeDesc());
        List<InteractionQuestion> records = page.getRecords();
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(page);
        }

        // 3.准备VO需要的数据：用户数据、漫剧数据、章节数据
        Map<Long, List<HotReplyCandidate>> hotReplyMap =
                hotReplyService.loadHotReplies(records, HotReplyConstants.TOP_REPLY_SIZE);
        Set<Long> userIds = new HashSet<>();
        Set<Long> cIds = new HashSet<>();
        Set<Long> cataIds = new HashSet<>();
        // 3.1.获取各种数据的id集合
        for (InteractionQuestion q : records) {
            userIds.add(q.getUserId());
            cIds.add(q.getCourseId());
            cataIds.add(q.getChapterId());
            cataIds.add(q.getSectionId());
            for (HotReplyCandidate reply : hotReplyMap.getOrDefault(q.getId(), Collections.emptyList())) {
                if (!Boolean.TRUE.equals(reply.getAnonymity())) {
                    userIds.add(reply.getUserId());
                }
            }
        }
        // 3.2.根据id查询用户
        Map<Long, UserDTO> userMap = queryUserMap(userIds);

        // 3.3.根据id查询漫剧
        List<ComicSimpleInfoDTO> cInfos = courseClient.getSimpleInfoList(cIds);
        Map<Long, ComicSimpleInfoDTO> cInfoMap = new HashMap<>(cInfos.size());
        if (CollUtils.isNotEmpty(cInfos)) {
            cInfoMap = cInfos.stream().collect(Collectors.toMap(ComicSimpleInfoDTO::getId, c -> c));
        }

        // 3.4.根据id查询章节
        List<EpisodeSimpleInfoDTO> catas = catalogueClient.batchQueryCatalogue(cataIds);
        Map<Long, String> cataMap = new HashMap<>(catas.size());
        if (CollUtils.isNotEmpty(catas)) {
            cataMap = catas.stream()
                    .collect(Collectors.toMap(EpisodeSimpleInfoDTO::getId, EpisodeSimpleInfoDTO::getName));
        }


        // 4.封装VO
        List<QuestionAdminVO> voList = new ArrayList<>(records.size());
        for (InteractionQuestion q : records) {
            // 4.1.将PO转VO，属性拷贝
            QuestionAdminVO vo = BeanUtils.copyBean(q, QuestionAdminVO.class);
            voList.add(vo);
            // 4.2.用户信息
            UserDTO user = userMap.get(q.getUserId());
            if (user != null) {
                vo.setUserName(user.getName());
            }
            // 4.3.漫剧信息以及分类信息
            ComicSimpleInfoDTO cInfo = cInfoMap.get(q.getCourseId());
            if (cInfo != null) {
                vo.setCourseName(cInfo.getName());
                vo.setCategoryName(categoryCache.getCategoryNames(cInfo.getCategoryIds()));
            }
            // 4.4.章节信息
            vo.setChapterName(cataMap.getOrDefault(q.getChapterId(), ""));
            vo.setSectionName(cataMap.getOrDefault(q.getSectionId(), ""));
            // 4.5.热度回答
            vo.setHotReplies(buildHotReplyVOList(
                    hotReplyMap.getOrDefault(q.getId(), Collections.emptyList()), userMap));
        }
        return PageDTO.of(page, voList);
    }

    @Override
    public QuestionAdminVO queryQuestionByIdAdmin(Long id) {
        // 1.根据id查询问题
        InteractionQuestion question = getById(id);
        if (question == null) {
            return null;
        }
        // 2.转PO为VO
        QuestionAdminVO vo = BeanUtils.copyBean(question, QuestionAdminVO.class);
        // 3.加载热度回答，并批量查询提问者与回答者信息
        Map<Long, List<HotReplyCandidate>> hotReplyMap =
                hotReplyService.loadHotReplies(Collections.singletonList(question),
                        HotReplyConstants.TOP_REPLY_SIZE);
        List<HotReplyCandidate> hotReplies =
                hotReplyMap.getOrDefault(id, Collections.emptyList());
        Set<Long> userIds = new HashSet<>();
        userIds.add(question.getUserId());
        for (HotReplyCandidate reply : hotReplies) {
            if (!Boolean.TRUE.equals(reply.getAnonymity())) {
                userIds.add(reply.getUserId());
            }
        }
        Map<Long, UserDTO> userMap = queryUserMap(userIds);
        UserDTO user = userMap.get(question.getUserId());
        if (user != null) {
            vo.setUserName(user.getName());
            vo.setUserIcon(user.getIcon());
        }
        // 4.查询漫剧信息
        ComicFullInfoDTO cInfo = courseClient.getCourseInfoById(
                question.getCourseId(), false, true);
        if (cInfo != null) {
            // 4.1.漫剧名称信息
            vo.setCourseName(cInfo.getName());
            // 4.2.分类信息
            vo.setCategoryName(categoryCache.getCategoryNames(cInfo.getCategoryIds()));
            // 4.3.创作者信息
            List<Long> creatorIds = cInfo.getCreatorIds();
            List<UserDTO> creators = userClient.queryUserByIds(creatorIds);
            if(CollUtils.isNotEmpty(creators)) {
                vo.setCreatorName(creators.stream()
                        .map(UserDTO::getName).collect(Collectors.joining("/")));
            }
        }
        // 5.查询章节信息
        List<EpisodeSimpleInfoDTO> catas = catalogueClient.batchQueryCatalogue(
                List.of(question.getChapterId(), question.getSectionId()));
        Map<Long, String> cataMap = new HashMap<>(catas.size());
        if (CollUtils.isNotEmpty(catas)) {
            cataMap = catas.stream()
                    .collect(Collectors.toMap(EpisodeSimpleInfoDTO::getId, EpisodeSimpleInfoDTO::getName));
        }
        vo.setChapterName(cataMap.getOrDefault(question.getChapterId(), ""));
        vo.setSectionName(cataMap.getOrDefault(question.getSectionId(), ""));
        // 6.封装热度回答
        vo.setHotReplies(buildHotReplyVOList(hotReplies, userMap));
        return vo;
    }

    @Override
    public void hiddenQuestion(Long id, Boolean hidden) {
        // 1.更新问题
        InteractionQuestion question = new InteractionQuestion();
        question.setId(id);
        question.setHidden(hidden);
        updateById(question);
        // 2.取消隐藏后热度榜可能已过期，打脏标记等待重算
        hotReplyService.markDirty(Collections.singleton(id));
    }

    @Override
    public void updateQuestion(Long id, QuestionFormDTO questionDTO) {
        // 1.获取当前登录用户
        Long userId = UserContext.getUser();
        // 2.查询当前问题
        InteractionQuestion q = getById(id);
        if (q == null) {
            throw new BadRequestException("问题不存在");
        }
        // 3.判断是否是当前用户的问题
        if (!q.getUserId().equals(userId)) {
            // 不是，抛出异常
            throw new BadRequestException("无权修改他人的问题");
        }
        // 4.修改问题
        InteractionQuestion question = BeanUtils.toBean(questionDTO, InteractionQuestion.class);
        question.setId(id);
        updateById(question);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        // 1.获取当前登录用户
        Long userId = UserContext.getUser();
        // 2.查询当前问题
        InteractionQuestion q = getById(id);
        if (q == null) {
            return;
        }
        // 3.判断是否是当前用户的问题
        if (!q.getUserId().equals(userId)) {
            // 不是，抛出异常
            throw new BadRequestException("无权删除他人的问题");
        }
        // 4.删除问题
        removeById(id);
        // 5.删除答案
        replyMapper.delete(
                new QueryWrapper<InteractionReply>().lambda().eq(InteractionReply::getQuestionId, id)
        );
        // 6.清理热度缓存
        hotReplyService.cleanup(id);
    }

    /**
     * 批量查询用户信息
     */
    private Map<Long, UserDTO> queryUserMap(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        userIds.remove(null);
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<UserDTO> users = userClient.queryUserByIds(userIds);
        if (CollUtils.isEmpty(users)) {
            return Collections.emptyMap();
        }
        return users.stream().collect(Collectors.toMap(UserDTO::getId, u -> u));
    }

    /**
     * 组装热度回答 VO；匿名回答不暴露用户身份
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
            vo.setReplyTimes(candidate.getReplyTimes());
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
