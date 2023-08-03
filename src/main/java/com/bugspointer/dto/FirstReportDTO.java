package com.bugspointer.dto;

import lombok.Data;

import java.util.Date;

@Data
public class FirstReportDTO {

    private Long id;

    private Long companyId;

    private String companyName;
    private String domaine;

    private Date dateConfirm = new Date();

    private boolean sendIsChecked = false;

    private String description = null;

    private Date send = null;

}
