package com.bugspointer.controller;

import com.bugspointer.dto.*;
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

@Controller
@Slf4j
@RequestMapping("app/private/")
public class Private {

    private final CompanyService companyService;

    private final BugService bugService;


    private final CompanyPreferencesService preferencesService;

    private final JwtTokenUtil jwtTokenUtil;

    @ModelAttribute("plans")
    public EnumPlan[] getPlans(){
        return EnumPlan.values();
    }

    public Private(CompanyService companyService, BugService bugService, CompanyPreferencesService preferencesService, JwtTokenUtil jwtTokenUtil) {
        this.companyService = companyService;
        this.bugService = bugService;
        this.preferencesService = preferencesService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @GetMapping("invoices")
    String getInvoices(){
        return "private/invoices";
    }

    @GetMapping("notifications")
    String getNotifications(Model model, HttpServletRequest request){
        HttpSession session = request.getSession();
        log.info("session mail : {}", session.getAttribute("mail"));
        model.addAttribute("company", preferencesService.getCompanyPreferenceDTO(companyService.getCompanyWithToken(request)));
        return "private/notifications";
    }

    @PostMapping("notifications")
    String updateNotifications(@Valid CompanyPreferenceDTO dto, BindingResult result, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request){
        String action = request.getParameter("action");
        log.info("buttonName : {}", action);
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
        log.info("session mail : {}", session.getAttribute("mail"));
        model.addAttribute("company", companyService.getAccountDeleteDto(companyService.getCompanyWithToken(request)));
        return "private/deleteAccount";
    }

    @PostMapping("account/delete/confirm")
    String deleteAccount(@Valid AccountDeleteDTO dto, BindingResult result, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request){
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
        log.info("session mail : {}", session.getAttribute("mail"));
        model.addAttribute("company", companyService.getAccountDto(companyService.getCompanyWithToken(request)));
        return "private/account";
    }

    @PostMapping("account/delete")
    String delete(@Valid AccountDTO dto,
                  BindingResult result,
                  Model model,
                  HttpServletRequest request) {
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
        /*HttpSession session = request.getSession();
        log.info("session mail : {}", session.getAttribute("mail"));*/
        model.addAttribute("company", companyService.getDashboardDto(companyService.getCompanyWithToken(request)));
        return "private/dashboard";
    }

    @GetMapping("newBugList")
    String getNewBugList(){
        return "private/newBugList";
    }

    @GetMapping("bugReport/{id}")
    String getNewBugReport(Model map, @PathVariable Long id){ // Long id
        map.addAttribute("title", "New Bug Report");

        map.addAttribute("code", bugService.codeBlockFormatter(id));
        return "private/bugReport";
    }

    @GetMapping("pendingBugList")
    String getPendingBugList(){
        return "private/pendingBugList";
    }

    @GetMapping("pendingBugReport")
    String getPendingBugReport(){
        return "private/pendingBugReport";
    }

    @GetMapping("solvedBugList")
    String getSolvedBugReport(){
        return "private/solvedBugList";
    }

    @GetMapping(value="thanks")
    String thanks(Model model, HttpServletRequest request){

        HttpSession session = request.getSession();
        Company company = companyService.getCompanyByMail(jwtTokenUtil.getUsernameFromToken(jwtTokenUtil.getTokenWithoutBearer((String) session.getAttribute("token"))));

        model.addAttribute("plan", String.valueOf(company.getPlan()));
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
        return "private/recapPayment";
    }

    @GetMapping(value = "BankAccount")
    String getBankAccount(@ModelAttribute("customer") CustomerDTO customerDTO, Model model, HttpServletRequest request){

        HttpSession session = request.getSession();
        Company company = companyService.getCompanyByMail(jwtTokenUtil.getUsernameFromToken(jwtTokenUtil.getTokenWithoutBearer((String) session.getAttribute("token"))));

        customerDTO.setPublicKey(company.getPublicKey());

        log.info("customer : {}", customerDTO);

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
        return "private/confirmSubscription";
    }

}
