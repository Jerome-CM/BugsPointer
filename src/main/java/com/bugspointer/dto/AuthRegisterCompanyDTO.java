package com.bugspointer.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
public class AuthRegisterCompanyDTO {

    @NotBlank(message = "Le nom de l'entreprise est obligatoire")
    private String companyName;

    @NotBlank(message = "L'e-mail est obligatoire")
    @Email(message = "L'e-mail n'est pas valide")
    private String mail;

    @NotBlank(message = "La confirmation de l'e-mail est obligatoire")
    @Email(message = "La confirmation de l'e-mail n'est pas valide")
    private String confirmMail;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;

    @NotBlank(message = "La confirmation du mot de passe est obligatoire")
    private String confirmPassword;
}
