package com.aicomic.comic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aicomic.api.client.quiz.QuizClient;
import com.aicomic.api.client.watch.WatchClient;
import com.aicomic.api.client.order.OrderClient;
import com.aicomic.api.client.user.UserClient;
import com.aicomic.api.dto.comic.ComicDTO;
import com.aicomic.api.dto.comic.ComicPurchaseInfoDTO;
import com.aicomic.api.dto.quiz.QuestionBizDTO;
import com.aicomic.api.dto.user.UserDTO;
import com.aicomic.common.autoconfigure.mq.RabbitMqHelper;
import com.aicomic.common.constants.ErrorInfo;
import com.aicomic.common.constants.MqConstants;
import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.common.exceptions.BadRequestException;
import com.aicomic.common.exceptions.BizIllegalException;
import com.aicomic.common.exceptions.DbException;
import com.aicomic.common.utils.*;
import com.aicomic.comic.constants.ComicConstants;
import com.aicomic.comic.constants.ComicErrorInfo;
import com.aicomic.comic.constants.ComicStatus;
import com.aicomic.comic.domain.dto.ComicBaseInfoSaveDTO;
import com.aicomic.comic.domain.dto.ComicPageQuery;
import com.aicomic.comic.domain.po.*;
import com.aicomic.comic.domain.vo.ComicBaseInfoVO;
import com.aicomic.comic.domain.vo.ComicPageVO;
import com.aicomic.comic.domain.vo.ComicSaveVO;
import com.aicomic.comic.domain.vo.NameExistVO;
import com.aicomic.comic.mapper.*;
import com.aicomic.comic.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ValidatorFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 草稿漫剧 服务实现类
 * </p>
 *
 * @since 2022-07-18
 */
@Service
public class ComicDraftServiceImpl extends ServiceImpl<ComicDraftMapper, ComicDraft> implements IComicDraftService {

    @Autowired
    private ComicMapper courseMapper;

    @Autowired
    private IComicService courseService;

    @Autowired
    private ComicContentDraftMapper courseContentDraftMapper;

    @Autowired
    private ComicContentMapper courseContentMapper;

    @Autowired
    private ValidatorFactory validatorFactory;

    @Autowired
    private IComicEpisodeDraftService comicEpisodeDraftService;

    @Autowired
    private IComicCreatorDraftService comicCreatorDraftService;

    @Autowired
    private ComicEpisodeDraftMapper comicEpisodeDraftMapper;

    @Autowired
    private ComicCreatorDraftMapper comicCreatorDraftMapper;

    @Autowired
    private ComicEpisodeTagDraftMapper courseCataSubjectDraftMapper;

    @Autowired
    private UserClient userClient;

    @Autowired
    private ICategoryService categoryService;

    @Autowired
    private RabbitMqHelper rabbitMqHelper;

    @Autowired
    private OrderClient orderClient;

    @Autowired
    private QuizClient quizClient;

