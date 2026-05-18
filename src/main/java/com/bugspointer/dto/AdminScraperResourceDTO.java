package com.bugspointer.dto;

public class AdminScraperResourceDTO {

    private final String sourceUrl;

    private final String url;

    private final String type;

    private final int statusCode;

    private final String error;

    private final boolean internal;

    public AdminScraperResourceDTO(String sourceUrl, String url, String type, int statusCode, String error, boolean internal) {
        this.sourceUrl = sourceUrl;
        this.url = url;
        this.type = type;
        this.statusCode = statusCode;
        this.error = error;
        this.internal = internal;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getUrl() {
        return url;
    }

    public String getType() {
        return type;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getError() {
        return error;
    }

    public boolean isInternal() {
        return internal;
    }

    public boolean isSuccess() {
        return statusCode == 200 && (error == null || error.isEmpty());
    }
}
