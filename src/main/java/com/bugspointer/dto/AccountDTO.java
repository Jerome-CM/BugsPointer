package com.bugspointer.dto;

import com.bugspointer.entity.EnumIndicatif;
import lombok.Data;

@Data
public class AccountDTO {

    private Long id;

    private String mail;

    private String password;

    private String newPassword;

    private String confirmPassword;

    private EnumIndicatif indicatif;

    private String phoneNumber;

    private String publicKey;
}
