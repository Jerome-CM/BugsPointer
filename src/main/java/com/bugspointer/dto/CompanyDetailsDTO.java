package com.bugspointer.dto;

import com.bugspointer.entity.EnumMotif;
import com.bugspointer.entity.EnumPlan;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class CompanyDetailsDTO{

    private Long companyId;
    private String companyName;

    private String domaine;

    private Date dateCreation;

    private Date lastVisit;

    private Date lastReport = null;
    private String motifEnable;

    private boolean isEnable;
    private int profit;

    private EnumPlan plan;

    private int nbNewBug;

    private int nbPendingBug;

    private int nbSolvedBug;

    private int nbIgnoredBug;

    private String customerId;

    private String dateLastMandate;

    private List<MandateDTO> mandatesList;

}
