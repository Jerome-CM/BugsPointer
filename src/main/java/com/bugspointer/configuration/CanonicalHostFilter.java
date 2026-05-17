package com.bugspointer.configuration;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CanonicalHostFilter extends OncePerRequestFilter {

    private static final String WWW_HOST = "www.bugspointer.com";
    private static final String CANONICAL_HOST = "bugspointer.com";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (WWW_HOST.equalsIgnoreCase(request.getServerName())) {
            String target = "https://" + CANONICAL_HOST + request.getRequestURI();
            if (request.getQueryString() != null && !request.getQueryString().isEmpty()) {
                target += "?" + request.getQueryString();
            }
            response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            response.setHeader("Location", target);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
