package com.bugspointer.service.implementation;

import com.bugspointer.dto.AccountDTO;
import com.bugspointer.dto.AuthRegisterCompanyDTO;
import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.Response;
import com.bugspointer.entity.Company;
import com.bugspointer.entity.EnumIndicatif;
import com.bugspointer.jwtConfig.JwtTokenUtil;
import com.bugspointer.service.ICompany;
import com.bugspointer.repository.CompanyRepository;
import com.bugspointer.utility.Utility;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Optional;

@Service
@Slf4j
public class CompanyService implements ICompany {

    private final CompanyRepository companyRepository;

    private final Utility utility;
    private final PasswordEncoder passwordEncoder;

    private final JwtTokenUtil jwtTokenUtil;

    private final ModelMapper modelMapper;

    public CompanyService(CompanyRepository companyRepository, Utility utility, PasswordEncoder passwordEncoder, JwtTokenUtil jwtTokenUtil, ModelMapper modelMapper) {
        this.companyRepository = companyRepository;
        this.utility = utility;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
        this.modelMapper = modelMapper;
    }

    public Company getCompanyByMail(String mail){
        Optional<Company> company = companyRepository.findByMail(mail);
        Company companyGet = null;

        if (company.isPresent()){
            companyGet = company.get();
        }

        return companyGet;
    }

    public String createPublicKey() {
        return utility.createPublicKey();
    }

    @Override
    public Response saveCompany(AuthRegisterCompanyDTO dto) {
        log.info("SaveCompany");
        log.info("in dto :  {}", dto);
        boolean mail;
        boolean pw;
        boolean name;

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

    public Company getCompanyWithToken(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String token = (String) session.getAttribute("token");
        log.info(token);
        String realToken = token.substring(7);
        log.info(" token retrieved : {}", jwtTokenUtil.getUsernameFromToken(realToken));

        Optional<Company> companyOptional = companyRepository.findByMail(jwtTokenUtil.getUsernameFromToken(realToken));
        if (companyOptional.isPresent()){
            return companyOptional.get();
        }

        return null;
    }

    public AccountDTO getAccountDto(Company company) {
        log.info("AccountDTO : company : {}", company);
        AccountDTO dto;
        dto = modelMapper.map(company, AccountDTO.class);
        log.info("AccountDTO : dto : {}", dto);

        return dto;
    }

    public Response mailUpdate(AccountDTO dto) {
        log.info("mailUpdate");
        log.info("dto : {}", dto);
        boolean mail = false;
        boolean pw = false;

        Optional<Company> companyOptional = companyRepository.findByPublicKey(dto.getPublicKey());
        Company company = new Company();
        if (companyOptional.isPresent()){
            company = companyOptional.get();
            log.info("Company : {}", company);
            if (dto.getPassword().isEmpty()){
                return new Response(EnumStatus.ERROR, null, "Password empty");
            } else {
                if (passwordEncoder.matches(dto.getPassword(), company.getPassword())){
                    pw = true;
                    log.info("password ok");
                }
                else {
                    log.info("Error password");
                    return new Response(EnumStatus.ERROR, null, "Error password");
                }
            }

            if (dto.getMail().isEmpty()) {
                log.info("Mail empty");
                return new Response(EnumStatus.ERROR, null, "Mail empty");
            } else {
                log.info("Mail completed");
                mail = true;
            }
        }

        if (pw && mail) {
            log.info("mail at modify : {}", dto.getMail());
            company.setMail(dto.getMail());
            log.info("Company mail update : {}", company);

            return companyTryRegistration(company);
        }

        return new Response(EnumStatus.ERROR, null, "Error in the process");
    }

    public Response passwordUpdate(AccountDTO dto) {
        log.info("passwordUpdate :");
        log.info("dto : {}", dto);

        Optional<Company> companyOptional = companyRepository.findByPublicKey(dto.getPublicKey());
        Company company;
        if (companyOptional.isPresent()) {
            company = companyOptional.get();
            log.info("Company : {}", company);
            if (dto.getPassword().isEmpty() || dto.getNewPassword().isEmpty() || dto.getConfirmPassword().isEmpty()) {
                return new Response(EnumStatus.ERROR, null, "Password empty");
            } else {
                if (passwordEncoder.matches(dto.getPassword(), company.getPassword())) {
                    log.info("password ok");
                    if (dto.getNewPassword().equals(dto.getConfirmPassword())){
                        company.setPassword(passwordEncoder.encode(dto.getNewPassword()));
                        log.info("company modified :  {}", company);
                        return companyTryRegistration(company);
                    } else {
                        return new Response(EnumStatus.ERROR, null, "Password not identical");
                    }
                } else {
                    return new Response(EnumStatus.ERROR, null, "Wrong password");
                }
            }
        }

        return new Response(EnumStatus.ERROR, null, "Error in the process");
    }

    public Response smsUpdate(AccountDTO dto){
        log.info("smsUpdate :");
        log.info("dto : {}", dto);

        Optional<Company> companyOptional = companyRepository.findByPublicKey(dto.getPublicKey());
        Company company;
        if (companyOptional.isPresent()) {
            company = companyOptional.get();
            log.info("Company : {}", company);

            if (dto.getPhoneNumber().isEmpty()){
                return new Response(EnumStatus.ERROR, null, "PhoneNumber empty");
            } else {
                if (dto.getPhoneNumber().length()==10 && (dto.getPhoneNumber().startsWith("06") || dto.getPhoneNumber().startsWith("07"))) {
                    EnumIndicatif indicatif = dto.getIndicatif();
                    if (indicatif != null) {
                        company.setIndicatif(indicatif);
                    } else {
                        return new Response(EnumStatus.ERROR, null, "the code doesn't exist");
                    }

                    company.setPhoneNumber(dto.getPhoneNumber());
                    return companyTryRegistration(company);
                } else {
                    return new Response(EnumStatus.ERROR, null, "It's not a phone number");
                }
            }
        }

        return new Response(EnumStatus.ERROR, null, "Error in the process");
    }

    private Response companyTryRegistration(Company company) {
        try {
            Company savedCompany = companyRepository.save(company);
            log.info("Company update :  {}", savedCompany);
            return new Response(EnumStatus.OK, null, "Update ok");
        }
        catch (Exception e){
            log.error("Error :  {}", e.getMessage());
            return new Response(EnumStatus.ERROR, null, "Error in update");
        }
    }
}
