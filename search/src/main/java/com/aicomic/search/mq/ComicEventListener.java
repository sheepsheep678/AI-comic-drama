package com.aicomic.search.mq;

import com.aicomic.search.service.IComicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.aicomic.common.constants.MqConstants.Exchange.COMIC_EXCHANGE;
import static com.aicomic.common.constants.MqConstants.Key.*;

@Slf4j
@Component
public class ComicEventListener {

    @Autowired
    private IComicService courseService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "search.course.up.queue", durable = "true"),
            exchange = @Exchange(name = COMIC_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = COMIC_UP_KEY
    ))
    public void listenCourseUp(Long courseId){
        log.debug("监听到漫剧{}上架", courseId);
        courseService.handleCourseUp(courseId);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "search.course.down.queue", durable = "true"),
            exchange = @Exchange(name = COMIC_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = COMIC_DOWN_KEY
    ))
    public void listenCourseDown(Long courseId){
        log.debug("监听到漫剧{}下架", courseId);
        courseService.handleCourseDelete(courseId);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "search.course.expire.queue", durable = "true"),
            exchange = @Exchange(name = COMIC_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = COMIC_EXPIRE_KEY
    ))
    public void listenCourseExpire(Long courseId){
        courseService.handleCourseDelete(courseId);
    }
}
