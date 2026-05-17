package com.bugspointer.controller;

import be.woutschoovaerts.mollie.exception.MollieException;
import com.bugspointer.configuration.UserAuthenticationUtil;
import com.bugspointer.dto.*;
import com.bugspointer.entity.Bug;
import com.bugspointer.entity.Company;
import com.bugspointer.entity.EnumPlan;
import com.bugspointer.jwtConfig.JwtTokenUtil;
import com.bugspointer.service.implementation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Controller
@Slf4j
@RequestMapping("app/private/")
public class Private {

    private final CompanyService companyService;

    private final BugService bugService;

    private final CompanyPreferencesService preferencesService;

    private final CustomerService customerService;

    private final JwtTokenUtil jwtTokenUtil;

    private final UserAuthenticationUtil userAuthenticationUtil;

    private final PaymentService paymentService;

    private final PlanPricingService planPricingService;

    @ModelAttribute("plans")
    public EnumPlan[] getFilteredPlans(){
        return Arrays.stream(EnumPlan.values())
                .filter(plan -> plan != EnumPlan.FREE)
                .toArray(EnumPlan[]::new);
    }


    public Private(CompanyService companyService, BugService bugService, CompanyPreferencesService preferencesService, CustomerService customerService, JwtTokenUtil jwtTokenUtil, UserAuthenticationUtil userAuthenticationUtil, PaymentService paymentService, PlanPricingService planPricingService) {
        this.companyService = companyService;
        this.bugService = bugService;
        this.preferencesService = preferencesService;
        this.customerService = customerService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userAuthenticationUtil = userAuthenticationUtil;
        this.paymentService = paymentService;
        this.planPricingService = planPricingService;
    }

    @GetMapping("invoices")
    String getInvoices(Model model){
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        return "private/invoices";
    }

    @GetMapping("notifications")
    String getNotifications(Model model, HttpServletRequest request){
        HttpSession session = request.getSession();
        model.addAttribute("company", preferencesService.getCompanyPreferenceDTO(companyService.getCompanyWithToken(request)));
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        return "private/notifications";
    }

    @PostMapping("notifications")
    String updateNotifications(@Valid @ModelAttribute("company") CompanyPreferenceDTO dto, BindingResult result, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request){
        String action = request.getParameter("action");
        log.info("buttonName : {}", action);
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        if (!result.hasErrors()) {
            Response response = preferencesService.updatePreference(dto, action);
            if (response.getStatus().equals(EnumStatus.OK)) {
                redirectAttributes.addFlashAttribute("notification", response.getMessage());
                redirectAttributes.addFlashAttribute("status", String.valueOf(response.getStatus()));
                return "redirect:notifications";
            } else {
                model.addAttribute("status", String.valueOf(response.getStatus()));
                model.addAttribute("notification", response.getMessage());
            }
        } else {
            model.addAttribute("status", "ERROR");
            model.addAttribute("notification", "Merci de corriger les champs indiqués.");
        }
        return "private/notifications";
    }

    @GetMapping("account/delete")
    String getDelete(Model model, HttpServletRequest request){
        HttpSession session = request.getSession();
        model.addAttribute("company", companyService.getAccountDeleteDto(companyService.getCompanyWithToken(request)));
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        return "private/deleteAccount";
    }

    @PostMapping("account/delete/confirm")
    String deleteAccount(@Validated(AccountDTO.Delete.class) @ModelAttribute("company") AccountDeleteDTO dto, BindingResult result, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request){
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        Company currentCompany = companyService.getCompanyWithToken(request);
        dto.setPublicKey(currentCompany.getPublicKey());
        if (!result.hasErrors()) {
            Response response = companyService.delete(dto);
            if (response.getStatus().equals(EnumStatus.OK)) {
                model.addAttribute("notification", response.getMessage());
                model.addAttribute("status", String.valueOf(response.getStatus()));
                model.addAttribute("page", "delete");
                HttpSession session = request.getSession();
                session.invalidate();
                return "public/registerConfirm";
            } else {
                model.addAttribute("status", String.valueOf(response.getStatus()));
                model.addAttribute("notification", response.getMessage());
            }
        } else {
            model.addAttribute("status", "ERROR");
            model.addAttribute("notification", "Merci de saisir votre mot de passe.");
        }
        AccountDeleteDTO current = companyService.getAccountDeleteDto(companyService.getCompanyWithToken(request));
        dto.setMail(current.getMail());
        dto.setPublicKey(current.getPublicKey());
        dto.setNbNewBug(current.getNbNewBug());
        dto.setNbPendingBug(current.getNbPendingBug());
        dto.setDateLineFacturePlan(current.getDateLineFacturePlan());
        return "private/deleteAccount";
    }

