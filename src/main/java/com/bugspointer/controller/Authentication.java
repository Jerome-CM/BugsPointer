package com.bugspointer.controller;

import com.bugspointer.dto.RegisterCompanyDTO;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.ui.Model;

@Controller
public class Authentication {

    @GetMapping("/authentication")
    String getAuthenticationPage(Model model, RegisterCompanyDTO dto){
        model.addAttribute("compagny", dto);
        return "public/authentication";
    }

    @PostMapping("/register")
    String register(@Valid RegisterCompanyDTO dto, BindingResult result, Model model){
        if(!result.hasErrors()){
            return "private/dashboard";
        }
        return "public/authentication";
    }

    @PostMapping("/login")
    String login(@Valid RegisterCompanyDTO dto, BindingResult result, Model model){
        if(!result.hasErrors()){
            return "private/dashboard";
        }
        return "public/authentication";
    }
}
