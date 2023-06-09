package com.bugspointer.service.implementation;

import be.woutschoovaerts.mollie.Client;
import be.woutschoovaerts.mollie.ClientBuilder;
import be.woutschoovaerts.mollie.data.common.Amount;
import be.woutschoovaerts.mollie.data.customer.CustomerRequest;
import be.woutschoovaerts.mollie.data.customer.CustomerResponse;
import be.woutschoovaerts.mollie.data.mandate.MandateRequest;
import be.woutschoovaerts.mollie.data.mandate.MandateResponse;
import be.woutschoovaerts.mollie.data.mandate.MandateStatus;
import be.woutschoovaerts.mollie.data.subscription.SubscriptionRequest;
import be.woutschoovaerts.mollie.data.subscription.SubscriptionResponse;
import be.woutschoovaerts.mollie.data.subscription.SubscriptionStatus;
import be.woutschoovaerts.mollie.exception.MollieException;
import com.bugspointer.dto.CustomerDTO;
import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.Response;
import com.bugspointer.entity.Company;
import com.bugspointer.entity.Customer;
import com.bugspointer.repository.CompanyRepository;
import com.bugspointer.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static be.woutschoovaerts.mollie.data.common.Locale.fr_FR;

@Service
@Slf4j
public class PaymentService {

    private final CompanyRepository companyRepository;

    private final CustomerRepository customerRepository;

    private final HttpServletResponse responseHttp;


    public PaymentService(CompanyRepository companyRepository, CustomerRepository customerRepository, HttpServletResponse response) {
        this.companyRepository = companyRepository;
        this.customerRepository = customerRepository;
        this.responseHttp = response;
    }

    Client client = new ClientBuilder()
            .withApiKey("test_upURPW9vMxSv3M5MzEEC6c2yywuKwe")
            .build();

    public Response createNewCustomer(CustomerDTO customerDTO) throws MollieException {

        if (!customerDTO.isCguAccepted()){
            return new Response(EnumStatus.ERROR, null, "Veuillez accepter les CGU pour procéder au paiement");
        }
        if (customerDTO.getMail() == null || customerDTO.getCompanyName() == null || customerDTO.getAddress1() == null || customerDTO.getCp() == null || customerDTO.getCity() == null){
            return new Response(EnumStatus.ERROR, null, "Veuillez compléter les champs obligatoires");
        }

        Company companyCustomer;
        Customer customerBugspointer = null;
        Optional<Company> companyOptional = companyRepository.findByPublicKey(customerDTO.getPublicKey());
        if (companyOptional.isPresent()){
            companyCustomer = companyOptional.get();
            log.info("companyCustomer : {}", companyCustomer);
            Optional<Customer> customerOptional = customerRepository.findByCompany_CompanyId(companyCustomer.getCompanyId());
            if (customerOptional.isPresent()) {
                customerBugspointer = customerOptional.get();
                log.info("customer Bugspointer : {}", customerBugspointer);
            }
        } else {
            log.error("Company non trouvée");
            return new Response(EnumStatus.ERROR, null, "Un erreur est survenu, merci de vous connecter");
        }

        //On crée le customer qui sera créé ou modifié chez Mollie
        CustomerRequest customer = new CustomerRequest();
        // Create customer
        customer.setEmail(Optional.of(customerDTO.getMail()));
        customer.setName(Optional.of(customerDTO.getCompanyName()));
        customer.setLocale(Optional.of(fr_FR));
        //TODO Faire un enum comme les indicatifs, Si pays choisit c'est france (liste déroulante dans newCustomer), alors ici c'est fr_FR
        // Si c'est la belgique alors fr_BE

        //TODO Rendre obligatoire ces données ( rapide require en front, mais surtout renvoie d'erreur en back )
        Map<String, Object> meta = new HashMap<>();
        meta.put("address1", customerDTO.getAddress1());
        meta.put("address2", customerDTO.getAddress2());
        meta.put("cp", customerDTO.getCp());
        meta.put("city", customerDTO.getCity());
        meta.put("country", customerDTO.getCountry());

        customer.setMetadata(meta);

        //On vérifie que le client n'existe pas
        if (customerBugspointer != null){

            //On récupère le customer de chez Mollie
            CustomerResponse customerResponse = client.customers().getCustomer(companyCustomer.getCustomer().getCustomerId());

            log.info("meta : {}", customerResponse.getMetadata());

            //On modifie le customer avec les données saisies par le client
            CustomerResponse customerHandler = client.customers().updateCustomer(customerResponse.getId(), customer);

            log.info("meta : {}", customerHandler.getMetadata());

            log.info("\r -------  ");
            log.info("Mollie customer : {} - {} - {}", customerHandler.getName(),customerHandler.getEmail(),customerHandler.getId());
            log.info("\r -------  ");

            log.info("\r -------  ");
            log.info("Bugspointer Customer : {} - {}", customerBugspointer.getId(), customerBugspointer.getCustomerId());
            log.info("\r -------  ");

        } else {

            //On crée le customer chez Mollie
            CustomerResponse customerHandler = client.customers().createCustomer(customer);

            //Si on récupère l'Id du customer de chez mollie on crée le customer chez nous
            if (customerHandler.getId() != null) {
                log.info("\r -------  ");
                log.info("New Mollie customer : {} - {} - {}", customerHandler.getName(),customerHandler.getEmail(),customerHandler.getId());
                log.info("\r -------  ");
                customerBugspointer = new Customer();

                customerBugspointer.setCustomerId(customerHandler.getId());
                /*customerBugspointer.setCompany(companyRepository.findByCompanyName(customerDTO.getCompanyName()).get());*/
                customerBugspointer.setCompany(companyCustomer);

                try {
                    Customer returnCustomerSaved = customerRepository.save(customerBugspointer);
                    companyCustomer.setCustomer(returnCustomerSaved);
                    Company companySaveCustomer = companyRepository.save(companyCustomer);
                    log.info("\r -------  ");
                    log.info("New Bugspointer Customer : {} - {}", returnCustomerSaved.getId(), returnCustomerSaved.getCustomerId());
                    log.info("\r -------  ");
                    log.info("company get customer : {}", companySaveCustomer.getCustomer());
                    return new Response(EnumStatus.OK, customerBugspointer, null);
                } catch (Exception e) {
                    log.error("Impossible to save a new customer : {}", e.getMessage());
                    return new Response(EnumStatus.ERROR, null, null);
                }

            }
        }

        return new Response(EnumStatus.ERROR, null, null);

    }

