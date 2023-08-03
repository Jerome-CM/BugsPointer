package com.bugspointer.controller;

import be.woutschoovaerts.mollie.exception.MollieException;
import com.bugspointer.configuration.UserAuthenticationUtil;
import com.bugspointer.dto.*;
import com.bugspointer.entity.Company;
import com.bugspointer.entity.EnumPlan;
import com.bugspointer.service.implementation.CompanyService;
import com.bugspointer.service.implementation.CustomerService;
import com.bugspointer.service.implementation.ModalService;
import com.bugspointer.service.implementation.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Controller
@Slf4j
@RequestMapping("api/user")
public class ApiFormUser {

    private final ModalService modalService;

    public final PaymentService paymentService;

    public final CustomerService customerService;

    private final CompanyService companyService;

    private  final UserAuthenticationUtil userAuthenticationUtil;

    public ApiFormUser(ModalService modalService, PaymentService paymentService, CustomerService customerService, CompanyService companyService, UserAuthenticationUtil userAuthenticationUtil) {
        this.modalService = modalService;
        this.paymentService = paymentService;
        this.customerService = customerService;
        this.companyService = companyService;
        this.userAuthenticationUtil = userAuthenticationUtil;
    }


    @PostMapping("/modalControl")
    RedirectView modal(@Valid ModalDTO dto, BindingResult result, HttpServletRequest request){
        if (!result.hasErrors()){
            String adresseIp = request.getRemoteAddr();
            log.info("{} send a new bug", adresseIp);
            dto.setAdresseIp(adresseIp);
            Response response = modalService.saveModal(dto);
            if(response.getStatus().equals(EnumStatus.ERROR)){
                log.info(response.getMessage());
            }
        }

        return new RedirectView(dto.getUrl());
    }

    @PostMapping("/newCustomer")
    String createNewCustomer(@Valid @ModelAttribute CustomerDTO customer, BindingResult result, RedirectAttributes redirectAttributes, Model model) throws MollieException {

        if (!result.hasErrors()) {

            Company company = companyService.getCompanyByPublicKey(customer.getPublicKey());
            if(customer.getPlan().equals(EnumPlan.FREE)){

                if(company != null){
                    if(customer.getPlan().equals(company.getPlan())){
                        redirectAttributes.addFlashAttribute("status", EnumStatus.ERROR);
                        redirectAttributes.addFlashAttribute("notification", "Vous êtes déjà en offre gratuite");
                        return "redirect:/app/private/dashboard";
                    }
                }

                Response response = paymentService.returnFreePlan(customer);
                if(response.getStatus().equals(EnumStatus.OK)){
                    redirectAttributes.addFlashAttribute("status", String.valueOf(response.getStatus()));
                    redirectAttributes.addFlashAttribute("notification", response.getMessage());
                    return "redirect:/app/private/dashboard"; //TODO recapPayment plutot
                }
            }

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
            // Iban isn't already find and valid
            if(!customerService.haveAndValidMandateWithIban(customer)) {
                Response response = paymentService.createMandate(customer);

                redirectAttributes.addFlashAttribute("notification", response.getMessage());
                redirectAttributes.addFlashAttribute("status", String.valueOf(response.getStatus()));

                Object content = response.getContent();
                // If content is instanceOf customer, response create mandat contins error
                if (content instanceof CustomerDTO) {
                    CustomerDTO customerResponse = (CustomerDTO) content;
                    customer.setIban(customerResponse.getIban());
                    customer.setBic(customerResponse.getBic());
                    redirectAttributes.addFlashAttribute("customer", customer);
                    log.info("redirect/app/private/BankAccount");
                    return "redirect:/app/private/BankAccount";
                }

                redirectAttributes.addFlashAttribute("response", response);
                redirectAttributes.addFlashAttribute("customer", customer);

                if (response.getStatus().equals(EnumStatus.OK)) {
                    return "redirect:/api/user/newSubscription";
                } else {
                    log.info("Create mandate is error, go to bankAccount");
                    return "redirect:/app/private/BankAccount";
                }
                
            } else {
                Response response = paymentService.getMandate(customer);
                log.info("response getMandate : {}", response);
                redirectAttributes.addFlashAttribute("response", response);
                redirectAttributes.addFlashAttribute("customer", customer);
                return "redirect:/api/user/newSubscription";
            }
        }
        return "redirect:/app/private/BankAccount";
    }

    @GetMapping("newSubscription")
    String createSubscription(Model model, @ModelAttribute("response") Response response, @ModelAttribute("customer") CustomerDTO customer, BindingResult result, RedirectAttributes redirectAttributes) throws MollieException {
        model.addAttribute("isLoggedIn", userAuthenticationUtil.isUserLoggedIn());

        if (!result.hasErrors()) {
            Response responseSub = paymentService.createSubscription(response, customer);
            Object content = responseSub.getContent();

            redirectAttributes.addFlashAttribute("notification", responseSub.getMessage());
            redirectAttributes.addFlashAttribute("status", String.valueOf(responseSub.getStatus()));

            if (responseSub.getStatus().equals(EnumStatus.OK)) {
                if (content instanceof SubscriptionDTO){
                    SubscriptionDTO dto = (SubscriptionDTO) content;
                    redirectAttributes.addFlashAttribute("subscription", dto);
                    log.info("subscription : {}", dto);
                    return "redirect:/app/private/confirmSubscription";
                }

                return "redirect:/app/private/thanks";
            } else {
                if(responseSub.getMessage().equals("Vous avez déjà souscrit à cet abonnement")){
                    redirectAttributes.addFlashAttribute("notification", responseSub.getMessage());
                    redirectAttributes.addFlashAttribute("status", "OK");

                    return "redirect:/app/private/thanks";
                }
            }
        }

        redirectAttributes.addFlashAttribute("notification", "Une erreur inattendue s'est produite, contacter : contact@bugspointer.com");
        redirectAttributes.addFlashAttribute("status", "ERROR");

        return "redirect:/app/private/dashboard";
    }

    @PostMapping("subscription")
    String subscription(@Valid @ModelAttribute SubscriptionDTO subscription,
                        BindingResult result,
                        RedirectAttributes redirectAttributes,
                        HttpServletRequest request) throws MollieException {
        String action = request.getParameter("action");
        log.info("action : {}", action);
        log.info("subscriptionDTO : {}", subscription);
        if (!result.hasErrors()){
            if ("annuler".equals(action)){
                return "redirect:/app/private/dashboard";
            }

            Response response = paymentService.changeSubscription(subscription);

            redirectAttributes.addFlashAttribute("notification", response.getMessage());
            redirectAttributes.addFlashAttribute("status", String.valueOf(response.getStatus()));

            if (response.getStatus().equals(EnumStatus.OK)) {
                return "redirect:/app/private/thanks";
            }
        }
        return "redirect:/";
    }

    @GetMapping("/addDateDownload/{id}")
    public String addDateDownload(@PathVariable("id") Long id){
        companyService.addDateForDownload(id);
        return "public/download";
    }

}
