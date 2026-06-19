package com.bugspointer.controller;

import com.bugspointer.configuration.UserAuthenticationUtil;
import com.bugspointer.entity.EnumPlan;
import com.bugspointer.service.implementation.PlanPricingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SeoPagesController {

    private final UserAuthenticationUtil userAuthenticationUtil;

    private final PlanPricingService planPricingService;

    public SeoPagesController(UserAuthenticationUtil userAuthenticationUtil, PlanPricingService planPricingService) {
        this.userAuthenticationUtil = userAuthenticationUtil;
        this.planPricingService = planPricingService;
    }

    @GetMapping("outil-remontee-bugs")
    String getBugReportingLanding(Model model) {
        addCommonSeoAttributes(model);
        return "seo/bugReportingTool";
    }

    @GetMapping("agences-web")
    String getWebAgenciesLanding(Model model) {
        addCommonSeoAttributes(model);
        addTargetPlanPrice(model);
        return "seo/agencies";
    }

    @GetMapping("signalement-bug-site-web")
    String getWebsiteBugReportLanding(Model model) {
        addCommonSeoAttributes(model);
        return "seo/websiteBugReport";
    }

    @GetMapping("debuguer-site-web")
    String getDebugWebsiteLanding(Model model) {
        addCommonSeoAttributes(model);
        return "seo/debugWebsite";
    }

    @GetMapping("checklist-recette-site-web")
    String getWebsiteReleaseChecklist(Model model) {
        addCommonSeoAttributes(model);
        return "seo/releaseChecklist";
    }

    @GetMapping("modele-rapport-bug")
    String getBugReportTemplate(Model model) {
        addCommonSeoAttributes(model);
        return "seo/bugReportTemplate";
    }

    @GetMapping("scanner-site-avant-mise-en-production")
    String getPreProductionScanner(Model model) {
        addCommonSeoAttributes(model);
        return "seo/preProductionScanner";
    }

    private void addCommonSeoAttributes(Model model) {
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
    }

    private void addTargetPlanPrice(Model model) {
        String targetPlanPrice = planPricingService.format(planPricingService.getNewSubscriptionAmount(EnumPlan.TARGET));
        String targetPlanPriceLabel = "0.00".equals(targetPlanPrice) ? "0€ jusqu'au 01/09/2026" : targetPlanPrice + "€ / an";
        model.addAttribute("targetPlanPrice", targetPlanPrice);
        model.addAttribute("targetPlanPriceLabel", targetPlanPriceLabel);
    }
}
