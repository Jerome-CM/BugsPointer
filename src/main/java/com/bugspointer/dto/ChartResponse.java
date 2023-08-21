package com.bugspointer.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChartResponse {
    private String chartName;

    // labels for axe x
    private List<String> labels;


    private List<Dataset> datasets;
}
