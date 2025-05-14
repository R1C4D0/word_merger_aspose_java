package org.example.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OutputDocumentProperties {

    @JsonProperty("title")
    private String title;

    @JsonProperty("author")
    private String author;

    // Constructors
    public OutputDocumentProperties() {}

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
