package com.sl.boot.logging.core;

import ch.qos.logback.classic.LoggerContext;
import lombok.Getter;
import org.springframework.core.env.Environment;

/**
 * @author sl
 * @Description 日志上下文
 * @createTime 2021/12/24 16:13:00
 */
@Getter
public class SlLoggingContext {

    // logback上下文
    private final LoggerContext loggerContext;
    // spring环境
    private final Environment environment;
    // logback配置器
    private final SlLogbackConfigurator configurator;

    public SlLoggingContext(LoggerContext loggerContext, Environment environment) {
        this.loggerContext = loggerContext;
        this.environment = environment;
        this.configurator = new SlLogbackConfigurator(loggerContext);
    }

}
