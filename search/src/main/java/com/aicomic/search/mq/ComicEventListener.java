package com.aicomic.search.mq;

import com.aicomic.common.autoconfigure.mq.AbstractRocketMqListener;
import com.aicomic.search.service.IComicService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.aicomic.common.constants.MqConstants.ConsumerGroup.SEARCH_COMIC_GROUP;
import static com.aicomic.common.constants.MqConstants.Tag.COMIC_DOWN_TAG;
import static com.aicomic.common.constants.MqConstants.Tag.COMIC_EXPIRE_TAG;
import static com.aicomic.common.constants.MqConstants.Tag.COMIC_UP_TAG;
import static com.aicomic.common.constants.MqConstants.Topic.COMIC_TOPIC;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = COMIC_TOPIC,
        selectorExpression = COMIC_UP_TAG + " || " + COMIC_DOWN_TAG + " || " + COMIC_EXPIRE_TAG,
        consumerGroup = SEARCH_COMIC_GROUP
)
public class ComicEventListener extends AbstractRocketMqListener {

    @Autowired
    private IComicService courseService;

    @Override
    protected void handleMessage(MessageExt message) {
        Long courseId = readMessage(message, Long.class);
        String tag = message.getTags();
        if (COMIC_UP_TAG.equals(tag)) {
            log.debug("监听到漫剧{}上架", courseId);
            courseService.handleCourseUp(courseId);
        } else if (COMIC_DOWN_TAG.equals(tag) || COMIC_EXPIRE_TAG.equals(tag)) {
            log.debug("监听到漫剧{}下架", courseId);
            courseService.handleCourseDelete(courseId);
        }
    }
}
