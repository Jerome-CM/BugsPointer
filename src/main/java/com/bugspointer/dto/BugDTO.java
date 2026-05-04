package com.bugspointer.dto;

import com.bugspointer.entity.EnumEtatBug;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class BugDTO {

    @NotNull
    private Long id;

    @NotNull
    private String url;

    private String description;

    private String codeLocation;

    private EnumEtatBug etatBug;

    private String os;

    private String browser;

    private String browserLanguage;

    private String deviceType;

    private String adresseIp;

    private String screenSize;

    @JsonFormat(pattern = "dd-MM-yyyy HH:ii:ss")
    private Date dateCreation;

    @JsonFormat(pattern = "dd-MM-yyyy HH:ii:ss")
    private Date dateEnvoi;

    @JsonFormat(pattern = "dd-MM-yyyy HH:ii:ss")
    private Date dateView;

    @JsonFormat(pattern = "dd-MM-yyyy HH:ii:ss")
    private Date dateSolved;
}
