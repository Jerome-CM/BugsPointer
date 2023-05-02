package com.bugspointer.service.implementation;

import com.bugspointer.entity.Company;
import com.bugspointer.service.ICompany;
import com.bugspointer.repository.CompanyRepository;
import com.bugspointer.utility.Utility;
import org.springframework.stereotype.Service;

@Service
public class CompanyService implements ICompany {

    private final CompanyRepository companyRepository;

    private final Utility utility;

    public CompanyService(CompanyRepository companyRepository, Utility utility) {
        this.companyRepository = companyRepository;
        this.utility = utility;
    }

    public Company getCompanyByMail(String mail){
        return  companyRepository.findByMail(mail);
    }

    //Création de la méthode createPublicKey
    public String createPublicKey() {
        return utility.createPublicKey();
    }

}