    @GetMapping("account")
    String getAccount(Model model, HttpServletRequest request) throws MollieException {
        HttpSession session = request.getSession();
        Company company = companyService.getCompanyWithToken(request);
        model.addAttribute("company", companyService.getAccountDto(company));
        model.addAttribute("mandates", customerService.getMandateList(company));
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        return "private/account";
    }

    @GetMapping("widget")
    String getWidget(Model model, HttpServletRequest request) {
        Company company = companyService.getCompanyWithToken(request);
        model.addAttribute("company", companyService.getDashboardDto(company));
        model.addAttribute("widget", preferencesService.getCompanyPreferenceDTO(company));
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        return "private/widget";
    }

    @GetMapping("onboarding/widget")
    String getWidgetOnboarding(Model model, HttpServletRequest request) {
        Company company = companyService.getCompanyWithToken(request);
        boolean wasVerified = company.isDomainVerified();
        if (company.getDomainVerificationUrl() != null && !company.getDomainVerificationUrl().trim().isEmpty()) {
            Response verification = companyService.verifyDomainInstallation(company);
            if (wasVerified && verification.getStatus().equals(EnumStatus.ERROR)) {
                model.addAttribute("status", String.valueOf(verification.getStatus()));
                model.addAttribute("notification", verification.getMessage());
            }
            company = companyService.getCompanyWithToken(request);
        }
        model.addAttribute("company", companyService.getAccountDto(company));
        model.addAttribute("widget", preferencesService.getCompanyPreferenceDTO(company));
        model.addAttribute("publicKey", company.getPublicKey());
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        return "private/widgetOnboarding";
    }

    @PostMapping("onboarding/widget/domain")
    String updateOnboardingDomain(@Validated(AccountDTO.Domain.class) @ModelAttribute("company") AccountDTO dto,
                                  BindingResult result,
                                  Model model,
                                  RedirectAttributes redirectAttributes,
                                  HttpServletRequest request) {
        Company company = companyService.getCompanyWithToken(request);
        dto.setPublicKey(company.getPublicKey());

        if (!result.hasErrors()) {
            Response response = companyService.registerDomaine(company, dto);
            redirectAttributes.addFlashAttribute("status", String.valueOf(response.getStatus()));
            redirectAttributes.addFlashAttribute("notification", response.getMessage());
            return "redirect:/app/private/onboarding/widget";
        }

        model.addAttribute("status", "ERROR");
        model.addAttribute("notification", "Merci de saisir un domaine valide.");
        model.addAttribute("company", companyService.getAccountDto(company));
        model.addAttribute("widget", preferencesService.getCompanyPreferenceDTO(company));
        model.addAttribute("publicKey", company.getPublicKey());
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        return "private/widgetOnboarding";
    }

    @PostMapping("onboarding/widget/verify")
    String verifyOnboardingDomain(@RequestParam("verificationUrl") String verificationUrl,
                                  RedirectAttributes redirectAttributes,
                                  HttpServletRequest request) {
        Company company = companyService.getCompanyWithToken(request);
        Response response = companyService.verifyDomainInstallation(company, verificationUrl);
        redirectAttributes.addFlashAttribute("status", String.valueOf(response.getStatus()));
        redirectAttributes.addFlashAttribute("notification", response.getMessage());
        if (response.getStatus().equals(EnumStatus.OK)) {
            return "redirect:/app/private/dashboard";
        }
        return "redirect:/app/private/onboarding/widget";
    }

