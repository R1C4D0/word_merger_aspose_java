package org.example.pojo;

import com.fasterxml.jackson.annotation.JsonProperty; // 如果使用Jackson
import java.util.List;

// 注意：为了简洁，这里使用了Jackson的注解。如果你使用Gson或其他库，注解方式会有所不同。
// 或者，如果字段名称与JSON中的键完全一致（包括大小写和下划线风格），有时可以省略注解。

public class MergeTemplate {

    @JsonProperty("template_name")
    private String templateName;

    @JsonProperty("output_filename_prefix")
    private String outputFilenamePrefix;

    @JsonProperty("output_directory")
    private String outputDirectory;

    @JsonProperty("output_document_properties")
    private OutputDocumentProperties outputDocumentProperties;

    @JsonProperty("global_settings_override")
    private GlobalSettingsOverride globalSettingsOverride; // 新增此类

    @JsonProperty("chapters_to_merge")
    private List<ChapterConfig> chaptersToMerge;

    @JsonProperty("default_on_chapter_not_found")
    private String defaultOnChapterNotFound;

    // Constructors (optional, Jackson can use default constructor)
    public MergeTemplate() {}

    // Getters and Setters
    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getOutputFilenamePrefix() {
        return outputFilenamePrefix;
    }

    public void setOutputFilenamePrefix(String outputFilenamePrefix) {
        this.outputFilenamePrefix = outputFilenamePrefix;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public OutputDocumentProperties getOutputDocumentProperties() {
        return outputDocumentProperties;
    }

    public void setOutputDocumentProperties(OutputDocumentProperties outputDocumentProperties) {
        this.outputDocumentProperties = outputDocumentProperties;
    }

    public GlobalSettingsOverride getGlobalSettingsOverride() {
        return globalSettingsOverride;
    }

    public void setGlobalSettingsOverride(GlobalSettingsOverride globalSettingsOverride) {
        this.globalSettingsOverride = globalSettingsOverride;
    }

    public List<ChapterConfig> getChaptersToMerge() {
        return chaptersToMerge;
    }

    public void setChaptersToMerge(List<ChapterConfig> chaptersToMerge) {
        this.chaptersToMerge = chaptersToMerge;
    }

    public String getDefaultOnChapterNotFound() {
        // 根据需求文档，默认值为 "error"
        // 需求文档: 2.2 字段详细说明 -> default_on_chapter_not_found
        return (defaultOnChapterNotFound != null) ? defaultOnChapterNotFound : "error";
    }

    public void setDefaultOnChapterNotFound(String defaultOnChapterNotFound) {
        this.defaultOnChapterNotFound = defaultOnChapterNotFound;
    }
}
