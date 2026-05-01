package com.bugspointer.configuration;

import com.bugspointer.entity.Company;
import com.bugspointer.jwtConfig.JwtTokenUtil;
import com.bugspointer.repository.CompanyRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Optional;

@Component
public class DomainRequiredInterceptor implements HandlerInterceptor {

    private final CompanyRepository companyRepository;
    private final JwtTokenUtil jwtTokenUtil;

    public DomainRequiredInterceptor(CompanyRepository companyRepository, JwtTokenUtil jwtTokenUtil) {
        this.companyRepository = companyRepository;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("token") == null) {
            response.sendRedirect("/authentication");
            return false;
        }

        String token = String.valueOf(session.getAttribute("token"));
        String realToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        Optional<Company> companyOptional = companyRepository.findByMail(jwtTokenUtil.getUsernameFromToken(realToken));
        if (!companyOptional.isPresent()) {
            response.sendRedirect("/authentication?status=ERROR&message=Compte introuvable");
            return false;
        }

        Company company = companyOptional.get();
        if (!"ROLE_ADMIN".equals(company.getRole()) && (company.getDomaine() == null || company.getDomaine().trim().isEmpty() || !company.isDomainVerified())) {
            response.sendRedirect("/app/private/onboarding/widget");
            return false;
        }

        return true;
    }
}
