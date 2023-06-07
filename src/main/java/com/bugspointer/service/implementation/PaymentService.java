package com.bugspointer.service.implementation;

import be.woutschoovaerts.mollie.Client;
import be.woutschoovaerts.mollie.ClientBuilder;
import be.woutschoovaerts.mollie.data.common.Amount;
import be.woutschoovaerts.mollie.data.customer.CustomerRequest;
import be.woutschoovaerts.mollie.data.customer.CustomerResponse;
import be.woutschoovaerts.mollie.data.payment.PaymentRequest;
import be.woutschoovaerts.mollie.data.payment.PaymentResponse;
import be.woutschoovaerts.mollie.data.payment.SequenceType;
import be.woutschoovaerts.mollie.exception.MollieException;
import com.bugspointer.dto.CustomerDTO;
import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.Response;
import com.bugspointer.entity.Customer;
import com.bugspointer.repository.CompanyRepository;
import com.bugspointer.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
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
        // Create customer
        CustomerRequest customer = new CustomerRequest();

        customer.setEmail(Optional.of(customerDTO.getMail()));
        customer.setName(Optional.of(customerDTO.getCompanyName()));
        customer.setLocale(Optional.of(fr_FR)); //TODO gérer dynamiquement le lieu de residence

        Map<String, Object> meta = new HashMap<>();
        meta.put("address1", customerDTO.getAddress1());
        meta.put("address2", customerDTO.getAddress2());
        meta.put("cp", customerDTO.getCp());
        meta.put("city", customerDTO.getCity());
        meta.put("country", customerDTO.getCountry());

        customer.setMetadata(meta);
        CustomerResponse customerHandler = client.customers().createCustomer(customer);

        if(customerHandler.getId() != null){
            Customer customerBugspointer = new Customer();

            customerBugspointer.setCustomerId(customerHandler.getId());
            customerBugspointer.setCompany(companyRepository.findByCompanyName(customerDTO.getCompanyName()).get());

            try{
                Customer returnCustomerSaved = customerRepository.save(customerBugspointer);
                log.info("Customer save with success : {}", returnCustomerSaved);
                return new Response(EnumStatus.OK, customerBugspointer, null);
            } catch (Exception e){
                log.error("Impossible to save a new customer : {}", e.getMessage());
                return new Response(EnumStatus.ERROR, null, null);
            }

        }

        return new Response(EnumStatus.ERROR, null, null);

    }

    public Response createFirstPayment(Response response) throws MollieException, IOException {

        Customer customer = (Customer) response.getContent();

        PaymentRequest paymentRequest = new PaymentRequest();

        paymentRequest.setAmount(new Amount("EUR", new BigDecimal("15.00")));
        paymentRequest.setDescription("Subscribe to Target Plan, Order #12345");
        paymentRequest.setRedirectUrl(Optional.of("http://bugspointer.com/thanks"));
        paymentRequest.setSequenceType(Optional.of(SequenceType.FIRST));
        paymentRequest.setCustomerId(Optional.ofNullable(customer.getCustomerId()));

        PaymentResponse paymentResponse = client.payments().createPayment(paymentRequest);

        if(paymentResponse.getLinks().getCheckout().getHref() != null){

            responseHttp.sendRedirect(paymentResponse.getLinks().getCheckout().getHref());

            // TODO redirect user to checkout page with paymentResponse.getLinks().getCheckout().getHref()
            // Pour simplifier la chose, il n'y aura pas de carte de crédit, seulement le virement / prelevement SEPA
            // Tu n'as pas forcement besoin de mon interface Mollie, car dans les logs tous est correctement noté suivant le succès ou l'erreur de la transaction
        }

// Il faut un retour ici mais je n'ai pas eu le temps de réflechir auquel
    }


    // Pour définir un paiement récurrent,
    // s'assurer que le client à un mandat valide et garder son id => Mandate API / list mandate : https://docs.mollie.com/reference/v2/mandates-api/list-mandates
    // si mandat valide; configurer paiement récurrent.

    //S'assurer que le client ne soit pas facturer 2 fois le même premier mois





    //Ancien code pour tester
    /*SubscriptionRequest reccuring = new SubscriptionRequest();
        reccuring.setAmount(new Amount("EUR", new BigDecimal("15.00")));
        reccuring.setDescription("Subscribe to Target Plan, Order #12345");
        reccuring.setTimes(Optional.of(2));
        reccuring.setInterval("12 months");
        reccuring.setMandateId(Optional.ofNullable(mandateHandler.getId()));

    SubscriptionResponse subResponse = client.subscriptions().createSubscription(customerHandler.getId(),reccuring);*/



    public void paymentTest(HttpServletResponse response) throws MollieException, IOException {

        /*// Create customer
        CustomerRequest customer = new CustomerRequest();

        customer.setEmail(Optional.of("test@test.com"));
        customer.setName(Optional.of("My Company"));
        customer.setLocale(Optional.of(fr_FR));
        CustomerResponse customerHandler = client.customers().createCustomer(customer);
        // TODO Save customer in DB



        // Create mandate
        MandateRequest mandate = new MandateRequest();

        mandate.setMethod("directdebit");
        mandate.setConsumerName(customerHandler.getName());
        mandate.setConsumerAccount("FR7617418000010000052248408");
        mandate.setConsumerBic(Optional.of("SNNNFR22XXX"));
        mandate.setSignatureDate(Optional.of(LocalDate.parse("2023-06-06")));
        mandate.setMandateReference(Optional.of("BugsPointerMandate-2023-06-06-cst_V4fQbSFEn7"));
        MandateResponse mandateHandler = client.mandates().createMandate(customerHandler.getId(), mandate);

        // Create reccurring payment
        SubscriptionRequest reccuring = new SubscriptionRequest();
        reccuring.setAmount(new Amount("EUR", new BigDecimal("15.00")));
        reccuring.setDescription("Subscribe to Target Plan, Order #12345");
        reccuring.setTimes(Optional.of(2));
        reccuring.setInterval("12 months");
        reccuring.setMandateId(Optional.ofNullable(mandateHandler.getId()));

        SubscriptionResponse subResponse = client.subscriptions().createSubscription(customerHandler.getId(),reccuring);

        PaymentRequest request = new PaymentRequest();
        request.setAmount(new Amount("EUR", new BigDecimal("20.00")));
        request.setDescription("Subscribe to Target Plan, Order #12345");
        request.setRedirectUrl(Optional.of("http://bugspointer.com/thanks"));
        request.setBillingEmail(Optional.of("test@test.fr"));
        request.setConsumerName(Optional.of("My Company"));
        request.setLocale(Optional.of(fr_FR));

        PaymentResponse paymentResponse = client.payments().createPayment(request);

        log.info("{}",paymentResponse);

        String checkoutUrl = paymentResponse.getLinks().getCheckout().getHref();
        // Redirection vers l'URL de paiement
        //HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();

        response.sendRedirect(checkoutUrl); */
    }

















}
