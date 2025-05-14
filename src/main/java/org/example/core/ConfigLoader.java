package org.example.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.exception.InvalidTemplateException;
import org.example.pojo.ChapterConfig;
import org.example.pojo.MergeTemplate;
import org.example.pojo.OutputDocumentProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.pojo.GlobalSettingsOverride;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ConfigLoader {

    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
    private final ObjectMapper objectMapper;

    public ConfigLoader() {
        this.objectMapper = new ObjectMapper();
        // 可以配置objectMapper，例如：
        // objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 以允许JSON中有多余字段而不报错
    }

    /**
     * Loads and validates a merge template from a JSON file.
     *
     * @param templatePath Path to the template JSON file.
     * @return MergeTemplate object.
     * @throws IOException              If there's an error reading the file or parsing JSON.
     * @throws InvalidTemplateException If the template validation fails.
     */
    public MergeTemplate loadAndValidateTemplate(String templatePath) throws IOException, InvalidTemplateException {
        logger.info("Loading merge template from: {}", templatePath);
        File templateFile = new File(templatePath);
        if (!templateFile.exists() || !templateFile.isFile()) {
            String errorMsg = "Template file not found or is not a file: " + templatePath;
            logger.error(errorMsg);
            throw new IOException(errorMsg);
        }

        MergeTemplate template;
        try {
            template = objectMapper.readValue(templateFile, MergeTemplate.class);
        } catch (IOException e) {
            logger.error("Failed to parse template JSON from {}: {}", templatePath, e.getMessage(), e);
            throw e; // Re-throw original Jackson parsing exception
        }

        validateTemplate(template, templatePath); // Pass templatePath for better error messages
        logger.info("Template '{}' loaded and validated successfully.", template.getTemplateName() != null ? template.getTemplateName() : templatePath);
        return template;
    }

    /**
     * Validates the structure and content of a merge template.
     * This mirrors the validation logic from the Python version's config.py.
     *
     * @param template     The MergeTemplate object to validate.
     * @param templatePath The path from which the template was loaded (for logging).
     * @throws InvalidTemplateException If validation fails.
     */
    public void validateTemplate(MergeTemplate template, String templatePath) throws InvalidTemplateException {
        logger.debug("Validating template from: {}", templatePath);

        // Validate output_document_properties if present
        if (template.getOutputDocumentProperties() != null) {
            OutputDocumentProperties props = template.getOutputDocumentProperties();
            if (props.getTitle() != null && !(props.getTitle() instanceof String)) { // Should be caught by Jackson already
                throw new InvalidTemplateException("'output_document_properties.title' must be a string.", templatePath);
            }
            if (props.getAuthor() != null && !(props.getAuthor() instanceof String)) {
                throw new InvalidTemplateException("'output_document_properties.author' must be a string.", templatePath);
            }
        }

        // Validate global_settings_override if present
        if (template.getGlobalSettingsOverride() != null) {
            GlobalSettingsOverride settings = template.getGlobalSettingsOverride();
            if (settings.getLogLevel() != null) {
                List<String> allowedLogLevels = Arrays.asList("TRACE", "DEBUG", "INFO", "WARNING", "ERROR", "CRITICAL"); // CRITICAL is python, map to ERROR or OFF in Java logging
                if (!allowedLogLevels.contains(settings.getLogLevel().toUpperCase())) {
                    throw new InvalidTemplateException(
                            String.format("Invalid 'global_settings_override.log_level': %s. Must be one of: %s",
                                    settings.getLogLevel(), String.join(", ", allowedLogLevels)), templatePath);
                }
            }
        }

        // Check required top-level fields
        if (template.getChaptersToMerge() == null || template.getChaptersToMerge().isEmpty()) {
            throw new InvalidTemplateException("Template must contain a non-empty 'chapters_to_merge' array.", templatePath);
        }

        // Validate each chapter entry
        Set<Integer> orders = new HashSet<>();
        List<Integer> chapterOrdersList;

        for (int i = 0; i < template.getChaptersToMerge().size(); i++) {
            ChapterConfig chapter = template.getChaptersToMerge().get(i);
            String chapterContext = String.format("chapter entry at index %d (source: %s)", i, chapter.getSourceDocumentPath());

            if (chapter.getSourceDocumentPath() == null || chapter.getSourceDocumentPath().trim().isEmpty()) {
                throw new InvalidTemplateException("Chapter entry missing required field: source_document_path. " + chapterContext, templatePath);
            }
            if (chapter.getChapterIdentifierType() == null) {
                throw new InvalidTemplateException("Chapter entry missing required field: chapter_identifier_type. " + chapterContext, templatePath);
            }
            if (chapter.getChapterIdentifierValue() == null) {
                throw new InvalidTemplateException("Chapter entry missing required field: chapter_identifier_value. " + chapterContext, templatePath);
            }
            if (chapter.getOrderInMergedDocument() <= 0) { // order_in_merged_document is int, so can't be null
                throw new InvalidTemplateException("Chapter entry missing or invalid required field: order_in_merged_document (must be > 0). " + chapterContext, templatePath);
            }


            if (!Arrays.asList("name", "number").contains(chapter.getChapterIdentifierType().toLowerCase())) {
                throw new InvalidTemplateException(
                        String.format("Invalid chapter_identifier_type: %s. Must be 'name' or 'number'. %s",
                                chapter.getChapterIdentifierType(), chapterContext), templatePath);
            }

            int order = chapter.getOrderInMergedDocument();
            if (order < 1) {
                throw new InvalidTemplateException(
                        String.format("Invalid order_in_merged_document: %d. Must be a positive integer. %s",
                                order, chapterContext), templatePath);
            }
            if (orders.contains(order)) {
                throw new InvalidTemplateException(
                        String.format("Duplicate order_in_merged_document value: %d. %s", order, chapterContext), templatePath);
            }
            orders.add(order);

            if (chapter.getTargetHeadingLevel() != null && chapter.getTargetHeadingLevel() < 1) {
                throw new InvalidTemplateException(
                        String.format("Invalid 'target_heading_level': %d. Must be a positive integer if provided. %s",
                                chapter.getTargetHeadingLevel(), chapterContext), templatePath);
            }

            if (chapter.getOnChapterNotFound() != null) { // Getter handles default if not present in JSON
                List<String> validActions = Arrays.asList("error", "skip", "insert_placeholder");
                if (!validActions.contains(chapter.getOnChapterNotFound().toLowerCase())) {
                    throw new InvalidTemplateException(
                            String.format("Invalid on_chapter_not_found action: %s. Must be one of: %s. %s",
                                    chapter.getOnChapterNotFound(), String.join(", ", validActions), chapterContext), templatePath);
                }
            }
        }

        // Check continuity of order_in_merged_document
        // Based on Python: sorted_orders != list(range(1, len(sorted_orders) + 1))
        if (!orders.isEmpty()) {
            chapterOrdersList = orders.stream().sorted().collect(Collectors.toList());
            for (int i = 0; i < chapterOrdersList.size(); i++) {
                if (chapterOrdersList.get(i) != (i + 1)) {
                    throw new InvalidTemplateException(
                            String.format("'order_in_merged_document' values must be continuous starting from 1. Found: %s",
                                    chapterOrdersList.toString()), templatePath);
                }
            }
        }


        if (template.getDefaultOnChapterNotFound() != null) { // Getter handles default if not present in JSON
            List<String> validActions = Arrays.asList("error", "skip", "insert_placeholder");
            if (!validActions.contains(template.getDefaultOnChapterNotFound().toLowerCase())) { // Check actual value
                throw new InvalidTemplateException(
                        String.format("Invalid 'default_on_chapter_not_found' action: %s. Must be one of: %s",
                                template.getDefaultOnChapterNotFound(), String.join(", ", validActions)), templatePath);
            }
        }
        logger.debug("Template validation successful for: {}", templatePath);
    }
}

