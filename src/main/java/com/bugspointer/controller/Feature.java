package com.bugspointer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Feature {

    @GetMapping("/features")
    String getAuthenticationPage(){
        return "features";
    }
}
