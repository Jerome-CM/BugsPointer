package com.bugspointer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterCompanyDTO {

    @NotBlank(message = "Le nom de l'entreprise est obligatoire")
    private String compagnyName;

    @NotBlank(message = "L'email doit être unique")
    private String mail;

    @NotBlank(message = "La confirmation est obligatoire")
    private String confirmMail;

    @NotBlank(message = "Le password est obligatoire")
    private String password;

    @NotBlank(message = "La confirmation est obligatoire")
    private String confirmPassword;

    private boolean rememberMe;
}
