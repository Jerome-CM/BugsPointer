package com.bugspointer.dto;

import com.bugspointer.entity.Poll;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PollSummaryDTO {

    private String context;

    private String label;

    private int responseCount;

    private double averageFindEasy;

    private double averageStepClarity;

    private double averageTargetFeatureGoodWork;

    private double globalAverage;

    private List<Poll> recentPolls;
}
