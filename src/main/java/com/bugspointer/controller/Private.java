package com.bugspointer.controller;

import com.bugspointer.dto.*;
import com.bugspointer.service.implementation.CompanyPreferencesService;
import com.bugspointer.service.implementation.CompanyService;
import com.bugspointer.service.implementation.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
@Slf4j
@RequestMapping("app/private/")
public class Private {

    private final CompanyService companyService;

    private final MailService mailService;

    private final CompanyPreferencesService preferencesService;

    public Private(CompanyService companyService, MailService mailService, CompanyPreferencesService preferencesService) {
        this.companyService = companyService;
        this.mailService = mailService;
        this.preferencesService = preferencesService;
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
    String updateNotifications(@Valid CompanyPreferenceDTO dto, BindingResult result, Model model, HttpServletRequest request){
        String action = request.getParameter("action");
        log.info("buttonName : {}", action);
        if (!result.hasErrors()) {
            Response response = preferencesService.updatePreference(dto, action);
            if (response.getStatus().equals(EnumStatus.OK)) {
                model.addAttribute("notification", response.getMessage());
                return "redirect:notifications";
            } else {
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
    String deleteAccount(@Valid AccountDeleteDTO dto, BindingResult result, Model model, HttpServletRequest request){
        if (!result.hasErrors()) {
            Response response = companyService.delete(dto);
            if (response.getStatus().equals(EnumStatus.OK)) {
                model.addAttribute("notification", response.getMessage());
                HttpSession session = request.getSession();
                session.invalidate();
                return "redirect:/authentication";
            } else {
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

    @PostMapping("account") //TODO: afficher les messages de succes ou d'echec sur la page
    String update(@Valid AccountDTO dto,
                  BindingResult result,
                  Model model,
                  HttpServletRequest request){
        String action = request.getParameter("action");
        log.info("buttonName : {}", action);
        if (!result.hasErrors()) {
            Response response;
            if ("updateMail".equals(action)){
                response = companyService.mailUpdate(dto);
                if (response.getStatus().equals(EnumStatus.OK)) {
                    model.addAttribute("notification", response.getMessage());
                    HttpSession session = request.getSession();
                    session.invalidate();
                    return "redirect:/authentication";
                } else {
                    model.addAttribute("notification", response.getMessage());
                }
            } else if ("updatePw".equals(action)){
                response = companyService.passwordUpdate(dto);
            } else if ("updateSms".equals(action)){
                response = companyService.smsUpdate(dto);
            } else {
                response = new Response(EnumStatus.ERROR, null, "Error to click of button");
            }

            if (response.getStatus().equals(EnumStatus.OK)) {
                model.addAttribute("notification", response.getMessage());
                model.addAttribute("company", companyService.getAccountDto(companyService.getCompanyWithToken(request)));
                return "private/account";
            } else {
                model.addAttribute("notification", response.getMessage());
            }
        }

        model.addAttribute("company", companyService.getAccountDto(companyService.getCompanyWithToken(request)));
        return "private/account";
    }

    @GetMapping("dashboard")
    String getDashboard(Model model, HttpServletRequest request){
        HttpSession session = request.getSession();
        log.info("session mail : {}", session.getAttribute("mail"));
        model.addAttribute("company", companyService.getDashboardDto(companyService.getCompanyWithToken(request)));
        return "private/dashboard";
    }

    @GetMapping("newBugList")
    String getNewBugList(){
        return "private/newBugList";
    }

    @GetMapping("newBugReport")
    String getNewBugReport(){
        return "private/newBugReport";
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

    // TODO : Juste pour l'exemple, à supprimer après, ne pas oublier le constructeur à clean
    @GetMapping(value="mail")
    String sendMailRegister(){
        Response response = mailService.sendMailRegister("monemaildetest@hotmail.fr", "HKVJHsgfir813dkfh");
        if(response.getStatus().equals(EnumStatus.OK)){
            return "index";
        }
        return "private/dashboard";
    }

}
