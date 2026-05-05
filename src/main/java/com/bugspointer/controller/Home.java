package com.bugspointer.controller;

import com.bugspointer.configuration.UserAuthenticationUtil;
import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.Response;
import com.bugspointer.entity.Company;
import com.bugspointer.entity.EnumViewCounterPage;
import com.bugspointer.entity.Poll;
import com.bugspointer.jwtConfig.JwtTokenUtil;
import com.bugspointer.service.implementation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
@Slf4j
public class Home {

    private final CompanyService companyService;

    private final MailService mailService;

    private final BugService bugService;

    private final UserAuthenticationUtil userAuthenticationUtil;

    private final JwtTokenUtil jwtTokenUtil;

    private final PollService pollService;

    private final ViewCounterService viewCounterService;


    public Home(CompanyService companyService, MailService mailService, BugService bugService, UserAuthenticationUtil userAuthenticationUtil, JwtTokenUtil jwtTokenUtil, PollService pollService, ViewCounterService viewCounterService) {
        this.companyService = companyService;
        this.mailService = mailService;
        this.bugService = bugService;
        this.userAuthenticationUtil = userAuthenticationUtil;
        this.jwtTokenUtil = jwtTokenUtil;
        this.pollService = pollService;
        this.viewCounterService = viewCounterService;
    }


    @GetMapping("/")
    String getHome(Model model){
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        model.addAttribute("nbrBugReported", 277 ); // bugService.getNbrBugReportedForIndex());
        model.addAttribute("companyCount", 32 );
        model.addAttribute("averageSatisfyingUser", "9,4");
        viewCounterService.addVisit(EnumViewCounterPage.INDEX);
        return "index";
    }

    @GetMapping("download")
    String getDownloadPage(Model model, HttpServletRequest request){

        if(userAuthenticationUtil.isUserLoggedIn()){
            return "redirect:/app/private/onboarding/widget";
        } else {
            request.getSession().setAttribute("redirectAfterLogin", "/app/private/onboarding/widget");
            return "redirect:authentication?status=ERROR&message=Merci de vous connecter";
        }

    }

    @GetMapping("features")
    String getFeatures(Model model){
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        return "public/features";
    }

    @GetMapping("documentations")
    String getDocumentations(Model model, HttpServletRequest request){
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        if(userAuthenticationUtil.isUserLoggedIn()){
            model.addAttribute("publicKey", companyService.getCompanyWithToken(request).getPublicKey());
        }
        return "public/documentations";
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
        viewCounterService.addVisit(EnumViewCounterPage.TESTPAGE);
        return "public/testPage";
    }

    @GetMapping("pollUser")
    String getPullUser(Model model){
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        int[] ranks = new int[] { 0,1,2,3,4,5,6,7,8,9,10 };
        model.addAttribute("ranks", ranks);
        model.addAttribute("user", "yes");
        model.addAttribute("pollUser", new Poll());
        viewCounterService.addVisit(EnumViewCounterPage.POLLUSER);
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
