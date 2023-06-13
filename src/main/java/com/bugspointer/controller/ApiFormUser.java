package com.bugspointer.controller;

import be.woutschoovaerts.mollie.data.subscription.SubscriptionResponse;
import be.woutschoovaerts.mollie.exception.MollieException;
import com.bugspointer.dto.CustomerDTO;
import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.ModalDTO;
import com.bugspointer.dto.Response;
import com.bugspointer.service.implementation.ModalService;
import com.bugspointer.service.implementation.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
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
    String createNewCustomer(@Valid @ModelAttribute CustomerDTO customer, BindingResult result, RedirectAttributes redirectAttributes) throws MollieException {

        if (!result.hasErrors()) {
            Response response = paymentService.createNewCustomer(customer);
            //On récupère l'objet envoyé dans la response
            Object content = response.getContent();
            //S'il s'agit d'un CustomerDTO on récupère l'Iban et le Bic
            if (content instanceof CustomerDTO) {
                CustomerDTO customerResponse = (CustomerDTO) content;
                customer.setIban(customerResponse.getIban());
                customer.setBic(customerResponse.getBic());
                customer.setSignature(customerResponse.isSignature());
            }
            redirectAttributes.addFlashAttribute("customer", customer);
            log.info("customer response : {}", customer);
            redirectAttributes.addFlashAttribute("response", response);


            if (response.getStatus().equals(EnumStatus.OK)){
                return "redirect:/app/private/BankAccount";
            }
            redirectAttributes.addFlashAttribute("product", customer.getPlan().name());
            redirectAttributes.addFlashAttribute("notification", response.getMessage());
            redirectAttributes.addFlashAttribute("status", String.valueOf(response.getStatus()));
            log.info("message {}", response.getMessage());
            return "redirect:/app/private/recapPayment?product="+customer.getPlan().name();
        }

        return "redirect:/";
    }

    @PostMapping("/newMandate")
    String createMandate(@Valid @ModelAttribute CustomerDTO customer, BindingResult result, RedirectAttributes redirectAttributes) throws MollieException {

        if (!result.hasErrors()) {
            Response response = paymentService.createMandate(customer);

            redirectAttributes.addFlashAttribute("notification", response.getMessage());
            redirectAttributes.addFlashAttribute("status", String.valueOf(response.getStatus()));

            Object content = response.getContent();
            if (content instanceof CustomerDTO) {
                CustomerDTO customerResponse = (CustomerDTO) content;
                customer.setIban(customerResponse.getIban());
                customer.setBic(customerResponse.getBic());
                redirectAttributes.addFlashAttribute("customer", customer);
                return "redirect:/app/private/BankAccount";
            }

            redirectAttributes.addFlashAttribute("response", response);
            redirectAttributes.addFlashAttribute("customer", customer);

            if (response.getStatus().equals(EnumStatus.OK)) {
                return "redirect:/api/user/newSubscription";
            }

            return "redirect:/app/private/dashboard";
        }
        return "redirect:/";
    }

    /*@GetMapping("/newPayment")
    String createFirstPaiement(@ModelAttribute("response") Response response, RedirectAttributes redirectAttributes) throws MollieException, IOException {

        Response response1 = paymentService.createFirstPayment(response);
        redirectAttributes.addFlashAttribute("response", response1);
        return "redirect:/api/user/newMandate";
    }*/

    /*@GetMapping("/newMandate")
    String createMandate(@ModelAttribute("response") Response response, RedirectAttributes redirectAttributes) throws MollieException {

        Response response1 = paymentService.createMandate(response);
        redirectAttributes.addFlashAttribute("response", response1);
        redirectAttributes.addFlashAttribute("customer", response.getContent());
        return "redirect:/api/user/newSubscription";
    }*/

    @GetMapping("newSubscription")
    String createSubscription(@ModelAttribute("response") Response response, @ModelAttribute("customer") CustomerDTO customer, BindingResult result, RedirectAttributes redirectAttributes) throws MollieException {

        if (!result.hasErrors()) {
            Response response1 = paymentService.createSubscription(response, customer);
            Object content = response.getContent();

            redirectAttributes.addFlashAttribute("notification", response1.getMessage());
            redirectAttributes.addFlashAttribute("status", String.valueOf(response1.getStatus()));

            if (content instanceof SubscriptionResponse){//TODO: modifier instanceof par subscriptionDTO pour aller à la page pour valider
                SubscriptionResponse subscriptionResponse = (SubscriptionResponse) content;
                redirectAttributes.addFlashAttribute("subscription", subscriptionResponse);
                redirectAttributes.addFlashAttribute("customer", customer);
                log.info("customer : {}", customer);
                log.info("subscription : {}", subscriptionResponse);
                return "redirect:/app/private/confirmSubscription";//TODO: retourner vers une page pour valider la subscription
            }

            if (response.getStatus().equals(EnumStatus.OK)) {
                return "redirect:/app/private/dashboard";
            }
        }
        return "redirect:/";
    }

    @PostMapping("subscription")
    String subscription(@Valid @ModelAttribute CustomerDTO customer,
                        @ModelAttribute("subscription")SubscriptionResponse subscriptionResponse,
                        BindingResult result,
                        RedirectAttributes redirectAttributes,
                        HttpServletRequest request) throws MollieException {
        String action = request.getParameter("action");
        if (!result.hasErrors()){
            if ("annuler".equals(action)){
                return "redirect:/dashboard";
            }

            Response response = paymentService.changeSubscription(customer, subscriptionResponse);

            redirectAttributes.addFlashAttribute("notification", response.getMessage());
            redirectAttributes.addFlashAttribute("status", String.valueOf(response.getStatus()));

            if (response.getStatus().equals(EnumStatus.OK)) {
                return "redirect:/app/private/dashboard";
            }
        }
        return "redirect:/";
    }

}