    @PostMapping("widget")
    String updateWidget(@Valid @ModelAttribute("widget") CompanyPreferenceDTO dto,
                        BindingResult result,
                        Model model,
                        RedirectAttributes redirectAttributes,
                        HttpServletRequest request) {
        Company currentCompany = companyService.getCompanyWithToken(request);
        dto.setCompanyPublicKey(currentCompany.getPublicKey());
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        model.addAttribute("company", companyService.getDashboardDto(currentCompany));

        if (!result.hasErrors()) {
            String action = currentCompany.getPlan().equals(EnumPlan.FREE) ? "updateWidgetFree" : "updateWidget";
            Response response = preferencesService.updatePreference(dto, action);
            if (response.getStatus().equals(EnumStatus.OK)) {
                redirectAttributes.addFlashAttribute("notification", response.getMessage());
                redirectAttributes.addFlashAttribute("status", String.valueOf(response.getStatus()));
                return "redirect:/app/private/widget";
            }
            model.addAttribute("status", String.valueOf(response.getStatus()));
            model.addAttribute("notification", response.getMessage());
        } else {
            model.addAttribute("status", "ERROR");
            model.addAttribute("notification", "Merci de corriger les champs indiqués.");
        }

        model.addAttribute("widget", preferencesService.getCompanyPreferenceDTO(currentCompany));
        return "private/widget";
    }

    @PostMapping("account/delete")
    String delete(@Valid @ModelAttribute("company") AccountDTO dto,
                  BindingResult result,
                  Model model,
                  HttpServletRequest request) {
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        dto.setPublicKey(companyService.getCompanyWithToken(request).getPublicKey());
        if (!result.hasErrors()) {
            model.addAttribute("company", companyService.getAccountDeleteDto(companyService.getCompanyWithToken(request)));
            return "private/deleteAccount";
        }
        model.addAttribute("company", companyService.getAccountDto(companyService.getCompanyWithToken(request)));
        return "private/account";
    }

    @PostMapping("account")
    String update(@Valid @ModelAttribute("company") AccountDTO dto,
                  BindingResult result,
                  Model model,
                  RedirectAttributes redirectAttributes,
                  HttpServletRequest request) throws MollieException {
        String action = request.getParameter("action");
        log.info("buttonName : {}", action);
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        if (!result.hasErrors()) {
            Response response;
            Company currentCompany = companyService.getCompanyWithToken(request);
            dto.setPublicKey(currentCompany.getPublicKey());
            if ("updateMail".equals(action)){
                response = companyService.mailUpdate(dto);
                if (response.getStatus().equals(EnumStatus.OK)) {
                    redirectAttributes.addFlashAttribute("notification", response.getMessage());
                    redirectAttributes.addFlashAttribute("status", String.valueOf(response.getStatus()));
                    HttpSession session = request.getSession();
                    session.invalidate();
                    return "redirect:/authentication";
                } else {
                    model.addAttribute("status", String.valueOf(response.getStatus()));
                    model.addAttribute("notification", response.getMessage());
                }
            } else if ("updatePw".equals(action)){
                response = companyService.passwordUpdate(dto);
            } else if ("updateSms".equals(action)) {
                response = companyService.smsUpdate(dto);
            } else if ("updateDomaine".equals(action)) {
                response = companyService.updateDomaine(currentCompany, dto);
            } else {
                response = new Response(EnumStatus.ERROR, null, "Error to click of button");
            }

            if (response.getStatus().equals(EnumStatus.OK)) {
                model.addAttribute("status", String.valueOf(response.getStatus()));
                model.addAttribute("notification", response.getMessage());
                model.addAttribute("company", companyService.getAccountDto(companyService.getCompanyWithToken(request)));
                return "private/account";
            } else {
                model.addAttribute("status", String.valueOf(response.getStatus()));
                model.addAttribute("notification", response.getMessage());
            }
        }

        model.addAttribute("company", companyService.getAccountDto(companyService.getCompanyWithToken(request)));
        model.addAttribute("mandates", customerService.getMandateList(companyService.getCompanyWithToken(request)));
        return "private/account";
    }

