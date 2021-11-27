package com.pofengsystem.server.config;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "char")
@ToString
public class CharConfig {
    /**
     * 压缩根路径
     */
    private String root;

    /**
     * 生成的案件压缩文件路径
     */
    private String targetZipRoot;
}
