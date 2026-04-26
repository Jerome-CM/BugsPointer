package com.bugspointer.configuration;

import be.woutschoovaerts.mollie.Client;
import be.woutschoovaerts.mollie.ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MollieConfiguration {

    @Bean
    public Client mollieClient(@Value("${mollie.key}") String mollieApiKey) {
        return new ClientBuilder()
                .withApiKey(mollieApiKey)
                .build();
    }
}