    @GetMapping("dashboard")
    String getDashboard(Model model, HttpServletRequest request,
                        @RequestParam(value = "status", required = false) String status,
                        @RequestParam(value = "message", required = false) String message){
        Company company = companyService.getCompanyWithToken(request);
        model.addAttribute("company", companyService.getDashboardDto(company));
        addTargetPlanPrice(model);
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        if (status != null && message != null) {
            model.addAttribute("status", status);
            model.addAttribute("notification", message);
        }
        return "private/dashboard";
    }

    @GetMapping("bugReport/{id}")
    String getNewBugReport(Model map, @PathVariable Long id, HttpServletRequest request, RedirectAttributes redirectAttributes){
        DashboardDTO company = companyService.getDashboardDto(companyService.getCompanyWithToken(request));
        Long idCompany = company.getId();
        EnumPlan plan = company.getPlan();
        String publicKey = company.getPublicKey();
        map.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        map.addAttribute("dataBug", company);

        Response response = bugService.viewBug(id, idCompany, plan);
        if (response.getStatus()==EnumStatus.OK){
            Bug bug = (Bug) response.getContent();
            String title = bugService.getTitle(bug.getEtatBug().name().toLowerCase(), false);
            //String title = bug.getEtatBug().name().substring(0, 1).toUpperCase() + bug.getEtatBug().name().substring(1).toLowerCase() + " Bug Report";
            map.addAttribute("title", title);
            map.addAttribute("bug", bug);
            map.addAttribute("code", bugService.codeBlockFormatter(bug.getCodeLocation()));
            map.addAttribute("publicKey", publicKey);
            return "private/bugReport";
        }
        else {
            redirectAttributes.addFlashAttribute("status", String.valueOf(response.getStatus()));
            redirectAttributes.addFlashAttribute("notification", response.getMessage());
            log.info("{}", response.getMessage());
            return "redirect:/app/private/dashboard";
        }
    }

    @PostMapping("confirmBug/{id}")
    String getConfirmeBug(@PathVariable Long id, HttpServletRequest request){
        DashboardDTO company = companyService.getDashboardDto(companyService.getCompanyWithToken(request));
        Long idCompany = company.getId();
        String plan = company.getPlan().name();

        bugService.bugPending(id, idCompany, plan);//Si le bug a l'état pending alors on passe à la méthode solved
        return "redirect:/app/private/bugReport/{id}";
    }

    @PostMapping("ignoredBug/{id}")
    String getIgnoredBug(@PathVariable Long id, HttpServletRequest request){
        DashboardDTO company = companyService.getDashboardDto(companyService.getCompanyWithToken(request));
        Long idCompany = company.getId();
        String plan = company.getPlan().name();

        bugService.bugIgnored(id, idCompany, plan);
        return "redirect:/app/private/bugReport/{id}";
    }

    @GetMapping("bugList")
    String getBugList(Model model,
                      @RequestParam(value = "publicKey", required = false) String publicKey,
                      @RequestParam("state") String state,
                      HttpServletRequest request,
                      RedirectAttributes redirectAttributes) {
        DashboardDTO company = companyService.getDashboardDto(companyService.getCompanyWithToken(request));
        if (publicKey == null || publicKey.trim().isEmpty()) {
            publicKey = company.getPublicKey();
        }
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        model.addAttribute("dataBug", company);
        model.addAttribute("company", company);

        if (company.getPublicKey() == null || !company.getPublicKey().equals(publicKey)) {
            redirectAttributes.addFlashAttribute("status", String.valueOf(EnumStatus.ERROR));
            redirectAttributes.addFlashAttribute("notification", "Vous ne pouvez consulter que les rapports de votre société.");
            return "redirect:/app/private/dashboard";
        }

        Response responseBugList = bugService.getBugDTOByCompanyAndState(publicKey, state);
        if (responseBugList.getStatus() != EnumStatus.OK) {
            redirectAttributes.addFlashAttribute("status", String.valueOf(responseBugList.getStatus()));
            redirectAttributes.addFlashAttribute("notification", responseBugList.getMessage());
            return "redirect:/app/private/dashboard";
        }

        model.addAttribute("title", bugService.getTitle(state, true));
        model.addAttribute("bugList", responseBugList.getContent());
        model.addAttribute("publicKey", publicKey);
        model.addAttribute("state", state);
        model.addAttribute("emptyMessage", responseBugList.getMessage());
        return "private/bugList";
    }

