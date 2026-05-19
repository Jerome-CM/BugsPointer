package com.bugspointer.dto;

public class AdminScraperJobDTO {

    private final String id;

    private final String websiteUrl;

    private volatile boolean running = true;

    private volatile AdminScraperResultDTO result;

    private volatile String error;

    private volatile boolean cancelled;

    private final long createdAt;

    private volatile long completedAt;

    public AdminScraperJobDTO(String id, String websiteUrl) {
        this.id = id;
        this.websiteUrl = websiteUrl;
        this.createdAt = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public boolean isRunning() {
        return running;
    }

    public AdminScraperResultDTO getResult() {
        return result;
    }

    public String getError() {
        return error;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getCompletedAt() {
        return completedAt;
    }

    public void complete(AdminScraperResultDTO result) {
        if (cancelled) {
            return;
        }
        this.result = result;
        this.running = false;
        this.completedAt = System.currentTimeMillis();
    }

    public void fail(String error) {
        if (cancelled) {
            return;
        }
        this.error = error;
        this.running = false;
        this.completedAt = System.currentTimeMillis();
    }

    public void cancel() {
        this.cancelled = true;
        this.error = "Scan arrêté.";
        this.running = false;
        this.completedAt = System.currentTimeMillis();
    }
}
