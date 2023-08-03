package com.bugspointer.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
public class FirstReport {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private Long companyId;

    private String companyName;

    private String domaine;

    private Date dateConfirm;

    private boolean firstReport = false;

    private String firstDescription = null;

    private Date firstSend = null;

    private boolean secondReport = false;

    private String secondDescription = null;

    private Date secondSend = null;

}
