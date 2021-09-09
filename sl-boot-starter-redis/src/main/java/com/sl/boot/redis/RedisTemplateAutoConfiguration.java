package com.sl.boot.redis;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;

@Slf4j
@Configuration
@EnableConfigurationProperties(RedisConfigProperties.class)
//ConditionalOnClass
public class RedisTemplateAutoConfiguration {

    @Autowired
    private RedisConfigProperties redisConfigProperties;

    /**
     * 默认实例
     *
     * @return
     */
    @Bean("stringRedisTemplate")
    @Primary
    public StringRedisTemplate stringRedisTemplate() {
        Assert.notNull(redisConfigProperties, "redis RedisConfigProperties empty.");

        return RedisConnectionConfigBuilder.build(redisConfigProperties.getHost(), redisConfigProperties.getPort(),
                redisConfigProperties.getIndex(), redisConfigProperties.getPassword(),
                redisConfigProperties.getMaxIdle(), redisConfigProperties.getMinIdle(), redisConfigProperties.getMaxTotal())
                .buildStringRedisTemplate();
    }
}
