package com.bugspointer.dto;

import com.bugspointer.entity.EnumPlan;
import lombok.Data;

@Data
public class DashboardDTO {

    private Long id;

    private String mail;

    private String publicKey;

    private EnumPlan plan;

    private int nbNewBug;

    private int nbPendingBug;

    private int nbSolvedBug;

    private int nbIgnoredBug;
}
