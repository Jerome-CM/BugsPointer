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


@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    @Autowired
    private JwtRequestFilter jwtRequestFilter;
    @Autowired
    private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

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
                .antMatchers("/css/**", "/js/**", "/widget/**", "/favicon.ico");
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
                .contentSecurityPolicy("default-src 'self'; script-src 'self' 'unsafe-inline' https://bugspointer.com; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self' data:; connect-src 'self' https://bugspointer.com; frame-ancestors 'self'; form-action 'self'; base-uri 'self'")
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
                .antMatchers("/widget/**").permitAll()
                .antMatchers("/robots.txt").permitAll()
                .antMatchers("/sitemap.xml").permitAll()
                .antMatchers("/").permitAll()
                .antMatchers("/authentication").permitAll()
                .antMatchers("/testPage").permitAll()
                .antMatchers("/pollUser").permitAll()
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
                .exceptionHandling().accessDeniedHandler(customAccessDeniedHandler)
                .and()
                .rememberMe().disable()
                .sessionManagement()
                .sessionFixation().migrateSession()
                .invalidSessionUrl("/")
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
}
