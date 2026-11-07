package com.aicomic.unlock.config;

import com.aicomic.unlock.utils.AESUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExchangeCodeConfig {
    /**
     * 加密密钥，长度必须是256字节
     */
    @Value("${ac.unlock.aes.key}")
    private String key;
    /**
     * 初始向量，长度必须是16字节
     */
    @Value("${ac.unlock.aes.iv}")
    private String iv;

    @Bean
    public AESUtil aesUtil(){
        return new AESUtil(key, iv);
    }
}
