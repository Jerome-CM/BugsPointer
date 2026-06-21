package com.bugspointer.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class WidgetInstallationScanDTO {

    private String domain;

    private String publicKey;

    private int checkedPageCount;

    private boolean limitReached;

    private String errorMessage;

    private Date scannedAt = new Date();

    private List<String> widgetUrls = new ArrayList<>();

    private List<String> linkUrls = new ArrayList<>();

    public int getWidgetPageCount() {
        return widgetUrls.size();
    }

    public int getLinkPageCount() {
        return linkUrls.size();
    }

    public boolean hasResult() {
        return errorMessage == null || errorMessage.trim().isEmpty();
    }
}
