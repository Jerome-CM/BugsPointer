package com.bugspointer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class Home {

    @GetMapping("/")
    String getHome(){
        return "index";
    }

    @GetMapping("features")
    String getFeatures(){
        return "public/features";
    }

    @GetMapping("documentations")
    String getDocumentations(){
        return "public/documentations";
    }
}
