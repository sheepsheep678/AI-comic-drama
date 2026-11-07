package com.aicomic.comic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aicomic.api.client.quiz.QuizClient;
import com.aicomic.api.dto.comic.EpisodeDTO;
import com.aicomic.api.dto.comic.MediaQuoteDTO;
import com.aicomic.api.dto.comic.ClipInfoDTO;
import com.aicomic.api.dto.quiz.QuestionBizDTO;
import com.aicomic.common.exceptions.BizIllegalException;
import com.aicomic.common.utils.*;
import com.aicomic.comic.constants.ComicConstants;
import com.aicomic.comic.constants.ComicErrorInfo;
import com.aicomic.comic.domain.po.ComicEpisode;
import com.aicomic.comic.domain.vo.EpisodeSimpleInfoVO;
import com.aicomic.comic.domain.vo.EpisodeVO;
import com.aicomic.comic.mapper.ComicEpisodeMapper;
import com.aicomic.comic.properties.ComicProperties;
import com.aicomic.comic.service.IComicEpisodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 目录草稿 服务实现类
 * </p>
 *
 * @since 2022-07-19
 */
@Service
public class ComicEpisodeServiceImpl extends ServiceImpl<ComicEpisodeMapper, ComicEpisode> implements IComicEpisodeService {

    @Autowired
    private ComicProperties comicProperties;

    @Autowired
    private QuizClient quizClient;

    @Override
    public List<EpisodeDTO> queryComicEpisodes(Long courseId, Boolean withPractice) {
        //1.漫剧目录查询条件
        LambdaQueryWrapper<ComicEpisode> queryWrapper =
                Wrappers.lambdaQuery(ComicEpisode.class)
                        .eq(ComicEpisode::getCourseId, courseId);
        if (!withPractice) {
            //1.1漫剧目录不带练习设置目录查询类型
            queryWrapper.in(ComicEpisode::getType,
                    Arrays.asList(ComicConstants.EpisodeType.CLIP,
                            ComicConstants.EpisodeType.CHAPTER));
        }
        //1.2根据目录类型和序号排序
        queryWrapper.last(" order by type,c_index");
        //2.查询漫剧目录列表
        List<ComicEpisode> comicEpisodes = baseMapper.selectList(queryWrapper);
        if (CollUtils.isEmpty(comicEpisodes)) {
            return null;
        }
        //3.查询漫剧目录对应题目数量
        Set<Long> ids = comicEpisodes.stream().map(ComicEpisode::getId).collect(Collectors.toSet());
        List<QuestionBizDTO> questionBizDTOS = quizClient.queryQuestionIdsByBizIds(ids);
        //4.转化目录id和题目id、分数对应关系
        Map<Long, Long> cataIdAndNumMap =
                CollUtils.isEmpty(questionBizDTOS)
                        ? new HashMap<>() :
                        questionBizDTOS
                                .stream()
                                .collect(Collectors.groupingBy(QuestionBizDTO::getBizId, Collectors.counting()));
        // 5.组织树结构并返回
        return TreeDataUtils.parseToTree(comicEpisodes, EpisodeDTO.class, (comicEpisode, cataVO)->{
            cataVO.setMediaName(comicEpisode.getVideoName());
            cataVO.setIndex(comicEpisode.getCIndex());
            cataVO.setSubjectNum(cataIdAndNumMap.getOrDefault(comicEpisode.getId(), 0L).intValue());
        }, new ComicEpisodeDataWrapper());
    }

    @Override
    public List<MediaQuoteDTO> countMediaUserInfo(List<Long> mediaIds) {
        //1.判断素材id列表为空判断
        if (CollUtils.isEmpty(mediaIds)) {
            return CollUtils.emptyList();
        }
        //2.漫剧目录查询条件
        LambdaQueryWrapper<ComicEpisode> queryWrapper =
                Wrappers.lambdaQuery(ComicEpisode.class)
                .in(ComicEpisode::getMediaId, mediaIds);
        //2.1查询漫剧目录列表
        List<ComicEpisode> comicEpisodes = baseMapper.selectList(queryWrapper);
        //3.未查询到素材对应的漫剧目录
        if (CollUtils.isEmpty(comicEpisodes)) {
            //3.1素材都未被引用，都给一个引用次数0
            return mediaIds.stream()
                    .map(mediaId -> new MediaQuoteDTO(mediaId, 0))
                    .collect(Collectors.toList());
        }
        //4.分组统计素材被引用次数
        Map<Long, Long> mediaAndCount =
                comicEpisodes.stream()
                .collect(Collectors.groupingBy(
                        ComicEpisode::getMediaId, Collectors.counting()));
        //5.组装数据，设置
        return mediaIds.stream().map(
                mediaId -> new MediaQuoteDTO(mediaId,
                        NumberUtils.null2Zero(mediaAndCount.get(mediaId))
                                .intValue())
        ).collect(Collectors.toList());
    }

