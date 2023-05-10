package com.bugspointer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ApiCompany {

    @GetMapping("plan/{publicKey}")
    void getPlan(@RequestParam String publicKey){

    }

}
