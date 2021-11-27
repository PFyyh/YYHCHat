package com.pofengsystem.server.config;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;

@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "chat")
@ToString
public class ChatConfig {
    /**
     * 字符集
     */
    private String charset;

    /**
     * 服务器端口
     */
    private Integer serverPort;

    @PostConstruct
    public void init(){
        log.info("字符集：{}",charset);
    }

}
