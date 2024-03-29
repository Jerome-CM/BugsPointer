package com.bugspointer.dto;

import com.bugspointer.entity.Customer;
import com.bugspointer.entity.EnumIndicatif;
import com.bugspointer.entity.EnumPlan;
import lombok.Data;

@Data
public class AccountDTO {

    private Long id;

    private String customerId;

    private String mail;

    private String password;

    private String newPassword;

    private String confirmPassword;

    private EnumIndicatif indicatif;

    private String phoneNumber;

    private String publicKey;

    private EnumPlan plan;

    private String domaine;
}
