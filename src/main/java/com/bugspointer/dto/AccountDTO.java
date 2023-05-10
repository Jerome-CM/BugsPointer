package com.bugspointer.dto;

import lombok.Data;

@Data
public class AccountDTO {

    private Long id;

    private String mail;

    private String password;

    private String newPassword;

    private String confirmPassword;

    private String indicatif;

    private String phoneNumber;

    private String publicKey;
}
