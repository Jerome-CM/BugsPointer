package com.bugspointer.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AuthLoginCompanyDTO {

    private String mail = "community-technologie@gmail.com";

    private String password = "test";

}
