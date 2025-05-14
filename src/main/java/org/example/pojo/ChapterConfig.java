package org.example.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChapterConfig {

    @JsonProperty("source_document_path")
    private String sourceDocumentPath;

    @JsonProperty("chapter_identifier_type")
    private String chapterIdentifierType;

    @JsonProperty("chapter_identifier_value")
    private String chapterIdentifierValue;

    @JsonProperty("chapter_identifier_case_sensitive")
    private Boolean chapterIdentifierCaseSensitive; // 使用 Boolean 以允许 null，然后处理默认值

    @JsonProperty("order_in_merged_document")
    private int orderInMergedDocument;

    @JsonProperty("target_heading_level")
    private Integer targetHeadingLevel; // 使用 Integer 以允许 null (可选字段)

    @JsonProperty("include_subsections")
    private Boolean includeSubsections; // 使用 Boolean 以允许 null，然后处理默认值

    @JsonProperty("source_document_password")
    private String sourceDocumentPassword; // 可以为 null

    @JsonProperty("on_chapter_not_found")
    private String onChapterNotFound; // 可以为 null, 将使用 MergeTemplate 中的默认值

    @JsonProperty("insert_page_break_after")
    private Boolean insertPageBreakAfter; // 使用 Boolean 以允许 null，然后处理默认值

    // Constructors
    public ChapterConfig() {}

    // Getters and Setters
    public String getSourceDocumentPath() {
        return sourceDocumentPath;
    }

    public void setSourceDocumentPath(String sourceDocumentPath) {
        this.sourceDocumentPath = sourceDocumentPath;
    }

    public String getChapterIdentifierType() {
        return chapterIdentifierType;
    }

    public void setChapterIdentifierType(String chapterIdentifierType) {
        this.chapterIdentifierType = chapterIdentifierType;
    }

    public String getChapterIdentifierValue() {
        return chapterIdentifierValue;
    }

    public void setChapterIdentifierValue(String chapterIdentifierValue) {
        this.chapterIdentifierValue = chapterIdentifierValue;
    }

    public boolean isChapterIdentifierCaseSensitive() {
        // 根据需求文档，默认为 false
        // 需求文档: 2.2 字段详细说明 -> chapter_identifier_case_sensitive
        return (chapterIdentifierCaseSensitive != null) ? chapterIdentifierCaseSensitive : false;
    }

    public void setChapterIdentifierCaseSensitive(Boolean chapterIdentifierCaseSensitive) {
        this.chapterIdentifierCaseSensitive = chapterIdentifierCaseSensitive;
    }

    public int getOrderInMergedDocument() {
        return orderInMergedDocument;
    }

    public void setOrderInMergedDocument(int orderInMergedDocument) {
        this.orderInMergedDocument = orderInMergedDocument;
    }

    public Integer getTargetHeadingLevel() {
        return targetHeadingLevel;
    }

    public void setTargetHeadingLevel(Integer targetHeadingLevel) {
        this.targetHeadingLevel = targetHeadingLevel;
    }

    public boolean isIncludeSubsections() {
        // 根据需求文档，默认为 true
        // 需求文档: 2.2 字段详细说明 -> include_subsections
        return (includeSubsections != null) ? includeSubsections : true;
    }

    public void setIncludeSubsections(Boolean includeSubsections) {
        this.includeSubsections = includeSubsections;
    }

    public String getSourceDocumentPassword() {
        return sourceDocumentPassword;
    }

    public void setSourceDocumentPassword(String sourceDocumentPassword) {
        this.sourceDocumentPassword = sourceDocumentPassword;
    }

    public String getOnChapterNotFound() {
        // 如果此字段为 null，则 DocumentMerger 逻辑会回退到 MergeTemplate 中的 default_on_chapter_not_found
        return onChapterNotFound;
    }

    public void setOnChapterNotFound(String onChapterNotFound) {
        this.onChapterNotFound = onChapterNotFound;
    }

    public boolean isInsertPageBreakAfter() {
        // 根据需求文档，默认为 false
        // 需求文档: 2.2 字段详细说明 -> insert_page_break_after
        return (insertPageBreakAfter != null) ? insertPageBreakAfter : false;
    }

    public void setInsertPageBreakAfter(Boolean insertPageBreakAfter) {
        this.insertPageBreakAfter = insertPageBreakAfter;
    }
}
