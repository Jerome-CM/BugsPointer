package com.bugspointer.configuration;

import javax.servlet.http.HttpServletRequest;

public final class LoginRedirectUtil {

    private static final String DEFAULT_REDIRECT = "/app/private/dashboard";

    private LoginRedirectUtil() {
    }

    public static boolean isSafeRedirect(String redirect) {
        if (redirect == null) {
            return false;
        }
        String value = redirect.trim();
        if (value.isEmpty() || !value.startsWith("/") || value.startsWith("//")) {
            return false;
        }
        return !value.equals("/logout")
                && !value.startsWith("/logout?")
                && !value.equals("/login")
                && !value.startsWith("/login?")
                && !value.equals("/authentication")
                && !value.startsWith("/authentication?")
                && !value.equals("/favicon.svg")
                && !value.equals("/favicon.ico")
                && !value.equals("/app/private/widget-installation-scan")
                && !value.startsWith("/app/private/widget-installation-scan?")
                && !value.startsWith("/oauth2/")
                && !value.startsWith("/login/oauth2/");
    }

    public static String getSafeRedirectPath(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return DEFAULT_REDIRECT;
        }

        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String redirect = query == null || query.trim().isEmpty() ? uri : uri + "?" + query;
        return isSafeRedirect(redirect) ? redirect : DEFAULT_REDIRECT;
    }
}
