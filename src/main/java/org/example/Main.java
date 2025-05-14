package org.example;

import com.fasterxml.jackson.databind.ObjectMapper; // 需要引入这个
import org.example.core.ConfigLoader;
import org.example.core.DocumentMerger;
import org.example.exception.InvalidTemplateException;
import org.example.core.LoggerSetup;
import org.example.pojo.GlobalSettingsOverride; // 确保POJO的路径正确
import org.example.pojo.MergeTemplate;        // 确保POJO的路径正确
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map; // 需要引入这个
import java.util.concurrent.Callable;

@Command(name = "word-merger-java",
        mixinStandardHelpOptions = true,
        version = "WordMergerJava 0.1.0",
        description = "Merges specific chapters from Word documents based on a JSON template.")
public class Main implements Callable<Integer> {

    private static Logger logger; // 将在 setupLogging 后初始化

    @Option(names = {"-t", "--template"},
            required = true,
            description = "Path to the merge template JSON file.")
    private File templateFile;

    @Option(names = {"-o", "--output-dir"},
            defaultValue = "./output",
            description = "Output directory for merged documents. Default: ${DEFAULT-VALUE}")
    private File outputDir;

    @Option(names = {"-l", "--log-level"},
            // defaultValue = "INFO", // LoggerSetup 将处理默认值
            description = "Set the logging level for console. Options: TRACE, DEBUG, INFO, WARN, ERROR. Default: INFO (can be overridden by template)")
    private String logLevel; // LoggerSetup 将会使用这个值

    @Option(names = {"--log-file"},
            description = "Path to the log file. If not specified, logging to file might use a default path or be disabled depending on LoggerSetup.")
    private File logFile;


    @Override
    public Integer call() throws Exception {
        // 1. 初始化日志系统
        // 首先尝试从模板预读取日志级别设置
        GlobalSettingsOverride templateGlobalLogSettings = null;
        if (templateFile.exists() && templateFile.isFile()) {
            try {
                ObjectMapper tempMapper = new ObjectMapper();
                Map<String, Object> tempJson = tempMapper.readValue(templateFile, new com.fasterxml.jackson.core.type.TypeReference<Map<String,Object>>() {});
                if (tempJson.containsKey("global_settings_override")) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> globalSettingsMap = (Map<String, String>) tempJson.get("global_settings_override");
                    if (globalSettingsMap != null && globalSettingsMap.containsKey("log_level")) {
                        templateGlobalLogSettings = new GlobalSettingsOverride();
                        templateGlobalLogSettings.setLogLevel(globalSettingsMap.get("log_level"));
                    }
                }
            } catch (Exception e) {
                // 预读失败，没关系，LoggerSetup会使用默认值或命令行参数
                System.err.println("Warning: Could not pre-read template for log settings: " + e.getMessage());
            }
        }

        // 调用 LoggerSetup
        LoggerSetup.setupLogging(logLevel, (logFile != null ? logFile.getAbsolutePath() : null), templateGlobalLogSettings);
        logger = LoggerFactory.getLogger(Main.class); // 获取已配置的logger实例

        logger.info("Word Merger Java starting...");
        logger.debug("Command line arguments received:");
        logger.debug("  Template file: {}", templateFile.getAbsolutePath());
        logger.debug("  Output directory: {}", outputDir.getAbsolutePath());
        logger.debug("  CLI Log level: {}", logLevel); // 这是命令行传入的，实际级别由LoggerSetup决定
        if (logFile != null) {
            logger.debug("  CLI Log file: {}", logFile.getAbsolutePath());
        }


        // 2. 确保输出目录存在
        if (!outputDir.exists()) {
            logger.info("Output directory {} does not exist, creating it.", outputDir.getAbsolutePath());
            try {
                Files.createDirectories(outputDir.toPath());
            } catch (Exception e) {
                logger.error("Failed to create output directory: {}", outputDir.getAbsolutePath(), e);
                return 1;
            }
        } else if (!outputDir.isDirectory()) {
            logger.error("Specified output path {} is not a directory.", outputDir.getAbsolutePath());
            return 1;
        }


        // 3. 加载并验证模板
        ConfigLoader configLoader = new ConfigLoader();
        MergeTemplate templateConfig;
        try {
            logger.info("Loading and validating template: {}", templateFile.getAbsolutePath());
            templateConfig = configLoader.loadAndValidateTemplate(templateFile.getAbsolutePath());
            // 如果模板中的日志级别改变了，并且LoggerSetup支持动态重配，这里可以触发一次
            // 但我们当前的LoggerSetup是在启动时一次性配置。
        } catch (InvalidTemplateException | IOException e) { // Catch specific exceptions
            logger.error("Failed to load or validate template: {}", templateFile.getAbsolutePath(), e);
            return 1;
        } catch (Exception e) { // Catch any other unexpected errors during load/validation
            logger.error("Unexpected error loading or validating template: {}", templateFile.getAbsolutePath(), e);
            return 1;
        }

        // 4. 初始化 DocumentMerger
        logger.debug("Initializing DocumentMerger with template: {}", templateConfig.getTemplateName() != null ? templateConfig.getTemplateName() : "Unnamed Template");
        DocumentMerger merger = new DocumentMerger(templateConfig);

        // 5. 执行合并操作
        try {
            logger.info("Starting document merge process...");
            String outputPath = merger.execute(outputDir.getAbsolutePath());
            logger.info("Successfully created merged document: {}", outputPath);
        } catch (Exception e) {
            logger.error("Error during document merge process: {}", e.getMessage(), e);
            // 考虑在 DocumentMerger 中更细致地捕获和记录错误，这里是最终的捕获
            return 1;
        }

        logger.info("Word Merger Java finished successfully.");
        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}