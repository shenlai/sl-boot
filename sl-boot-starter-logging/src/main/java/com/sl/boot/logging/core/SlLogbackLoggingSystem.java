/* 
 * 作者：钟勋 (e-mail:zhongxunking@163.com)
 */

/*
 * 修订记录:
 * @author 钟勋 2017-10-19 19:38 创建
 */
package com.sl.boot.logging.core;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.jul.LevelChangePropagator;
import com.sl.boot.logging.LoggingInitializer;
import org.slf4j.ILoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.slf4j.impl.StaticLoggerBinder;
import org.springframework.boot.logging.LogFile;
import org.springframework.boot.logging.LoggingInitializationContext;
import org.springframework.boot.logging.logback.ColorConverter;
import org.springframework.boot.logging.logback.ExtendedWhitespaceThrowableProxyConverter;
import org.springframework.boot.logging.logback.LogbackLoggingSystem;
import org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.Assert;

import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogManager;

/**
 * @author sl
 * @Description 日志系统
 * @createTime 2021/12/24 16:19:00
 */
public class SlLogbackLoggingSystem extends LogbackLoggingSystem {

    public SlLogbackLoggingSystem(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    protected void loadDefaults(LoggingInitializationContext initializationContext, LogFile logFile) {
        LoggerContext loggerContext = getLoggerContext();
        stopAndReset(loggerContext);
        // 构建日志上下文
        SlLoggingContext context = new SlLoggingContext(loggerContext, initializationContext.getEnvironment());
        // 基本配置
        baseConfig(context.getConfigurator());
        // 初始化日志
        initLogging(context);
        loggerContext.setPackagingDataEnabled(true);
    }

    // 基本配置（参考：DefaultLogbackConfiguration#base）
    private void baseConfig(SlLogbackConfigurator config) {
        config.conversionRule("clr", ColorConverter.class);
        config.conversionRule("wex", WhitespaceThrowableProxyConverter.class);
        config.conversionRule("wEx", ExtendedWhitespaceThrowableProxyConverter.class);
    }

    // 初始化日志
    private void initLogging(SlLoggingContext context) {
        // 加载日志初始化器
        List<LoggingInitializer> initializers = SpringFactoriesLoader.loadFactories(LoggingInitializer.class, getClassLoader());
        for (LoggingInitializer initializer : initializers) {
            // 初始化
            initializer.init(context);
        }
    }

    //------ 以下方法由于在LogbackLoggingSystem中是私有的，不能调用，所以拷贝过来 ------
    private LoggerContext getLoggerContext() {
        ILoggerFactory factory = StaticLoggerBinder.getSingleton().getLoggerFactory();
        Assert.isInstanceOf(LoggerContext.class, factory,
                String.format(
                        "LoggerFactory is not a Logback LoggerContext but Logback is on "
                                + "the classpath. Either remove Logback or the competing "
                                + "implementation (%s loaded from %s). If you are using "
                                + "WebLogic you will need to add 'org.slf4j' to "
                                + "prefer-application-packages in WEB-INF/weblogic.xml",
                        factory.getClass(), getLocation(factory)));
        return (LoggerContext) factory;
    }

    private Object getLocation(ILoggerFactory factory) {
        try {
            ProtectionDomain protectionDomain = factory.getClass().getProtectionDomain();
            CodeSource codeSource = protectionDomain.getCodeSource();
            if (codeSource != null) {
                return codeSource.getLocation();
            }
        } catch (SecurityException ex) {
            // Unable to determine location
        }
        return "unknown location";
    }

    private void stopAndReset(LoggerContext loggerContext) {
        loggerContext.stop();
        loggerContext.reset();
        if (isBridgeHandlerInstalled()) {
            addLevelChangePropagator(loggerContext);
        }
    }

    private boolean isBridgeHandlerInstalled() {
        if (!isBridgeHandlerAvailable()) {
            return false;
        }
        java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        return handlers.length == 1 && SLF4JBridgeHandler.class.isInstance(handlers[0]);
    }

    private void addLevelChangePropagator(LoggerContext loggerContext) {
        LevelChangePropagator levelChangePropagator = new LevelChangePropagator();
        levelChangePropagator.setResetJUL(true);
        levelChangePropagator.setContext(loggerContext);
        loggerContext.addListener(levelChangePropagator);
    }
}
