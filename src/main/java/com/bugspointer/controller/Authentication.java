package com.bugspointer.controller;

import com.bugspointer.dto.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import com.bugspointer.entity.Company;
import com.bugspointer.service.implementation.CompanyService;
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

    public Authentication(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping("/authentication")
    String getAuthenticationPage(Model model, AuthRegisterCompanyDTO dtoRegister, AuthLoginCompanyDTO dtoLogin, HttpServletRequest request){
        model.addAttribute("companyRegister", dtoRegister);
        model.addAttribute("companyLogin", dtoLogin);
        model.addAttribute("status", request.getParameter("status"));
        model.addAttribute("notification", request.getParameter("message"));
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
        model.addAttribute("status", request.getParameter("status"));
        model.addAttribute("notification", request.getParameter("message"));
        return "public/registerConfirm";
    }

    @GetMapping("newUser/{mail}")//TODO: ajouter des variables dans l'url pour identifier la company et sécuriser
    String getNewUser(@PathVariable("mail") String companyMail,  Model model, HttpServletRequest request){
        Company company = companyService.getCompanyByMail(companyMail);
        model.addAttribute("company", company);
        model.addAttribute("status", request.getParameter("status"));
        model.addAttribute("notification", request.getParameter("message"));
        log.info("mail : {}", companyMail);
        return "public/newUser";
    }

    @PostMapping("newUser/{mail}")
    String registerSite(@PathVariable("mail") String companyMail,
                        @Valid AccountDTO dto,
                        BindingResult result,
                        Model model){
        if (!result.hasErrors()) {
            companyMail = dto.getMail();
            Response response = companyService.registerSite(dto);
            if (response.getStatus().equals(EnumStatus.OK)) {
                model.addAttribute("company", companyService.getAccountDto(companyService.getCompanyByMail(companyMail)));
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
        model.addAttribute("mail", companyMail);
        model.addAttribute("company", companyService.getAccountDto(companyService.getCompanyByMail(companyMail)));
        return "public/newUser";
    }


    @GetMapping("/logout")
    String logout(HttpServletRequest request){

        HttpSession session = request.getSession();
        session.invalidate();
        return "index";
    }
    
}
