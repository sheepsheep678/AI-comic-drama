package com.aicomic.comic.handler;

import com.aicomic.comic.service.IComicService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @ClassName ComicJobHandler
 * @Author 
 * @Date 2022/9/16 15:04
 * @Version
 **/
@Component
@Slf4j
public class ComicJobHandler {

    @Autowired
    private IComicService courseService;

    @XxlJob("courseFinished")
    public void courseFinished(){
        courseService.courseFinished();
    }
}
