package com.aicomic.comic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aicomic.api.client.quiz.QuizClient;
import com.aicomic.api.client.watch.WatchClient;
import com.aicomic.api.client.order.OrderClient;
import com.aicomic.api.client.user.UserClient;
import com.aicomic.api.dto.IdAndNumDTO;
import com.aicomic.api.dto.comic.*;
import com.aicomic.api.dto.watch.WatchEpisodeDTO;
import com.aicomic.api.dto.watch.WatchRecordDTO;
import com.aicomic.api.dto.user.UserDTO;
import com.aicomic.common.autoconfigure.mq.RabbitMqHelper;
import com.aicomic.common.constants.ErrorInfo;
import com.aicomic.common.constants.MqConstants;
import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.common.exceptions.BadRequestException;
import com.aicomic.common.exceptions.DbException;
import com.aicomic.common.utils.*;
import com.aicomic.comic.constants.ComicErrorInfo;
import com.aicomic.comic.constants.ComicStatus;
import com.aicomic.comic.constants.RedisContants;
import com.aicomic.comic.domain.dto.ComicPageQuery;
import com.aicomic.comic.domain.dto.ComicSimpleInfoListDTO;
import com.aicomic.comic.domain.po.Category;
import com.aicomic.comic.domain.po.Category3PO;
import com.aicomic.comic.domain.po.Comic;
import com.aicomic.comic.domain.po.ComicCreator;
import com.aicomic.comic.domain.vo.*;
import com.aicomic.comic.mapper.ComicDraftMapper;
import com.aicomic.comic.mapper.ComicMapper;
import com.aicomic.comic.mapper.ComicCreatorMapper;
import com.aicomic.comic.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 草稿漫剧 服务实现类
 * </p>
 *
 * @since 2022-07-20
 */
@Service
@Slf4j
public class ComicServiceImpl extends ServiceImpl<ComicMapper, Comic> implements IComicService {

    @Autowired
    private ComicCreatorMapper comicCreatorMapper;

    @Autowired
    private ComicDraftMapper comicDraftMapper;

    @Autowired
    private IComicDraftService comicDraftService;

    @Autowired
    private RabbitMqHelper rabbitMqHelper;

    @Autowired
    private IComicEpisodeService comicEpisodeService;

    @Autowired
    private IComicCreatorService comicCreatorService;

    @Autowired
    private ICategoryService categoryService;

    @Autowired
    private UserClient userClient;

    @Autowired
    private OrderClient orderClient;

    @Autowired
    private QuizClient quizClient;

