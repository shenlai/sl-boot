package com.sl.boot.redis;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ConfigurationProperties(prefix = "config.redis")
public class RedisConfigProperties {


    /**
     * 主机
     */
    private String host;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 密码
     */
    private String password;


    /**
     * 本项目DB
     */
    private Integer index;

    /**
     * 最大空闲数
     */
    private Integer maxIdle;

    /**
     * 连接池的最大数据库连接数
     */
    private Integer maxTotal;

    private Integer minIdle;


}
