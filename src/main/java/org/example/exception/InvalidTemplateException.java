package org.example.exception;


public class InvalidTemplateException extends Exception {

    private final String templatePath;

    public InvalidTemplateException(String message, String templatePath) {
        super(String.format("Invalid template (%s): %s", templatePath, message));
        this.templatePath = templatePath;
    }

    public InvalidTemplateException(String message, String templatePath, Throwable cause) {
        super(String.format("Invalid template (%s): %s", templatePath, message), cause);
        this.templatePath = templatePath;
    }

    /**
     * Gets the path of the template that caused the validation error.
     * @return The template file path.
     */
    public String getTemplatePath() {
        return templatePath;
    }
}
