package com.bugspointer.service.implementation;

import com.bugspointer.entity.Company;
import com.bugspointer.entity.CompanyToken;
import com.bugspointer.repository.CompanyTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
public class CompanyTokenService {

    private final PasswordEncoder passwordEncoder;

    private final CompanyTokenRepository companyTokenRepository;

    public CompanyTokenService(PasswordEncoder passwordEncoder, CompanyTokenRepository companyTokenRepository) {
        this.passwordEncoder = passwordEncoder;
        this.companyTokenRepository = companyTokenRepository;
    }

    public boolean getDateNotExpired(CompanyToken companyToken){
        Date now = new Date();

        long diffInMillis = now.getTime() - companyToken.getDateCreation().getTime();
        long diffInMinutes = diffInMillis / (60 * 1000);

        return diffInMinutes <= 5;
    }

    public CompanyToken saveCompanyToken(Company company, String token){
        Date creation = new Date();
        CompanyToken companyToken = new CompanyToken();
        companyToken.setCompanyMail(company.getMail());
        companyToken.setPublicKey(company.getPublicKey());
        companyToken.setTokenReset(passwordEncoder.encode(token));
        companyToken.setDateCreation(creation);
        log.info("companyToken : {}", companyToken);

        try {
            return companyTokenRepository.save(companyToken);
        }catch (Exception e){
            log.error("Error : {}", e.getMessage());
            return null;
        }
    }

    public boolean testToken(String tokenUrl, String key){
        Optional<CompanyToken> companyTokenOptional = companyTokenRepository.findByPublicKey(key);

        if (companyTokenOptional.isPresent()){
            CompanyToken companyToken = companyTokenOptional.get();
            log.info("token present");
            if (getDateNotExpired(companyToken)){
                log.info("date dépassée");
                return passwordEncoder.matches(tokenUrl, companyToken.getTokenReset());
            }
        }
        return false;
    }
}
