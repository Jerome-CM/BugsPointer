package com.bugspointer.controller;

import be.woutschoovaerts.mollie.exception.MollieException;
import com.bugspointer.dto.AdminBillingDTO;
import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.FirstReportDTO;
import com.bugspointer.dto.Response;
import com.bugspointer.entity.EnumViewCounterPage;
import com.bugspointer.service.implementation.AdminService;
import com.bugspointer.service.implementation.ChartService;
import com.bugspointer.service.implementation.FirstReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.ParseException;
import javax.validation.Valid;

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
        model.addAttribute("billingTotal", adminService.getBillingTotal());
        model.addAttribute("estimatedRevenue", adminService.getEstimatedAnnualRevenue());
        model.addAttribute("estimatedProfit", adminService.getEstimatedProfit());
        model.addAttribute("paidCompanyCount", adminService.getPaidCompanyCount());
        model.addAttribute("totalCompanyCount", adminService.getTotalCompanyCount());
        model.addAttribute("freeCompanyCount", adminService.getFreeCompanyCount());
        model.addAttribute("verifiedDomainCount", adminService.getVerifiedDomainCount());
        model.addAttribute("missingDomainCount", adminService.getMissingDomainCount());
        model.addAttribute("totalBugCount", adminService.getTotalBugCount());
        return "admin/dashboard";
    }

    @GetMapping("addBilling")
    String getAddBilling(Model model, AdminBillingDTO billing){
        model.addAttribute("billing", billing);
        model.addAttribute("billings", adminService.getBillings());
        model.addAttribute("billingTotal", adminService.getBillingTotal());
        model.addAttribute("estimatedRevenue", adminService.getEstimatedAnnualRevenue());
        model.addAttribute("estimatedProfit", adminService.getEstimatedProfit());
        return "admin/addBilling";
    }

    @GetMapping("addData")
    String getAddData(){
        return "redirect:/app/admin/addBilling";
    }

    @PostMapping("addBilling")
    String addBilling(@Valid @ModelAttribute("billing") AdminBillingDTO billing,
                      BindingResult result,
                      Model model,
                      RedirectAttributes redirectAttributes){
        if (!result.hasErrors()) {
            Response response = adminService.saveBilling(billing);
            redirectAttributes.addFlashAttribute("notification", response.getMessage());
            redirectAttributes.addFlashAttribute("status", String.valueOf(response.getStatus()));
            return "redirect:/app/admin/addBilling";
        }
        model.addAttribute("status", String.valueOf(EnumStatus.ERROR));
        model.addAttribute("notification", "Merci de corriger les champs indiqués.");
        model.addAttribute("billings", adminService.getBillings());
        model.addAttribute("billingTotal", adminService.getBillingTotal());
        model.addAttribute("estimatedRevenue", adminService.getEstimatedAnnualRevenue());
        model.addAttribute("estimatedProfit", adminService.getEstimatedProfit());
        return "admin/addBilling";
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

    @PostMapping("/lockCompany/{id}")
    public String changeEnableCompanyStatus(@PathVariable("id") Long id){
        adminService.changeEnableCompanyStatus(id);
        return "redirect:/app/admin/companyDetails/{id}";
    }
}
