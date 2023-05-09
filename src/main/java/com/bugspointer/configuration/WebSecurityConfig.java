package com.bugspointer.configuration;

import com.bugspointer.jwtConfig.JwtAuthenticationEntryPoint;
import com.bugspointer.jwtConfig.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;



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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
       httpSecurity.csrf().disable()
                .authorizeRequests()
                // restricted url
                .antMatchers("/app/admin/**").hasRole("ADMIN")
                // public url
                .antMatchers("/css/**").permitAll()
                .antMatchers("/js/**").permitAll()
                .antMatchers("/").permitAll()
                .antMatchers("/authentication").permitAll()
                .antMatchers(HttpMethod.POST, "/login").permitAll()
                .antMatchers(HttpMethod.POST, "/register").permitAll()
                .antMatchers("/features").permitAll()
                .antMatchers("/documentations").permitAll()
                .antMatchers(HttpMethod.GET,"/modal").permitAll()
                 // TODO A retirer en production
                .antMatchers("/logo").permitAll()
                //.anyRequest().authenticated() TODO décommenter la ligne pour demander l'authentification
                .and()
                .formLogin().loginPage("/login")
                .usernameParameter("mail")
                .failureHandler(customAuthenticationFailureHandler)
                .successHandler(customAuthenticationSuccessHandler)
                .and()
                // Show 403.html if access is denied, /error is a get controller
                //exceptionHandling().accessDeniedPage("/app/error")
                //.and()
               .rememberMe()
               .and()
               .logout()
               .deleteCookies("JSESSIONID", "remember-me")
               .and()
               .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint)
               .and()
               .sessionManagement()
               // make sure we use stateless session; session won't be used to store user's state.
               .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // Add a filter to validate the tokens with every request
        httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
