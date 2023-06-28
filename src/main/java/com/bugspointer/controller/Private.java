package com.bugspointer.controller;

import com.bugspointer.configuration.UserAuthenticationUtil;
import com.bugspointer.dto.*;
import com.bugspointer.entity.Bug;
import com.bugspointer.entity.Company;
import com.bugspointer.entity.EnumPlan;
import com.bugspointer.jwtConfig.JwtTokenUtil;
import com.bugspointer.service.implementation.BugService;
import com.bugspointer.service.implementation.CompanyPreferencesService;
import com.bugspointer.service.implementation.CompanyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Date;
import java.util.List;

@Controller
@Slf4j
@RequestMapping("app/private/")
public class Private {

    private final CompanyService companyService;

    private final BugService bugService;

    private final CompanyPreferencesService preferencesService;

    private final JwtTokenUtil jwtTokenUtil;

    private final UserAuthenticationUtil userAuthenticationUtil;

    @ModelAttribute("plans")
    public EnumPlan[] getPlans(){
        return EnumPlan.values();
    }

    public Private(CompanyService companyService, BugService bugService, CompanyPreferencesService preferencesService, JwtTokenUtil jwtTokenUtil, UserAuthenticationUtil userAuthenticationUtil) {
        this.companyService = companyService;
        this.bugService = bugService;
        this.preferencesService = preferencesService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userAuthenticationUtil = userAuthenticationUtil;
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
    String updateNotifications(@Valid CompanyPreferenceDTO dto, BindingResult result, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request){
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
        }
        model.addAttribute("company", companyService.getAccountDto(companyService.getCompanyWithToken(request)));
        return "redirect:account";
    }

    @GetMapping("account/delete")
    String getDelete(Model model, HttpServletRequest request){
        HttpSession session = request.getSession();
        model.addAttribute("company", companyService.getAccountDeleteDto(companyService.getCompanyWithToken(request)));
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        return "private/deleteAccount";
    }

    @PostMapping("account/delete/confirm")
    String deleteAccount(@Valid AccountDeleteDTO dto, BindingResult result, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request){
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
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
        }
        model.addAttribute("company", companyService.getAccountDto(companyService.getCompanyWithToken(request)));
        return "private/account";
    }

    @GetMapping("account")
    String getAccount(Model model, HttpServletRequest request){
        HttpSession session = request.getSession();
        model.addAttribute("company", companyService.getAccountDto(companyService.getCompanyWithToken(request)));
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        return "private/account";
    }

    @PostMapping("account/delete")
    String delete(@Valid AccountDTO dto,
                  BindingResult result,
                  Model model,
                  HttpServletRequest request) {
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        if (!result.hasErrors()) {
            model.addAttribute("company", companyService.getAccountDeleteDto(companyService.getCompanyWithToken(request)));
            return "private/deleteAccount";
        }
        model.addAttribute("company", companyService.getAccountDto(companyService.getCompanyWithToken(request)));
        return "private/account";
    }

    @PostMapping("account")
    String update(@Valid AccountDTO dto,
                  BindingResult result,
                  Model model,
                  RedirectAttributes redirectAttributes,
                  HttpServletRequest request){
        String action = request.getParameter("action");
        log.info("buttonName : {}", action);
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        if (!result.hasErrors()) {
            Response response;
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
                response = companyService.updateDomaine(dto);
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
        return "private/account";
    }

    @GetMapping("dashboard")
    String getDashboard(Model model, HttpServletRequest request){
        model.addAttribute("company", companyService.getDashboardDto(companyService.getCompanyWithToken(request)));
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
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

    @GetMapping("confirmBug/{id}")
    String getConfirmeBug(@PathVariable Long id, HttpServletRequest request){
        DashboardDTO company = companyService.getDashboardDto(companyService.getCompanyWithToken(request));
        Long idCompany = company.getId();
        String plan = company.getPlan().name();

        bugService.bugPending(id, idCompany, plan);//Si le bug a l'état pending alors on passe à la méthode solved
        return "redirect:/app/private/bugReport/{id}";
    }

    @GetMapping("ignoredBug/{id}")
    String getIgnoredBug(@PathVariable Long id, HttpServletRequest request){
        DashboardDTO company = companyService.getDashboardDto(companyService.getCompanyWithToken(request));
        Long idCompany = company.getId();
        String plan = company.getPlan().name();

        bugService.bugIgnored(id, idCompany, plan);
        return "redirect:/app/private/bugReport/{id}";
    }

    @GetMapping("bugList")
    String getbugList(Model map,@RequestParam("publicKey") String publicKey, @RequestParam("state") String state, HttpServletRequest request){
        map.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        map.addAttribute("dataBug", companyService.getDashboardDto(companyService.getCompanyWithToken(request)));
        Response responseBugList = bugService.getBugDTOByCompanyAndState(publicKey, state);
        List<BugDTO> bugDTOList = (List<BugDTO>) responseBugList.getContent();

        if(responseBugList.getContent() == null){
            return "redirect:dashboard";
        }
        BugDTO bugDTO = bugDTOList.get(0);
        if(bugDTOList.size() == 1){
            map.addAttribute("title", bugService.getTitle(state, false));
            map.addAttribute("state", bugDTO.getEtatBug());
            map.addAttribute("publicKey", publicKey);
            return "redirect:bugReport/"+bugDTO.getId()+"?publicKey="+publicKey;
        } else {
            map.addAttribute("title", bugService.getTitle(state, true));
            map.addAttribute("bugList", bugDTOList);
            map.addAttribute("state", bugDTO.getEtatBug());
            map.addAttribute("publicKey", publicKey);

            return "private/bugList";
        }

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
    String getRecapPayment(@RequestParam("product") EnumPlan selectedProduct, Model model, HttpServletRequest request){

        HttpSession session = request.getSession();
        Company company = companyService.getCompanyByMail(jwtTokenUtil.getUsernameFromToken(jwtTokenUtil.getTokenWithoutBearer((String) session.getAttribute("token"))));

        CustomerDTO customer = new CustomerDTO();
        customer.setMail(company.getMail());
        customer.setCompanyName(company.getCompanyName());
        customer.setPublicKey(company.getPublicKey());
        model.addAttribute("product", selectedProduct);
        model.addAttribute("selectedProduct", selectedProduct);
        log.info("product GetRecap : {}", selectedProduct);
        model.addAttribute("customer", customer);
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        return "private/recapPayment";
    }

    @GetMapping(value = "BankAccount")
    String getBankAccount(@ModelAttribute("customer") CustomerDTO customerDTO, Model model, HttpServletRequest request){

        HttpSession session = request.getSession();
        Company company = companyService.getCompanyByMail(jwtTokenUtil.getUsernameFromToken(jwtTokenUtil.getTokenWithoutBearer((String) session.getAttribute("token"))));

        customerDTO.setPublicKey(company.getPublicKey());

        log.info("customer : {}", customerDTO);
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        model.addAttribute("customer", customerDTO);
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
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        return "private/confirmSubscription";
    }

}
