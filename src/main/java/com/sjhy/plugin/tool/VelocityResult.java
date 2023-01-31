package com.sjhy.plugin.tool;

/**
 * @author bruce ge 2023/1/30
 */
public class VelocityResult {
    private String code;

    private String finalTemplate;

    private boolean hasError = false;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getFinalTemplate() {
        return finalTemplate;
    }

    public void setFinalTemplate(String finalTemplate) {
        this.finalTemplate = finalTemplate;
    }

    public boolean isHasError() {
        return hasError;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }
}
