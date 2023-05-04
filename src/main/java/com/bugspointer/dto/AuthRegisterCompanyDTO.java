package com.bugspointer.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRegisterCompanyDTO {

    private String companyName;

    private String mail;

    private String confirmMail;

    private String password;

    private String confirmPassword;
}
