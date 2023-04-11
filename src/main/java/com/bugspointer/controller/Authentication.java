package com.bugspointer.controller;

import com.bugspointer.dto.AuthCompanyDTO;
import javax.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.ui.Model;

@Controller
public class Authentication {

    @GetMapping("/authentication")
    String getAuthenticationPage(Model model, AuthCompanyDTO dto){
        model.addAttribute("company", dto);
        return "public/authentication";
    }

    @PostMapping("/register")
    String register(@Valid AuthCompanyDTO dto, BindingResult result, Model model){
        if(!result.hasErrors()){
            return "private/dashboard";
        }
        return "public/authentication";
    }

    @PostMapping("/login")
    String login(@Valid AuthCompanyDTO dto, BindingResult result, Model model){
        if(!result.hasErrors()){
            return "private/dashboard";
        }
        return "public/authentication";
    }
}
