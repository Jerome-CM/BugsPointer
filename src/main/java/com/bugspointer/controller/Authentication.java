package com.bugspointer.controller;

import com.bugspointer.dto.AuthLoginCompanyDTO;
import com.bugspointer.dto.AuthRegisterCompanyDTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.Response;
import com.bugspointer.service.implementation.CompanyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/logout")
    String logout(HttpServletRequest request){

        HttpSession session = request.getSession();
        session.invalidate();
        return "index";
    }
    
}