    @Autowired
    private WatchClient learningClient;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {DbException.class, Exception.class})
    public ComicSaveVO save(ComicBaseInfoSaveDTO courseBaseInfoSaveDTO) {
        List<Long> categoryIdList = null;
        Comic course = null;
        //1.数据校验
        if (courseBaseInfoSaveDTO.getId() == null) {
            //1.1新增数据调起数据校验器
            ViolationUtils.process(validatorFactory.getValidator().validate(courseBaseInfoSaveDTO));
            //1.1.2.校验漫剧分类
            categoryIdList = categoryService.checkCategory(courseBaseInfoSaveDTO.getThirdCateId());
        } else {
            //1.2.未上架漫剧校验
            course = courseMapper.selectById(courseBaseInfoSaveDTO.getId());
            if (course == null) {
                //1.2.1.未上架漫剧校验请求参数
                ViolationUtils.process(validatorFactory.getValidator().validate(courseBaseInfoSaveDTO));
                //1.2.2.同名漫剧判空
                checkSameCourse(courseBaseInfoSaveDTO.getId(), courseBaseInfoSaveDTO.getName());
                //1.2.3.校验漫剧分类
                categoryIdList = categoryService.checkCategory(courseBaseInfoSaveDTO.getThirdCateId());
            }
        }

        ComicDraft comicDraft = new ComicDraft();
        //2.数据封装
        //2.1.content数据封装 漫剧介绍、漫剧细节、适用人群
        ComicContentDraft courseContentDraft = new ComicContentDraft();
        courseContentDraft.setCourseIntroduce(courseBaseInfoSaveDTO.getIntroduce());
        courseContentDraft.setCourseDetail(courseBaseInfoSaveDTO.getDetail());
        courseContentDraft.setUsePeople(courseBaseInfoSaveDTO.getUsePeople());
        //2.2.漫剧封面和漫剧下架时间
        comicDraft.setCoverUrl(courseBaseInfoSaveDTO.getCoverUrl());
        comicDraft.setPurchaseEndTime(courseBaseInfoSaveDTO.getPurchaseEndTime());
        //2.3.未上架数据封装，已上架漫剧不能修改字段
        if (course == null) {
            //2.3.1.漫剧价格
            comicDraft.setPrice(NumberUtils.null2Zero(courseBaseInfoSaveDTO.getPrice()));
            //2.3.2.漫剧有效期
            comicDraft.setValidDuration(courseBaseInfoSaveDTO.getValidDuration());
            //2.3.3.漫剧状态
            comicDraft.setStatus(ComicStatus.NO_UP_SHELF.getStatus());
            //2.3.4.一级漫剧分类id
            comicDraft.setFirstCateId(categoryIdList.get(0));
            //2.3.5.二级漫剧分类id
            comicDraft.setSecondCateId(categoryIdList.get(1));
            //2.3.6.三级漫剧分类id
            comicDraft.setThirdCateId(categoryIdList.get(2));
            //2.3.7.售卖模式
            comicDraft.setFree(courseBaseInfoSaveDTO.getFree() ? 1 : 0);
            //2.3.8.漫剧名称
            comicDraft.setName(courseBaseInfoSaveDTO.getName());
        }

        //3.操作
        if (courseBaseInfoSaveDTO.getId() == null) {
            //3.1.新增漫剧草稿
            //3.1.1.新生成漫剧id
            Long id = IdWorker.getId();
            //3.1.2.设置漫剧id
            courseContentDraft.setId(id);
            comicDraft.setId(id);
            //3.1.3.设置漫剧编辑进度
            comicDraft.setStep(ComicConstants.ComicStep.BASE_INFO);
            //3.1.4.插入漫剧草稿
            baseMapper.insert(comicDraft);
            //3.1.5.插入漫剧草稿内容
            courseContentDraftMapper.insert(courseContentDraft);
        } else {
            //3.2.编辑漫剧草稿
            //3.2.1.设置漫剧id
            courseContentDraft.setId(courseBaseInfoSaveDTO.getId());
            comicDraft.setId(courseBaseInfoSaveDTO.getId());
            //3.2.2.更新漫剧草稿
            baseMapper.updateById(comicDraft);
            //3.2.3.更新漫剧草稿内容
            courseContentDraftMapper.updateById(courseContentDraft);
        }
        //4.返回漫剧新增dto
        return ComicSaveVO
                .builder()
                .id(comicDraft.getId())
                .build();
    }

    @Override
    public ComicBaseInfoVO getCourseBaseInfo(Long id, Boolean see) {

        ComicBaseInfoVO comicBaseInfoVO = null;
        if (see) {
            //1.查询漫剧信息
            Comic course = courseMapper.selectById(id);
            if (course != null) {
                //1.1.查询漫剧对应的领取购买人数和退款人数
                ComicPurchaseInfoDTO comicPurchaseInfoDTO = orderClient.getPurchaseInfoOfCourse(id);
                //1.2.组装数据
                comicBaseInfoVO = BeanUtils.toBean(course, ComicBaseInfoVO.class);
                //1.3.查询漫剧内容
                ComicContent courseContent = courseContentMapper.selectById(id);
                //1.4.设置漫剧评分
                comicBaseInfoVO.setCoureScore(NumberUtils.div(NumberUtils.null2Zero(course.getScore()) * 1.0, 10, 2));
                //1.5.设置领取人数
                comicBaseInfoVO.setPurchaseNum(comicPurchaseInfoDTO.getPurchaseNum());
                //1.6.设置观看人数
                comicBaseInfoVO.setStudyNum(learningClient.countWatchEpisodeByCourse(id));
                //1.7.设置退款人数
                comicBaseInfoVO.setRefundNum(comicPurchaseInfoDTO.getRefundNum());
                //1.8.设置实付金额
                comicBaseInfoVO.setRealPayAmount(comicPurchaseInfoDTO.getRealPayAmount());
                //1.9.设置漫剧详情
                comicBaseInfoVO.setDetail(courseContent.getCourseDetail());
                //1.10.设置漫剧介绍
                comicBaseInfoVO.setIntroduce(courseContent.getCourseIntroduce());
                //1.11.设置漫剧适用人群
                comicBaseInfoVO.setUsePeople(courseContent.getUsePeople());
                //1.12.设置片段总数量
                comicBaseInfoVO.setCataTotalNum(course.getSectionNum());
            }
        }
        //2.查询草稿信息
        if (comicBaseInfoVO == null) {
            //2.1.查询草稿漫剧信息
            ComicDraft comicDraft = baseMapper.selectById(id);
            //2.2.有草稿漫剧信息
            if (comicDraft != null) {
                //2.3.组装漫剧信息
                comicBaseInfoVO = BeanUtils.toBean(comicDraft, ComicBaseInfoVO.class);
                //2.4.查询漫剧内容信息
                ComicContentDraft courseContentDraft = courseContentDraftMapper.selectById(id);
                //2.5.设置漫剧详情
                comicBaseInfoVO.setDetail(courseContentDraft.getCourseDetail());
                //2.6.设置漫剧介绍
                comicBaseInfoVO.setIntroduce(courseContentDraft.getCourseIntroduce());
                //2.7.适用人群
                comicBaseInfoVO.setUsePeople(courseContentDraft.getUsePeople());
                //2.8.漫剧章节数
                comicBaseInfoVO.setCataTotalNum(comicDraft.getSectionNum());
                //2.9.设置漫剧评分
                comicBaseInfoVO.setCoureScore(0d);
                //2.10.设置领取人数
                comicBaseInfoVO.setPurchaseNum(0);
                //2.11.设置观看人数
                comicBaseInfoVO.setStudyNum(0);
                //2.12.设置退款人数
                comicBaseInfoVO.setRefundNum(0);
                //2.13.设置实付金额
                comicBaseInfoVO.setRealPayAmount(0);
            }
        }
        if(comicBaseInfoVO == null){
            return new ComicBaseInfoVO();
        }

        //3.查询创建者，更新者姓名
        List<UserDTO> userDTOS = userClient.queryUserByIds(
                Arrays.asList(comicBaseInfoVO.getCreater(), comicBaseInfoVO.getUpdater())
                        .stream()
                        .distinct()
                        .collect(Collectors.toList())
        );
        if (CollUtils.isNotEmpty(userDTOS)) {
            //3.1.创建者，更新至id+name映射关系
            Map<Long, String> operatorMap = userDTOS
                    .stream()
                    .collect(Collectors.toMap(UserDTO::getId, UserDTO::getName));
            //3.2.设置创建者姓名
            comicBaseInfoVO.setCreaterName(operatorMap.get(comicBaseInfoVO.getCreater()));
            //3.3.设置更新者姓名
            comicBaseInfoVO.setUpdaterName(operatorMap.get(comicBaseInfoVO.getUpdater()));
        }

        //4.漫剧分类信息
        List<Category> categories = categoryService.queryByIds(
                Arrays.asList(comicBaseInfoVO.getFirstCateId(),
                        comicBaseInfoVO.getSecondCateId(),
                        comicBaseInfoVO.getThirdCateId()));
        if (CollUtils.isNotEmpty(categories)) {
            //4.1分类id和名称关系
            Map<Long, String> categoryIdAndName = categories
                    .stream()
                    .collect(Collectors.toMap(Category::getId, Category::getName));
            //4.2.设置漫剧分类名称
            comicBaseInfoVO.setCateNames(
                    StringUtils.format("{}/{}/{}",
                            categoryIdAndName.get(comicBaseInfoVO.getFirstCateId()),
                            categoryIdAndName.get(comicBaseInfoVO.getSecondCateId()),
                            categoryIdAndName.get(comicBaseInfoVO.getThirdCateId()))
            );
        }
        return comicBaseInfoVO;
    }

    @Override
    public void updateStep(Long id, Integer step) {
        //1.查询漫剧草稿
        ComicDraft comicDraft = baseMapper.selectById(id);
        ComicDraft updateCourseDraft = new ComicDraft();
        updateCourseDraft.setId(id);
        updateCourseDraft.setCVersion(comicDraft.getCVersion() + 1);
        //2.设置漫剧步骤，漫剧步骤只能前进不能后退
        if (comicDraft.getStep() < step) {
            updateCourseDraft.setStep(step);
        }else {
            updateCourseDraft.setStep(comicDraft.getStep());
        }
        //3.设置课时数，保存目录和保存题目两部进行修改
        if (ComicConstants.ComicStep.EPISODE == step ||
                ComicConstants.ComicStep.TAG == step) {
            //3.1题目保存和目录保存都会修改课时数量
            updateCourseDraft.setSectionNum(comicEpisodeDraftService.totalSectionNums(id));
        }
        //4.更新課程狀態
        baseMapper.updateById(updateCourseDraft);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {DbException.class, Exception.class})
    public void upShelf(Long id) {
        // 1.信息获取
        //1.1获取上架的漫剧草稿信息
        ComicDraft comicDraft = baseMapper.selectById(id);
        //1.2获取漫剧信息
        Comic course = courseMapper.selectById(id);
        boolean isFirstUpShelf = (course == null);

        // 2.校验漫剧
        checkBeforeUpShelf(id);

        //3.计算每个章节的课时视频时长
        Map<Long, Integer> mediaDurations = comicEpisodeDraftService.calculateMediaDuration(id);
        //3.1.计算漫剧视屏总时长
        int totalMediaDuration = mediaDurations
                .values()
                .stream()
                .mapToInt(p -> p)
                .sum();


        //4.草稿信息上架到正式环境
        //4.1漫剧创作者信息
        comicCreatorDraftService.copyToShelf(id, isFirstUpShelf);
        //4.2 题目信息上架
        comicEpisodeDraftService.copySubjectToShelf(id, isFirstUpShelf);
        //4.3目录信息上架
        comicEpisodeDraftService.copyToShelf(id, isFirstUpShelf);
        //4.4 组装漫剧基本信息、漫剧内容信息
        ComicContentDraft courseContentDraft = courseContentDraftMapper.selectById(id);
        ComicContent courseContent = BeanUtils.toBean(courseContentDraft, ComicContent.class);
        Comic courseToShelf = BeanUtils.toBean(comicDraft, Comic.class);
        //4.4.1.漫剧视频总时长
        courseToShelf.setMediaDuration(totalMediaDuration);
        //4.4.2.漫剧有效期月数
        courseToShelf.setValidDuration(comicDraft.getValidDuration());
        //4.4.3.漫剧发布时间
        courseToShelf.setPublishTime(DateUtils.now());
        //4.4.4.漫剧状态设置为已上架
        courseToShelf.setStatus(ComicStatus.SHELF.getStatus());
        //4.4.5.漫剧发布次数
        int publishTimes = (course == null) ?
                1 : NumberUtils.null2Zero(course.getPublishTimes()) + 1;
        courseToShelf.setPublishTimes(publishTimes);
        // 4.4.6.评分
        courseToShelf.setScore((int)(Math.random() * 10) + 40);

        //4.5.首次上架
        if (isFirstUpShelf) {
            //4.5.1.插入漫剧内容信息
            int result = courseContentMapper.insert(courseContent);
            if (result <= 0) {
                throw new DbException(ErrorInfo.Msg.DB_UPDATE_EXCEPTION);
            }
            //4.5.2.插入漫剧基本信息
            result = courseMapper.insert(courseToShelf);
            if (result <= 0) {
                throw new DbException(ErrorInfo.Msg.DB_UPDATE_EXCEPTION);
            }
            //4.5.1.删除漫剧草稿基本信息
            baseMapper.deleteById(id);
            //4.5.2.删除漫剧草稿内容信息
            courseContentDraftMapper.deleteById(id);
        } else {
            //4.6.再次上架
            //4.6.1.更新正式漫剧内容信息
            int result = courseContentMapper.updateById(courseContent);
            if (result <= 0) {
                throw new DbException(ErrorInfo.Msg.DB_UPDATE_EXCEPTION);
            }
            //4.6.2.更新正式漫剧基本信息
            result = courseMapper.updateVariableById(courseToShelf);
            if (result <= 0) {
                throw new DbException(ErrorInfo.Msg.DB_UPDATE_EXCEPTION);
            }
            //4.6.3.删除漫剧草稿基本信息
            baseMapper.deleteById(id);
            //4.6.4.删除漫剧草稿内容信息
            courseContentDraftMapper.deleteById(id);

        }
        //5.漫剧上架mq
        rabbitMqHelper.sendAsyn(MqConstants.Exchange.COMIC_EXCHANGE,
                MqConstants.Key.COMIC_UP_KEY,
                id,
                200L);
    }

    @Override
    public void checkBeforeUpShelf(Long id) {
        //1.获取上架的漫剧草稿信息
        ComicDraft comicDraft = baseMapper.selectById(id);
        //1.1.获取漫剧信息
        Comic course = courseMapper.selectById(id);
        //2.漫剧校验
        //2.1.漫剧上架幂等校验
        if (comicDraft == null && course != null) {
            throw new BizIllegalException(ComicErrorInfo.Msg.COMIC_UP_SHELF_AREADY);
        }
        //2.2.漫剧id不存在的漫剧无法上架
        if (comicDraft == null && course == null) {
            throw new BizIllegalException(ComicErrorInfo.Msg.COMIC_UP_SHELF_NOT_FOUND_COMIC);

        }
        //2.3.草稿信息不完整无法上架
        if (comicDraft.getStep() != ComicConstants.ComicStep.CREATOR) {
            throw new BizIllegalException(ComicErrorInfo.Msg.COMIC_UP_SHELF_INFO_INCOMPLETE);
        }
        //漫剧
        //2.4.已上架/已完结漫剧不能上架
        if (course != null && course.getStatus() != ComicStatus.DOWN_SHELF.getStatus()) {
            throw new BizIllegalException(ComicErrorInfo.Msg.COMIC_UP_SHELF_STATE_WRONG);
        }
        //2.5.校验漫剧结束时间
        if(comicDraft.getPurchaseEndTime().isBefore(DateUtils.now())){
            throw new BizIllegalException(ComicErrorInfo.Msg.COMIC_UP_SHELF_PURCHASE_ILLEGAL);
        }
        //2.6.首次上架校验逻辑
        if (course == null) {
            //2.5.1.统计同名的漫剧数量
            int sameNameNum = courseMapper.countSameName(comicDraft.getName());
            //2.5.2.有同名漫剧不能上架
            if (sameNameNum > 0) {
                throw new BadRequestException(ComicErrorInfo.Msg.COMIC_SAVE_NAME_EXISTS);
            }
        }
        //2.7.校验漫剧目录
        comicEpisodeDraftService.checkCataInfoImplated(id);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {DbException.class, Exception.class})
    public void downShelf(Long id) {
        //1.查询漫剧基本信息
        Comic course = courseService.getById(id);
        //1.1漫剧状态判断
        if (course == null || !course.getStatus().equals(ComicStatus.SHELF.getStatus())) {
            throw new BizIllegalException(ComicErrorInfo.Msg.COMIC_DOWN_SHELF_FAILD);
        }
        //2.先更新漫剧状态
        courseService.updateStatus(id, ComicStatus.DOWN_SHELF.getStatus());
        //3.漫剧基本信息和内容信息copy到草稿中
        baseMapper.insertFromCourse(id);
        //4.漫剧内容copy到草稿中
        courseContentDraftMapper.insertFromCourseContent(id);
        //5.目录内容copy到草稿中
        comicEpisodeDraftMapper.insertFromComicEpisode(id);
        //6.漫剧题目copy到草稿中
        copySubject2Draft(id);
        //7.漫剧创作者copy到草稿中
        comicCreatorDraftMapper.insertFromCourseTeacher(id);
        //8.下架mq广播
        rabbitMqHelper.send(MqConstants.Exchange.COMIC_EXCHANGE, MqConstants.Key.COMIC_DOWN_KEY, id);
    }

    @GlobalTransactional
    public void copySubject2Draft(Long courseId) {
        // 1.查询漫剧有关的片段信息
        List<Long> sectionIds = comicEpisodeDraftMapper.getSectionIdByCourseId(courseId);
        if (CollUtils.isEmpty(sectionIds)) {
            log.error("漫剧片段数据为空");
            return;
        }
        // 2.查询题目关系
        List<QuestionBizDTO> qbs = quizClient.queryQuestionIdsByBizIds(sectionIds);
        if (CollUtils.isEmpty(qbs)) {
            return;
        }
        List<ComicEpisodeTagDraft> list = qbs.stream().map(q -> new ComicEpisodeTagDraft()
                .setCourseId(courseId).setCataId(q.getBizId()).setSubjectId(q.getQuestionId())
        ).collect(Collectors.toList());
        // 3.保存到草稿表
        courseCataSubjectDraftMapper.batchInsert(list);
    }

    @Override
    public ComicDTO getCourseDTOById(Long id) {
        //1.查询漫剧草稿基础信息
        ComicDraft comicDraft = baseMapper.selectById(id);
        //1.1.判空
        if (comicDraft == null) {
            return new ComicDTO();
        }
        //2.查询漫剧创作者列表，并去序号的第一个
        LambdaQueryWrapper<ComicCreatorDraft> queryWrapper =
                Wrappers.lambdaQuery(ComicCreatorDraft.class)
                .eq(ComicCreatorDraft::getCourseId, id)
                .orderBy(true, false, ComicCreatorDraft::getCIndex)
                .last(true, "limit 1");
        //2.1.查询漫剧创作者信息
        ComicCreatorDraft comicCreatorDraft = comicCreatorDraftMapper.selectOne(queryWrapper);
        //3.组装数据
        ComicDTO courseDTO = BeanUtils.toBean(comicDraft, ComicDTO.class);
        //3.1.设置漫剧分类，一级、二级、三级漫剧分类
        courseDTO.setCategoryIdLv1(comicDraft.getFirstCateId());
        courseDTO.setCategoryIdLv2(comicDraft.getSecondCateId());
        courseDTO.setCategoryIdLv3(comicDraft.getThirdCateId());
        //3.2.设置漫剧视频播放总时长
        courseDTO.setDuration(comicDraft.getMediaDuration());
        //3.3.设置漫剧总片段数
        courseDTO.setSections(comicDraft.getSectionNum());
        //3.4.设置漫剧创作者id
        if (comicCreatorDraft != null) {
            courseDTO.setCreator(comicCreatorDraft.getTeacherId());
        } else {
            courseDTO.setCreator(0L);
        }

        return courseDTO;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {DbException.class, Exception.class})
    public void delete(Long id) {
        //1.删除漫剧草稿
        baseMapper.deleteById(id);
        //2.删除漫剧内容草稿
        courseContentDraftMapper.deleteById(id);
        //3.删除漫剧题目关系草稿
        courseCataSubjectDraftMapper.deleteByCourseId(id);
        //4.删除漫剧目录草稿
        comicEpisodeDraftMapper.deleteByCourseId(id, Arrays.asList(
                ComicConstants.EpisodeType.CHAPTER,
                ComicConstants.EpisodeType.CLIP,
                ComicConstants.EpisodeType.PRATICE
        ));
        //5.删除漫剧创作者关系草稿
        comicCreatorDraftMapper.deleteByCourseId(id);
    }

    @Override
    public PageDTO<ComicPageVO> queryForPage(ComicPageQuery coursePageQuery) {
        //1.漫剧草稿分页查询条件
        LambdaQueryWrapper<ComicDraft> queryWrapper =
                SqlWrapperUtils.toLambdaQueryWrapper(coursePageQuery, ComicDraft.class);
        //1.1漫剧查询条件-更新时间
        queryWrapper.between(
                ObjectUtils.isNotEmpty(coursePageQuery.getBeginTime()) &&
                        ObjectUtils.isNotEmpty(coursePageQuery.getEndTime()),
                ComicDraft::getUpdateTime,
                coursePageQuery.getBeginTime(),
                coursePageQuery.getEndTime());
        //1.2漫剧查询条件-搜索关键字
        queryWrapper.like(StringUtils.isNotEmpty(coursePageQuery.getKeyword()),
                ComicDraft::getName, coursePageQuery.getKeyword());
        //1.3.分页查询查询数据
        Page<ComicDraft> page = page(coursePageQuery.toMpPage(), queryWrapper);
        //1.4.分页查询结果判空
        if (CollUtils.isEmpty(page.getRecords())) {
            return PageDTO.empty(page);
        }
        //2.组装数据查询
        //2.1.漫剧更新人id列表
        List<Long> updaterList = page.getRecords().stream()
                .map(ComicDraft::getUpdater)
                .collect(Collectors.toList());
        //2.2.查询更新人用户信息
        List<UserDTO> userDTOS = userClient.queryUserByIds(updaterList);
        //2.3.转化更新人用户id+name 映射关系
        Map<Long, String> updaterMap =
                CollUtils.isEmpty(updaterList) ?
                        new HashMap<>() : userDTOS.stream().collect(Collectors.toMap(UserDTO::getId, UserDTO::getName));
        //2.4.查询漫剧分类列表
        List<Category> list = categoryService.list();
        //2.5.转化漫剧分类id+name映射关系
        Map<Long, String> categoryNameMap =
                CollUtils.isEmpty(list) ?
                        new HashMap<>() : list.stream().collect(Collectors.toMap(Category::getId, Category::getName));
        //2.6.漫剧id列表
        List<Long> courseIdList = page.getRecords().stream().map(ComicDraft::getId).collect(Collectors.toList());
        //2.7.统计漫剧领取人数map
        Map<Long, Integer> peoNumOfCourseMap = orderClient.countPurchaseNumOfComic(courseIdList);
        //3.数据封装
        return PageDTO.of(page, ComicPageVO.class, (course, coursePageVO) -> {
            //3.1.拼接漫剧分类
            String categories = StringUtils.format("{}/{}/{}",
                    categoryNameMap.get(course.getFirstCateId()),
                    categoryNameMap.get(course.getSecondCateId()),
                    categoryNameMap.get(course.getThirdCateId()));
            //3.2.设置漫剧分类
            coursePageVO.setCategories(categories);
            //3.3.设置漫剧更新人
            coursePageVO.setUpdaterName(updaterMap.get(course.getUpdater()));
            //3.4.设置漫剧领取人数
            coursePageVO.setSold(NumberUtils.null2Zero(peoNumOfCourseMap.get(course.getId())));
            //3.5.设置漫剧总课时数
            coursePageVO.setSections(course.getSectionNum());
        });
    }

    @Override
    public NameExistVO checkName(String name, Long id) {
        //1.漫剧草稿同名查询条件
        LambdaQueryWrapper<ComicDraft> queryWrapper =
                Wrappers.lambdaQuery(ComicDraft.class)
                        .eq(ComicDraft::getName, name)
                        .last(id != null, " and id !=" + id);
        //2.统计同名漫剧数量
        Integer num = baseMapper.selectCount(queryWrapper);
        //3.返回同名漫剧VO
        return new NameExistVO(num > 0);
    }

    @Override
    public List<Long> queryExists(List<Long> idList) {
        //1.查询草稿漫剧基础信息列表
        List<ComicDraft> courses = baseMapper.selectBatchIds(idList);
        //1.1.草稿漫剧信息列表判空
        if (CollUtils.isEmpty(courses)) {
            return null;
        }
        //2.组装数据
        return courses.stream()
                .map(ComicDraft::getId)
                .collect(Collectors.toList());
    }

    @Override
    public Map<Long, Integer> countCourseNumOfCategory() {
        //1.查询所有漫剧草稿
        List<ComicDraft> courses = baseMapper.selectList(null);
        Map<Long, Integer> cateIdAndNumMap = new HashMap<>();
        //2.遍历统计每个漫剧分类拥有的漫剧数量
        for (ComicDraft course : courses) {
            //2.1.统计一级漫剧分类漫剧数量
            Integer firstCateNum = cateIdAndNumMap.get(course.getFirstCateId());
            cateIdAndNumMap.put(course.getFirstCateId(), firstCateNum == null ? 1 : firstCateNum + 1);
            //2.2.统计二级漫剧分类漫剧数量
            Integer secondCateNum = cateIdAndNumMap.get(course.getSecondCateId());
            cateIdAndNumMap.put(course.getSecondCateId(), secondCateNum == null ? 1 : secondCateNum + 1);
            //2.3.统计三级漫剧分类漫剧数量
            Integer thirdCateNum = cateIdAndNumMap.get(course.getThirdCateId());
            cateIdAndNumMap.put(course.getThirdCateId(), thirdCateNum == null ? 1 : thirdCateNum + 1);
        }
        return cateIdAndNumMap;
    }

    private void checkSameCourse(Long id, String name) {
        //1.查询正式环境是否有同名漫剧
        int countSameNameNum = courseMapper.countSameName(name);
        //1.1.同名漫剧数据判0
        if (countSameNameNum > 0) { //名称已经存在，提交时做双重校验
            throw new BadRequestException(ComicErrorInfo.Msg.COMIC_SAVE_NAME_EXISTS);
        }
        //2.查询正式环境是否有同名漫剧
        countSameNameNum = baseMapper.countByNameAndId(name, id);
        //2.1.同名漫剧数据判0
        if (countSameNameNum > 0) {
            throw new BadRequestException(ComicErrorInfo.Msg.COMIC_SAVE_NAME_EXISTS);
        }
    }
}
