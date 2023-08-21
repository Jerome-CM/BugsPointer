package com.bugspointer.controller;

import be.woutschoovaerts.mollie.exception.MollieException;
import com.bugspointer.dto.FirstReportDTO;
import com.bugspointer.entity.EnumViewCounterPage;
import com.bugspointer.service.implementation.AdminService;
import com.bugspointer.service.implementation.ChartService;
import com.bugspointer.service.implementation.FirstReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;

@Controller
@RequestMapping("app/admin")
@Slf4j
public class Admin {
    private final AdminService adminService;

    private final FirstReportService firstReportService;

    private final ChartService chartService;

    public Admin(AdminService adminService, FirstReportService firstReportService, ChartService chartService) {
        this.adminService = adminService;
        this.firstReportService = firstReportService;
        this.chartService = chartService;
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
    String getMetrics(Model model,@RequestParam(required = false) String dayOpt) throws ParseException {
        int day = 30;
        if(dayOpt != null){
            day = Integer.parseInt(dayOpt);
        }
        model.addAttribute("nbrVisit", chartService.getDataForViewForLastestXdaysForVisits(day));
        model.addAttribute("nbrUser", chartService.getDataForViewForLastestXdaysForUsers(day));
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
