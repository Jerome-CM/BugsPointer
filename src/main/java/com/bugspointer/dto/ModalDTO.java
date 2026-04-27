package com.bugspointer.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class ModalDTO {

    @NotBlank(message = "L'URL est obligatoire")
    private String url;

    @NotBlank(message = "La description est obligatoire")
    @Size(min = 10, message = "La description doit contenir au moins 10 caractères")
    private String description;

    private String codeLocation;

    private String os;

    private String browser;

    private String adresseIp;

    private String screenSize;

    @NotBlank(message = "La clé publique est obligatoire")
    private String key;

    private String bot;

    private String mail;

}
