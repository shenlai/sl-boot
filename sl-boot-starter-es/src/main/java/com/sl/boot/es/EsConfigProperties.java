package com.sl.boot.es;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * ES 配置类
 */
@Getter
@Setter
@ConfigurationProperties("config.es")
public class EsConfigProperties {

    /**
     * 地址
     */
    private String addr;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 账户
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 默认连接超时时间
     */
    public int                         connectTimeout           = 1000;
    /**
     * 默认Socket 连接超时时间
     */
    public int                         socketTimeout            = 2000;
    /**
     * 默认获取连接的超时时间
     */
    public int                         connectionRequestTimeout = 1000;
    /**
     * 默认最大路由连接数
     */
    public int                         maxConnPerRoute          = 100;
    /**
     * 默认最大连接数
     */
    public int maxConnTotal = 100;

}