    @Override
    public ClipInfoDTO getSimpleSectionInfo(Long sectionId) {
        //1.片段id判空
        if (sectionId == null) {
            throw new BizIllegalException(ComicErrorInfo.Msg.CATEGORY_QUERY_ID_NULL);
        }
        //2.获取片段对应的目录信息
        ComicEpisode comicEpisode = baseMapper.selectById(sectionId);
        if (comicEpisode == null) {
            return new ClipInfoDTO();
        }
        //3.判断目录类型是否为片段
        if (comicEpisode.getType() != ComicConstants.EpisodeType.CLIP) {
            return new ClipInfoDTO();
        }
        //4.组装数据
        ClipInfoDTO sectionInfoDTO = BeanUtils.toBean(comicEpisode, ClipInfoDTO.class);
        //5.设置免费试看时长
        sectionInfoDTO.setFreeDuration(comicEpisode.getTrailer() == 1 ?
                comicProperties.getMedia().getTrailerDuration() : 0);
        return sectionInfoDTO;
    }

    @Override
    public List<EpisodeSimpleInfoVO> getCatasIndexList(Long courseId) {
        //1.漫剧目录（不含练习）查询条件
        LambdaQueryWrapper<ComicEpisode> queryWrapper =
                Wrappers.lambdaQuery(ComicEpisode.class)
                        .eq(ComicEpisode::getCourseId, courseId)
                        .in(ComicEpisode::getType, Arrays.asList(
                                ComicConstants.EpisodeType.CHAPTER,
                                ComicConstants.EpisodeType.CLIP
                        ));
        //1.1查询漫剧目录
        List<ComicEpisode> comicEpisodes = baseMapper.selectList(queryWrapper);
        if (CollUtils.isEmpty(comicEpisodes)) {
            return new ArrayList<>();
        }
        //2.章id与章序号映射关系
        Map<Long, Integer> chapterMap =
                comicEpisodes
                        .stream()
                        .filter(comicEpisode -> comicEpisode.getType() == ComicConstants.EpisodeType.CHAPTER)
                        .collect(Collectors.toMap(ComicEpisode::getId, ComicEpisode::getCIndex));
        //3.遍历漫剧目录，组装数据
        return comicEpisodes.stream()
                .filter(comicEpisode -> comicEpisode.getType() != ComicConstants.EpisodeType.CHAPTER)

                .map(comicEpisode -> {
                    //3.1组装目录序号
                    String index = StringUtils.format("{}-{}",
                            chapterMap.get(comicEpisode.getParentCatalogueId()),
                            comicEpisode.getCIndex());
                    //3.2组装目录信息，目录id，目录名称，目录序号
                    return new EpisodeSimpleInfoVO(comicEpisode.getId(),
                            comicEpisode.getName(), index, comicEpisode.getCIndex(), null);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<EpisodeSimpleInfoVO> getManyCataSimpleInfo(List<Long> ids) {
        // 1.判空
        if(CollUtils.isEmpty(ids)){
            return CollUtils.emptyList();
        }
        //2.查询条件
        LambdaQueryWrapper<ComicEpisode> queryWrapper =
                Wrappers.lambdaQuery(ComicEpisode.class)
                .in(ComicEpisode::getId, ids);
        //3.查询数据
        List<ComicEpisode> comicEpisodes = baseMapper.selectList(queryWrapper);
        //4.数据转化
        return BeanUtils.copyList(comicEpisodes, EpisodeSimpleInfoVO.class);
    }

    @Override
    public EpisodeSimpleInfoVO querySectionInfoById(Long id) {
        // 1.查询数据
        ComicEpisode currentComicEpisode = baseMapper.selectById(id);
        // 2.判断是否是片段
        if(currentComicEpisode != null
                && currentComicEpisode.getType() == ComicConstants.EpisodeType.CLIP) {
            // 2.1.转换数据
            EpisodeSimpleInfoVO episodeSimpleInfoVO = BeanUtils.toBean(currentComicEpisode, EpisodeSimpleInfoVO.class);
            // 3.查询章信息
            ComicEpisode comicEpisode = baseMapper.selectById(currentComicEpisode.getParentCatalogueId());
            // 3.1.章id
            episodeSimpleInfoVO.setChapterIndex(comicEpisode.getCIndex());
            return episodeSimpleInfoVO;
        }
        // 4.返回空数据
        return new EpisodeSimpleInfoVO();
    }

    @Override
    public List<EpisodeVO> queryComicEpisodesVO(Long courseId, Boolean withPractice) {
        //1.漫剧目录查询条件
        LambdaQueryWrapper<ComicEpisode> queryWrapper =
                Wrappers.lambdaQuery(ComicEpisode.class)
                        .eq(ComicEpisode::getCourseId, courseId);
        if (!withPractice) {
            //1.1漫剧目录不带练习设置目录查询类型
            queryWrapper.in(ComicEpisode::getType,
                    Arrays.asList(ComicConstants.EpisodeType.CLIP,
                            ComicConstants.EpisodeType.CHAPTER));
        }
        //1.2根据目录类型和序号排序
        queryWrapper.last(" order by type,c_index");
        //2.查询漫剧目录列表
        List<ComicEpisode> comicEpisodes = baseMapper.selectList(queryWrapper);
        if (CollUtils.isEmpty(comicEpisodes)) {
            return null;
        }

        //3.查询漫剧目录id、题目id及分数列表
        Set<Long> ids = comicEpisodes.stream().map(ComicEpisode::getId).collect(Collectors.toSet());
        List<QuestionBizDTO> questionBizDTOS = quizClient.queryQuestionIdsByBizIds(ids);
        //4.转化目录id和题目id、分数对应关系
        Map<Long, Long> cataIdAndNumMap =
                CollUtils.isEmpty(questionBizDTOS)
                        ? new HashMap<>() :
                        questionBizDTOS
                                .stream()
                                .collect(Collectors.groupingBy(QuestionBizDTO::getBizId, Collectors.counting()));
        //5.转化录id和题目id、总分数关系
        Map<Long, Integer> cataIdAndTotalScoreMap = quizClient.queryQuestionScoresByBizIds(ids);
        //6.数据目录结构转化
        List<EpisodeVO> episodeVOS =
                TreeDataUtils.parseToTree(comicEpisodes, EpisodeVO.class,
                        (comicEpisode, cataVO) -> {
                            //6.1设置素材名称
                            cataVO.setMediaName(comicEpisode.getVideoName());
                            //6.2设置目录索引
                            cataVO.setIndex(comicEpisode.getCIndex());
                            //6.3设置题目数量
                            cataVO.setSubjectNum(NumberUtils.null2Zero(
                                    cataIdAndNumMap.get(comicEpisode.getId()))
                                    .intValue()); //练习总数量
                            //6.4设置题目总分数
                            cataVO.setTotalScore(NumberUtils.null2Zero(
                                    cataIdAndTotalScoreMap.get(comicEpisode.getId()))); //练习总分数
                        }, new ComicEpisodeDataWrapper2());

        return episodeVOS;
    }

    //漫剧目录树形转化模型
    private static class ComicEpisodeDataWrapper implements TreeDataUtils.DataProcessor<EpisodeDTO, ComicEpisode> {


        @Override
        public Object getParentKey(ComicEpisode comicEpisode) {
            return comicEpisode.getParentCatalogueId();
        }

        @Override
        public Object getKey(ComicEpisode comicEpisode) {
            return comicEpisode.getId();
        }

        @Override
        public Object getRootKey() {
            return 0L;
        }

        @Override
        public List<EpisodeDTO> getChild(EpisodeDTO catalogueDTO) {
            return catalogueDTO.getSections();
        }

        @Override
        public void setChild(EpisodeDTO parent, List<EpisodeDTO> child) {
            parent.setSections(child);
        }
    }

    //漫剧目录树形转化模型
    private static class ComicEpisodeDataWrapper2 implements TreeDataUtils.DataProcessor<EpisodeVO, ComicEpisode> {


        @Override
        public Object getParentKey(ComicEpisode comicEpisode) {
            return comicEpisode.getParentCatalogueId();
        }

        @Override
        public Object getKey(ComicEpisode comicEpisode) {
            return comicEpisode.getId();
        }

        @Override
        public Object getRootKey() {
            return 0L;
        }

        @Override
        public List<EpisodeVO> getChild(EpisodeVO catalogueDTO) {
            return catalogueDTO.getSections();
        }

        @Override
        public void setChild(EpisodeVO parent, List<EpisodeVO> child) {
            parent.setSections(child);
        }
    }
}
