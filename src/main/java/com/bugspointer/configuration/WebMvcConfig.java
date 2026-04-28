package com.bugspointer.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final DomainRequiredInterceptor domainRequiredInterceptor;

    public WebMvcConfig(DomainRequiredInterceptor domainRequiredInterceptor) {
        this.domainRequiredInterceptor = domainRequiredInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(domainRequiredInterceptor)
                .addPathPatterns("/app/private/**")
                .excludePathPatterns("/app/private/thanks");
    }
}