    public Response createMandate(Response response) throws MollieException {

        Customer customer = (Customer) response.getContent();
        log.info("customer paiement : {}", customer);

        MandateRequest mandateRequest = new MandateRequest();

        mandateRequest.setMethod("directdebit");
        mandateRequest.setConsumerName(customer.getCompany().getCompanyName());
        mandateRequest.setConsumerAccount("FR7617418000010000052248408");
        mandateRequest.setConsumerBic(Optional.of("SNNNFR22XXX"));
        mandateRequest.setSignatureDate(Optional.of(LocalDate.parse("2023-06-06"))); //TODO Mettre la date du jour actuel
        mandateRequest.setMandateReference(Optional.of("BugsPointer-Mandate-2023-06-06-cst_V4fQbSFEn7"));
        // TODO Valeur de MandateReference : customer.getCompany()-Mandate-Bugspointer-directdebit-date()

        MandateResponse mandateHandler = client.mandates().createMandate(customer.getCustomerId(), mandateRequest);

        if (mandateHandler.getStatus().equals(MandateStatus.VALID)){
            log.info("\r -------  ");
            log.info("New Mollie Mandate : {} - {} - {}", customer.getCompany(), mandateHandler.getSignatureDate(), mandateHandler.getId());
            log.info("\r -------  ");
            // TODO Envoie de mail pour indiquer la confirmation du mandat
            return new Response(EnumStatus.OK, mandateHandler, ""); // Ne pas mettre de message car on passe directement à la méthode de subscrible
        }

        return new Response(EnumStatus.ERROR, null, "Oups, le mandat est non valide, merci de vérifier");
    }


    public Response createSubscription(Response response, Customer customer) throws MollieException, IOException {

        MandateResponse mandateResponse = (MandateResponse) response.getContent();

        // TODO Avant de faire la souscription, il faut s'assurer que le mandat est encore valide pendant au moins 70j
        //Mandate API / list mandate : https://docs.mollie.com/reference/v2/mandates-api/list-mandates
        
        SubscriptionRequest subscriptionRequest = new SubscriptionRequest();

        // TODO rendre le nom du plan et le montant dynamique (peut être faire une table en BDD, id,plan,amount)
        subscriptionRequest.setAmount(new Amount("EUR", new BigDecimal("15.00")));
        subscriptionRequest.setDescription("Subscribe to Target Plan");
        subscriptionRequest.setTimes(Optional.of(4));
        subscriptionRequest.setInterval("12 months");
        subscriptionRequest.setMandateId(Optional.ofNullable(mandateResponse.getId()));

        SubscriptionResponse subscriptionResponse = client.subscriptions().createSubscription(customer.getCustomerId(), subscriptionRequest);

        if(subscriptionResponse.getStatus() == SubscriptionStatus.PENDING || subscriptionResponse.getStatus() == SubscriptionStatus.ACTIVE){
            log.info("\r -------  ");
            log.info("New Mollie Subscription : {} - {}", customer.getCustomerId(), subscriptionRequest.getDescription());
            log.info("\r -------  ");
            // TODO Envoie de mail pour confirmer la souscription
            return new Response(EnumStatus.OK, null, "La souscription à Target Plan est validé");
        }                                                               // TODO reprendre le nom dynamique du plan

        return new Response(EnumStatus.ERROR, null, "Erreur lors de la souscription");
    }

}


