    @Autowired
    private WatchClient learningClient;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {DbException.class, Exception.class})
    public void updateStatus(Long id, Integer status) {
        //1.组装数据
        Comic course = new Comic();
        course.setId(id);
        course.setStatus(status);
        course.setUpdateTime(LocalDateTime.now());
        //2.更新数据
        int result = baseMapper.updateById(course);
        //3.判断直接条数是1条
        if (result != 1) {
            throw new DbException(ErrorInfo.Msg.DB_UPDATE_EXCEPTION);
        }
    }

    @Override
    public ComicDTO getCourseDTOById(Long id) {
        // 1.查询漫剧信息
        Comic course = baseMapper.selectById(id);
        //1.1判空
        if (course == null) {
            return null;
        }
        //2.创作者查询条件
        LambdaQueryWrapper<ComicCreator> queryWrapper =
                Wrappers.lambdaQuery(ComicCreator.class)
                        .eq(ComicCreator::getCourseId, id)
                        .orderBy(true, false, ComicCreator::getCIndex)
                        .last(true, "limit 1");
        // 2.查询创作者
        List<ComicCreator> comicCreators = comicCreatorMapper.selectList(queryWrapper);

        // 3.漫剧数据封装
        ComicDTO courseDTO = BeanUtils.toBean(course, ComicDTO.class);
        //3.1.一级漫剧分类
        courseDTO.setCategoryIdLv1(course.getFirstCateId());
        //3.2.二级漫剧分类
        courseDTO.setCategoryIdLv2(course.getSecondCateId());
        //3.3.三级漫剧分类
        courseDTO.setCategoryIdLv3(course.getThirdCateId());
        //3.4.素材信息
        courseDTO.setDuration(course.getMediaDuration());
        //3.5.漫剧发布时间
        courseDTO.setPublishTime(course.getCreateTime());
        //3.6.漫剧片段数量
        courseDTO.setSections(course.getSectionNum());
        //3.7.漫剧第一位创作者
        if (CollUtils.isNotEmpty(comicCreators)) {
            courseDTO.setCreator(comicCreators.get(0).getTeacherId());
        } else {
            courseDTO.setCreator(0L);
        }

        // 4.统计漫剧销量
        Map<Long, Integer> peoNumOfCourseMap = orderClient.countPurchaseNumOfComic(CollUtils.singletonList(id));
        if (CollUtils.isNotEmpty(peoNumOfCourseMap)) {
            courseDTO.setSold(peoNumOfCourseMap.getOrDefault(id, 0));
        }
        //5.返回数据
        return courseDTO;

    }

    @Override
    public void delete(Long id) {
        //1.删除草稿信息
        comicDraftService.delete(id);
        //2.发送删除草稿mq
        rabbitMqHelper.send(MqConstants.Exchange.COMIC_EXCHANGE, MqConstants.Key.COMIC_DELETE_KEY, id);
    }

    @Override
    public List<ComicSimpleInfoDTO> getSimpleInfoList(ComicSimpleInfoListDTO courseSimpleInfoListDTO) {
        //1.漫剧查询条件
        LambdaQueryWrapper<Comic> queryWrapper =
                Wrappers.lambdaQuery(Comic.class)
                        .in(CollUtils.isNotEmpty(courseSimpleInfoListDTO.getThirdCataIds()),
                                Comic::getThirdCateId, courseSimpleInfoListDTO.getThirdCataIds())
                        .in(CollUtils.isNotEmpty(courseSimpleInfoListDTO.getIds()),
                                Comic::getId, courseSimpleInfoListDTO.getIds());
        //2.查询漫剧
        List<Comic> courses = baseMapper.selectList(queryWrapper);
        //3.漫剧信息转化
        return BeanUtils.copyList(courses, ComicSimpleInfoDTO.class);
    }

    @Override
    public List<Comic> queryByCategoryIdAndLevel(Long categoryId, Integer level) {
        //1.漫剧基本信息查询条件
        LambdaQueryWrapper<Comic> queryWrapper =
                Wrappers.lambdaQuery(Comic.class)
                        .eq(level == 1, Comic::getFirstCateId, categoryId) //一级漫剧分类
                        .eq(level == 2, Comic::getSecondCateId, categoryId) //二级漫剧分类
                        .eq(level == 3, Comic::getThirdCateId, categoryId);//三级漫剧分类
        //2.查询漫剧基本信息
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public NameExistVO checkName(String name, Long id) {
        //1.正式漫剧同名漫剧数量
        LambdaQueryWrapper<Comic> queryWrapper =
                Wrappers.lambdaQuery(Comic.class)
                        .eq(Comic::getName, name)
                        .last(id != null, " and id !=" + id);
        //2.统计数量
        Integer num = baseMapper.selectCount(queryWrapper);
        if (num > 0) {
            return NameExistVO.EXISTED;
        }
        //3.统计草稿漫剧同名数量
        return comicDraftService.checkName(name, id);
    }

    @Override
    public List<Long> queryExists(List<Long> idList, List<Integer> statusList) {
        //1.指定状态的漫剧id存在查询条件
        LambdaQueryWrapper<Comic> queryWrapper =
                Wrappers.lambdaQuery(Comic.class)
                        .in(Comic::getId, idList)
                        .in(Comic::getStatus, statusList);
        //2.根据条件查询漫剧
        List<Comic> courses = baseMapper.selectList(queryWrapper);
        if (CollUtils.isEmpty(courses)) {
            return null;
        }
        //3.组装数据
        return courses.stream()
                .map(Comic::getId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> queryCourseIdByName(String name) {
        // 1.查询条件
        LambdaQueryWrapper<Comic> queryWrapper =
                Wrappers.lambdaQuery(Comic.class)
                .like(Comic::getName, name);
        // 2.查询数据
        List<Comic> courses = baseMapper.selectList(queryWrapper);
        // 3.转换出漫剧id列表
        return courses.stream()
                .map(Comic::getId)
                .collect(Collectors.toList());
    }


    @Override
    public ComicAndClipVO queryCourseAndCatalogById(Long courseId) {
        // 1.获取当前用户
        Long userId = UserContext.getUser();
        // 2.查询漫剧详情
        ComicFullInfoDTO course = getInfoById(courseId, true, true);
        if (course == null) {
            return null;
        }
        // 3.组织VO
        ComicAndClipVO vo = new ComicAndClipVO();
        vo.setId(courseId);
        vo.setName(course.getName());
        vo.setSections(course.getSectionNum());
        vo.setCoverUrl(course.getCoverUrl());
        // 4.查询创作者信息
        List<UserDTO> creators = userClient.queryUserByIds(course.getCreatorIds());
        if (CollUtils.isNotEmpty(creators)) {
            UserDTO creator = creators.get(0);
            vo.setCreatorName(creator.getName());
            vo.setCreatorIcon(creator.getIcon());
        }
        // 5.组装片段信息
        List<EpisodeDTO> catas = course.getChapters();
        List<ChapterVO> chapters = new ArrayList<>(catas.size());
        for (EpisodeDTO c : catas) {
            ChapterVO cv = new ChapterVO();
            cv.setId(c.getId());
            cv.setName(c.getName());
            cv.setIndex(c.getIndex());
            cv.setMediaDuration(c.getMediaDuration());
            List<ClipVO> sections = BeanUtils.copyToList(c.getSections(), ClipVO.class);
            cv.setSections(sections);
            chapters.add(cv);
        }
        vo.setChapters(chapters);
        // 6.查询观看进度
        if (learningClient == null) {
            return vo;
        }
        // 6.1.查询观看记录
        WatchEpisodeDTO lessonDTO = learningClient.queryWatchRecordByCourse(courseId);
        if (lessonDTO == null) {
            // 没有查询到追剧列表信息，说明是免费试看，直接返回
            return vo;
        }
        vo.setLessonId(lessonDTO.getId());
        if (CollUtils.isEmpty(lessonDTO.getRecords())) {
            // 有追剧列表信息，但是没有观看记录，不用处理进度问题了，直接返回
            return vo;
        }
        List<WatchRecordDTO> records = lessonDTO.getRecords();
        // 6.2.获取最近观看的记录。由于查询时按照观看时间排序，第一条记录就是最近观看的片段记录
        Long latestSectionId = lessonDTO.getLatestSectionId();
        if(latestSectionId == null) {
            latestSectionId = records.get(0).getSectionId();
        }
        vo.setLatestSectionId(latestSectionId);
        // 6.3.处理记录为一个map
        Map<Long, WatchRecordDTO> rMap = records.stream()
                .collect(Collectors.toMap(WatchRecordDTO::getSectionId, r -> r));
        // 6.4.填充观看进度到章节中
        for (ChapterVO chapter : vo.getChapters()) {
            for (ClipVO section : chapter.getSections()) {
                WatchRecordDTO r = rMap.get(section.getId());
                if (r == null) continue;
                section.setFinished(r.getFinished());
                section.setMoment(r.getMoment());
            }
        }
        return vo;
    }

    @Override
    public List<SubNumAndComicNumDTO> countSubjectNumAndCourseNumOfTeacher(List<Long> creatorIds) {
        // 1.统计数据
        // 1.1.创作者id和漫剧数量(已上架、已下架、已过期)
        Map<Long, Integer> creatorIdAndComicNumMap =
                IdAndNumDTO.toMap(baseMapper.countCourseNumOfTeacher(creatorIds));
        // 1.2.待上架
        Map<Long, Integer> creatorIdAndComicNumMap2 =
                IdAndNumDTO.toMap(comicDraftMapper.countCourseNumOfTeacher(creatorIds));
        // 1.3.统计创作者的出题数量
        Map<Long, Integer> creatorIdAndTagNumMap = quizClient.countSubjectNumOfTeacher(creatorIds);

        // 2.遍历创作者id
        List<SubNumAndComicNumDTO> subNumAndCourseNumDTOS = new ArrayList<>();
        for (Long teacherId : creatorIds) {
            subNumAndCourseNumDTOS.add(new SubNumAndComicNumDTO(
                    //2.1.设置创作者id
                    teacherId,
                    //2.2.设置创作者漫剧数量
                    NumberUtils.null2Zero(creatorIdAndComicNumMap.get(teacherId)) +
                            NumberUtils.null2Zero(creatorIdAndComicNumMap2.get(teacherId)),
                    //2.3.设置创作者出题数量
                    NumberUtils.null2Zero(creatorIdAndTagNumMap.get(teacherId))));
        }
        return subNumAndCourseNumDTOS;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public int courseFinished() {
        //1.完结漫剧查询条件
        LambdaQueryWrapper<Comic> queryWrapper =
                Wrappers.lambdaQuery(Comic.class)
                        .le(Comic::getPurchaseEndTime, LocalDateTime.now())
                        .in(Comic::getStatus,
                                List.of(ComicStatus.DOWN_SHELF.getStatus(),
                                        ComicStatus.SHELF.getStatus()));

        //1.2.查询完结漫剧
        List<Comic> courses = baseMapper.selectList(queryWrapper);
        //1.3.完结漫剧判空
        if (CollUtils.isEmpty(courses)) {
            return 0;
        }
        //2.组装数据
        List<Comic> updateCourses = new ArrayList<>();
        for (Comic course : courses) {
            Comic updateCourse = new Comic();
            //2.1.设置漫剧id
            updateCourse.setId(course.getId());
            //2.2.设置漫剧状态-已完结
            updateCourse.setStatus(ComicStatus.FINISHED.getStatus());
            updateCourses.add(updateCourse);
        }
        //3.批量完结漫剧
        updateBatchById(updateCourses);
        //4.发送漫剧完结mq
        sendFinishedCourse(courses);
        //5.清理草稿
        for (Comic course: courses){
            comicDraftService.delete(course.getId());
        }

        return updateCourses.size();
    }

    @Override
    @Cacheable(cacheNames = RedisContants.Formatter.STATISTICS_COMIC_NUM_CATE)
    public Map<Long, Integer> countCourseNumOfCategory() {
        //1.统计漫剧分类拥有已上架、已完成的漫剧数量
        Map<Long, Integer> nomalCourseNumOfCategory =
                countNomalCourseNumOfCategory();
        //2.统计漫剧分类待上架，已下架的漫剧分类的数量
        Map<Long, Integer> draftCourseNumOfCategory =
                comicDraftService.countCourseNumOfCategory();
        //3.两组数据聚合
        return CollUtils.union(nomalCourseNumOfCategory, draftCourseNumOfCategory);
    }

    @Override
    @Cacheable(cacheNames = RedisContants.Formatter.CATEGORY_ID_LIST_HAVE_COMIC)
    public List<Long> getCategoryIdListWithCourse() {
        // 1.查询条件
        List<Category3PO> category3s = baseMapper.queryCategoryIdWithCourse();
        // 1.1.判空
        if(CollUtils.isEmpty(category3s)) {
            return new ArrayList<>();
        }
        // 2.将漫剧分类id设置到categoryIdList
        List<Long> categoryIdList = new ArrayList<>();
        category3s.stream().forEach(category3->{
            category3.setId(categoryIdList);
        });
        // 2.1.去重，并返回数据
        return categoryIdList.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public Integer countCourseNumOfCategory(Long categoryId) {
        //1.漫剧统计条件
        LambdaQueryWrapper<Comic> queryWrapper =
                Wrappers.lambdaQuery(Comic.class)
                        .or().eq(Comic::getFirstCateId, categoryId)
                        .or().eq(Comic::getSecondCateId, categoryId)
                        .or().eq(Comic::getThirdCateId, categoryId);
        //2.统计漫剧数量
        return baseMapper.selectCount(queryWrapper);
    }

    @Override
    public ComicFullInfoDTO getInfoById(Long id, boolean withCatalogue, boolean withTeachers) {
        // 1.查询漫剧基本信息
        Comic course = baseMapper.selectById(id);
        if (course == null) {
            throw new BadRequestException(ComicErrorInfo.Msg.COMIC_CHECK_NOT_EXISTS);
        }
        // 2.转换vo
        ComicFullInfoDTO courseFullInfoDTO = BeanUtils.toBean(course, ComicFullInfoDTO.class);

        // 3.查询目录信息
        if (withCatalogue) {
            courseFullInfoDTO.setChapters(comicEpisodeService.queryComicEpisodes(id, true));
        }
        // 4.查询创作者信息
        if (withTeachers) {
            courseFullInfoDTO.setCreatorIds(comicCreatorService.getTeacherIdOfCourse(id));
        }
        return courseFullInfoDTO;
    }

    @Override
    public PageDTO<ComicPageVO> queryForPage(ComicPageQuery coursePageQuery) {
        //1.漫剧查询条件
        LambdaQueryWrapper<Comic> queryWrapper =
                SqlWrapperUtils.toLambdaQueryWrapper(coursePageQuery, Comic.class);
        //1.1.漫剧条件-更新时间
        queryWrapper.between(
                ObjectUtils.isNotEmpty(coursePageQuery.getBeginTime())
                        && ObjectUtils.isNotEmpty(coursePageQuery.getEndTime()),
                Comic::getUpdateTime,
                coursePageQuery.getBeginTime(),
                coursePageQuery.getEndTime());
        //1.2.漫剧查询条件-名称
        queryWrapper.like(
                StringUtils.isNotEmpty(coursePageQuery.getKeyword()),
                Comic::getName,
                coursePageQuery.getKeyword());
        //1.3.分页查询数据
        Page<Comic> page = page(coursePageQuery.toMpPage(), queryWrapper);
        //1.4.分页数据判空
        if (CollUtils.isEmpty(page.getRecords())) {
            return PageDTO.empty(page);
        }
        //2.漫剧更新人id列表
        List<Long> updaterList = page
                .getRecords()
                .stream()
                .map(Comic::getUpdater)
                .collect(Collectors.toList());
        //2.1查询更新人用户信息
        List<UserDTO> userDTOS = userClient.queryUserByIds(updaterList);
        //2.2.转化漫剧更新人id+name map
        Map<Long, String> updaterMap =
                CollUtils.isEmpty(updaterList) ?
                        new HashMap<>()
                        : userDTOS
                        .stream()
                        .collect(Collectors.toMap(UserDTO::getId, UserDTO::getName));
        //3.获取所有漫剧分类信息
        List<Category> list = categoryService.list();
        //3.1.转化漫剧分类id+name map关系
        Map<Long, String> categoryNameMap =
                CollUtils.isEmpty(list) ?
                        new HashMap<>()
                        : list.stream()
                        .collect(Collectors.toMap(Category::getId, Category::getName));
        //4.漫剧id列表
        List<Long> courseIdList = page
                .getRecords()
                .stream()
                .map(Comic::getId)
                .collect(Collectors.toList());
        //4.1.统计每个漫剧领取人数map关系
        Map<Long, Integer> peoNumOfCourseMap = orderClient.countPurchaseNumOfComic(courseIdList);
        //5.组装数据
        return PageDTO.of(page, ComicPageVO.class, (course, coursePageVO) -> {
            //5.1.拼接漫剧分类名称
            String categories = StringUtils.format("{}/{}/{}",
                    categoryNameMap.get(course.getFirstCateId()),
                    categoryNameMap.get(course.getSecondCateId()),
                    categoryNameMap.get(course.getThirdCateId()));
            //5.2.设置漫剧分类名称
            coursePageVO.setCategories(categories);
            //5.3.设置漫剧更新人姓名
            coursePageVO.setUpdaterName(updaterMap.get(course.getUpdater()));
            //5.4.设置漫剧领取人数
            coursePageVO.setSold(NumberUtils.null2Zero(peoNumOfCourseMap.get(course.getId())));
            //5.5.设置漫剧片段数
            coursePageVO.setSections(course.getSectionNum());
        });
    }

    /**
     * 异步发送漫剧完结mq
     *
     * @param finishedCourse
     */
    private void sendFinishedCourse(List<Comic> finishedCourse) {
        //1.遍历发送漫剧完结mq
        for (Comic course : finishedCourse) {
            rabbitMqHelper.sendAsyn(MqConstants.Exchange.COMIC_EXCHANGE,
                    MqConstants.Key.COMIC_EXPIRE_KEY,
                    course.getId());
        }
    }

    /**
     * 统计漫剧分类上架和已完结漫剧的数量
     *
     * @return
     */
    private Map<Long, Integer> countNomalCourseNumOfCategory() {
        //1.查询条件
        LambdaQueryWrapper<Comic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Comic::getStatus,
                Arrays.asList(ComicStatus.SHELF.getStatus(), ComicStatus.FINISHED.getStatus()));
        //2.查询数据
        List<Comic> courses = baseMapper.selectList(queryWrapper);
        Map<Long, Integer> cateIdAndNumMap = new HashMap<>();
        //3.统计每个分类的漫剧数量
        for (Comic course : courses) {
            //3.1一级分类数量
            Integer firstCateNum = cateIdAndNumMap.get(course.getFirstCateId());
            cateIdAndNumMap.put(course.getFirstCateId(), firstCateNum == null ? 1 : firstCateNum + 1);
            //3.2二级分类数量
            Integer secondCateNum = cateIdAndNumMap.get(course.getSecondCateId());
            cateIdAndNumMap.put(course.getSecondCateId(), secondCateNum == null ? 1 : secondCateNum + 1);
            //3.3三级分类数量够
            Integer thirdCateNum = cateIdAndNumMap.get(course.getThirdCateId());
            cateIdAndNumMap.put(course.getThirdCateId(), thirdCateNum == null ? 1 : thirdCateNum + 1);
        }
        return cateIdAndNumMap;
    }
}
