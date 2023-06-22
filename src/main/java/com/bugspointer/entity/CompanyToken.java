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

    private String publicKey;

    @Column(unique = true)
    private String tokenReset;

    private Date dateCreation;
}
