package org.example.pojo;
// package your.package.name; // 替换为你的包名

import com.fasterxml.jackson.annotation.JsonProperty; // 如果使用Jackson

/**
 * Represents the "global_settings_override" section in the merge template JSON.
 * This allows the template to override certain global application settings.
 */
public class GlobalSettingsOverride {

    @JsonProperty("log_level")
    private String logLevel;

    /**
     * Default constructor.
     * Used by JSON deserializers like Jackson.
     */
    public GlobalSettingsOverride() {
        // Jackson或其他JSON库可能需要一个无参数的构造函数
    }

    /**
     * Gets the log level specified in the template to override global settings.
     *
     * @return The log level string (e.g., "DEBUG", "INFO"). Can be null if not specified.
     */
    public String getLogLevel() {
        return logLevel;
    }

    /**
     * Sets the log level.
     * This method would typically be called by the JSON deserializer.
     *
     * @param logLevel The log level string.
     */
    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    // Potentially add other global settings here in the future
    // For example:
    // @JsonProperty("some_other_global_setting")
    // private boolean someOtherGlobalSetting;
    //
    // public boolean isSomeOtherGlobalSetting() {
    //     return someOtherGlobalSetting;
    // }
    //
    // public void setSomeOtherGlobalSetting(boolean someOtherGlobalSetting) {
    //     this.someOtherGlobalSetting = someOtherGlobalSetting;
    // }

    @Override
    public String toString() {
        return "GlobalSettingsOverride{" +
                "logLevel='" + logLevel + '\'' +
                // Add other fields here if they are added
                '}';
    }
}