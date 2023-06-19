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

    private EnumEtatBug etatBug;

    @JsonFormat(pattern = "dd-MM-yyyy HH:ii:ss")
    private Date dateCreation;

    @JsonFormat(pattern = "dd-MM-yyyy HH:ii:ss")
    private Date dateView;

    @JsonFormat(pattern = "dd-MM-yyyy HH:ii:ss")
    private Date dateSolved;
}
