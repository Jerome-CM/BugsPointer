package com.bugspointer.service.implementation;

import be.woutschoovaerts.mollie.Client;
import be.woutschoovaerts.mollie.ClientBuilder;
import be.woutschoovaerts.mollie.data.common.Amount;
import be.woutschoovaerts.mollie.data.customer.CustomerRequest;
import be.woutschoovaerts.mollie.data.customer.CustomerResponse;
import be.woutschoovaerts.mollie.data.payment.PaymentLinks;
import be.woutschoovaerts.mollie.data.payment.PaymentRequest;
import be.woutschoovaerts.mollie.data.payment.PaymentResponse;
import be.woutschoovaerts.mollie.data.payment.SequenceType;
import be.woutschoovaerts.mollie.exception.MollieException;
import be.woutschoovaerts.mollie.handler.CustomerHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

import static be.woutschoovaerts.mollie.data.common.Locale.fr_FR;

@Service
@Slf4j
public class PaymentService {

    Client client = new ClientBuilder()
            .withApiKey("test_KJ6cxKC9yMJGKqfwbAepHHatx6knug") // Polygon
            .build();


    public void paymentTest() throws MollieException, IOException {

        // Create customer
        CustomerRequest customer = new CustomerRequest();

        customer.setEmail(Optional.of("test@test.com"));
        customer.setName(Optional.of("My Company"));
        customer.setLocale(Optional.of(fr_FR));
        CustomerResponse customerHandler = client.customers().createCustomer(customer);
        // TODO Save customer in DB

        // Create mandate


        // Create reccurring payment
        PaymentRequest request = new PaymentRequest();
        request.setAmount(new Amount("EUR", new BigDecimal("20.00")));
        request.setDescription("Subscribe to Target Plan, Order #12345");
        request.setRedirectUrl(Optional.of("localhost:8080/"));
        request.setBillingEmail(Optional.of("test@test.fr"));
        request.setConsumerName(Optional.of("My Company"));
        request.setLocale(Optional.of(fr_FR));




        PaymentResponse paymentResponse = client.payments().createPayment(request);

        String checkoutUrl = paymentResponse.getLinks().getCheckout().getHref();
        // Redirection vers l'URL de paiement
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        if (response != null) {
            response.sendRedirect(checkoutUrl);
        } else {
            // Gérer l'absence de réponse HTTP appropriée
        }
    }

}
