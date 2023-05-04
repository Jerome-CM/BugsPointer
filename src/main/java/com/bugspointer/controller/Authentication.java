package com.bugspointer.controller;

import com.bugspointer.dto.AuthLoginCompanyDTO;
import com.bugspointer.dto.AuthRegisterCompanyDTO;
import javax.validation.Valid;

import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.Response;
import com.bugspointer.service.implementation.CompanyService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.ui.Model;

@Controller
public class Authentication {
    private final CompanyService companyService;

    public Authentication(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping("/authentication")
    String getAuthenticationPage(Model model, AuthRegisterCompanyDTO dtoRegister, AuthLoginCompanyDTO dtoLogin){
        model.addAttribute("companyRegister", dtoRegister);
        model.addAttribute("companyLogin", dtoLogin);
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
                return "public/authentication";
            }
        }
        return "public/authentication";
    }
    
}
