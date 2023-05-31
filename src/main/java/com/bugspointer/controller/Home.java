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

    // TODO a delete
    @GetMapping("modal")
    String getModalForTest(){
        return "download/modal";
    }

    @GetMapping("cgu")
    String getCgu(){
        return "public/cgu";
    }

    @GetMapping("cgv")
    String getCgv(){
        return "public/cgv";
    }

    @GetMapping("mentions")
    String getMentions(){
        return "public/mentions";
    }

    @GetMapping("testPage")
    String getTestPage(){
        return "public/testPage";
    }
}
