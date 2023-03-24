package com.bugspointer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Authentication {

    @GetMapping("/authentication")
    String getAuthenticationPage(){
        return "authentication";
    }

    @GetMapping("/private")
    String getAdminHome(){
        return "private/dashboard";
    }


}
