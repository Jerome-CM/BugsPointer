package com.bugspointer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Home {

    @GetMapping("/")
    String getHome(){
        return "index";
    }

    @GetMapping("download")
    String getDownloadPage(){
        return "public/download";
    }

    @GetMapping("features")
    String getFeatures(){
        return "public/features";
    }

    @GetMapping("documentations")
    String getDocumentations(){
        return "public/documentations";
    }

    @GetMapping("modal")
    String getModal(){
        return "download/modal";
    }

}
