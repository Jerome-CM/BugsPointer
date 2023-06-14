package com.bugspointer.controller;

import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.Response;
import com.bugspointer.service.implementation.MailService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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
    String getDownloadPage(HttpServletRequest request){
        HttpSession session = request.getSession();
        String mail = (String) session.getAttribute("mail");

        if(mail != null){
            return "public/download";
        } else {
            return "redirect:authentication?status=ERROR&message=Please Login In or Register";
        }

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

}
