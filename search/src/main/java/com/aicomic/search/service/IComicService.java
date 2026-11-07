package com.aicomic.search.service;

import java.util.List;

public interface IComicService {

    void handleCourseDelete(Long courseId);

    void handleCourseUp(Long courseId);

    void updateCourseSold(List<Long> courseId, int amount);

    void handleCourseDeletes(List<Long> courseIds);
}
