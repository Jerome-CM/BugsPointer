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

    private String widgetPrimaryColor = "#27215F";

    private String widgetModalBackgroundColor = "#FFFFFF";

    private String widgetButtonText = "Signaler un bug";

    private String widgetButtonStyle = "button";

    private String widgetTitle = "Signaler un nouveau bug";

    private String widgetDescriptionLabel = "Description du bug";

    private String widgetPosition = "bottom-right";

    private Integer widgetMarginX = 15;

    private Integer widgetMarginY = 15;

}
