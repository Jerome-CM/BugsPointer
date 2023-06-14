package com.bugspointer.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
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

    private EnumIndicatif indicatif = EnumIndicatif.FRANCE;

    private String phoneNumber;

    private String domaine;

    private EnumPlan plan = EnumPlan.FREE;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateLineFacturePlan;

    @Column(unique = true, updatable = false)
    private String publicKey = null;

    @NotNull
    @Column(updatable = false)
    private Date dateCreation = new Date();

    private Date lastVisit = new Date();

    private boolean isEnable; //Compte actif ou non ?

    private EnumMotif motifEnable; //Si isEnable = true => motifEnable = VALIDATE, Sinon indiquer le motif

    private Date dateCloture; //Ajoute la date quand le compte passe en inactif

    private String role = "ROLE_USER";


}
