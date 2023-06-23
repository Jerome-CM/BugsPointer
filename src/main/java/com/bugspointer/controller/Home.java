package com.bugspointer.controller;

import com.bugspointer.configuration.UserAuthenticationUtil;
import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.Response;
import com.bugspointer.entity.Poll;
import com.bugspointer.service.implementation.MailService;
import com.bugspointer.service.implementation.PollService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@Slf4j
public class Home {

    private final MailService mailService;

    private final UserAuthenticationUtil userAuthenticationUtil;

    private final PollService pollService;

    public Home(MailService mailService, UserAuthenticationUtil userAuthenticationUtil, PollService pollService) {
        this.mailService = mailService;
        this.userAuthenticationUtil = userAuthenticationUtil;
        this.pollService = pollService;
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

    @GetMapping("pollUser")
    String getPullUser(Model model){
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        int[] ranks = new int[] { 0,1,2,3,4,5,6,7,8,9,10 };
        model.addAttribute("ranks", ranks);
        model.addAttribute("user", "yes");
        model.addAttribute("pollUser", new Poll());
        return "public/poll";
    }

    @PostMapping("pollUser")
    String savePoll(Poll poll, Model model){
        Response response = pollService.savePoll(poll);
        if(response.getStatus().equals(EnumStatus.OK)){
            model.addAttribute("title", "Merci beaucoup pour votre participation au sondage");
            return "private/thanks";
        }
        return "redirect:/pollUser";
    }

}
