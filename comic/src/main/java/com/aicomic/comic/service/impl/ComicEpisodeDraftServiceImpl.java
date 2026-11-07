package com.aicomic.comic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aicomic.api.client.quiz.QuizClient;
import com.aicomic.api.dto.quiz.QuestionBizDTO;
import com.aicomic.api.dto.quiz.QuestionDTO;
import com.aicomic.common.constants.ErrorInfo;
import com.aicomic.common.exceptions.BizIllegalException;
import com.aicomic.common.exceptions.DbException;
import com.aicomic.common.utils.*;
import com.aicomic.common.validate.Checker;
import com.aicomic.comic.constants.ComicConstants;
import com.aicomic.comic.constants.ComicErrorInfo;
import com.aicomic.comic.constants.ComicStatus;
import com.aicomic.comic.domain.dto.EpisodeSaveDTO;
import com.aicomic.comic.domain.dto.EpisodeTagDTO;
import com.aicomic.comic.domain.dto.ComicMediaDTO;
import com.aicomic.comic.domain.po.*;
import com.aicomic.comic.domain.vo.EpisodeSimpleTagVO;
import com.aicomic.comic.domain.vo.EpisodeVO;
import com.aicomic.comic.mapper.ComicEpisodeTagDraftMapper;
import com.aicomic.comic.mapper.ComicEpisodeTagMapper;
import com.aicomic.comic.mapper.ComicEpisodeDraftMapper;
import com.aicomic.comic.mapper.ComicEpisodeMapper;
import com.aicomic.comic.service.IComicEpisodeTagDraftService;
import com.aicomic.comic.service.IComicEpisodeDraftService;
import com.aicomic.comic.service.IComicEpisodeService;
import com.aicomic.comic.service.IComicDraftService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * <p>
 * 目录草稿 服务实现类
 * </p>
 *
 * @since 2022-07-19
 */
@Service
@Slf4j
public class ComicEpisodeDraftServiceImpl extends ServiceImpl<ComicEpisodeDraftMapper, ComicEpisodeDraft> implements IComicEpisodeDraftService {

    @Autowired
    private ComicEpisodeMapper comicEpisodeMapper;

    @Autowired
    private IComicEpisodeService comicEpisodeService;

    @Autowired
    private IComicDraftService comicDraftService;

    @Autowired
    private ComicEpisodeTagDraftMapper courseCataSubjectDraftMapper;

    @Autowired
    private ComicEpisodeTagMapper courseCataSubjectMapper;

    @Autowired
    private ComicEpisodeDraftMapper comicEpisodeDraftMapper;

    @Autowired
    private QuizClient quizClient;

