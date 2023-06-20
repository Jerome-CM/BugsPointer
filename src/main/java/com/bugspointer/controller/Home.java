package com.bugspointer.controller;

import com.bugspointer.configuration.UserAuthenticationUtil;
import com.bugspointer.service.implementation.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@Slf4j
public class Home {

    private final MailService mailService;

    private final UserAuthenticationUtil userAuthenticationUtil;

    public Home(MailService mailService, UserAuthenticationUtil userAuthenticationUtil) {
        this.mailService = mailService;
        this.userAuthenticationUtil = userAuthenticationUtil;
    }


    @GetMapping("/")
    String getHome(Model model){
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        return "index";
    }

    @GetMapping("download")
    String getDownloadPage(Model model){

        if(userAuthenticationUtil.isUserLoggedIn()){
            model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
            return "public/download";
        } else {
            return "redirect:authentication?status=ERROR&message=Merci de vous connecter";
        }

    }

    @GetMapping("features")
    String getFeatures(Model model){
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        return "public/features";
    }

    @GetMapping("documentations")
    String getDocumentations(Model model){
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        return "public/documentations";
    }

    @GetMapping("modal")
    String getModalForTest(Model model){
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        return "download/modal";
    }

    @GetMapping("cgu")
    String getCgu(Model model){
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        return "public/cgu";
    }

    @GetMapping("cgv")
    String getCgv(Model model){
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        return "public/cgv";
    }

    @GetMapping("mentions")
    String getMentions(Model model){
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        return "public/mentions";
    }

    @GetMapping("testPage")
    String getTestPage(Model model){
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        return "public/testPage";
    }

}
