package com.bugspointer.controller;

import com.bugspointer.configuration.UserAuthenticationUtil;
import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.Response;
import com.bugspointer.service.implementation.CompanyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@Slf4j
public class ApiCompany {

    private final CompanyService companyService;

    private  final UserAuthenticationUtil userAuthenticationUtil;

    HttpServletResponse response;

    public ApiCompany(CompanyService companyService, UserAuthenticationUtil userAuthenticationUtil) {
        this.companyService = companyService;
        this.userAuthenticationUtil = userAuthenticationUtil;
    }

    @GetMapping("plan/{publicKey}")
    void getPlan(@RequestParam String publicKey){

    }

    @GetMapping("/confirmRegister/{publicKey}")
    String confirmRegister(Model model, @PathVariable("publicKey") String publicKey, RedirectAttributes redirectAttributes, HttpServletRequest request){
        Response response = companyService.validateRegister(publicKey);
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        redirectAttributes.addFlashAttribute("notification", response.getMessage());
        redirectAttributes.addFlashAttribute("status", String.valueOf(response.getStatus()));

        if (response.getStatus().equals(EnumStatus.OK)) {
            return "redirect:/newUser/{publicKey}";
        }
        return "redirect:/authentication";
    }


}
