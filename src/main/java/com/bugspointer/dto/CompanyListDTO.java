package com.bugspointer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class CompanyListDTO {

    private Long companyId;
    private String companyName;

    private int nbrTotalBug;

    private int nbrSolved;

    private String motifEnable;

    private String creationDate;

    private String dateDownload;

    private String plan;

}
