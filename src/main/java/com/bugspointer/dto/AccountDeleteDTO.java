package com.bugspointer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class AccountDeleteDTO extends AccountDTO {

    private int nbNewBug;

    private int nbPendingBug;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateLineFacturePlan;
}
