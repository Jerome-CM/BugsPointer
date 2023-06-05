package com.bugspointer.controller;

import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.Response;
import com.bugspointer.service.implementation.MailService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Home {


    private final MailService mailService;

    public Home(MailService mailService) {
        this.mailService = mailService;
    }


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

    // TODO : Juste pour l'exemple, à supprimer après, ne pas oublier le constructeur à clean
    @GetMapping(value="mail")
    String sendMailRegister(){
        Response response = mailService.sendMailRegister("bouteveillejerome@hotmail.fr", "HKVJHsgfir813dkfh");
        if(response.getStatus().equals(EnumStatus.OK)){
            return "public/testPage";
        }
        return "public/testPage";
    }
}
