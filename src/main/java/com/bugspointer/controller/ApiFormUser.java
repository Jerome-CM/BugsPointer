package com.bugspointer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("api/user")
public class ApiFormUser {

    @GetMapping("plan/{publicKey}")
    void getPlan(@RequestParam String publicKey){

    }

}
