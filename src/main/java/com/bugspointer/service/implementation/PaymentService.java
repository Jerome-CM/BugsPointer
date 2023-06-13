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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static be.woutschoovaerts.mollie.data.common.Locale.fr_FR;

@Service
@Slf4j
public class PaymentService {

    private final CompanyRepository companyRepository;
    private final CompanyService companyService;

    private final CustomerRepository customerRepository;


    public PaymentService(CompanyRepository companyRepository, CompanyService companyService, CustomerRepository customerRepository) {
        this.companyRepository = companyRepository;
        this.companyService = companyService;
        this.customerRepository = customerRepository;
    }

    Client client = new ClientBuilder()
            .withApiKey("test_upURPW9vMxSv3M5MzEEC6c2yywuKwe")
            .build();

    public Response createNewCustomer(CustomerDTO customerDTO) throws MollieException {

        if (!customerDTO.isCguAccepted()){
            return new Response(EnumStatus.ERROR, null, "Veuillez accepter les CGU pour procéder au paiement");
        }
        if (customerDTO.getMail().trim().isEmpty() || customerDTO.getCompanyName().trim().isEmpty() || customerDTO.getAddress1().trim().isEmpty() || customerDTO.getCp().trim().isEmpty() || customerDTO.getCity().trim().isEmpty()){
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

        //On vérifie que le client existe
        if (customerBugspointer != null){

            //On récupère le customer de chez Mollie
            CustomerResponse customerResponse = client.customers().getCustomer(companyCustomer.getCustomer().getCustomerId());

            //On modifie le customer avec les données saisies par le client
            CustomerResponse customerHandler = client.customers().updateCustomer(customerResponse.getId(), customer);

            log.info("\r -------  ");
            log.info("Mollie customer : {} - {} - {} - {}", customerHandler.getName(),customerHandler.getEmail(),customerHandler.getId(),customerHandler.getMetadata());
            log.info("\r -------  ");

            log.info("\r -------  ");
            log.info("Bugspointer Customer : {} - {}", customerBugspointer.getId(), customerBugspointer.getCustomerId());
            log.info("\r -------  ");

            //On vérifie la présence d'un mandat valide
            List<MandateResponse> mandates = client.mandates().listMandates(customerBugspointer.getCustomerId()).getEmbedded().getMandates();
            log.info("liste mandates : {}", mandates);
            if (!mandates.isEmpty()){
                MandateResponse mandat = mandates.get(0);
                log.info("\r -------  ");
                log.info("Mollie Mandate : {} - {} - {}", customerBugspointer.getCompany(), mandat.getSignatureDate(), mandat.getId());
                log.info("\r -------  ");
                //Si le mandat a été signé il y a moins de 3 ans
                if (mandat.getStatus()!=MandateStatus.INVALID && mandat.getSignatureDate().isAfter(LocalDate.now().minusYears(3))){
                    customerDTO.setIban(mandat.getDetails().getConsumerAccount().orElse(""));
                    customerDTO.setBic(mandat.getDetails().getConsumerBic().orElse(""));
                    if (mandat.getSignatureDate() != null) {
                        customerDTO.setSignature(true);
                    }
                    return new Response(EnumStatus.OK, customerDTO, "Un mandat est déjà présent vous pouvez le valider ou modifier vos coordonnées bancaire pour créer un nouveau mandat");
                }
            }
            log.info("Pas de mandat enregistré");
            return new Response(EnumStatus.OK, customerBugspointer, null);

            //TODO vérifier s'il existe un mandat, si présent on affiche les données (milieu haché) getMandate.getDetail() -> on vérifie les infos envoyé si identique rien sinon on révoque et créé

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

    public Response createMandate(CustomerDTO customerDTO) throws MollieException {

        if (!customerDTO.isSignature()){
            return new Response(EnumStatus.ERROR, customerDTO, "Veuillez signer le mandat pour valider votre abonnement");
        }
        if (customerDTO.getIban().trim().isEmpty() || customerDTO.getBic().trim().isEmpty()){
            return new Response(EnumStatus.ERROR, customerDTO, "Veuillez compléter les champs obligatoires");
        }

        Customer customer;
        //On cherche la company à partir de la publicKey
        Optional<Company> companyOptional = companyRepository.findByPublicKey(customerDTO.getPublicKey());
        if (companyOptional.isPresent()){
            //Si on retrouve la company on recherche le customer
            Optional<Customer> customerOptional = customerRepository.findByCompany_CompanyId(companyOptional.get().getCompanyId());
            if (customerOptional.isPresent()) {
                customer = customerOptional.get();
            } else {
                //Si le customer n'est pas présent alors il faut le créer TODO: retourne une erreur et renvoi vers la page de création
                return new Response(EnumStatus.ERROR, null, "Une erreur est survenu");
            }
        } else {
            log.error("Company non trouvée");
            return new Response(EnumStatus.ERROR, null, "Une erreur est survenu, merci de vous connecter");
        }

        log.info("customer paiement : {}", customer);

        //On vérifie si un mandat existe et qu'il est valide
        //On récupère la liste des mandats
        //Pagination<MandateListResponse> mandateListResponses = client.mandates().listMandates(customer.getCustomerId());
        List<MandateResponse> mandates = client.mandates().listMandates(customer.getCustomerId()).getEmbedded().getMandates();
        log.info("liste mandates : {}", mandates);
        if (!mandates.isEmpty()){
            MandateResponse mandat = mandates.get(0);
            log.info("\r -------  ");
            log.info("Mollie Mandate : {} - {} - {}", customer.getCompany(), mandat.getSignatureDate(), mandat.getId());
            log.info("\r -------  ");
            if (mandat.getStatus()!=MandateStatus.INVALID){
                if (mandat.getSignatureDate().isAfter(LocalDate.now().minusYears(3))){
                    if (customerDTO.getIban().equals(mandat.getDetails().getConsumerAccount().orElse("")) && customerDTO.getBic().equals(mandat.getDetails().getConsumerBic().orElse(""))) {
                        log.info("Mollie Mandate not modified");
                        return new Response(EnumStatus.OK, mandat, "Votre mandat a bien été gardé");
                    } else {
                        client.mandates().revokeMandate(customer.getCustomerId(), mandat.getId());
                        log.info("Mollie Mandate is changed, it's revoked");
                    }
                } else {
                    client.mandates().revokeMandate(customer.getCustomerId(), mandat.getId());
                    log.info("Mollie Mandate is more than 3 years, it's revoked");
                }
            }
        }

        MandateRequest mandateRequest = new MandateRequest();

        mandateRequest.setMethod("directdebit");
        mandateRequest.setConsumerName(customer.getCompany().getCompanyName());
        mandateRequest.setConsumerAccount(customerDTO.getIban().replace(" ", ""));
        mandateRequest.setConsumerBic(Optional.of(customerDTO.getBic().replace(" ", "")));
        mandateRequest.setSignatureDate(Optional.of(LocalDate.now()));
        mandateRequest.setMandateReference(Optional.of(customer.getCustomerId() + "-" + customer.getCompany().getCompanyName() + "-Mandate-BugsPointer-directdebit-" + LocalDate.now()));

        MandateResponse mandateHandler = client.mandates().createMandate(customer.getCustomerId(), mandateRequest);

        if (!mandateHandler.getStatus().equals(MandateStatus.INVALID)){
            log.info("\r -------  ");
            log.info("New Mollie Mandate : {} - {} - {}", customer.getCompany(), mandateHandler.getSignatureDate(), mandateHandler.getId());
            log.info("\r -------  ");
            // TODO Envoie de mail pour indiquer la confirmation du mandat
            return new Response(EnumStatus.OK, mandateHandler, ""); // Ne pas mettre de message car on passe directement à la méthode de subscrible
        }

        return new Response(EnumStatus.ERROR, null, "Oups, le mandat est non valide, merci de vérifier");
    }


    public Response createSubscription(Response response, CustomerDTO customerDTO) throws MollieException {

        MandateResponse mandateResponse = (MandateResponse) response.getContent();
        if (mandateResponse.getStatus()==MandateStatus.INVALID){
            return new Response(EnumStatus.ERROR, null, "Mandat invalide");
        }
        Customer customer;
        Company company;
        //On cherche la company à partir de la publicKey
        Optional<Company> companyOptional = companyRepository.findByPublicKey(customerDTO.getPublicKey());
        if (companyOptional.isPresent()){
            company = companyOptional.get();
            //Si on retrouve la company on recherche le customer
            Optional<Customer> customerOptional = customerRepository.findByCompany_CompanyId(companyOptional.get().getCompanyId());
            if (customerOptional.isPresent()) {
                customer = customerOptional.get();
            } else {
                //Si le customer n'est pas présent alors il faut le créer TODO: retourne une erreur et renvoi vers la page de création
                return new Response(EnumStatus.ERROR, null, "Une erreur est survenu");
            }
        } else {
            log.error("Company non trouvée");
            return new Response(EnumStatus.ERROR, null, "Une erreur est survenu, merci de vous connecter");
        }

        List<SubscriptionResponse> subscriptionResponses = client.subscriptions().listSubscriptions(customer.getCustomerId()).getEmbedded().getSubscriptions();
        log.info("liste subscription : {}", subscriptionResponses);
        if (!subscriptionResponses.isEmpty()){
            SubscriptionResponse subscriptionResponse = subscriptionResponses.get(0);
            if (mandateResponse.getId().equals(subscriptionResponse.getMandateId().orElse(""))) {
                log.info("statut : {}", subscriptionResponse.getStatus());
                if (subscriptionResponse.getStatus().equals(SubscriptionStatus.ACTIVE) ||
                        subscriptionResponse.getStatus().equals(SubscriptionStatus.PENDING)) {
                    if (String.valueOf(subscriptionResponse.getAmount().getValue()).equals(customerDTO.getPlan().getValeur())){
                        log.info("\r -------  ");
                        log.info("Mollie Subscription : {} - {}", customer.getCustomerId(), subscriptionResponse.getDescription());
                        log.info("\r -------  ");
                        return new Response(EnumStatus.ERROR, subscriptionResponse, "Vous avez déjà souscrit à cet abonnement");
                    } else {
                        log.info("\r -------  ");
                        log.info("Mollie Subscription : {} - {}", customer.getCustomerId(), subscriptionResponse.getDescription());
                        log.info("A modifier par : Subscribe to {} Plan", customerDTO.getPlan().name());
                        log.info("\r -------  ");
                        //TODO: Créer un DTO suscription avec les info pertinente pour le retourner
                        return new Response(EnumStatus.OK, subscriptionResponse, "Voulez-vous changer d'abonnement ?");
                    }
                }
            }
        } else {
            log.info("Pas d'abonnement d'enregistré");
        }

        String mandateId = mandateResponse.getId();

        return saveSubscription(customerDTO, customer, company, mandateId);
    }

    public Response changeSubscription(CustomerDTO customerDTO, SubscriptionResponse subscription) throws MollieException {

        log.info("suscription actual : {}", subscription);
        Customer customer;
        Company company;
        //On cherche la company à partir de la publicKey
        Optional<Company> companyOptional = companyRepository.findByPublicKey(customerDTO.getPublicKey());
        if (companyOptional.isPresent()){
            company = companyOptional.get();
            //Si on retrouve la company on recherche le customer
            Optional<Customer> customerOptional = customerRepository.findByCompany_CompanyId(companyOptional.get().getCompanyId());
            if (customerOptional.isPresent()) {
                customer = customerOptional.get();
            } else {
                //Si le customer n'est pas présent alors il faut le créer TODO: retourne une erreur et renvoi vers la page de création
                return new Response(EnumStatus.ERROR, null, "Une erreur est survenu");
            }
        } else {
            log.error("Company non trouvée");
            return new Response(EnumStatus.ERROR, null, "Une erreur est survenu, merci de vous connecter");
        }

        client.subscriptions().cancelSubscription(customer.getCustomerId(), subscription.getId());

        String mandateId = String.valueOf(subscription.getMandateId());

        return saveSubscription(customerDTO, customer, company, mandateId);
    }

    private Response saveSubscription(CustomerDTO customerDTO, Customer customer, Company company, String mandateId) throws MollieException {

        SubscriptionRequest subscriptionRequest = new SubscriptionRequest();

        subscriptionRequest.setAmount(new Amount("EUR", new BigDecimal(customerDTO.getPlan().getValeur())));
        subscriptionRequest.setDescription("Subscribe to " + customerDTO.getPlan().name() + " Plan");
        subscriptionRequest.setTimes(Optional.of(4));
        subscriptionRequest.setInterval("12 months");
        subscriptionRequest.setMandateId(Optional.ofNullable(mandateId));

        SubscriptionResponse subscriptionResponse = client.subscriptions().createSubscription(customer.getCustomerId(), subscriptionRequest);

        if(subscriptionResponse.getStatus() == SubscriptionStatus.PENDING || subscriptionResponse.getStatus() == SubscriptionStatus.ACTIVE){
            log.info("\r -------  ");
            log.info("New Mollie Subscription : {} - {}", customer.getCustomerId(), subscriptionRequest.getDescription());
            log.info("\r -------  ");
            // TODO Envoie de mail pour confirmer la souscription

            //Modification du plan de la company
            LocalDate dateLineFacture = subscriptionResponse.getStartDate().plusYears(1);
            Date dateLine = Date.from(dateLineFacture.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Response responseUpdate = companyService.updatePlan(company, dateLine, customerDTO.getPlan());

            if (responseUpdate.getStatus().equals(EnumStatus.ERROR)){
                return new Response(EnumStatus.ERROR, responseUpdate, "Une erreur est survenu, merci de nous contacter à l'adresse mail ci-dessous");
            }

            return new Response(EnumStatus.OK, null, "La souscription à "+ customerDTO.getPlan().name() +" Plan est validé");
        }

        return new Response(EnumStatus.ERROR, null, "Erreur lors de la souscription");
    }

}


















