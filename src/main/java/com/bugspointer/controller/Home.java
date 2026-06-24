package com.bugspointer.controller;

import com.bugspointer.configuration.UserAuthenticationUtil;
import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.Response;
import com.bugspointer.entity.Company;
import com.bugspointer.entity.EnumPlan;
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

    private final PlanPricingService planPricingService;

    public Home(CompanyService companyService, MailService mailService, BugService bugService, UserAuthenticationUtil userAuthenticationUtil, JwtTokenUtil jwtTokenUtil, PollService pollService, ViewCounterService viewCounterService, PlanPricingService planPricingService) {
        this.companyService = companyService;
        this.mailService = mailService;
        this.bugService = bugService;
        this.userAuthenticationUtil = userAuthenticationUtil;
        this.jwtTokenUtil = jwtTokenUtil;
        this.pollService = pollService;
        this.viewCounterService = viewCounterService;
        this.planPricingService = planPricingService;
    }


    @GetMapping("/")
    String getHome(Model model, HttpServletRequest request){
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        model.addAttribute("nbrBugReported", 277 ); // bugService.getNbrBugReportedForIndex());
        model.addAttribute("companyCount", 32 );
        model.addAttribute("averageSatisfyingUser", "9,4");
        addTargetPlanPrice(model);
        viewCounterService.addVisit(EnumViewCounterPage.INDEX, request);
        return "index";
    }

    @GetMapping("download")
    String getDownloadPage(Model model, HttpServletRequest request){
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        if(userAuthenticationUtil.isUserLoggedIn()){
            model.addAttribute("publicKey", companyService.getCompanyWithToken(request).getPublicKey());
        } else {
            model.addAttribute("publicKey", "pk_xxxxx");
        }
        return "public/download";
    }

    @GetMapping("installation")
    String getInstallationPage(HttpServletRequest request){
        return redirectToWidgetInstallation(request);
    }

    private String redirectToWidgetInstallation(HttpServletRequest request) {
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
        addTargetPlanPrice(model);
        return "public/features";
    }

    @GetMapping("documentations")
    String getDocumentations(Model model, HttpServletRequest request){
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        if(userAuthenticationUtil.isUserLoggedIn()){
            Company company = companyService.getCompanyWithToken(request);
            model.addAttribute("publicKey", company.getPublicKey());
            model.addAttribute("planLabel", company.getPlan().name().charAt(0) + company.getPlan().name().substring(1).toLowerCase());
        } else {
            model.addAttribute("planLabel", "Free");
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
    String getTestPage(Model model, HttpServletRequest request){
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        viewCounterService.addVisit(EnumViewCounterPage.TESTPAGE, request);
        return "public/testPage";
    }

    @GetMapping("pollUser")
    String getPullUser(Model model, HttpServletRequest request){
        addPollAttributes(model, new Poll(), Poll.CONTEXT_PRODUCT, "/pollUser",
                "Votre avis sur BugsPointer",
                "Quelques réponses suffisent pour améliorer la modal de signalement et l'expérience utilisateur.",
                "Votre expérience",
                "Notez le signalement",
                "Le bouton ou le lien de signalement était-il facile à trouver ?",
                "La modal vous a-t-elle semblé claire pour décrire le problème ?",
                "Le rapport envoyé à l'équipe vous semble-t-il utile pour corriger plus vite ?",
                "Ce qui a aidé, manqué ou gêné pendant le signalement");
        viewCounterService.addVisit(EnumViewCounterPage.POLLUSER, request);
        return "public/poll";
    }

    @PostMapping("pollUser")
    String savePoll(Poll poll, Model model){
        poll.setPollContext(Poll.CONTEXT_PRODUCT);
        Response response = pollService.savePoll(poll);
        if(response.getStatus().equals(EnumStatus.OK)){
            model.addAttribute("title", "Merci beaucoup pour votre participation au sondage");
            return "private/thanks";
        }
        return "redirect:/pollUser";
    }

    @GetMapping("pollInstallation")
    String getPollInstallation(Model model){
        Poll poll = new Poll();
        poll.setPollContext(Poll.CONTEXT_INSTALLATION);
        addPollAttributes(model, poll, Poll.CONTEXT_INSTALLATION, "/pollInstallation",
                "Votre avis sur l'installation",
                "Trois réponses suffisent pour savoir si le parcours d'installation est clair.",
                "Installation du widget",
                "Notez le parcours",
                "L'étape de copie du script ou du lien était-elle simple à comprendre ?",
                "La vérification du domaine vous a-t-elle semblé claire ?",
                "Après l'installation, sauriez-vous tester un premier signalement ?",
                "Ce qui a bloqué, manqué ou aidé pendant l'installation");
        return "public/poll";
    }

    @PostMapping("pollInstallation")
    String saveInstallationPoll(Poll poll, Model model){
        poll.setPollContext(Poll.CONTEXT_INSTALLATION);
        Response response = pollService.savePoll(poll);
        if(response.getStatus().equals(EnumStatus.OK)){
            model.addAttribute("title", "Merci beaucoup pour votre participation au sondage");
            return "private/thanks";
        }
        return "redirect:/pollInstallation";
    }

    private void addTargetPlanPrice(Model model) {
        String targetPlanPrice = planPricingService.format(planPricingService.getNewSubscriptionAmount(EnumPlan.TARGET));
        String targetPlanPriceLabel = "0.00".equals(targetPlanPrice) ? "0€ jusqu'au 01/09/2026" : targetPlanPrice + "€ / an";
        model.addAttribute("targetPlanPrice", targetPlanPrice);
        model.addAttribute("targetPlanPriceLabel", targetPlanPriceLabel);
    }

    private void addPollAttributes(Model model, Poll poll, String pollContext, String pollAction, String pollTitle, String pollIntro, String pollEyebrow, String pollSectionTitle, String pollQuestionFindEasy, String pollQuestionStepClarity, String pollQuestionTargetFeature, String pollCommentPlaceholder) {
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        int[] ranks = new int[] { 0,1,2,3,4,5,6,7,8,9,10 };
        model.addAttribute("ranks", ranks);
        model.addAttribute("user", "yes");
        model.addAttribute("pollUser", poll);
        model.addAttribute("pollContext", pollContext);
        model.addAttribute("pollAction", pollAction);
        model.addAttribute("pollTitle", pollTitle);
        model.addAttribute("pollIntro", pollIntro);
        model.addAttribute("pollEyebrow", pollEyebrow);
        model.addAttribute("pollSectionTitle", pollSectionTitle);
        model.addAttribute("pollQuestionFindEasy", pollQuestionFindEasy);
        model.addAttribute("pollQuestionStepClarity", pollQuestionStepClarity);
        model.addAttribute("pollQuestionTargetFeature", pollQuestionTargetFeature);
        model.addAttribute("pollCommentPlaceholder", pollCommentPlaceholder);
    }

}
