package com.bugspointer.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
public class CompagnyPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @NotNull
    private Compagny compagny;

    private boolean mailNewBug;

    private boolean mailInactivity;

    private boolean mailNewFeature;

    private boolean smsNewBug;

    private boolean smsInactivity;

    private boolean smsNewFeature;

}
