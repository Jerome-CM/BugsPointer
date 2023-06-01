package com.bugspointer.controller;

import com.bugspointer.dto.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import com.bugspointer.entity.Company;
import com.bugspointer.service.implementation.CompanyService;
import com.bugspointer.service.implementation.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.ui.Model;

@Controller
@Slf4j
public class Authentication {
    private final CompanyService companyService;

    private final MailService mailService;

    public Authentication(CompanyService companyService, MailService mailService) {
        this.companyService = companyService;
        this.mailService = mailService;
    }

    @GetMapping("/authentication")
    String getAuthenticationPage(Model model, AuthRegisterCompanyDTO dtoRegister, AuthLoginCompanyDTO dtoLogin, HttpServletRequest request){
        model.addAttribute("companyRegister", dtoRegister);
        model.addAttribute("companyLogin", dtoLogin);
        if (request.getParameter("status") != null) {
            model.addAttribute("status", request.getParameter("status"));
            model.addAttribute("notification", request.getParameter("message"));
        }
        log.info("status : {}", request.getParameter("status"));
        return "public/authentication";
    }

    @PostMapping("/register")
    String register(@Valid AuthRegisterCompanyDTO dto, BindingResult result, Model model, AuthRegisterCompanyDTO dtoRegister, AuthLoginCompanyDTO dtoLogin){

        if(!result.hasErrors()){
            Response response;
            response = companyService.saveCompany(dto);
            if(response.getStatus() == EnumStatus.OK){
                model.addAttribute("companyRegister", dtoRegister);
                model.addAttribute("companyLogin", dtoLogin);
                model.addAttribute("page", "register");
                //TODO: modifier l'adresse mail par dto.getMail()
                try {
                    response = mailService.sendMailRegister(dto.getMail(), companyService.getCompanyByMail(dto.getMail()).getPublicKey());
                }
                catch (Exception e){
                    log.error("Error : {}", e.getMessage());
                }
                model.addAttribute("notification", response.getMessage());
                model.addAttribute("status", String.valueOf(response.getStatus()));
                return "public/registerConfirm";
            } else {
                model.addAttribute("companyRegister", dtoRegister);
                model.addAttribute("companyLogin", dtoLogin);
                model.addAttribute("notification", response.getMessage());
                model.addAttribute("status", String.valueOf(response.getStatus()));
            }
        }
        return "public/authentication";
    }

    @GetMapping("/registerConfirm")
    String getRegisterConfirm(Model model, AuthRegisterCompanyDTO dtoRegister, AuthLoginCompanyDTO dtoLogin, HttpServletRequest request){
        model.addAttribute("companyRegister", dtoRegister);
        model.addAttribute("companyLogin", dtoLogin);
        return "public/registerConfirm";
    }

    @GetMapping("newUser/{publicKey}")//TODO: ajouter des variables dans l'url pour identifier la company et sécuriser
    String getNewUser(@PathVariable("publicKey") String publicKey,  Model model, HttpServletRequest request){
        Company company = companyService.getCompanyByPublicKey(publicKey);
        model.addAttribute("company", company);
        return "public/newUser";
    }

    @PostMapping("newUser/{publicKey}")
    String registerSite(@PathVariable("publicKey") String publicKey,
                        @Valid AccountDTO dto,
                        BindingResult result,
                        Model model){
        if (!result.hasErrors()) {
            String companyMail = dto.getMail();
            Response response = companyService.registerDomaine(dto);
            if (response.getStatus().equals(EnumStatus.OK)) {
                model.addAttribute("company", companyService.getAccountDto(companyService.getCompanyByPublicKey(publicKey)));
                model.addAttribute("status", String.valueOf(response.getStatus()));
                model.addAttribute("notification", response.getMessage());
                model.addAttribute("etat", "ok");
                model.addAttribute("mail", companyMail);
                return "public/newUser";
            } else {
                model.addAttribute("status", String.valueOf(response.getStatus()));
                model.addAttribute("notification", response.getMessage());
                model.addAttribute("etat", "error");
            }
        }
        model.addAttribute("publicKey", publicKey);
        model.addAttribute("company", companyService.getAccountDto(companyService.getCompanyByPublicKey(publicKey)));
        return "public/newUser";
    }


    @GetMapping("/logout")
    String logout(HttpServletRequest request){

        HttpSession session = request.getSession();
        session.invalidate();
        return "index";
    }
    
}
