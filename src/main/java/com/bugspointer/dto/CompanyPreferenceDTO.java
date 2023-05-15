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
}