    @Autowired
    private IComicEpisodeTagDraftService courseCataSubjectDraftService;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {DbException.class, Exception.class})
    public void save(Long courseId, List<EpisodeSaveDTO> episodeSaveDTOS, Integer step) {
        //1.根据章的序号按照升序重新排序
        episodeSaveDTOS = episodeSaveDTOS
                .stream()
                .sorted(Comparator.comparing(EpisodeSaveDTO::getIndex))
                .collect(Collectors.toList());

        //2.校验章的序号
        if (episodeSaveDTOS.size() != episodeSaveDTOS
                .stream()
                .map(EpisodeSaveDTO::getIndex)
                .distinct()
                .count()) {
            throw new BizIllegalException(ComicErrorInfo.Msg.COMIC_EPISODES_SAVE_INEDX);
        }
        if (episodeSaveDTOS.size() < episodeSaveDTOS.get(episodeSaveDTOS.size() - 1).getIndex()) {
            throw new BizIllegalException(ComicErrorInfo.Msg.COMIC_EPISODES_SAVE_INEDX_JUMP);
        }

        //2.已经上架的目录
        LambdaQueryWrapper<ComicEpisode> queryWrapper =
                Wrappers.lambdaQuery(ComicEpisode.class)
                        .eq(ComicEpisode::getCourseId, courseId);
        List<ComicEpisode> comicEpisodes = comicEpisodeMapper.selectList(queryWrapper);

        //2.1.校验以上架目录是否更新
        checkIndex(episodeSaveDTOS, comicEpisodes);
        //3.组装数据保存到数据库
        List<ComicEpisodeDraft> comicEpisodeDrafts =
                packageCatalogue(courseId, episodeSaveDTOS, comicEpisodes);
        //4.删除原有目录信息
        if (step == ComicConstants.ComicStep.EPISODE) {
            //4.1删除片段和章数据
            comicEpisodeDraftMapper.deleteByCourseId(courseId,
                    Arrays.asList(
                            ComicConstants.EpisodeType.CHAPTER,
                            ComicConstants.EpisodeType.CLIP));
        } else if (step == ComicConstants.ComicStep.TAG) {
            //4.2保存题目时需要删除所有目录
            comicEpisodeDraftMapper.deleteByCourseId(courseId,
                    Arrays.asList(
                            ComicConstants.EpisodeType.CHAPTER,
                            ComicConstants.EpisodeType.CLIP,
                            ComicConstants.EpisodeType.PRATICE));
        } else {
            throw new BizIllegalException(ErrorInfo.Msg.OPERATE_FAILED);
        }
        //5.目录重新插入草稿
        this.saveOrUpdateBatch(comicEpisodeDrafts);

        //6.修改漫剧编辑进度
        comicDraftService.updateStep(courseId, ComicConstants.ComicStep.EPISODE);

        //7.删除已删除章节题目
        courseCataSubjectDraftService.deleteNotInCataIdList(courseId);
    }

    @Override
    public List<EpisodeVO> queryComicEpisodes(Long courseId, Boolean see, Boolean withPractice) {
        if (see) {
            //1.1查询正式数据目录
            List<EpisodeVO> episodeVOS = comicEpisodeService.queryComicEpisodesVO(courseId, withPractice);
            if (CollUtils.isNotEmpty(episodeVOS)) {
                return episodeVOS;
            }
            //1.2查看草稿目录
            episodeVOS = queryComicEpisodes(courseId, withPractice);
            return CollUtils.isEmpty(episodeVOS) ? new ArrayList<>() : episodeVOS;

        } else {
            //2.1查看草稿目录
            List<EpisodeVO> episodeVOS = queryComicEpisodes(courseId, withPractice);
            return CollUtils.isEmpty(episodeVOS) ? new ArrayList<>() : episodeVOS;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {DbException.class, Exception.class})
    public void saveMediaInfo(Long courseId, List<ComicMediaDTO> courseMediaDTOS) {
        //1.校验保存视频的片段id是否属于当前漫剧中的片段，
        List<Long> cataIds =
                courseMediaDTOS.stream()
                        .map(ComicMediaDTO::getCataId)
                        .collect(Collectors.toList());
        //2.每个片段都有上传素材
        checkSectionIds(cataIds, courseId);

        //3.获取漫剧草稿信息
        ComicDraft comicDraft = comicDraftService.getById(courseId);
        //3.1判断新增漫剧是否按照顺序上传素材
        if (comicDraft == null ||
                comicDraft.getStep() < ComicConstants.ComicStep.EPISODE) {
            throw new BizIllegalException(ComicErrorInfo.Msg.COMIC_MEDIA_SAVE_NO_EXECUTE);
        }

        //4.素材设置到片段信息中
        List<ComicEpisodeDraft> catalogueDrafts =
                BeanUtils.copyList(courseMediaDTOS, ComicEpisodeDraft.class,
                        (dto, comicEpisodeDraft) ->
                                comicEpisodeDraft.setId(dto.getCataId()));
        //4.1.更新片段素材信息
        this.updateBatchById(catalogueDrafts);
        //4.2.更新漫剧填写步骤
        comicDraftService.updateStep(courseId, ComicConstants.ComicStep.MEDIA);
        //5.统计每个章节素材播放总时长
        List<ComicEpisodeDraft> comicEpisodeDrafts = calculateCatalogMediaDuration(courseId);
        //5.1批量更新每个大章的课时总数量
        this.updateBatchById(comicEpisodeDrafts, 500);


    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {DbException.class, Exception.class})
    public void saveSuject(Long courseId, List<EpisodeTagDTO> cataSubjectDTOS) {
        //1.数据校验
        //1.1.转化目录id列表
        List<Long> cataIds = cataSubjectDTOS
                .stream()
                .map(EpisodeTagDTO::getCataId)
                .collect(Collectors.toList());
        checkPracticeIds(cataIds, courseId);
        List<ComicEpisodeTagDraft> cataSubjectDrafts = new ArrayList<>();

        //2.查询漫剧草稿
        ComicDraft comicDraft = comicDraftService.getById(courseId);
        //2.1.判断当前是否可以上传题目
        if (comicDraft == null || comicDraft.getStep() < ComicConstants.ComicStep.MEDIA) {
            throw new BizIllegalException(ComicErrorInfo.Msg.COMIC_MEDIA_SAVE_NO_EXECUTE);
        }

        //3.组装题目目录关系
        for (EpisodeTagDTO cataSubjectDTO : cataSubjectDTOS) {
            for (Long subjectId : cataSubjectDTO.getSubjectIds()) {
                ComicEpisodeTagDraft courseCataSubjectDraft = new ComicEpisodeTagDraft();
                //3.1.漫剧id
                courseCataSubjectDraft.setCourseId(courseId);
                //3.2.题目id
                courseCataSubjectDraft.setSubjectId(subjectId);
                //3.3.漫剧目录
                courseCataSubjectDraft.setCataId(cataSubjectDTO.getCataId());
                if (courseCataSubjectDraft.getId() == null) {
                    courseCataSubjectDraft.setId(IdWorker.getId());
                }
                //3.4.漫剧题目关系添加到
                cataSubjectDrafts.add(courseCataSubjectDraft);
            }
        }
        //4.删除练习和题目对应的关系
        courseCataSubjectDraftMapper.deleteByCourseId(courseId);
        //5.批量插入练习和题目之间的关系
        if (!cataSubjectDrafts.isEmpty()) {
            courseCataSubjectDraftMapper.batchInsert(cataSubjectDrafts);
        }
        //6.修改漫剧填写进度
        comicDraftService.updateStep(courseId, ComicConstants.ComicStep.TAG);
    }

    @Override
    public List<EpisodeSimpleTagVO> getSuject(Long courseId) {

        //1.查询漫剧目录和题目关系
        List<ComicEpisodeTagDraft> cataSubjectDrafts = courseCataSubjectDraftMapper.getByCourseId(courseId);
        if (CollUtils.isEmpty(cataSubjectDrafts)) {
            return new ArrayList<>();
        }
        List<Long> subjectIdList = cataSubjectDrafts.stream().map(ComicEpisodeTagDraft::getSubjectId).collect(Collectors.toList());
        List<QuestionDTO> tags = quizClient.queryQuestionByIds(subjectIdList);
        Map<Long, String> subjectIdAndNameMap = tags.stream()
                .collect(Collectors.toMap(QuestionDTO::getId, QuestionDTO::getName));

        //4.组装数据
        return cataSubjectDrafts.stream()
                //4.1.分组
                .collect(Collectors.groupingBy(ComicEpisodeTagDraft::getCataId))
                .entrySet().stream().map(entry -> {
                    //4.2.片段或测试对应的题目列表
                    List<EpisodeSimpleTagVO.TagInfo> tagInfos = new ArrayList<>();
                    for (ComicEpisodeTagDraft cataSubjectDraft : entry.getValue()) {
                        //4.3.将题目id和题目名称加入到片段或测试的题目列表中
                        tagInfos.add(new EpisodeSimpleTagVO.TagInfo(
                                cataSubjectDraft.getSubjectId(),
                                subjectIdAndNameMap.get(cataSubjectDraft.getSubjectId())));
                    }
                    //4.4.组装片段或测试对应题目列表model
                    return new EpisodeSimpleTagVO(entry.getKey(), tagInfos);
                }).collect(Collectors.toList());
    }

    @Override
    public void checkCataInfoImplated(Long courseId) {
        //查询所有目录
        LambdaQueryWrapper<ComicEpisodeDraft> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ComicEpisodeDraft::getCourseId, courseId);
        List<ComicEpisodeDraft> comicEpisodeDrafts = baseMapper.selectList(queryWrapper);

        List<ComicEpisodeTagDraft> courseCataSubjectDrafts = courseCataSubjectDraftMapper.getByCourseId(courseId);

        Map<Long, Long> subjectNumMap = CollUtils.isEmpty(courseCataSubjectDrafts) ? new HashMap<>() :
                courseCataSubjectDrafts.stream().collect(
                        Collectors.groupingBy(ComicEpisodeTagDraft::getCataId,
                                Collectors.counting()));

        //校验练习或片段有没有上传题目和素材
        CollUtils.check(comicEpisodeDrafts, new Checker<ComicEpisodeDraft>() {
            @Override
            public void check(ComicEpisodeDraft comicEpisodeDraft) {
                if (comicEpisodeDraft.getType() == ComicConstants.EpisodeType.CLIP
                        && StringUtils.isEmpty(comicEpisodeDraft.getVideoName())) { //片段未上传视频
                    throw new BizIllegalException(
                            StringUtils.format(ComicErrorInfo.Msg.COMIC_UP_SHELF_CLIP_WITHOUT_MEDIA, comicEpisodeDraft.getName()));
                } else if (comicEpisodeDraft.getType() == ComicConstants.EpisodeType.PRATICE
                        && NumberUtils.null2Zero(subjectNumMap.get(comicEpisodeDraft.getId())) <= 0) { //练习未添加题目
                    throw new BizIllegalException(
                            StringUtils.format(ComicErrorInfo.Msg.COMIC_UP_SHELF_PRACTICE_WITHOUT_TAG,
                                    comicEpisodeDraft.getName())
                    );
                }
            }
        });
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {DbException.class, Exception.class})
    public void copySubjectToShelf(Long courseId, Boolean isFirstShelf) {
        //1.从草稿中查出题目信息
        List<ComicEpisodeTagDraft> courseCataSubjectDrafts = courseCataSubjectDraftMapper.getByCourseId(courseId);
        List<QuestionBizDTO> tags = courseCataSubjectDrafts.stream()
                .map(s -> QuestionBizDTO.of(s.getCataId(), s.getSubjectId())).collect(Collectors.toList());
        //2.删除练习和题目之间的关系
        if (CollUtils.isEmpty(courseCataSubjectDrafts)) {
            // 草稿中没有题目，直接结束
            return;
        }
        //3.将新的练习和题目之间的关系上架
        quizClient.saveQuestionBizInfoBatch(tags);
        //4.删除草稿
        int result = courseCataSubjectDraftMapper.deleteByCourseId(courseId);
        if (result != courseCataSubjectDrafts.size()) {
            throw new DbException(ErrorInfo.Msg.DB_UPDATE_EXCEPTION);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {DbException.class, Exception.class})
    public void copyToShelf(Long courseId, Boolean isFirstShelf) {
        //1.从草稿中查出目录信息
        List<ComicEpisodeDraft> comicEpisodeDrafts = baseMapper.getByCourseId(courseId);
        List<ComicEpisode> comicEpisodes = BeanUtils.copyList(comicEpisodeDrafts, ComicEpisode.class);
        //2.保存数据上架
        boolean result = comicEpisodeService.saveOrUpdateBatch(comicEpisodes);
        if (!result) {
            throw new DbException(ErrorInfo.Msg.DB_UPDATE_EXCEPTION);
        }
        //3.删除草稿
        int deleteResult = comicEpisodeDraftMapper.deleteByCourseId(courseId, Arrays.asList(
                ComicConstants.EpisodeType.CHAPTER,
                ComicConstants.EpisodeType.CLIP,
                ComicConstants.EpisodeType.PRATICE
        ));
        if (deleteResult != comicEpisodeDrafts.size()) {
            throw new DbException(ErrorInfo.Msg.DB_UPDATE_EXCEPTION);
        }
    }

    @Override
    public Map<Long, Integer> calculateMediaDuration(Long courseId) {
        LambdaQueryWrapper<ComicEpisodeDraft> queryWrapper =
                Wrappers.lambdaQuery(ComicEpisodeDraft.class)
                        .eq(ComicEpisodeDraft::getCourseId, courseId)
                        .eq(ComicEpisodeDraft::getType, ComicConstants.EpisodeType.CLIP);
        List<ComicEpisodeDraft> list = list(queryWrapper);
        if (CollUtils.isEmpty(list)) {
            return new HashMap<>();
        }
        return list.stream()
                .collect(Collectors.groupingBy(
                        ComicEpisodeDraft::getParentCatalogueId,
                        Collectors.summingInt(ComicEpisodeDraft::getMediaDuration)));
    }

    @Override
    public Integer totalSectionNums(Long courseId) {
        LambdaQueryWrapper<ComicEpisodeDraft> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ComicEpisodeDraft::getCourseId, courseId)
                .in(ComicEpisodeDraft::getType,
                        Arrays.asList(ComicConstants.EpisodeType.CLIP, ComicConstants.EpisodeType.PRATICE));
        return count(queryWrapper);
    }

    @Override
    public List<Long> queryCataIdsOfCourse(Long courseId, List<Integer> types) {
        //1.查询条件
        LambdaQueryWrapper<ComicEpisodeDraft> queryWrapper =
                Wrappers.lambdaQuery(ComicEpisodeDraft.class)
                        .eq(ComicEpisodeDraft::getCourseId, courseId)
                        .in(ComicEpisodeDraft::getType, types);
        //2.查询数据
        List<ComicEpisodeDraft> comicEpisodeDrafts = baseMapper.selectList(queryWrapper);
        //3.返回数据
        return CollUtils.isEmpty(comicEpisodeDrafts)
                ? new ArrayList<>()
                : comicEpisodeDrafts
                .stream()
                .map(ComicEpisodeDraft::getId)
                .collect(Collectors.toList());
    }


    /**
     * 校验已经上架漫剧的章节是否被调动或删除
     *
     * @param comicEpisodes 已上架的
     * @param episodeSaveDTOS     前端传来要保存的目录列表
     */
    private void checkIndex(List<EpisodeSaveDTO> episodeSaveDTOS, List<ComicEpisode> comicEpisodes) {

        //校验章序号是否有重复的
        Map<Integer, EpisodeSaveDTO> collect = episodeSaveDTOS.stream().collect(Collectors.toMap(EpisodeSaveDTO::getIndex, p -> p));
        if (collect.size() < episodeSaveDTOS.size()) { //有重复的章序号
            throw new BizIllegalException(ComicErrorInfo.Msg.COMIC_EPISODES_SAVE_CHAPTER_INDEX_REPEAT);
        }
        //episodeSaveDTOS 是按升序排的 最大的章序号大于章数量，说明章列表episodeSaveDTOS的序号有间断设置
        if (episodeSaveDTOS.get(episodeSaveDTOS.size() - 1).getIndex() > episodeSaveDTOS.size()) {
            throw new BizIllegalException(ComicErrorInfo.Msg.COMIC_EPISODES_SAVE_CHAPTER_INDEX_INTERRUPTED);
        }

        if (CollUtils.isEmpty(comicEpisodes)) {
            return;
        }
        final Map<Long, Integer> saveIndexMap = new HashMap<>();
        for (EpisodeSaveDTO episodeSaveDTO : episodeSaveDTOS) {//章的序号
            if (episodeSaveDTO.getId() != null) {
                saveIndexMap.put(episodeSaveDTO.getId(), episodeSaveDTO.getIndex());
            }

            //片段和练习的序号
            if (CollUtils.isEmpty(episodeSaveDTO.getSections())) { //章没有对应的片段
                throw new BizIllegalException(ComicErrorInfo.Msg.COMIC_EPISODES_SAVE_CHAPTER_WITHOUT_CLIP);
            }
            AtomicInteger count = new AtomicInteger(1);
            //片段需要排序，练习不需要排序
            episodeSaveDTO.getSections().stream().filter(section -> section.getType() == ComicConstants.EpisodeType.CLIP)
                    .forEach(section -> {
                        if (section.getId() == null) {
                            //序号从1开始
                            saveIndexMap.put(section.getId(), count.incrementAndGet());
                        }
                    });
        }

        for (ComicEpisode comicEpisode : comicEpisodes) {
            if (comicEpisode.getType() != ComicConstants.EpisodeType.CHAPTER) {
                continue;
            }
            Integer index = saveIndexMap.get(comicEpisode.getId());
            if (index == null) {
                throw new BizIllegalException(StringUtils.format(ComicErrorInfo.Msg.COMIC_EPISODES_SAVE_CHAPTER_NAME_DELETED, comicEpisode.getName()));
            }
            //章排序
            if (!index.equals(comicEpisode.getCIndex())) {
                throw new BizIllegalException(StringUtils.format(ErrorInfo.Msg.OPERATE_FAILED, comicEpisode.getName()));
            }
        }
    }

    /**
     * 组装数据,数据优先级，本次保存数据>草稿中的数据>已上架数据
     * 通过map来实现数据的覆盖，map key的目录id，value是目录信息
     * 1.先将已上架的数据放入map
     * 2.将草稿中的数据放入map，放入的过程中草稿中和已上架都存在，草稿中的数据会自动覆盖掉已上架数据
     * 3.遍历本次保存数据，
     * 3.1 如果目录信息没有id，生成一个
     * 3.2 在map中获取目录信息，没有找到生成一个目录草稿类
     * 3.3 向目录信息中添加要保存的数据 序号、名称、片段id、目录id
     *
     * @param courseId     漫剧id
     * @param episodeSaveDTOS 漫剧目录列表
     * @return 数据库中存储的漫剧目录列表
     */
    private List<ComicEpisodeDraft> packageCatalogue(Long courseId, List<EpisodeSaveDTO> episodeSaveDTOS, List<ComicEpisode> shelfComicEpisodes) {
        final Map<Long, ComicEpisodeDraft> savedMap = new HashMap<>();
        if (CollUtils.isNotEmpty(shelfComicEpisodes)) {
            //将已经上架的目录copy下来
            shelfComicEpisodes.stream().forEach(comicEpisode ->
                    savedMap.put(comicEpisode.getId(), BeanUtils.toBean(comicEpisode, ComicEpisodeDraft.class)));
        }
        //获取已经保存的草稿
        LambdaQueryWrapper<ComicEpisodeDraft> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ComicEpisodeDraft::getCourseId, courseId);
        List<ComicEpisodeDraft> savedComicEpisodesDraft = list(queryWrapper);
        if (CollUtils.isNotEmpty(savedComicEpisodesDraft)) {
            //存在草稿覆盖已经上架的漫剧
            savedComicEpisodesDraft.stream().forEach(comicEpisodeDraft ->
                    savedMap.put(comicEpisodeDraft.getId(), comicEpisodeDraft));
        }
        //需要保存的草稿
        List<ComicEpisodeDraft> comicEpisodeDrafts = new ArrayList<>();
        for (EpisodeSaveDTO episodeSaveDTO : episodeSaveDTOS) {
            Long chapterId = episodeSaveDTO.getId() == null ? IdWorker.getId() : episodeSaveDTO.getId();
            //章目录
            ComicEpisodeDraft comicEpisodeDraft = savedMap.get(chapterId);
            if (comicEpisodeDraft == null) { //未保存过
                comicEpisodeDraft = new ComicEpisodeDraft();
                comicEpisodeDraft.setId(chapterId);
            }

            //设置目录添加或修改时的基本信息
            comicEpisodeDraft.setCataBaseInfo(episodeSaveDTO.getIndex(), episodeSaveDTO.getName(), ComicConstants.EpisodeType.CHAPTER,
                    0L, courseId);
            comicEpisodeDrafts.add(comicEpisodeDraft);
            //片段练习目录，片段有需要，练习没有需要
            //序号
            AtomicInteger indexCount = new AtomicInteger(0);
            episodeSaveDTO.getSections().stream().forEach(section -> {
                Long sectionId = section.getId() == null ? IdWorker.getId() : section.getId();
                //片段练习目录
                ComicEpisodeDraft comicEpisodeDraftSection = savedMap.get(sectionId);
                if (comicEpisodeDraftSection == null) { //未保存过
                    comicEpisodeDraftSection = new ComicEpisodeDraft();
                    comicEpisodeDraftSection.setId(sectionId);
                }
                Integer index = section.getType() == ComicConstants.EpisodeType.CLIP ?
                        indexCount.incrementAndGet() : null;

                //设置目录添加或修改时的基本信息
                comicEpisodeDraftSection.setCataBaseInfo(index, section.getName(), section.getType(),
                        chapterId, courseId);
                comicEpisodeDrafts.add(comicEpisodeDraftSection);
            });
        }
        return comicEpisodeDrafts;
    }

    private List<EpisodeVO> queryComicEpisodes(Long courseId, Boolean withPractice) {
        LambdaQueryWrapper<ComicEpisodeDraft> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ComicEpisodeDraft::getCourseId, courseId);
        if (!withPractice) {
            queryWrapper.in(ComicEpisodeDraft::getType,
                    Arrays.asList(ComicConstants.EpisodeType.CHAPTER, ComicConstants.EpisodeType.CLIP));
        }
        //根据类型和排序，按照
        queryWrapper.last(" order by type,c_index");
        List<ComicEpisodeDraft> comicEpisodeDrafts = baseMapper.selectList(queryWrapper);
        if (CollUtils.isEmpty(comicEpisodeDrafts)) {
            return null;
        }
        ComicDraft comicDraft = comicDraftService.getById(courseId);

        // 最大上架数,待上架设置空map，已上架需要排序并去片段序号（同一个章中）中最大片段
        Map<Long, ComicEpisodeDraft> chapterIdAndMaxSectionMap =
                (comicDraft.getStatus() == ComicStatus.NO_UP_SHELF.getStatus())
                        ? new HashMap<>() :
                        comicEpisodeDrafts.parallelStream()
                                .filter(ccd -> ccd.getType() == ComicConstants.EpisodeType.CLIP && !ccd.getCanUpdate())
                                .collect(Collectors.groupingBy(ComicEpisodeDraft::getParentCatalogueId,
                                        Collectors.collectingAndThen(
                                                Collectors.reducing(
                                                        (c1, c2) -> c2.getCIndex().compareTo(c1.getCIndex()) > 0 ? c2 : c1),
                                                Optional::get)));
        int maxChapterIndex = (comicDraft.getStatus() == ComicStatus.NO_UP_SHELF.getStatus())
                ? 0
                : comicEpisodeDrafts.stream()
                .filter(ccd -> ccd.getType() == ComicConstants.EpisodeType.CHAPTER && !ccd.getCanUpdate())
                .map(ComicEpisodeDraft::getCIndex)
                .max(Integer::compare).get();


        // 4.查询漫剧对应的片段和题目信息
        List<ComicEpisodeTagDraft> tags = courseCataSubjectDraftMapper.getByCourseId(courseId);
        // 4.1.统计题目数量
        Map<Long, Long> cataIdAndNumMap = CollUtils.isEmpty(tags) ? new HashMap<>() :
                tags.stream().collect(Collectors.groupingBy(ComicEpisodeTagDraft::getCataId, Collectors.counting()));
        // 4.2.查询分数
        Map<Long, Integer> cataIdAndTotalScoreMap = new HashMap<>(cataIdAndNumMap.size());
        if (CollUtils.isNotEmpty(tags)) {
            Set<Long> questionIds = tags.stream().map(ComicEpisodeTagDraft::getSubjectId).collect(Collectors.toSet());
            Map<Long, Integer> scoreMap = quizClient.queryQuestionScores(questionIds);
            cataIdAndTotalScoreMap.putAll(
                    tags.stream().collect(Collectors.groupingBy(
                            ComicEpisodeTagDraft::getCataId,
                            Collectors.summingInt(d -> scoreMap.get(d.getSubjectId()))
                    )));
        }
        return TreeDataUtils.parseToTree(comicEpisodeDrafts, EpisodeVO.class, (catalogueDraft, vo) -> {
            int maxIndexOnShelf = 0;
            int maxSectionIndexOnShelf = 0;
            if (catalogueDraft.getType() == ComicConstants.EpisodeType.CLIP) {
                //片段最大编辑数
                ComicEpisodeDraft comicEpisodeDraft = chapterIdAndMaxSectionMap.get(catalogueDraft.getParentCatalogueId());
                maxIndexOnShelf = NumberUtils.null2Zero(
                        comicEpisodeDraft == null ? 0 : comicEpisodeDraft.getCIndex());
            } else if (catalogueDraft.getType() == ComicConstants.EpisodeType.CHAPTER) {
                maxIndexOnShelf = maxChapterIndex;
                ComicEpisodeDraft comicEpisodeDraft = chapterIdAndMaxSectionMap.get(catalogueDraft.getId());
                maxSectionIndexOnShelf = NumberUtils.null2Zero(
                        comicEpisodeDraft == null ? 0 : comicEpisodeDraft.getCIndex());
            }
            vo.setIndex(catalogueDraft.getCIndex());
            vo.setMediaName(catalogueDraft.getVideoName());
            vo.setSubjectNum(NumberUtils.null2Zero(cataIdAndNumMap.get(catalogueDraft.getId())).intValue()); //练习总数量
            vo.setTotalScore(NumberUtils.null2Zero(cataIdAndTotalScoreMap.get(catalogueDraft.getId()))); //练习总分数
            vo.setMaxIndexOnShelf(maxIndexOnShelf);
            vo.setMaxSectionIndexOnShelf(maxSectionIndexOnShelf);
        }, new ComicEpisodeDraftDataWrapper());
    }

    /**
     * 校验练习id列表
     * 1.校验本漫剧所有的练习都添加了题目
     * 2.校验上传的数据中有其他漫剧的数据，
     * 前端传过来的章节id列表为A，漫剧所有片段id和练习id列表为B，漫剧所有练习id列表为c
     * 原理：1. 判断A是不是B的子集，如果A不是B的子集，则前端传过来的id列表中有非当前漫剧的片段或练习
     * 2.判断c是A的子集，如果c不是A的子集，说明前端有漏传的该漫剧的练习id
     *
     * @param cataIds  前端传过来的片段id列表或者练习列表
     * @param courseId 漫剧id
     */
    private void checkPracticeIds(List<Long> cataIds, Long courseId) {
        //查询所有片段和练习的目录列表
        LambdaQueryWrapper<ComicEpisodeDraft> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ComicEpisodeDraft::getCourseId, courseId).in(ComicEpisodeDraft::getType,
                Arrays.asList(ComicConstants.EpisodeType.CLIP, ComicConstants.EpisodeType.PRATICE));
        List<ComicEpisodeDraft> comicEpisodeDrafts = baseMapper.selectList(queryWrapper);
        if (CollUtils.isEmpty(comicEpisodeDrafts)) {
            throw new BizIllegalException(ComicErrorInfo.Msg.COMIC_EPISODE_NOT_EXISTS);
        }
        //所有片段和练习的目录id列表
        List<Long> allCataIdList = comicEpisodeDrafts
                .stream()
                .map(ComicEpisodeDraft::getId)
                .collect(Collectors.toList());

        //判断前端传过来的章节id列表是该漫剧所有片段和练习集合的子集，
        // 如果不是子集，说明前端传过来的章节id不属于当前漫剧
        if (!CollUtils.containsAll(allCataIdList, cataIds)) {
            log.error("传过来了其他章节的章节id，courseId:{},cataIds:{}", courseId, cataIds);
            throw new BizIllegalException(ComicErrorInfo.Msg.COMIC_MEDIA_SAVE_ILLEGAL);
        }
        //所有练习的id列表
        List<Long> practiceIdList = comicEpisodeDrafts.stream().filter(practice -> practice.getType() == ComicConstants.EpisodeType.PRATICE).
                map(ComicEpisodeDraft::getId).collect(Collectors.toList());
        //判断前端传过来的章节id列表是否符合是漫剧练习id父集合
        if (!CollUtils.containsAll(cataIds, practiceIdList)) {
            throw new BizIllegalException(ComicErrorInfo.Msg.COMIC_TAG_SAVE_TAG_IDS_NULL);
        }
    }

    /**
     * 校验片段或练习id列表
     * 1.校验本漫剧所有的片段都添加了视频
     * 2.校验上传的数据中有其他漫剧的数据，
     * 原理：1.先比较接口传递过来的数据和数据库中的片段总数量一致,不一致返回失败
     * 2.总数量一致，再求两者的交集，交集集合长度和前端传过来的数量一致，则通过
     *
     * @param cataIds  前端传过来的片段id列表或者练习列表
     * @param courseId 漫剧id
     */
    private void checkSectionIds(List<Long> cataIds, Long courseId) {
        //1.数据库片段查询条件
        LambdaQueryWrapper<ComicEpisodeDraft> queryWrapper =
                Wrappers.lambdaQuery(ComicEpisodeDraft.class)
                        .eq(ComicEpisodeDraft::getType, ComicConstants.EpisodeType.CLIP)
                        .eq(ComicEpisodeDraft::getCourseId, courseId);
        //2.查询片段
        List<ComicEpisodeDraft> comicEpisodeDrafts = baseMapper.selectList(queryWrapper);

        //3.判断数据库中的片段和前端传来的片段数量是否一致
        if (CollUtils.size(comicEpisodeDrafts) != CollUtils.size(cataIds)) {
            throw new BizIllegalException(ComicErrorInfo.Msg.COMIC_MEDIA_SAVE_MEDIA_NULL);
        }
        //4.转化出数据库中漫剧片段id列表
        List<Long> cataIdsInDb =
                comicEpisodeDrafts
                        .stream()
                        .map(ComicEpisodeDraft::getId)
                        .collect(Collectors.toList());
        //5.取前端传来片段id列表和数据库中片段id列表交集
        Collection<Long> cataIdsOfIntersection = CollUtils.intersection(cataIds, cataIdsInDb);
        //6.判断片段交集id列表数量和前端传来片段id列表数量是否一致
        if (cataIdsOfIntersection.size() != cataIds.size()) {
            throw new BizIllegalException(ComicErrorInfo.Msg.COMIC_MEDIA_SAVE_MEDIA_NULL);
        }
    }

    /**
     * 根据漫剧id统计每个大章的素材总时长
     *
     * @param courseId
     * @return
     */
    private List<ComicEpisodeDraft> calculateCatalogMediaDuration(Long courseId) {
        //1.查询条件
        LambdaQueryWrapper<ComicEpisodeDraft> queryWrapper =
                Wrappers.lambdaQuery(ComicEpisodeDraft.class)
                        .eq(ComicEpisodeDraft::getCourseId, courseId)
                        .eq(ComicEpisodeDraft::getType, ComicConstants.EpisodeType.CLIP);
        //2.查询数据
        List<ComicEpisodeDraft> comicEpisodeDrafts = baseMapper.selectList(queryWrapper);
        if (CollUtils.isEmpty(comicEpisodeDrafts)) {
            return new ArrayList<>();
        }
        //3.统计每个章的课时总时长
        Map<Long, Integer> capthIdAndMediaDurationMap =
                comicEpisodeDrafts.stream().collect(Collectors.groupingBy(
                        ComicEpisodeDraft::getParentCatalogueId,
                        Collectors.summingInt(ComicEpisodeDraft::getMediaDuration)));
        //4.封装数据
        return capthIdAndMediaDurationMap.keySet()
                .stream()
                .map(key -> ComicEpisodeDraft.builder()
                        .id(key)
                        .mediaDuration(capthIdAndMediaDurationMap.get(key))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 漫剧目录草稿树型数据转换器
     */
    private class ComicEpisodeDraftDataWrapper implements TreeDataUtils.DataProcessor<EpisodeVO, ComicEpisodeDraft> {

        @Override
        public Object getParentKey(ComicEpisodeDraft comicEpisodeDraft) {
            return comicEpisodeDraft.getParentCatalogueId();
        }

        @Override
        public Object getKey(ComicEpisodeDraft comicEpisodeDraft) {
            return comicEpisodeDraft.getId();
        }

        @Override
        public Object getRootKey() {
            return 0L;
        }

        @Override
        public List<EpisodeVO> getChild(EpisodeVO cataVO) {
            return cataVO.getSections();
        }

        @Override
        public void setChild(EpisodeVO parent, List<EpisodeVO> child) {
            parent.setSections(child);
        }
    }
}
