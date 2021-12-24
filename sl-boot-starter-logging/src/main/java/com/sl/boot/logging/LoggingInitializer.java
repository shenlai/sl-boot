
package com.sl.boot.logging;


import com.sl.boot.logging.core.SlLoggingContext;

/**
 * 日志初始化器
 */
@FunctionalInterface
public interface LoggingInitializer {
    /**
     * 初始化
     *
     * @param context 日志上下文
     */
    void init(SlLoggingContext context);
}
