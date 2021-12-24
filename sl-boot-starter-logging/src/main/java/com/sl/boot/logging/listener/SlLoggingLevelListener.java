package com.sl.boot.logging.listener;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;

/**
 * @author rock
 * @Description 日志级别监听器
 * @createTime 2021/12/24 16:41:00
 */
@ConfigListener
public class SlLoggingLevelListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
}
