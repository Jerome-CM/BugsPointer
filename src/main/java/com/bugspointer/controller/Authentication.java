package com.bugspointer.controller;

import com.bugspointer.configuration.UserAuthenticationUtil;
import com.bugspointer.dto.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import com.bugspointer.entity.Company;
import com.bugspointer.service.implementation.CompanyService;
import com.bugspointer.service.implementation.CompanyTokenService;
import com.bugspointer.service.implementation.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Slf4j
public class Authentication {
    private final CompanyService companyService;

    private final CompanyTokenService companyTokenService;

    private final MailService mailService;

    private  final UserAuthenticationUtil userAuthenticationUtil;

    public Authentication(CompanyService companyService, CompanyTokenService companyTokenService, MailService mailService, UserAuthenticationUtil userAuthenticationUtil) {
        this.companyService = companyService;
        this.companyTokenService = companyTokenService;
        this.mailService = mailService;
        this.userAuthenticationUtil = userAuthenticationUtil;
    }

    @GetMapping("/authentication")
    String getAuthenticationPage(Model model, AuthRegisterCompanyDTO dtoRegister, AuthLoginCompanyDTO dtoLogin, HttpServletRequest request){
        model.addAttribute("companyRegister", dtoRegister);
        model.addAttribute("companyLogin", dtoLogin);
        if (request.getParameter("status") != null) {
            model.addAttribute("status", request.getParameter("status"));
            model.addAttribute("notification", request.getParameter("message"));
        }
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
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
                //Mail à modifier pour envoi forcé sinon dto.getMail()
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
    String getRegisterConfirm(Model model, AuthRegisterCompanyDTO dtoRegister, AuthLoginCompanyDTO dtoLogin){
        model.addAttribute("companyRegister", dtoRegister);
        model.addAttribute("companyLogin", dtoLogin);
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        return "public/registerConfirm";
    }

    @GetMapping("newUser/{publicKey}")//TODO: ajouter des variables dans l'url pour identifier la company et sécuriser ?
    String getNewUser(@PathVariable("publicKey") String publicKey,  Model model){
        Company company = companyService.getCompanyByPublicKey(publicKey);
        model.addAttribute("company", company);
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
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


    @GetMapping("pwLost")
    String getPwLost(Model model, AccountDTO dto){
        model.addAttribute("company", dto);
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
        return "public/pwLost";
    }

    @PostMapping("pwLost")
    String passwordLost(@Valid AccountDTO dto, BindingResult result, Model model){
        if(!result.hasErrors()){
            Response response;
            try {
                response = companyService.sendPwLost(dto);
            } catch (Exception e){
                log.error(e.getMessage());
                response = new Response(EnumStatus.ERROR, null, "Une erreur est survenu lors de l'envoi du mail. Si le problème persiste merci de nous contacter");
            }
            model.addAttribute("status", String.valueOf(response.getStatus()));
            model.addAttribute("notification", response.getMessage());
            model.addAttribute("etat", "ok");
        }

        return "public/pwLost";
    }

    @GetMapping("resetPassword/{publicKey}/{token}")
    String getResetPassword(@PathVariable("publicKey") String publicKey, @PathVariable("token") String token, Model model, AccountDTO dto){
        log.info("token : {} - key : {}", token, publicKey);
        boolean ok = companyTokenService.checkToken(token, publicKey);
        log.info("test : {}", ok);
        if (ok) {
            //AccountDTO dto = companyService.getAccountDto(companyService.getCompanyByPublicKey(publicKey));
            model.addAttribute("company", dto);
            model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
            return "public/resetPw";
        }
        return "redirect:/authentication";
    }

    @PostMapping("resetPassword/{publicKey}/{token}")
    String resetPassword(@PathVariable("publicKey") String publicKey,
                         @PathVariable("token") String token,
                         @Valid AccountDTO dto,
                         BindingResult result,
                         RedirectAttributes redirectAttributes){
        log.info("token : {}", token);
        if (!result.hasErrors()){

            Response response = companyService.resetPassword(publicKey, dto, token);
            redirectAttributes.addFlashAttribute("status", String.valueOf(response.getStatus()));
            redirectAttributes.addFlashAttribute("notification", response.getMessage());
            if (response.getStatus().equals(EnumStatus.OK)) {
                return "redirect:/authentication";
            }
        }
        redirectAttributes.addAttribute("publicKey", publicKey);
        redirectAttributes.addAttribute("token", token);
        return "redirect:/resetPassword/{publicKey}/{token}";
    }

    @GetMapping("/logout")
    String logout(HttpServletRequest request){
        HttpSession session = request.getSession();
        session.invalidate();
        return "index";
    }

    @GetMapping("/denied")
    String accesDenied(Model model){
        //TODO les attribute ne sont pas visible dans la vue
        model.addAttribute("status", "ERROR");
        model.addAttribute("notification", "Vous n'avez pas accès à cette page");
        if (userAuthenticationUtil.isUserLoggedIn()) {
            model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());
            return "redirect:/app/private/dashboard";
        }
        return "redirect:/authentication";
    }


}
