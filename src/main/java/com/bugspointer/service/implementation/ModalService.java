package com.bugspointer.service.implementation;

import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.ModalDTO;
import com.bugspointer.dto.Response;
import com.bugspointer.entity.Bug;
import com.bugspointer.entity.Company;
import com.bugspointer.entity.EnumEtatBug;
import com.bugspointer.repository.BugRepository;
import com.bugspointer.repository.CompanyRepository;
import com.bugspointer.service.IModal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ModalService implements IModal {

    private final BugRepository bugRepository;
    private final CompanyRepository companyRepository;

    public ModalService(BugRepository bugRepository, CompanyRepository companyRepository) {
        this.bugRepository = bugRepository;
        this.companyRepository = companyRepository;
    }


    public Response saveModalFree(ModalDTO dto){
        log.info("saveModalFree");
        log.info("in dto :  {}", dto);
        boolean bot = false;
        boolean description = false;
        boolean key = false;
        Company company = new Company();

        if (dto.getBot().isEmpty())
        {
            bot = true;
        } else {
            log.info("bot completed");
            return new Response(EnumStatus.ERROR, null, "Form completed by robot");
        }

        if (dto.getDescription().length()>10)
        {
            description = true;
        } else {
            log.info("description incomplete");
            return new Response(EnumStatus.ERROR, null, "Description incomplete");
        }
        Optional<Company> companyOptional = companyRepository.findByPublicKey(dto.getKey());
        if (companyOptional.isPresent()){
            log.info("Company exist : {}", companyOptional);

            company = companyOptional.get();
            if (company.isEnable())
            {
                key = true;
            } else {
                return new Response(EnumStatus.ERROR, null, "company not enable");
            }

        } else {
            log.info("Key is not match with a company");
            return new Response(EnumStatus.ERROR, null, "company not exist");
        }

        if (bot && description && key){
            Bug bug = new Bug();
            bug.setUrl(dto.getUrl());
            bug.setDescription(dto.getDescription());
            bug.setOs(dto.getOs());
            bug.setBrowser(dto.getBrowser());
            bug.setScreenSize(dto.getScreenSize());
            bug.setDateCreation(new Date());
            bug.setEtatBug(EnumEtatBug.NEW);
            bug.setCompany(company);
            log.info("bug :  {}", bug);
            try {
                Bug savedBug = bugRepository.save(bug);
                log.info("bug saved :  {}", savedBug);
                return new Response(EnumStatus.OK, null, "Send successfully");
            }
            catch (Exception e){
                log.error("Error :  {}", e.getMessage());
                return new Response(EnumStatus.ERROR, null, "A error is present, Retry");
            }
        }

        return null;
    }
}
