package com.bugspointer.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AuthLoginCompanyDTO {

    @NotBlank(message = "L'email doit être unique")
    private String mail = "community-technologie@gmail.com";

    @NotBlank(message = "Le password est obligatoire")
    private String password = "test";

    //private boolean rememberMe;
}
