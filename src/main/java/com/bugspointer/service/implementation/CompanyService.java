package com.bugspointer.service.implementation;

import com.bugspointer.service.Company;
import com.bugspointer.repository.CompanyRepository;
import org.springframework.stereotype.Service;

@Service
public class CompanyService implements Company {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Company getCompanyByMail(String mail){
        return (Company) companyRepository.findByMail(mail);
    }

}
