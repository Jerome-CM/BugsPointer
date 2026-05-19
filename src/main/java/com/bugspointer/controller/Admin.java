package com.bugspointer.controller;

import be.woutschoovaerts.mollie.exception.MollieException;
import com.bugspointer.dto.AdminBillingDTO;
import com.bugspointer.dto.AdminScraperJobDTO;
import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.FirstReportDTO;
import com.bugspointer.dto.Response;
import com.bugspointer.entity.EnumEtatBug;
import com.bugspointer.entity.EnumPlan;
import com.bugspointer.entity.EnumViewCounterPage;
import com.bugspointer.service.implementation.AdminService;
import com.bugspointer.service.implementation.AdminScraperService;
import com.bugspointer.service.implementation.ChartService;
import com.bugspointer.service.implementation.FirstReportService;
import com.bugspointer.service.implementation.PlanPricingService;
import com.bugspointer.service.implementation.ViewCounterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.ParseException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@RequestMapping("app/admin")
@Slf4j
public class Admin {
    private final AdminService adminService;

    private final FirstReportService firstReportService;

    private final ChartService chartService;

    private final PlanPricingService planPricingService;

    private final AdminScraperService adminScraperService;

    public Admin(AdminService adminService, FirstReportService firstReportService, ChartService chartService, PlanPricingService planPricingService, AdminScraperService adminScraperService) {
        this.adminService = adminService;
        this.firstReportService = firstReportService;
        this.chartService = chartService;
        this.planPricingService = planPricingService;
        this.adminScraperService = adminScraperService;
    }

    @GetMapping("dashboard")
    String getDashboard(Model model, @RequestParam(required = false) String scraperJobId){
        addDashboardAttributes(model);
        addScraperJobAttributes(model, scraperJobId);
        return "admin/dashboard";
    }

    @PostMapping("scraper")
    String scanWebsite(@RequestParam("websiteUrl") String websiteUrl){
        AdminScraperJobDTO job = adminScraperService.startScan(websiteUrl);
        return "redirect:/app/admin/dashboard?scraperJobId=" + job.getId() + "#scrapping";
    }

    @PostMapping("scraper/cancel")
    String cancelScraper(@RequestParam("scraperJobId") String scraperJobId){
        adminScraperService.cancelScan(scraperJobId);
        return "redirect:/app/admin/dashboard?scraperJobId=" + scraperJobId + "#scrapping";
    }

    @GetMapping("scraper/status")
    @ResponseBody
    Map<String, Object> getScraperStatus(@RequestParam("scraperJobId") String scraperJobId) {
        Map<String, Object> response = new HashMap<>();
        AdminScraperJobDTO job = adminScraperService.getJob(scraperJobId);
        if (job == null) {
            response.put("running", false);
            response.put("finished", true);
            response.put("missing", true);
            return response;
        }

        response.put("running", job.isRunning());
        response.put("finished", !job.isRunning());
        response.put("hasError", job.getError() != null);
        response.put("cancelled", job.isCancelled());
        return response;
    }

    private void addDashboardAttributes(Model model) {
        model.addAttribute("firstReports", firstReportService.getCandidateForFirstReport());
        model.addAttribute("secondReports", firstReportService.getCandidateForSecondReport());
        model.addAttribute("firstReportDTO", new FirstReportDTO());
        model.addAttribute("billingTotal", adminService.getBillingTotal());
        model.addAttribute("estimatedRevenue", adminService.getEstimatedAnnualRevenue());
        model.addAttribute("estimatedProfit", adminService.getEstimatedProfit());
        model.addAttribute("paidCompanyCount", adminService.getPaidCompanyCount());
        model.addAttribute("totalCompanyCount", adminService.getTotalCompanyCount());
        model.addAttribute("confirmedCompanyCount", adminService.getConfirmedCompanyCount());
        model.addAttribute("freeCompanyCount", adminService.getFreeCompanyCount());
        model.addAttribute("targetCompanyCount", adminService.getTargetCompanyCount());
        model.addAttribute("ultimateCompanyCount", adminService.getUltimateCompanyCount());
        model.addAttribute("verifiedDomainCount", adminService.getVerifiedDomainCount());
        model.addAttribute("missingDomainCount", adminService.getMissingDomainCount());
        model.addAttribute("totalBugCount", adminService.getTotalBugCount());
        model.addAttribute("pendingBugCount", adminService.getBugCount(EnumEtatBug.PENDING));
        model.addAttribute("solvedBugCount", adminService.getBugCount(EnumEtatBug.SOLVED));
        model.addAttribute("planPrices", planPricingService.getPlanPrices());
    }

    private void addScraperJobAttributes(Model model, String scraperJobId) {
        AdminScraperJobDTO scraperJob = adminScraperService.getJob(scraperJobId);
        if (scraperJob == null) {
            return;
        }

        model.addAttribute("scraperJob", scraperJob);
        model.addAttribute("scraperUrl", scraperJob.getWebsiteUrl());
        if (!scraperJob.isRunning() && scraperJob.getResult() != null) {
            model.addAttribute("scraperResult", scraperJob.getResult());
        }
    }

    @PostMapping("planPrice")
    String updatePlanPrice(@RequestParam("plan") EnumPlan plan,
                           @RequestParam("newSubscriptionAmount") BigDecimal newSubscriptionAmount,
                           RedirectAttributes redirectAttributes) {
        Response response = planPricingService.updateNewSubscriptionAmount(plan, newSubscriptionAmount);
        redirectAttributes.addFlashAttribute("notification", response.getMessage());
        redirectAttributes.addFlashAttribute("status", String.valueOf(response.getStatus()));
        return "redirect:/app/admin/dashboard";
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
        model.addAttribute("revenue", chartService.getDataForViewForLastestXdaysForRevenue(day));
        return "admin/metrics";
    }

    @PostMapping("metrics/exclude-browser")
    String excludeBrowserFromMetrics(HttpServletResponse response, RedirectAttributes redirectAttributes){
        Cookie cookie = new Cookie(ViewCounterService.EXCLUDE_COOKIE_NAME, "true");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(60 * 60 * 24 * 365);
        response.addCookie(cookie);
        redirectAttributes.addFlashAttribute("notification", "Ce navigateur ne sera plus comptabilisé dans les métriques.");
        redirectAttributes.addFlashAttribute("status", String.valueOf(EnumStatus.OK));
        return "redirect:/app/admin/metrics";
    }

    @PostMapping("metrics/include-browser")
    String includeBrowserInMetrics(HttpServletResponse response, RedirectAttributes redirectAttributes){
        Cookie cookie = new Cookie(ViewCounterService.EXCLUDE_COOKIE_NAME, "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        redirectAttributes.addFlashAttribute("notification", "Ce navigateur sera à nouveau comptabilisé dans les métriques.");
        redirectAttributes.addFlashAttribute("status", String.valueOf(EnumStatus.OK));
        return "redirect:/app/admin/metrics";
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
