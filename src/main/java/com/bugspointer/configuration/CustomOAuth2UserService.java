package com.bugspointer.configuration;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = delegate.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        if (!"github".equalsIgnoreCase(registrationId) || hasEmail(user)) {
            return user;
        }

        Map<String, Object> attributes = new LinkedHashMap<>(user.getAttributes());
        String email = fetchPrimaryGithubEmail(userRequest);
        if (email != null) {
            attributes.put("email", email);
        }

        String userNameAttribute = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();
        return new DefaultOAuth2User(user.getAuthorities(), attributes, userNameAttribute);
    }

    private boolean hasEmail(OAuth2User user) {
        Object email = user.getAttributes().get("email");
        return email != null && !String.valueOf(email).trim().isEmpty();
    }

    private String fetchPrimaryGithubEmail(OAuth2UserRequest userRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userRequest.getAccessToken().getTokenValue());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                "https://api.github.com/user/emails",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );

        List<Map<String, Object>> emails = response.getBody() == null ? new ArrayList<>() : response.getBody();
        for (Map<String, Object> email : emails) {
            boolean primary = Boolean.TRUE.equals(email.get("primary"));
            boolean verified = Boolean.TRUE.equals(email.get("verified"));
            Object value = email.get("email");
            if (primary && verified && value != null && !String.valueOf(value).trim().isEmpty()) {
                return String.valueOf(value).trim();
            }
        }
        return null;
    }
}
