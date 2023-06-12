package com.bugspointer.controller;

import be.woutschoovaerts.mollie.exception.MollieException;
import com.bugspointer.dto.CustomerDTO;
import com.bugspointer.dto.ModalDTO;
import com.bugspointer.dto.Response;
import com.bugspointer.entity.Company;
import com.bugspointer.entity.Customer;
import com.bugspointer.entity.EnumPlan;
import com.bugspointer.service.implementation.ModalService;
import com.bugspointer.service.implementation.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;

@Controller
@Slf4j
@RequestMapping("api/user")
public class ApiFormUser {

    private final ModalService modalService;

    private final PaymentService paymentService;

    public ApiFormUser(ModalService modalService, PaymentService paymentService) {
        this.modalService = modalService;
        this.paymentService = paymentService;
    }


    @PostMapping("/modalControl")
    RedirectView modal(@Valid ModalDTO dto, BindingResult result, HttpServletRequest request){
        if (!result.hasErrors()){
            String adresseIp = request.getRemoteAddr();
            log.info("adresseIp : {}", adresseIp);
            dto.setAdresseIp(adresseIp);
            modalService.saveModal(dto);
        }

        return new RedirectView(dto.getUrl());
    }

    @PostMapping("/newCustomer")
    String createNewCustomer(@Valid @ModelAttribute CustomerDTO customer, RedirectAttributes redirectAttributes) throws MollieException {

        //log.info("product : {}", product);
        Response response = paymentService.createNewCustomer(customer);
        redirectAttributes.addFlashAttribute("customer", customer);
        /*redirectAttributes.addFlashAttribute("product", product);*/

        redirectAttributes.addFlashAttribute("response", response);
        return "redirect:/app/private/BankAccount";


    }

    @PostMapping("/newMandate")
    String createMandate(@Valid @ModelAttribute CustomerDTO customer, RedirectAttributes redirectAttributes) throws MollieException {

        Response response = paymentService.createMandate(customer);
        redirectAttributes.addFlashAttribute("response", response);
        redirectAttributes.addFlashAttribute("customer", customer);
        return "redirect:/api/user/newSubscription";
    }

    @GetMapping("/newPayment")
    String createFirstPaiement(@ModelAttribute("response") Response response, RedirectAttributes redirectAttributes) throws MollieException, IOException {

        /*Response response1 = paymentService.createFirstPayment(response);
        redirectAttributes.addFlashAttribute("response", response1);*/
        return "redirect:/api/user/newMandate";
    }

    /*@GetMapping("/newMandate")
    String createMandate(@ModelAttribute("response") Response response, RedirectAttributes redirectAttributes) throws MollieException {

        Response response1 = paymentService.createMandate(response);
        redirectAttributes.addFlashAttribute("response", response1);
        redirectAttributes.addFlashAttribute("customer", response.getContent());
        return "redirect:/api/user/newSubscription";
    }*/

    @GetMapping("newSubscription")
    String createSubscription(@ModelAttribute("response") Response response, @ModelAttribute("customer") Customer customer) throws MollieException {

        Response response1 = paymentService.createSubscription(response, customer);
        return "redirect:/";
    }

}
