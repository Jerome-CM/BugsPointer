package com.bugspointer.controller;

import com.bugspointer.configuration.UserAuthenticationUtil;
import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.FirstReportDTO;
import com.bugspointer.dto.Response;
import com.bugspointer.service.implementation.AdminService;
import com.bugspointer.service.implementation.CompanyService;
import com.bugspointer.service.implementation.FirstReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@Slf4j
public class ApiCompany {

    private final CompanyService companyService;

    private final FirstReportService firstReportService;

    private final AdminService adminService;

    private  final UserAuthenticationUtil userAuthenticationUtil;

    HttpServletResponse response;

    public ApiCompany(CompanyService companyService, FirstReportService firstReportService, AdminService adminService, UserAuthenticationUtil userAuthenticationUtil) {
        this.companyService = companyService;
        this.firstReportService = firstReportService;
        this.adminService = adminService;
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
            // Create table for bugspointer send 2 reports on client website
            Response rep = firstReportService.initFirstReport(publicKey);
            if (rep.getStatus().equals(EnumStatus.OK)) {
                return "redirect:/newUser/{publicKey}";
            }
        }
        return "redirect:/authentication";
    }

    @PostMapping("/api/saveFirstReport")
    public String saveFirstReport(@Valid FirstReportDTO firstReportDTO, BindingResult result){
        if(!result.hasErrors()){
            log.info("in post : {}", firstReportDTO);
            firstReportService.saveReportSended(firstReportDTO);
        }
        return "/admin/dashboard";
    }


}
