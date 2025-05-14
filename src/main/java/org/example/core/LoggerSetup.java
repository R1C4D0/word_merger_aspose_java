package org.example.core;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
// import ch.qos.logback.core.FileAppender; // Not directly used if using RollingFileAppender
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP; // <--- 关键导入
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import org.example.pojo.GlobalSettingsOverride;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LoggerSetup {

    private static final org.slf4j.Logger staticLogger = LoggerFactory.getLogger(LoggerSetup.class);
    private static final String DEFAULT_LOG_FILE_PATH = "logs/word-merger-java.log";
    private static final String DEFAULT_CONSOLE_LOG_LEVEL = "INFO";
    private static final String DEFAULT_FILE_LOG_LEVEL = "DEBUG";

    public static void setupLogging(String cliLogLevel, String cliLogFile, GlobalSettingsOverride templateGlobalSettings) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();

        String consoleLogLevelStr = cliLogLevel != null ? cliLogLevel.toUpperCase() : DEFAULT_CONSOLE_LOG_LEVEL;
        String fileLogLevelStr = DEFAULT_FILE_LOG_LEVEL;
        String logFilePath = cliLogFile != null ? cliLogFile : DEFAULT_LOG_FILE_PATH;

        if (templateGlobalSettings != null && templateGlobalSettings.getLogLevel() != null && !templateGlobalSettings.getLogLevel().isEmpty()) {
            if (cliLogLevel == null) {
                consoleLogLevelStr = templateGlobalSettings.getLogLevel().toUpperCase();
            }
        }

        Level consoleLevel = Level.toLevel(consoleLogLevelStr, Level.INFO);
        Level fileLevel = Level.toLevel(fileLogLevelStr, Level.DEBUG);

        try {
            Path logDir = Paths.get(logFilePath).getParent();
            if (logDir != null) {
                Files.createDirectories(logDir);
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not create log directory for " + logFilePath + ". Error: " + e.getMessage());
            logFilePath = new File(logFilePath).getName();
        }

        // Console Appender
        PatternLayoutEncoder consoleEncoder = new PatternLayoutEncoder();
        consoleEncoder.setContext(context);
        consoleEncoder.setPattern("%d{yyyy-MM-dd HH:mm:ss,SSS} [%level] [%logger{0}.%M] - %msg%n");
        consoleEncoder.start();

        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(context);
        consoleAppender.setEncoder(consoleEncoder);
        consoleAppender.setName("STDOUT");
        consoleAppender.start();

        // File Appender (Rolling)
        PatternLayoutEncoder fileEncoder = new PatternLayoutEncoder();
        fileEncoder.setContext(context);
        fileEncoder.setPattern("%d{yyyy-MM-dd HH:mm:ss,SSS} [%level] [%thread] [%logger] - %msg%n%rEx");
        fileEncoder.start();

        RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<>();
        fileAppender.setContext(context);
        fileAppender.setEncoder(fileEncoder);
        fileAppender.setFile(logFilePath); // 设置基础文件名
        fileAppender.setName("FILE");

        // 使用 SizeAndTimeBasedFNATP 来结合基于时间和大小的滚动
        TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
        rollingPolicy.setContext(context);
        rollingPolicy.setParent(fileAppender);
        // 文件名模式：基础日志文件.日期.索引.gz
        // 例如: word-merger-java.log.2025-05-14.0.gz
        rollingPolicy.setFileNamePattern(logFilePath + ".%d{yyyy-MM-dd}.%i.gz");
        rollingPolicy.setMaxHistory(5); // 保留最近5个归档文件（例如5天，如果每天都滚动）
        rollingPolicy.setTotalSizeCap(new FileSize(50 * FileSize.MB_COEFFICIENT)); // 所有归档文件的总大小上限

        // 配置 SizeAndTimeBasedFNATP
        SizeAndTimeBasedFNATP<ILoggingEvent> sizeAndTimeBasedFNATP = new SizeAndTimeBasedFNATP<>();
        sizeAndTimeBasedFNATP.setContext(context);
        // 设置单个文件的最大大小，当文件达到此大小时，即使在同一天内也会滚动，并增加索引 %i
        sizeAndTimeBasedFNATP.setMaxFileSize(new FileSize(10 * FileSize.MB_COEFFICIENT));
        // 将此策略设置给 TimeBasedRollingPolicy
        rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(sizeAndTimeBasedFNATP);

        rollingPolicy.start(); // 启动滚动策略

        fileAppender.setRollingPolicy(rollingPolicy); // 将策略赋给Appender
        fileAppender.start(); // 启动Appender

        ch.qos.logback.classic.Logger rootLogger = context.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        rootLogger.detachAndStopAllAppenders();

        // 设置根日志记录器的级别。通常设置为所有附加程序所需的最详细级别。
        // 或者，可以设置为Level.ALL，然后在附加程序上使用过滤器（如果需要更复杂的过滤）。
        // 这里，我们简单地将根级别设置为文件日志级别，因为它通常更详细。
        rootLogger.setLevel(fileLevel.isGreaterOrEqual(consoleLevel) ? fileLevel : consoleLevel); // 取两者中较低（更详细）的级别

        // 将附加程序添加到根日志记录器
        // 你也可以为附加程序设置阈值过滤器来控制它们记录的级别，
        // 如果根日志级别非常低（例如TRACE或ALL）。
        // 例如:
        // ThresholdFilter consoleFilter = new ThresholdFilter();
        // consoleFilter.setLevel(consoleLevel.toString());
        // consoleFilter.start();
        // consoleAppender.addFilter(consoleFilter);
        rootLogger.addAppender(consoleAppender);
        rootLogger.addAppender(fileAppender);

        staticLogger.info("Logging setup complete. Effective Root Level: {}. Console appender active. File appender active for path: {}",
                rootLogger.getEffectiveLevel(), new File(logFilePath).getAbsolutePath());
    }
}