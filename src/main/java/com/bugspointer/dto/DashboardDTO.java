package com.bugspointer.dto;

import lombok.Data;

@Data
public class DashboardDTO {

    private Long id;

    private String mail;

    private String publicKey;

    private int nbNewBug;

    private int nbPendingBug;

    private int nbSolvedBug;

    private int nbIgnoredBug;
}
