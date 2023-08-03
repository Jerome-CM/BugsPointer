package com.bugspointer.controller;

import be.woutschoovaerts.mollie.exception.MollieException;
import com.bugspointer.configuration.UserAuthenticationUtil;
import com.bugspointer.dto.FirstReportDTO;
import com.bugspointer.dto.Response;
import com.bugspointer.service.implementation.AdminService;
import com.bugspointer.service.implementation.FirstReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("app/admin")
@Slf4j
public class Admin {
    private final AdminService adminService;

    private final FirstReportService firstReportService;

    public Admin(AdminService adminService, FirstReportService firstReportService) {
        this.adminService = adminService;
        this.firstReportService = firstReportService;
    }

    @GetMapping("dashboard")
    String getDashboard(Model model){
        model.addAttribute("firstReports", firstReportService.getCandidateForFirstReport());
        model.addAttribute("secondReports", firstReportService.getCandidateForSecondReport());
        model.addAttribute("firstReportDTO", new FirstReportDTO());
        return "admin/dashboard";
    }

    @GetMapping("addData")
    String getAddData(Model model){
        return "admin/addData";
    }

    @GetMapping("metrics")
    String getMetrics(Model model){
        return "admin/metrics";
    }

    @GetMapping("companiesList")
    String getCompaniesList(Model model){
        model.addAttribute("companies", adminService.getAllCompanyForList());
        return "admin/companiesList";
    }

    @GetMapping("companyDetails/{id}")
    String getCompanyDetails(Model model, @PathVariable("id") Long companyId) throws MollieException {
        model.addAttribute("company", adminService.getCompanyInfo(companyId));
        model.addAttribute("logs", adminService.getAllLogByCompany(companyId));
        return "admin/companyDetails";
    }

    @GetMapping("/lockCompany/{id}")
    public String changeEnableCompanyStatus(@PathVariable("id") Long id){
        adminService.changeEnableCompanyStatus(id);
        return "redirect:/app/admin/companyDetails/{id}";
    }
}
