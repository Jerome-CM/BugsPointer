package com.bugspointer.dto;

import com.bugspointer.entity.EnumPlan;
import lombok.Data;

@Data
public class CustomerDTO extends MetaDataMollieDTO{

    private String mail;

    private String companyName;

    private String address1;

    private String address2;

    private String cp;

    private String city;

    private String country;

    private EnumPlan plan;

    private boolean cguAccepted;

    private String publicKey;

    private String iban;

    private String bic;

    private boolean signature;

}
