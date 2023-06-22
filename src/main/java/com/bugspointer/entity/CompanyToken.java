package com.bugspointer.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
public class CompanyToken {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private String companyMail;

    @Column(unique = true)
    private String publicKey;

    private String tokenReset;

    private Date dateCreation;
}
