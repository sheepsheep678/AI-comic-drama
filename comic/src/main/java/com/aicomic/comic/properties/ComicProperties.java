package com.aicomic.comic.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName ComicProperties
 * @Author 
 * @Date 2022/7/20 9:13
 * @Version
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "ac.comic")
public class ComicProperties {

    @NestedConfigurationProperty
    private Media media;

    @Data
    public static class Media {
        //免费试看时间
        private Integer trailerDuration;
    }
}
