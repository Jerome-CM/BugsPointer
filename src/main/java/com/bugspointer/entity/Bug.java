package com.bugspointer.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NonNull;

import java.util.Date;

@Entity
@Data
public class Bug {


    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @NotNull
    private Company company;

    @NotNull
    private String url;

    @NotNull
    private String description;

    private String codeLocation;

    @NotNull
    private String os;

    @NotNull
    private String browser;

    @NotNull
    private String screenSize;

    @NotNull
    @Column(updatable = false)
    private Date dateCreation;

    private Date dateView;

    private Date dateSolved;

}
