package com.bugspointer.entity;

import lombok.Data;

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

    @NotBlank(message = "Le nom de l'entreprise est obligatoire")
    private String compagnyName;

    @NotBlank(message = "L'email doit être unique")
    private String mail;

    @NotBlank(message = "Le password est obligatoire")
    private String password;

    private String phoneNumber;

    private EnumPlan plan = EnumPlan.FREE;

    private String publicKey;

    @NotNull
    @Column(updatable = false)
    private Date dateCreation = new Date();

    private Date lastVisit;

    private boolean isEnable = true;

    private String role = "ROLE_USER";




}
