package com.sl.boot.logging.listener;

import com.sl.boot.logging.core.SlLogbackLoggingSystem;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.ApplicationListener;
import org.springframework.boot.context.logging.LoggingApplicationListener;

/**
 * @author sl
 * @Description 设置日志系统作为应用的日志系统的应用监听器（比{@link LoggingApplicationListener}先运行）
 * @createTime 2021/12/24 14:12:00
 */
public class SlLogBackLoggingApplicationListener implements ApplicationListener<ApplicationStartingEvent> {

    @Override
    public void onApplicationEvent(ApplicationStartingEvent event) {
        String loggingSystem = System.getProperty(LoggingSystem.SYSTEM_PROPERTY);
        if (loggingSystem == null) {
            // 如果未设置过日志系统，则设置为boot日志系统
            System.setProperty(LoggingSystem.SYSTEM_PROPERTY, SlLogbackLoggingSystem.class.getName());
        }
    }
}
