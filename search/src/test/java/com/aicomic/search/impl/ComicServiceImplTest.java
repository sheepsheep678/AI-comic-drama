package com.aicomic.search.impl;

import com.aicomic.api.client.comic.ComicClient;
import com.aicomic.api.dto.comic.ComicSearchDTO;
import com.aicomic.common.utils.BeanUtils;
import com.aicomic.common.utils.RandomUtils;
import com.aicomic.search.domain.po.Comic;
import com.aicomic.search.domain.vo.ComicVO;
import com.aicomic.search.repository.ComicRepository;
import com.aicomic.search.service.IComicService;
import com.aicomic.search.service.ISearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class ComicServiceImplTest {

    @Autowired
    private ISearchService searchService;

    @Autowired
    private ComicRepository repository;

    @Autowired
    private ComicClient courseClient;

    @Autowired
    private IComicService courseService;

    @Test
    void testTop5Best() {
        List<ComicVO> courseVOS = searchService.queryBestTopN();
        for (ComicVO courseVO : courseVOS) {
            System.out.println("courseVO = " + courseVO);
        }
    }

    @Test
    void testTop5New() {
        List<ComicVO> courseVOS = searchService.queryNewTopN();
        for (ComicVO courseVO : courseVOS) {
            System.out.println("courseVO = " + courseVO);
        }
    }

    @Test
    void testTop5Free() {
        List<ComicVO> courseVOS = searchService.queryFreeTopN();
        for (ComicVO courseVO : courseVOS) {
            System.out.println("courseVO = " + courseVO);
        }
    }

    static List<Long> creators = List.of(
            1548889371405492225L, 1548940676303970306L,
            1548940777449611265L, 1548940921662365698L,
            1548941239125041153L, 1548941336596471809L
    );

    @Test
    void testLoadCourse() {
        for (long i = 1549025085494521857L; i <= 1549025085494521857L; i++) {
            // 1.根据id查询漫剧信息
            ComicSearchDTO courseSearchDTO = courseClient.getSearchInfo(i);
            if (courseSearchDTO == null) {
                return;
            }
            // 2.数据转换
            Comic course = BeanUtils.toBean(courseSearchDTO, Comic.class);
            course.setType(courseSearchDTO.getCourseType());
            course.setScore(41 + RandomUtils.randomInt(10));
            course.setSold(0);
            // 3.写入索引库
            repository.save(course);
        }
    }

    @Test
    void testRepository() {
        List<Comic> list = new ArrayList<>();

        for (int i = 0; i < 200; i++) {
            LocalDateTime time = LocalDateTime.now().minusDays(RandomUtils.randomInt(20));
            Comic course = new Comic();
            course.setId(1L + i);
            course.setCategoryIdLv1(1001L);
            course.setCategoryIdLv2(2000L + RandomUtils.randomInt(1,9));
            course.setCategoryIdLv3(3007L);
            course.setCoverUrl("default-cover-url.jpg");
            course.setFree(RandomUtils.randomBoolean());
            course.setName("Java实战课");
            course.setPublishTime(time);
            course.setScore(30 + RandomUtils.randomInt(20));
            course.setSections(10 + RandomUtils.randomInt(10));
            course.setSold(200 + RandomUtils.randomInt(1000));
            course.setCreator(creators.get(RandomUtils.randomInt(5)));
            course.setType(2);
            list.add(course);
        }

        repository.saveAll(list);
    }
}