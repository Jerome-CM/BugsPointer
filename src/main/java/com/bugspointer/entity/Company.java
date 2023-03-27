package com.bugspointer.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Entity
@Data
public class Compagny {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom de l'entreprise est obligatoire")
    private String compagnyName;

    @NotBlank(message = "L'email doit être unique")
    private String mail;

    @NotBlank(message = "Le password est obligatoire")
    private String password;

    private EnumPlan plan = EnumPlan.FREE;

    private String publicKey;

    private String secretKey;

    private int inactiveActivityDay;

    @NotNull
    @Column(updatable = false)
    private Date creationDate = new Date();

    private Date lastVisit;

    private boolean isEnable = true;


}
