package com.bugspointer.entity;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Data
public class Company {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private String companyName;

    private String mail;

    private String password;

    private String phoneNumber = null;


    private EnumPlan plan = EnumPlan.FREE;

    @Column(unique = true)
    private String publicKey = null;

    @NotNull
    @Column(updatable = false)
    private Date dateCreation = new Date();

    private Date lastVisit = new Date();

    private boolean isEnable = true;

    private String role = "ROLE_USER";

}
