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
import com.bugspointer.dto.*;
import com.bugspointer.entity.Company;
import com.bugspointer.entity.Customer;
import com.bugspointer.entity.EnumPlan;
import com.bugspointer.entity.enumLogger.Action;
import com.bugspointer.entity.enumLogger.Adjective;
import com.bugspointer.entity.enumLogger.Raison;
import com.bugspointer.entity.enumLogger.What;
import com.bugspointer.repository.CompanyRepository;
import com.bugspointer.repository.CustomerRepository;
import com.bugspointer.utility.Utility;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
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

    private final MailService mailService;

    private final CustomerRepository customerRepository;

    private final ModelMapper modelMapper;


    public PaymentService(CompanyRepository companyRepository, CompanyService companyService, MailService mailService, CustomerRepository customerRepository, ModelMapper modelMapper) {
        this.companyRepository = companyRepository;
        this.companyService = companyService;
        this.mailService = mailService;
        this.customerRepository = customerRepository;
        this.modelMapper = modelMapper;
    }

    Client client = new ClientBuilder()
            .withApiKey("test_upURPW9vMxSv3M5MzEEC6c2yywuKwe")
            .build();

    public Response createNewCustomer(CustomerDTO customerDTO) throws MollieException {

        Optional<Company> companyOptional = companyRepository.findByPublicKey(customerDTO.getPublicKey());

        if (!customerDTO.isCguAccepted()) {
            return new Response(EnumStatus.ERROR, null, "Veuillez accepter les CGU pour procéder au paiement");
        }
        if (customerDTO.getMail().trim().isEmpty() || customerDTO.getCompanyName().trim().isEmpty() || customerDTO.getAddress1().trim().isEmpty() || customerDTO.getCp().trim().isEmpty() || customerDTO.getCity().trim().isEmpty() || customerDTO.getCountry().trim().isEmpty()) {
            return new Response(EnumStatus.ERROR, null, "Veuillez compléter les champs obligatoires");
        }

        if(customerDTO.getPlan().equals(companyOptional.get().getPlan())){
            return new Response(EnumStatus.ERROR, null, "Vous avez déjà cette offre");
        }


        Company company;
        Customer customerBugspointer = null;
        if (companyOptional.isPresent()) {
            company = companyOptional.get();
            Optional<Customer> customerOptional = customerRepository.findByCompany_CompanyId(company.getCompanyId());
            if (customerOptional.isPresent()) {
                customerBugspointer = customerOptional.get();
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

        Map<String, Object> meta = new HashMap<>();
        meta.put("address1", customerDTO.getAddress1());
        meta.put("address2", customerDTO.getAddress2());
        meta.put("cp", customerDTO.getCp());
        meta.put("city", customerDTO.getCity());
        meta.put("country", customerDTO.getCountry());

        customer.setMetadata(meta);

        //On vérifie que le client existe
        if (customerBugspointer != null) {

            //On récupère le customer de chez Mollie
            CustomerResponse customerResponse = client.customers().getCustomer(company.getCustomer().getCustomerId());

            //On modifie le customer avec les données saisies par le client
            CustomerResponse customerHandler = client.customers().updateCustomer(customerResponse.getId(), customer);

            log.info("\r -------  ");
            log.info("Mollie customer : {} - {} - {} - {}", customerHandler.getName(), customerHandler.getEmail(), customerHandler.getId(), customerHandler.getMetadata());
            log.info("\r -------  ");

            log.info("\r -------  ");
            log.info("Bugspointer Customer : {} - {}", customerBugspointer.getId(), customerBugspointer.getCustomerId());
            log.info("\r -------  ");

            //Sinon vérifie la présence d'un mandat valide
            List<MandateResponse> mandates = client.mandates().listMandates(customerBugspointer.getCustomerId()).getEmbedded().getMandates();
            log.info("liste mandates : {}", mandates);
            if (!mandates.isEmpty()) {
                MandateResponse mandat = mandates.get(0);
                log.info("\r -------  ");
                log.info("Mollie Mandate : {} - {} - {}", customerBugspointer.getCompany(), mandat.getSignatureDate(), mandat.getId());
                log.info("\r -------  ");
                //Si le mandat a été signé il y a moins de 4 ans
                if (mandat.getStatus() != MandateStatus.INVALID && mandat.getSignatureDate().isAfter(LocalDate.now().minusYears(4))) {
                    customerDTO.setIban(mandat.getDetails().getConsumerAccount().orElse(""));
                    customerDTO.setBic(mandat.getDetails().getConsumerBic().orElse(""));
                    if (mandat.getSignatureDate() != null) {
                        customerDTO.setSignature(true);
                    }
                    return new Response(EnumStatus.OK, customerDTO, "Un mandat est déjà présent. Vous pouvez le valider ou modifier vos coordonnées bancaire pour créer un nouveau mandat");
                }
            }
            log.info("Pas de mandat enregistré");
            return new Response(EnumStatus.OK, customerBugspointer, null);

        } else {

            //On crée le customer chez Mollie
            CustomerResponse customerHandler = client.customers().createCustomer(customer);

            //Si on récupère l'Id du customer de chez mollie on crée le customer chez nous
            if (customerHandler.getId() != null) {
                log.info("\r -------  ");
                log.info("New Mollie customer : {} - {} - {}", customerHandler.getName(), customerHandler.getEmail(), customerHandler.getId());
                log.info("\r -------  ");
                customerBugspointer = new Customer();

                customerBugspointer.setCustomerId(customerHandler.getId());
                /*customerBugspointer.setCompany(companyRepository.findByCompanyName(customerDTO.getCompanyName()).get());*/
                customerBugspointer.setCompany(company);

                try {
                    Customer returnCustomerSaved = customerRepository.save(customerBugspointer);
                    company.setCustomer(returnCustomerSaved);
                    Company companySaveCustomer = companyRepository.save(company);
                    log.info("\r -------  ");
                    log.info("New Bugspointer Customer : {} - {}", returnCustomerSaved.getId(), returnCustomerSaved.getCustomerId());
                    log.info("\r -------  ");
                    log.info("company get customer : {}", companySaveCustomer.getCustomer());
                    Utility.saveLog(companySaveCustomer.getCompanyId(), Action.SAVE, What.CUSTOMER, companySaveCustomer.getCustomer().getCustomerId(), null, null);
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

        if (!customerDTO.isSignature()) {
            return new Response(EnumStatus.ERROR, customerDTO, "Veuillez signer le mandat pour valider votre abonnement");
        }
        if (customerDTO.getIban().trim().isEmpty() || customerDTO.getBic().trim().isEmpty()) {
            return new Response(EnumStatus.ERROR, customerDTO, "Veuillez compléter les champs obligatoires");
        }

        Customer customer;
        //On cherche la company à partir de la publicKey
        Optional<Company> companyOptional = companyRepository.findByPublicKey(customerDTO.getPublicKey());
        if (companyOptional.isPresent()) {
            //Si on retrouve la company on recherche le customer
            Optional<Customer> customerOptional = customerRepository.findByCompany_CompanyId(companyOptional.get().getCompanyId());
            if (customerOptional.isPresent()) {
                customer = customerOptional.get();
            } else {
                //Si le customer n'est pas présent alors on arrête l'execution TODO Besoin de renvoyer vers la page recapPayment ??
                return new Response(EnumStatus.ERROR, null, "Vous n'avez pas de profil client chez notre prestataire de paiement");
            }
        } else {
            log.error("Company {} non trouvée", customerDTO.getPublicKey());
            return new Response(EnumStatus.ERROR, null, "Une erreur est survenue, merci de vous connecter");
        }

        //On récupère la liste des mandats
        List<MandateResponse> mandates = client.mandates().listMandates(customer.getCustomerId()).getEmbedded().getMandates();
        log.info("liste mandates : {}", mandates); //TODO delete after debugg
        if (!mandates.isEmpty()) {
            //On récupère le plus récent
            MandateResponse mandat = mandates.get(0);
            log.info("\r -------  ");
            log.info("Mollie Mandate found : {} - {} - {}", customer.getCompany(), mandat.getSignatureDate(), mandat.getId());
            log.info("\r -------  ");
            if (mandat.getStatus() != MandateStatus.INVALID) {
                // Si la date de signature est de moins de 4 an par rapport à maintenant
                if (mandat.getSignatureDate().isBefore(LocalDate.now().minusYears(4))) {
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

        try {
            MandateResponse mandateResponse = client.mandates().createMandate(customer.getCustomerId(), mandateRequest);

            if (!mandateResponse.getStatus().equals(MandateStatus.INVALID)) {
                log.info("\r -------  ");
                log.info("New Mollie Mandate : Company #{} - {} - {}", customer.getCompany().getCompanyId(), mandateResponse.getSignatureDate(), mandateResponse.getId());
                log.info("\r -------  ");

                mailService.sendMailNewMandate(customerDTO);
                Utility.saveLog(customer.getCompany().getCompanyId(), Action.CREATE, What.MANDATE, mandateResponse.getId() + ",signature date at " + mandateResponse.getSignatureDate(), null, null);
                return new Response(EnumStatus.OK, mandateResponse, ""); // Ne pas mettre de message, car on passe directement à la méthode de Subscription
            }
        } catch (MollieException ignored) {

        }

        return new Response(EnumStatus.ERROR, null, "Oups, le mandat est non valide, merci de vérifier");
    }


    public Response createSubscription(Response response, CustomerDTO customerDTO) throws MollieException {

        log.info("Start create new subscribe : Response = {}", response);
        if (response.getStatus() == EnumStatus.ERROR) {
            return new Response(EnumStatus.ERROR, null, "Aucun mandat trouvable pour faire la souscription");
        }

        MandateResponse mandateResponse = (MandateResponse) response.getContent();
        log.info("mandateResponse after cast => {}", mandateResponse);

        if (mandateResponse.getStatus() == MandateStatus.INVALID) {
            return new Response(EnumStatus.ERROR, null, "Mandat invalide");
        }

        Customer customer;
        Company company;
        //On cherche la company à partir de la publicKey
        Optional<Company> companyOptional = companyRepository.findByPublicKey(customerDTO.getPublicKey());
        if (companyOptional.isPresent()) {
            company = companyOptional.get();
            //Si on retrouve la company on recherche le customer
            Optional<Customer> customerOptional = customerRepository.findByCompany_CompanyId(companyOptional.get().getCompanyId());
            if (customerOptional.isPresent()) {
                customer = customerOptional.get();

                if(customerDTO.getPlan().equals(company.getPlan())){
                    return new Response(EnumStatus.ERROR, null, "Vous possédez déjà cette offre, pas besoin de vous ré-abonnez");
                }
            } else {
                //Si le customer n'est pas présent alors il faut le créer TODO: retourne une erreur et renvoi vers la page de création
                return new Response(EnumStatus.ERROR, null, "Une erreur est survenue");
            }
        } else {
            log.error("Company non trouvée");
            return new Response(EnumStatus.ERROR, null, "Une erreur est survenu, merci de vous connecter");
        }

        List<SubscriptionResponse> subscriptionResponses = client.subscriptions().listSubscriptions(customer.getCustomerId()).getEmbedded().getSubscriptions();
        log.info("subscription exist for customer {} : {}",customer.getCustomerId(), subscriptionResponses);
        if (!subscriptionResponses.isEmpty()) {
            SubscriptionResponse subscriptionResponse = subscriptionResponses.get(0);
            if (mandateResponse.getId().equals(subscriptionResponse.getMandateId().orElse(""))) {
                log.info("Mandate statut : {}", subscriptionResponse.getStatus());
                if (subscriptionResponse.getStatus().equals(SubscriptionStatus.ACTIVE) ||
                        subscriptionResponse.getStatus().equals(SubscriptionStatus.PENDING)) {
                    if (String.valueOf(subscriptionResponse.getAmount().getValue()).equals(customerDTO.getPlan().getValeur())) {
                        log.info("\r -------  ");
                        log.info("Mollie Subscription : {} - {}", customer.getCustomerId(), subscriptionResponse.getDescription());
                        log.info("\r -------  ");
                        SubscriptionDTO dto = getSubscriptionDTO(subscriptionResponse);
                        dto.setNewPlan(customerDTO.getPlan());
                        dto.setPublicKey(customerDTO.getPublicKey());
                        return new Response(EnumStatus.ERROR, dto, "Vous avez déjà souscrit à cet abonnement");
                    } else {
                        log.info("\r -------  ");
                        log.info("Mollie Subscription : {} - {}", customer.getCustomerId(), subscriptionResponse.getDescription());
                        log.info("A modifier par : Subscribe to {} Plan", customerDTO.getPlan().name());
                        log.info("\r -------  ");
                        SubscriptionDTO dto = getSubscriptionDTO(subscriptionResponse);
                        dto.setNewPlan(customerDTO.getPlan());
                        dto.setPublicKey(customerDTO.getPublicKey());
                        return new Response(EnumStatus.OK, dto, "Voulez-vous changer d'abonnement ?");
                    }
                }
            }
        } else {
            log.info("This customer does not have a subscription yet");
        }

        return saveSubscription(customerDTO.getPlan(), customer, company, mandateResponse.getId());
    }

    public Response changeSubscription(SubscriptionDTO subscription) throws MollieException {

        log.info("Change sub. Actual : {}", subscription);
        Customer customer;
        Company company;
        //On cherche la company à partir de la publicKey
        Optional<Company> companyOptional = companyRepository.findByPublicKey(subscription.getPublicKey());
        if (companyOptional.isPresent()) {
            company = companyOptional.get();
            //Si on retrouve la company on recherche le customer
            Optional<Customer> customerOptional = customerRepository.findByCompany_CompanyId(companyOptional.get().getCompanyId());
            if (customerOptional.isPresent()) {
                customer = customerOptional.get();
            } else {
                //Si le customer n'est pas présent alors il faut le créer TODO: retourne une erreur et renvoi vers la page de création
                return new Response(EnumStatus.ERROR, null, "Une erreur est survenue");
            }
        } else {
            log.error("Company non trouvée");
            return new Response(EnumStatus.ERROR, null, "Une erreur est survenu, merci de vous connecter");
        }

        client.subscriptions().cancelSubscription(customer.getCustomerId(), subscription.getId());

        String mandateId = subscription.getMandateId();
        EnumPlan plan = subscription.getNewPlan();

        return saveSubscription(plan, customer, company, mandateId);
    }

    private Response saveSubscription(EnumPlan plan, Customer customer, Company company, String mandateId) throws MollieException {

        SubscriptionRequest subscriptionRequest = new SubscriptionRequest();

        subscriptionRequest.setAmount(new Amount("EUR", new BigDecimal(plan.getValeur())));
        subscriptionRequest.setDescription("Subscribe to " + plan.name() + " Plan");
        subscriptionRequest.setTimes(Optional.of(4));
        subscriptionRequest.setInterval("12 months");
        subscriptionRequest.setMandateId(Optional.ofNullable(mandateId));

        SubscriptionResponse subscriptionResponse = client.subscriptions().createSubscription(customer.getCustomerId(), subscriptionRequest);

        if (subscriptionResponse.getStatus() == SubscriptionStatus.PENDING || subscriptionResponse.getStatus() == SubscriptionStatus.ACTIVE) {
            log.info("\r -------  ");
            log.info("New Mollie Subscription : {} - {}", customer.getCustomerId(), subscriptionRequest.getDescription());
            log.info("\r -------  ");

            mailService.sendMailChangePlan(plan, company.getMail());

            //Modification du plan de la company
            LocalDate dateLineFacture = subscriptionResponse.getStartDate().plusYears(1);
            Date dateLine = Date.from(dateLineFacture.atStartOfDay(ZoneId.systemDefault()).toInstant());

            customer.setDateStartSubscribe(String.valueOf(subscriptionResponse.getStartDate()));
            customer.setPlan(plan);
            customerRepository.save(customer);
            Response responseUpdate = companyService.updatePlan(company, dateLine, plan);

            if (responseUpdate.getStatus().equals(EnumStatus.ERROR)) {
                return new Response(EnumStatus.ERROR, responseUpdate, "Une erreur est survenu, merci de nous contacter à l'adresse mail ci-dessous");
            }

            Utility.saveLog(customer.getCompany().getCompanyId(), Action.UPDATE, What.SUBSCRIPTION, subscriptionResponse.getId(), null, null);
            return new Response(EnumStatus.OK, null, "La souscription à " + plan.name() + " Plan est validé");
        }

        return new Response(EnumStatus.ERROR, null, "Erreur lors de la souscription");
    }

    public SubscriptionDTO getSubscriptionDTO(SubscriptionResponse subscriptionResponse) {
        log.info("SubscriptionDTO - response : {}", subscriptionResponse);
        SubscriptionDTO dto;
        dto = modelMapper.map(subscriptionResponse, SubscriptionDTO.class);
        log.info("SubscriptionDTO - dto : {}", dto);

        return dto;
    }

    public Response deleteSubscription(String customerId, String mandateId) throws MollieException {
        log.warn("in deleteSub method");
        SubscriptionResponse subscriptionResponse = client.subscriptions().cancelSubscription(customerId, mandateId);

        Customer customer = customerRepository.findByCustomerId(customerId).get();

        if(subscriptionResponse.getStatus().equals(SubscriptionStatus.CANCELED)){
            log.info("Customer #{} delete subscription", customerId);
            Utility.saveLog(customer.getCompany().getCompanyId(), Action.DELETE, What.SUBSCRIPTION, subscriptionResponse.getId(),null, null);
            return new Response(EnumStatus.OK, null, "");
        }
        log.error("Customer #{} can't delete subscription", customerId);
        return new Response(EnumStatus.ERROR, null, "Subscription none cancelled");
    }

    public Response getMandate(CustomerDTO customer) throws MollieException {
        Company company = companyRepository.findByPublicKey(customer.getPublicKey()).get();

        if(company != null) {
            log.warn("getMandate - company finded ; {}", company);


            List<MandateResponse> mandates = client.mandates().listMandates(company.getCustomer().getCustomerId()).getEmbedded().getMandates();
log.warn("mandates brute before stream : {}", mandates);


            log.warn("DTO : iban : {} ; bic : {}", customer.getIban(), customer.getBic());
            log.warn("MOLLIE iban : {} ; bic : {}", mandates.get(0).getDetails().getConsumerAccount().get(), mandates.get(0).getDetails().getConsumerBic().get());

            if(mandates.size() > 0){
                MandateResponse foundMandate = null;
                for(MandateResponse mandate: mandates){
                    if(mandate.getDetails().getConsumerAccount().get().equals(customer.getIban())
                            && mandate.getDetails().getConsumerBic().get().equals(customer.getBic())){
                        foundMandate = mandate;
                    }
                };

                log.warn("foundMandate after first loop : {}", foundMandate);
                if(foundMandate == null){
                    for(MandateResponse mandate: mandates){
                        if(mandate.getStatus().equals(MandateStatus.VALID)){
                            foundMandate = mandate;
                        }
                    };
                }

            /*Optional<MandateResponse> foundMandate = mandates.stream()
                    .filter(mandate ->
                            Objects.equals(mandate.getDetails().getConsumerAccount().get(), customer.getIban())
                                    && Objects.equals(mandate.getDetails().getConsumerBic().get(), customer.getBic()))
                    .findFirst();*/

                log.info(" fond mandates : {}", foundMandate);
                if(foundMandate != null){
                    return new Response(EnumStatus.OK, foundMandate, null);
                } else {
                    return new Response(EnumStatus.ERROR, null, "mandate not found");
                }
            }


        }
        return new Response(EnumStatus.ERROR, null, "Company not found");
    }

    public Response returnFreePlan(CustomerDTO customerDTO) throws MollieException {
        log.warn("in returnToFree");
        // Si le client a un premium et reviens sur un free, on le change et on met à jour
        Optional<Company> companyOptional = companyRepository.findByPublicKey(customerDTO.getPublicKey());
log.warn("returnToFree - companyOptional : {}", companyOptional);

        if(companyOptional.isPresent()){
            Company company = companyOptional.get();
            Optional<Customer> customerOptional = customerRepository.findByCompany_CompanyId(company.getCompanyId());
            log.warn("returnToFree - customerOptional : {}", customerOptional);
            if(customerOptional.isPresent()){
                // change customer informations
                Customer custo = customerOptional.get();
                custo.setPlan(customerDTO.getPlan());
                custo.setDateStartSubscribe(null);

                // Change company informations
                company.setPlan(EnumPlan.FREE);

                // Recupère le dernier mandat qui correspond à l'iban et au bic du customer
                Response response = getMandate(customerDTO);
                log.warn("returnToFree - response getMandate : {}", response);
                MandateResponse mandat = (MandateResponse) response.getContent();
                log.warn("returnToFree - mandat : {}", mandat);
                try{
                    companyRepository.save(company);
                    customerRepository.save(custo);
                    deleteSubscription(custo.getCustomerId(), mandat.getId());
                    log.info("Company #{} return to Free plan", company.getCompanyId());
                    Utility.saveLog(company.getCompanyId(), Action.DELETE, What.PLAN, null, Adjective.TO, Raison.FREE);
                    return new Response(EnumStatus.OK, null, "Vous êtes maintenant avec l'offre gratuite");
                } catch (Exception e){
                    log.error("Company #{} : Impossible return to Free plan : {}", company.getCompanyId(), e.getMessage());
                    return new Response(EnumStatus.ERROR, null, "Erreur pour revenir sur un compte gratuit; Contactez-nous à contact@bugspointer.com");
                }
            }
        }
        return null;
    }
}

















