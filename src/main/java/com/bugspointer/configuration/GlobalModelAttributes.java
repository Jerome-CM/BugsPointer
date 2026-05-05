package com.bugspointer.configuration;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final UserAuthenticationUtil userAuthenticationUtil;

    public GlobalModelAttributes(UserAuthenticationUtil userAuthenticationUtil) {
        this.userAuthenticationUtil = userAuthenticationUtil;
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin() {
        return userAuthenticationUtil.isAdmin();
    }
}
