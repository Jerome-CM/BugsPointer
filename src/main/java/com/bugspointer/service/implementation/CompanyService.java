package com.bugspointer.service.implementation;

import com.bugspointer.dto.AuthRegisterCompanyDTO;
import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.Response;
import com.bugspointer.entity.Company;
import com.bugspointer.service.ICompany;
import com.bugspointer.repository.CompanyRepository;
import com.bugspointer.utility.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class CompanyService implements ICompany {

    private final CompanyRepository companyRepository;

    private final Utility utility;
    private final PasswordEncoder passwordEncoder;

    public CompanyService(CompanyRepository companyRepository, Utility utility, PasswordEncoder passwordEncoder) {
        this.companyRepository = companyRepository;
        this.utility = utility;
        this.passwordEncoder = passwordEncoder;
    }

    public Company getCompanyByMail(String mail){
        Optional<Company> company = companyRepository.findByMail(mail);
        Company companyGet = null;

        if (company.isPresent()){
            companyGet = company.get();
        }

        return  companyGet;
    }

    //Création de la méthode createPublicKey
    public String createPublicKey() {
        return utility.createPublicKey();
    }

    @Override
    public Response saveCompany(AuthRegisterCompanyDTO dto) {
        log.info("SaveCompany");
        log.info("in dto :  {}", dto);
        boolean mail = false;
        boolean pw = false;
        boolean name = false;

        if (dto.getPassword().equals(dto.getConfirmPassword())){
            pw = true;
        } else {
            return new Response(EnumStatus.ERROR, null, "Passwords not identical");
        }

        if (dto.getMail().equals(dto.getConfirmMail())) {
            Optional<Company> companyOptional = companyRepository.findByMail(dto.getMail());
            if(companyOptional.isPresent()){
                log.info("CompanyMail is exist :  {}", dto.getMail());
                return new Response(EnumStatus.ERROR, null, "CompanyMail is exist already");
            }
            else{
                mail = true;
            }
        } else {
            return new Response(EnumStatus.ERROR, null, "Mails not identical");
        }

        Optional<Company> companyOptional = companyRepository.findByCompanyName(dto.getCompanyName());
        if (companyOptional.isPresent()){
            log.info("CompanyName is exist :  {}", dto.getCompanyName());
            return new Response(EnumStatus.ERROR, null, "CompanyName is exist already");
        } else {
            name = true;
        }

        if(mail && pw && name){
            Company company = new Company();
            company.setCompanyName(dto.getCompanyName());
            company.setMail(dto.getMail());
            company.setPassword(passwordEncoder.encode(dto.getPassword()));
            company.setPublicKey(createPublicKey());
            log.info("company :  {}", company);
            try {
                Company savedCompany = companyRepository.save(company);
                log.info("Company saved :  {}", savedCompany);
                return new Response(EnumStatus.OK, null, "Register successfully - Please login");
            }
            catch (Exception e){
                log.error("Error :  {}", e.getMessage());
                return new Response(EnumStatus.ERROR, null, "Error in register");
            }
        }
        return new Response(EnumStatus.ERROR, null, "A error is present, Retry");
    }
}
