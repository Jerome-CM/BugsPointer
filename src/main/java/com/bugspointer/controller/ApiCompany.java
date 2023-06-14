package com.bugspointer.controller;

import be.woutschoovaerts.mollie.exception.MollieException;
import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.Response;
import com.bugspointer.service.implementation.CompanyService;
import com.bugspointer.service.implementation.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@Slf4j
public class ApiCompany {

    private final CompanyService companyService;

    private final PaymentService paymentService;

    HttpServletResponse response;

    public ApiCompany(CompanyService companyService, PaymentService paymentService) {
        this.companyService = companyService;
        this.paymentService = paymentService;
    }

    @GetMapping("plan/{publicKey}")
    void getPlan(@RequestParam String publicKey){

    }

    @GetMapping("/confirmRegister/{publicKey}")
    String confirmRegister(@PathVariable("publicKey") String publicKey, RedirectAttributes redirectAttributes, HttpServletRequest request){
        Response response = companyService.validateRegister(publicKey);

        redirectAttributes.addFlashAttribute("notification", response.getMessage());
        redirectAttributes.addFlashAttribute("status", String.valueOf(response.getStatus()));

        if (response.getStatus().equals(EnumStatus.OK)) {
            return "redirect:/newUser/{publicKey}";
        }
        return "redirect:/authentication";
    }

    /*@GetMapping(value="/payment")
    public void getPayment() throws MollieException, IOException {
        paymentService.paymentTest(response);
    }*/

}
