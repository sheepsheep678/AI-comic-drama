package com.aicomic.media.config;

import com.aicomic.media.enums.Platform;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ac.platform")
public class PlatformProperties {
    private Platform file;
    private Platform media;
}
