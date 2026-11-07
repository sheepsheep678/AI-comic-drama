package com.aicomic.search.service.impl;

import com.aicomic.api.client.comic.ComicClient;
import com.aicomic.api.dto.comic.ComicSearchDTO;
import com.aicomic.common.utils.BeanUtils;
import com.aicomic.search.domain.po.Comic;
import com.aicomic.search.repository.ComicRepository;
import com.aicomic.search.service.IComicService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ComicServiceImpl implements IComicService {

    @Resource
    private ComicRepository courseRepository;
    @Resource
    private ComicClient courseClient;

    @Override
    public void handleCourseDelete(Long courseId) {
        // 1.直接删除
        courseRepository.deleteById(courseId);
    }

    @Override
    public void handleCourseUp(Long courseId) {
        // 1.根据id查询漫剧信息
        ComicSearchDTO courseSearchDTO = courseClient.getSearchInfo(courseId);
        if (courseSearchDTO == null) {
            return;
        }
        // 2.数据转换
        Comic course = BeanUtils.toBean(courseSearchDTO, Comic.class);
        course.setType(courseSearchDTO.getCourseType());
        // 3.写入索引库
        courseRepository.save(course);

    }

    @Override
    public void updateCourseSold(List<Long> courseIds, int amount) {
        courseRepository.incrementSold(courseIds, amount);
    }

    @Override
    public void handleCourseDeletes(List<Long> courseIds) {
        // 1.直接删除
        courseRepository.deleteByIds(courseIds);
    }
}