    @GetMapping(value="thanks")
    String thanks(Model model, HttpServletRequest request){

        HttpSession session = request.getSession();
        Company company = companyService.getCompanyByMail(jwtTokenUtil.getUsernameFromToken(jwtTokenUtil.getTokenWithoutBearer((String) session.getAttribute("token"))));
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        model.addAttribute("plan", String.valueOf(company.getPlan()));
        model.addAttribute("title", "Merci beaucoup pour votre abonnement à Bugspointer");

        return "private/thanks";
    }

    @GetMapping(value="recapPayment")
    String getRecapPayment(@RequestParam("product") EnumPlan selectedProduct, Model model, HttpServletRequest request) throws MollieException {

        HttpSession session = request.getSession();
        Company company = companyService.getCompanyByMail(jwtTokenUtil.getUsernameFromToken(jwtTokenUtil.getTokenWithoutBearer((String) session.getAttribute("token"))));

        CustomerDTO customer = new CustomerDTO();
        customer.setMail(company.getMail());
        customer.setCompanyName(company.getCompanyName());
        customer.setPublicKey(company.getPublicKey());
        customer.setPlan(selectedProduct);
        if(company.getCustomer() != null){
            customer = customerService.getMetadata(customer, company.getCustomer().getCustomerId());
        }

        model.addAttribute("product", selectedProduct);
        model.addAttribute("productPrice", planPricingService.format(planPricingService.getNewSubscriptionAmount(selectedProduct)));
        /*model.addAttribute("selectedProduct", selectedProduct);*/
        model.addAttribute("customer", customer);
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        return "private/recapPayment";
    }

    @GetMapping(value = "BankAccount")
    String getBankAccount(@ModelAttribute("customer") CustomerDTO customerDTO, Model model, HttpServletRequest request) throws MollieException {

        HttpSession session = request.getSession();
        Company company = companyService.getCompanyByMail(jwtTokenUtil.getUsernameFromToken(jwtTokenUtil.getTokenWithoutBearer((String) session.getAttribute("token"))));

        customerDTO.setMail(company.getMail());
        customerDTO.setCompanyName(company.getCompanyName()); // A DELETE ? up and here
        customerDTO.setPublicKey(company.getPublicKey());
        customerDTO = customerService.getBankAccount(customerDTO, company.getCustomer().getCustomerId());
        log.info("customer : {}", customerDTO);
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        model.addAttribute("customer", customerDTO);
        model.addAttribute("productPrice", planPricingService.format(planPricingService.getNewSubscriptionAmount(customerDTO.getPlan())));
        return "private/bankAccount";
    }

    @GetMapping("confirmSubscription")
    String getSubscription(@ModelAttribute("subscription")SubscriptionDTO subscriptionDTO, Model model){

        String produit;
        if (subscriptionDTO.getDescription().contains("TARGET")){
            produit = "TARGET";
        } else if (subscriptionDTO.getDescription().contains("ULTIMATE")){
            produit = "ULTIMATE";
        } else {
            produit = subscriptionDTO.getDescription();
        }

        Date nextPaymentDate = java.sql.Date.valueOf(subscriptionDTO.getNextPaymentDate());

        model.addAttribute("produit", produit);
        model.addAttribute("nextPaymentDate", nextPaymentDate);
        model.addAttribute("newPlanPrice", planPricingService.format(planPricingService.getNewSubscriptionAmount(subscriptionDTO.getNewPlan())));
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        return "private/confirmSubscription";
    }

    private void addTargetPlanPrice(Model model) {
        String targetPlanPrice = planPricingService.format(planPricingService.getNewSubscriptionAmount(EnumPlan.TARGET));
        String targetPlanPriceLabel = "0.00".equals(targetPlanPrice) ? "0€ jusqu'au 1er juillet" : targetPlanPrice + "€ / an";
        model.addAttribute("targetPlanPrice", targetPlanPrice);
        model.addAttribute("targetPlanPriceLabel", targetPlanPriceLabel);
    }

}
