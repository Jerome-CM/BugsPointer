package com.bugspointer.controller;

import com.bugspointer.dto.AccountDTO;
import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.Response;
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

    public Private(CompanyService companyService, MailService mailService) {
        this.companyService = companyService;
        this.mailService = mailService;
    }

    @GetMapping("invoices")
    String getInvoices(){
        return "private/invoices";
    }

    @GetMapping("notifications")
    String getNotifications(){
        return "private/notifications";
    }

    @GetMapping("account")
    String getAccount(Model model, HttpServletRequest request){
        HttpSession session = request.getSession();
        log.info("session mail : {}", session.getAttribute("mail"));
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
    String getDashboard(){
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
