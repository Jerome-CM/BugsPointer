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

    final String WITHOUT_TOKEN = "PasDeDemandeEnCours";

    public CompanyTokenService(PasswordEncoder passwordEncoder, CompanyTokenRepository companyTokenRepository) {
        this.passwordEncoder = passwordEncoder;
        this.companyTokenRepository = companyTokenRepository;
    }

    public boolean getDateNotExpired(CompanyToken companyToken){
        final int TEMPS_TOKEN = 15;
        Date now = new Date();

        long diffInMillis = now.getTime() - companyToken.getDateCreation().getTime();
        long diffInMinutes = diffInMillis / (60 * 1000);

        return diffInMinutes <= TEMPS_TOKEN;
    }

    public void saveCompanyToken(Company company, String token){
        Date creation = new Date();
        CompanyToken companyToken;
        Optional<CompanyToken> companyTokenOptional = companyTokenRepository.findByPublicKey(company.getPublicKey());
        if (companyTokenOptional.isPresent()){
            companyToken = companyTokenOptional.get();
        } else {
            companyToken = new CompanyToken();
            companyToken.setCompanyMail(company.getMail());
            companyToken.setPublicKey(company.getPublicKey());
        }
        companyToken.setTokenReset(passwordEncoder.encode(token));
        companyToken.setDateCreation(creation);

        try {
            companyTokenRepository.save(companyToken);

        }catch (Exception e){
            log.error("Error : {}", e.getMessage());
        }
    }

    public void deleteToken(Company company){
        CompanyToken companyToken;
        Optional<CompanyToken> companyTokenOptional = companyTokenRepository.findByPublicKey(company.getPublicKey());
        if (companyTokenOptional.isPresent()){
            companyToken = companyTokenOptional.get();
            if (!companyToken.getCompanyMail().equals(company.getMail())){
                companyToken.setCompanyMail(company.getMail());
            }
            companyToken.setTokenReset(WITHOUT_TOKEN);
            companyToken.setDateCreation(null);
        }
    }

    public boolean checkToken(String tokenUrl, String key){
        if (tokenUrl == null || tokenUrl.equals(WITHOUT_TOKEN)){
            return false;
        }
        Optional<CompanyToken> companyTokenOptional = companyTokenRepository.findByPublicKey(key);

        if (companyTokenOptional.isPresent()){
            CompanyToken companyToken = companyTokenOptional.get();
            if (companyToken.getDateCreation() != null && getDateNotExpired(companyToken)){
                return passwordEncoder.matches(tokenUrl, companyToken.getTokenReset());
            }
        }
        return false;
    }
}
