package com.aicomic.search.repository;

import com.aicomic.common.utils.RandomUtils;
import com.aicomic.search.domain.po.Comic;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Optional;

@SpringBootTest
class ComicRepositoryTest {

    @Autowired
    private ComicRepository repository;

    @Test
    void save() {
        LocalDateTime time = LocalDateTime.now().minusDays(RandomUtils.randomInt(20));
        Comic course = new Comic();
        course.setId(1204101L);
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
        course.setCreator(1548889371405492225L);
        course.setType(2);
        repository.save(course);
    }

    @Test
    void deleteById() {
        repository.deleteById(1204101L);
    }

    @Test
    void findById() {
        Optional<Comic> op = repository.findById(1204101L);
        op.ifPresent(System.out::println);
    }

    @Test
    void updateById() {
    }

    @Test
    void increment() {
        repository.increment(1204101L, "sold", -1);
    }

}