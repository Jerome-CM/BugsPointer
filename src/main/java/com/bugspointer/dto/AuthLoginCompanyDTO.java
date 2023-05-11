package com.bugspointer.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AuthLoginCompanyDTO {

    private String mail = "comp1@comp.fr";

    private String password = "test";

}
