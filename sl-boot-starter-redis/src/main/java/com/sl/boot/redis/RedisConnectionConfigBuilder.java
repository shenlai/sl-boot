package com.sl.boot.redis;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;
import redis.clients.jedis.JedisPoolConfig;

@Getter
@Setter
@Slf4j
public class RedisConnectionConfigBuilder {

    private RedisConnectionConfigBuilder(String host, String password, Integer port, Integer index, Integer maxIdle, Integer minIdle, Integer maxTotal) {
        this.host = host;
        this.password = password;
        this.port = port;
        this.index = index;
        this.maxIdle = maxIdle == null ? this.maxIdle : maxIdle;
        this.minIdle = minIdle == null ? this.minIdle : minIdle;
        this.maxTotal = maxTotal == null ? this.maxTotal : maxTotal;
    }

    public static RedisConnectionConfigBuilder build(String host, Integer port, Integer index, String password, Integer maxIdle, Integer minIdle, Integer maxTotal) {
        return new RedisConnectionConfigBuilder(host, password, port, index, maxIdle, minIdle, maxTotal);
    }


    /**
     * 构建StringRedisTemplate实例
     *
     * @return
     */
    public StringRedisTemplate buildStringRedisTemplate() {

        log.info("Redis register start");
        Assert.notNull(getHost(), "host empty");
        Assert.notNull(getPort(), "port empty");
        Assert.notNull(getIndex(), "index empty");
        Assert.notNull(getPassword(), "password empty");

        JedisConnectionFactory factory = jedisConnectionFactory();
        factory.afterPropertiesSet();
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(factory);
        log.info("Redis register success");
        return template;
    }

    /**
     * 连接工厂
     *
     * @return
     */
    private JedisConnectionFactory jedisConnectionFactory() {

        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(getHost());
        redisStandaloneConfiguration.setPort(getPort());
        redisStandaloneConfiguration.setDatabase(getIndex());
        redisStandaloneConfiguration.setPassword(RedisPassword.of(getPassword()));

        JedisClientConfiguration.JedisPoolingClientConfigurationBuilder jpb = (JedisClientConfiguration.JedisPoolingClientConfigurationBuilder) JedisClientConfiguration.builder()
                .usePooling().poolConfig(jedisPoolConfig());

        return new JedisConnectionFactory(redisStandaloneConfiguration, jpb.build());
    }

    /**
     * 连接池
     *
     * @return
     */
    private JedisPoolConfig jedisPoolConfig() {

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        // 最大空闲数
        jedisPoolConfig.setMaxIdle(getMaxIdle());
        // 连接池的最大数据库连接数
        jedisPoolConfig.setMaxTotal(getMaxTotal());
        // 连接池的最大数据库连接数
        jedisPoolConfig.setMinIdle(getMinIdle());
        // 最大建立连接等待时间
        jedisPoolConfig.setMaxWaitMillis(getMaxWaitMillis());
        // 逐出连接的最小空闲时间 默认1800000毫秒(30分钟)
        jedisPoolConfig.setMinEvictableIdleTimeMillis(getMinEvictableIdleTimeMillis());
        // 每次逐出检查时 逐出的最大数目 如果为负数就是 : 1/abs(n), 默认3
        jedisPoolConfig.setNumTestsPerEvictionRun(getNumTestsPerEvictionRun());
        // 逐出扫描的时间间隔(毫秒) 如果为负数,则不运行逐出线程, 默认-1
        jedisPoolConfig.setTimeBetweenEvictionRunsMillis(getTimeBetweenEvictionRunsMillis());
        // 是否在从池中取出连接前进行检验,如果检验失败,则从池中去除连接并尝试取出另一个
        jedisPoolConfig.setTestOnBorrow(isTestOnBorrow());
        // 在空闲时检查有效性, 默认false
        jedisPoolConfig.setTestWhileIdle(isTestWhileIdle());
        return jedisPoolConfig;
    }


    /**
     * 主机
     */
    private String host;

    /**
     * 密码
     */
    private String password;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 本项目DB
     */
    private Integer index;

    /**
     * 最大空闲数
     */
    private int maxIdle = 100;

    /**
     * 连接池的最大数据库连接数
     */
    private int maxTotal = 100;

    private int minIdle = 50;

    /**
     * 最大建立连接等待时间
     */
    private int maxWaitMillis = 1000;

    /**
     * 逐出连接的最小空闲时间 默认1800000毫秒(30分钟)
     */
    private int minEvictableIdleTimeMillis = 300000;

    /**
     * 每次逐出检查时 逐出的最大数目 如果为负数就是 : 1/abs(n), 默认3
     */
    private int numTestsPerEvictionRun = 16;

    /**
     * 逐出扫描的时间间隔(毫秒) 如果为负数,则不运行逐出线程, 默认-1
     */
    private int timeBetweenEvictionRunsMillis = 3000;

    /**
     * 是否在从池中取出连接前进行检验,如果检验失败,则从池中去除连接并尝试取出另一个
     */
    private boolean testOnBorrow;

    /**
     * 在空闲时检查有效性, 默认false
     */
    private boolean testWhileIdle = true;
}
