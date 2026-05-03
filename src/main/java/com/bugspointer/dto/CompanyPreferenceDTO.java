package com.bugspointer.dto;

import lombok.Data;

@Data
public class CompanyPreferenceDTO {

    private Long id;

    private String companyPhoneNumber;

    private String companyPublicKey;

    private boolean mailNewBug;

    private boolean mailInactivity;

    private boolean mailNewFeature;

    private boolean smsNewBug;

    private boolean smsInactivity;

    private boolean smsNewFeature;

    private String widgetPrimaryColor;

    private String widgetModalBackgroundColor;

    private String widgetModalTextColor;

    private String widgetTitleColor;

    private String widgetLinkTextColor;

    private boolean widgetLinkUnderline;

    private String widgetButtonText;

    private String widgetButtonStyle;

    private Integer widgetButtonSize;

    private String widgetTitle;

    private String widgetDescriptionLabel;

    private String widgetPosition;

    private Integer widgetMarginX;

    private Integer widgetMarginY;
}
