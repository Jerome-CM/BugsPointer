package com.bugspointer.dto;

import lombok.Data;

import java.util.Date;

@Data
public class FirstReportDTO {

    private Long companyId;

    private String companyName;

    private Date dateConfirm;

    private boolean sendIsChecked;

    private String description;

    private Date send;

}
