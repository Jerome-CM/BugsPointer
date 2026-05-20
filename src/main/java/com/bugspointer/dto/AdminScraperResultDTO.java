package com.bugspointer.dto;

import java.util.ArrayList;
import java.util.List;

public class AdminScraperResultDTO {

    private String startUrl;

    private int checkedPageCount;

    private int checkedLinkCount;

    private int checkedImageCount;

    private int errorCount;

    private boolean limitReached;

    private String globalError;

    private final List<AdminScraperResourceDTO> errors = new ArrayList<>();

    public String getStartUrl() {
        return startUrl;
    }

    public void setStartUrl(String startUrl) {
        this.startUrl = startUrl;
    }

    public int getCheckedPageCount() {
        return checkedPageCount;
    }

    public void setCheckedPageCount(int checkedPageCount) {
        this.checkedPageCount = checkedPageCount;
    }

    public int getCheckedLinkCount() {
        return checkedLinkCount;
    }

    public void setCheckedLinkCount(int checkedLinkCount) {
        this.checkedLinkCount = checkedLinkCount;
    }

    public int getCheckedImageCount() {
        return checkedImageCount;
    }

    public void setCheckedImageCount(int checkedImageCount) {
        this.checkedImageCount = checkedImageCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public boolean isLimitReached() {
        return limitReached;
    }

    public void setLimitReached(boolean limitReached) {
        this.limitReached = limitReached;
    }

    public String getGlobalError() {
        return globalError;
    }

    public void setGlobalError(String globalError) {
        this.globalError = globalError;
    }

    public List<AdminScraperResourceDTO> getErrors() {
        return errors;
    }

    public void addError(AdminScraperResourceDTO resource) {
        errors.add(resource);
        errorCount = errors.size();
    }
}
