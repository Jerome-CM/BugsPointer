package com.bugspointer.configuration;

import com.bugspointer.entity.Company;
import com.bugspointer.jwtConfig.JwtTokenUtil;
import com.bugspointer.service.implementation.CompanyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;

@Component
@Slf4j
public class CustomOAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final CompanyService companyService;
    private final JwtTokenUtil jwtTokenUtil;

    public CustomOAuth2AuthenticationSuccessHandler(CompanyService companyService, JwtTokenUtil jwtTokenUtil) {
        this.companyService = companyService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            response.sendRedirect("/authentication?status=ERROR&message=Connexion%20OAuth%20impossible");
            return;
        }

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User user = oauthToken.getPrincipal();
        String provider = oauthToken.getAuthorizedClientRegistrationId();
        String email = extractEmail(user.getAttributes());
        String displayName = extractDisplayName(user.getAttributes(), email);

        if (email == null || email.trim().isEmpty()) {
            String message = "Connexion " + provider + " impossible: aucun e-mail vérifié n'a été transmis.";
            response.sendRedirect("/authentication?status=ERROR&message=" + URLEncoder.encode(message, "UTF-8"));
            return;
        }

        Company company;
        try {
            company = companyService.getOrCreateOAuthCompany(email, displayName, provider);
        } catch (IllegalStateException e) {
            response.sendRedirect(errorRedirect("Compte désactivé"));
            return;
        } catch (Exception e) {
            log.error("OAuth login failed for provider {} and email {}", provider, email, e);
            response.sendRedirect("/authentication?status=ERROR&message=Connexion%20OAuth%20impossible");
            return;
        }

        HttpSession session = request.getSession();
        try {
            session.setAttribute("token", jwtTokenUtil.createAuthenticationToken(company.getMail(), authentication));
            session.setAttribute("mail", company.getMail());
        } catch (Exception e) {
            log.error("Unable to create OAuth session token for {}", company.getMail(), e);
            response.sendRedirect("/authentication?status=ERROR&message=Connexion%20OAuth%20impossible");
            return;
        }

        redirectAfterLogin(request, response, session, company);
    }

    private String extractEmail(Map<String, Object> attributes) {
        Object email = attributes.get("email");
        if (email == null) {
            return null;
        }
        String value = String.valueOf(email).trim();
        return value.isEmpty() ? null : value.toLowerCase();
    }

    private String extractDisplayName(Map<String, Object> attributes, String email) {
        Object name = attributes.get("name");
        if (name == null) {
            name = attributes.get("login");
        }
        if (name == null) {
            return email;
        }
        String value = String.valueOf(name).trim();
        return value.isEmpty() ? email : value;
    }

    private void redirectAfterLogin(HttpServletRequest request, HttpServletResponse response, HttpSession session, Company company) throws IOException {
        Object redirect = session.getAttribute("redirectAfterLogin");
        if (redirect != null && LoginRedirectUtil.isSafeRedirect(String.valueOf(redirect))) {
            session.removeAttribute("redirectAfterLogin");
            response.sendRedirect(String.valueOf(redirect));
        } else if (isAdmin(company.getRole())) {
            response.sendRedirect("/app/admin/dashboard");
        } else if (company.getDomaine() == null || company.getDomaine().trim().isEmpty() || !company.isDomainVerified()) {
            response.sendRedirect("/app/private/onboarding/widget");
        } else {
            response.sendRedirect("/app/private/dashboard");
        }
    }

    private boolean isAdmin(String role) {
        if (role == null) {
            return false;
        }
        String normalized = role.trim().toUpperCase();
        return "ADMIN".equals(normalized) || "ROLE_ADMIN".equals(normalized);
    }

    private String errorRedirect(String message) throws IOException {
        return "/authentication?status=ERROR&message=" + URLEncoder.encode(message, "UTF-8");
    }
}
