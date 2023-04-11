package com.bugspointer.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
public class CompanyPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @NotNull
    private Company company;

    private boolean mailNewBug;

    private boolean mailInactivity;

    private boolean mailNewFeature;

    private boolean smsNewBug;

    private boolean smsInactivity;

    private boolean smsNewFeature;

}
