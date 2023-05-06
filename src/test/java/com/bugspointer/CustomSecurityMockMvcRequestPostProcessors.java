package com.bugspointer;

import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

public class CustomSecurityMockMvcRequestPostProcessors {

    public static RequestPostProcessor adminValue() {
        return user("community-technologie@gmail.com").password("test").roles("ADMIN");
    }

    public static RequestPostProcessor userValue() {
        return user("simplecompagny@test.com").password("azerty").roles("USER");
    }
}
