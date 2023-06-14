package com.bugspointer.configuration;

import com.bugspointer.entity.Company;
import com.bugspointer.entity.EnumMotif;
import com.bugspointer.jwtConfig.JwtTokenUtil;
import com.bugspointer.service.implementation.CompanyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;

@Component
@Slf4j
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    
    @Autowired
    private JwtTokenUtil jwtTest;

    @Autowired
    private CompanyService companyService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("--- Method onAuthenticationSuccess ---");
        /*Take the mail in the session after success login*/

        HttpSession session = request.getSession();

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String mail = userDetails.getUsername();
        Company company = (Company) companyService.getCompanyByMail(mail);

        if (!company.isEnable()) {
            if (company.getMotifEnable() == EnumMotif.CONFIRMATION){
                response.sendRedirect("/authentication?status=ERROR&message=Veuillez confirmer votre compte");//TODO: ajouter sur html lien pour renvoyer mail de confirmation
            } else {
                response.sendRedirect("/authentication?status=ERROR&message=Account disabled");
            }
            return;
        }

        if(session.getAttribute("token") == null){
            log.info("Create a new token because is null in the session");
            String token = null;
            try {
                token = jwtTest.createAuthenticationToken(mail, authentication);
            } catch (Exception e) {
                log.info("Impossible to create a token : {}", e.getMessage());
                throw new RuntimeException(e);
            }
            session.setAttribute("token", token);
            session.setAttribute("role", company.getRole());
            session.setAttribute("mail", mail);


        }
        log.info("Token in session : {}", session.getAttribute("token"));
        company.setLastVisit(new Date());
        Company companySaved = companyService.companyTryUpdateLastVisit(company);
        log.info("Dernière connexion company : {}", companySaved.getLastVisit());

        if(company.getRole().equals("ROLE_ADMIN")){
            response.sendRedirect("app/admin/dashboard");
        }else{
            response.sendRedirect("app/private/dashboard");
        }
    }
}