package com.bugspointer.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
public class FirstReportDTO {

    @NotNull(message = "Le rapport à modifier est obligatoire")
    private Long id;

    private Long companyId;

    private String companyName;
    private String domaine;

    private Date dateConfirm = new Date();

    private boolean sendIsChecked = false;

    @Size(max = 500, message = "La description doit contenir moins de 500 caractères")
    private String description = null;

    private Date send = null;

}
