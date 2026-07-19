package com.bugspointer.configuration;

import com.bugspointer.jwtConfig.JwtAuthenticationEntryPoint;
import com.bugspointer.jwtConfig.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.HttpStatusRequestRejectedHandler;
import org.springframework.security.web.firewall.RequestRejectedHandler;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;


@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private static final String[] PUBLIC_URLS = {
            "/css/**",
            "/js/**",
            "/social/**",
            "/widget/**",
            "/robots.txt",
            "/sitemap.xml",
            "/llms.txt",
            "/favicon.svg",
            "/favicon.ico",
            "/",
            "/authentication",
            "/oauth2/**",
            "/login/oauth2/**",
            "/testPage",
            "/pollUser",
            "/pollInstallation",
            "/registerConfirm",
            "/confirmRegister/*",
            "/newUser/*",
            "/pwLost",
            "/resetPassword/**",
            "/features",
            "/outil-remontee-bugs",
            "/agences-web",
            "/signalement-bug-site-web",
            "/debuguer-site-web",
            "/checklist-recette-site-web",
            "/modele-rapport-bug",
            "/scanner-site-avant-mise-en-production",
            "/documentations",
            "/cgu",
            "/cgv",
            "/mentions",
            "/download"
    };

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    @Autowired
    private JwtRequestFilter jwtRequestFilter;
    @Autowired
    private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    @Autowired
    private CustomOAuth2AuthenticationSuccessHandler customOAuth2AuthenticationSuccessHandler;
    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public RequestRejectedHandler requestRejectedHandler() {
        return new HttpStatusRequestRejectedHandler(400);
    }

    @Override
    public void configure(WebSecurity webSecurity) {
        webSecurity.ignoring()
                .antMatchers("/css/**", "/js/**", "/social/**", "/widget/**", "/favicon.ico", "/favicon.svg");
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
       httpSecurity
                .cors()
                .and()
                .csrf()
                .ignoringAntMatchers("/api/user/modalControl")
                .and()
                .headers()
                .httpStrictTransportSecurity()
                .includeSubDomains(true)
                .preload(true)
                .maxAgeInSeconds(31536000)
                .and()
                .contentTypeOptions()
                .and()
                .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                .and()
                .permissionsPolicy().policy("camera=(), microphone=(), geolocation=(), payment=()")
                .and()
                .contentSecurityPolicy("default-src 'self'; script-src 'self' 'unsafe-inline' https://bugspointer.com https://cdn.jsdelivr.net; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self' data:; connect-src 'self' https://bugspointer.com; frame-ancestors 'self'; form-action 'self'; base-uri 'self'")
                .and()
                .frameOptions().sameOrigin()
                .and()
                .authorizeRequests()
                // restricted url
                .antMatchers("/app/private/thanks").permitAll()
                .antMatchers("/app/admin/**").hasRole("ADMIN")
                .antMatchers("/app/private/**").hasAnyRole("ADMIN","USER")
                // public url
                .antMatchers("/css/**").permitAll()
                .antMatchers("/js/**").permitAll()
                .antMatchers("/social/**").permitAll()
                .antMatchers("/widget/**").permitAll()
                .antMatchers("/robots.txt").permitAll()
                .antMatchers("/sitemap.xml").permitAll()
                .antMatchers("/llms.txt").permitAll()
                .antMatchers("/favicon.svg").permitAll()
                .antMatchers("/favicon.ico").permitAll()
                .antMatchers("/").permitAll()
                .antMatchers("/authentication").permitAll()
                .antMatchers("/oauth2/**").permitAll()
                .antMatchers("/login/oauth2/**").permitAll()
                .antMatchers("/testPage").permitAll()
                .antMatchers("/pollUser").permitAll()
                .antMatchers("/pollInstallation").permitAll()
                .antMatchers(HttpMethod.POST, "/api/user/modalControl").permitAll()
                .antMatchers(HttpMethod.GET, "/api/widget/config").permitAll()
                .antMatchers(HttpMethod.POST, "/login").permitAll()
                .antMatchers(HttpMethod.POST, "/register").permitAll()
                .antMatchers("/registerConfirm").permitAll()
                .antMatchers("/confirmRegister/*").permitAll()
                .antMatchers("/newUser/*").permitAll()//TODO: demande de connexion puis redirection vers newUser ?
                .antMatchers(HttpMethod.POST, "/newUser/*/verify").permitAll()
                .antMatchers("/pwLost").permitAll()
                .antMatchers("/resetPassword/**").permitAll()
                .antMatchers("/features").permitAll()
                .antMatchers("/outil-remontee-bugs").permitAll()
                .antMatchers("/agences-web").permitAll()
                .antMatchers("/signalement-bug-site-web").permitAll()
                .antMatchers("/debuguer-site-web").permitAll()
                .antMatchers("/checklist-recette-site-web").permitAll()
                .antMatchers("/modele-rapport-bug").permitAll()
                .antMatchers("/scanner-site-avant-mise-en-production").permitAll()
                .antMatchers("/documentations").permitAll()
                .antMatchers("/cgu").permitAll()
                .antMatchers("/cgv").permitAll()
                .antMatchers("/mentions").permitAll()
                .antMatchers("/download").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin().loginPage("/login")
                .usernameParameter("mail")
                .failureHandler(customAuthenticationFailureHandler)
                .successHandler(customAuthenticationSuccessHandler)
                .and()
                .oauth2Login()
                .loginPage("/authentication")
                .userInfoEndpoint()
                .userService(customOAuth2UserService)
                .and()
                .successHandler(customOAuth2AuthenticationSuccessHandler)
                .and()
                .exceptionHandling().accessDeniedHandler(customAccessDeniedHandler)
                .and()
                .rememberMe().disable()
                .sessionManagement()
                .sessionFixation().migrateSession()
                .invalidSessionStrategy((request, response) -> {
                    String redirectPath = LoginRedirectUtil.getSafeRedirectPath(request);
                    if (isPublicRequest(request)) {
                        request.getSession();
                        response.sendRedirect(getCurrentRequestPath(request));
                        return;
                    }
                    request.getSession().setAttribute("redirectAfterLogin", redirectPath);
                    response.sendRedirect("/authentication?status=ERROR&message=Vous%20devez%20vous%20connecter&redirect=" + URLEncoder.encode(redirectPath, "UTF-8"));
                })
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .and()
                .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint);

                //.and()
                //.sessionManagement()
                // make sure we use stateless session; session won't be used to store user's state.
                //.sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // Add a filter to validate the tokens with every request
        httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    }

    private boolean isPublicRequest(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }

        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }

        for (String publicUrl : PUBLIC_URLS) {
            if (matchesPublicUrl(publicUrl, path)) {
                return true;
            }
        }
        return false;
    }

    private String getCurrentRequestPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        return query == null || query.trim().isEmpty() ? uri : uri + "?" + query;
    }

    private boolean matchesPublicUrl(String pattern, String path) {
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            return path.equals(prefix) || path.startsWith(prefix + "/");
        }
        if (pattern.endsWith("/*")) {
            String prefix = pattern.substring(0, pattern.length() - 2);
            if (!path.startsWith(prefix + "/")) {
                return false;
            }
            return path.indexOf('/', prefix.length() + 1) == -1;
        }
        return pattern.equals(path);
    }

}
