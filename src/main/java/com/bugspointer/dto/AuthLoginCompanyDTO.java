package com.bugspointer.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class AuthLoginCompanyDTO {

    @NotBlank(message = "L'e-mail est obligatoire")
    @Email(message = "L'e-mail n'est pas valide")
    private String mail;

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;

}
