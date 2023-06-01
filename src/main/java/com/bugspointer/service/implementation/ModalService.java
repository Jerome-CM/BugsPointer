package com.bugspointer.service.implementation;

import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.ModalDTO;
import com.bugspointer.dto.Response;
import com.bugspointer.entity.Bug;
import com.bugspointer.entity.Company;
import com.bugspointer.entity.EnumEtatBug;
import com.bugspointer.entity.EnumPlan;
import com.bugspointer.repository.BugRepository;
import com.bugspointer.repository.CompanyRepository;
import com.bugspointer.service.IModal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ModalService implements IModal {

    private final BugRepository bugRepository;
    private final CompanyRepository companyRepository;
    private final MailService mailService;

    public ModalService(BugRepository bugRepository, CompanyRepository companyRepository, MailService mailService) {
        this.bugRepository = bugRepository;
        this.companyRepository = companyRepository;
        this.mailService = mailService;
    }


    public Response saveModalFree(ModalDTO dto){
        log.info("saveModalFree");
        log.info("in dto :  {}", dto);
        boolean bot;
        boolean description;
        boolean key;
        boolean envoi = false;
        Company company;
        Date dateDernierEnvoi = null;

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

        if (company.getPlan().equals(EnumPlan.FREE)){
            //On récupère la liste des bugs reçu
            List<Bug> bugs = bugRepository.findAllByCompany(company);
            int i = bugs.size()-1;

            //On cherche la dernière dateEnvoi
            while (i >= 0 && dateDernierEnvoi == null ){
                if (bugs.get(i).getDateEnvoi()!=null){
                    dateDernierEnvoi = bugs.get(i).getDateEnvoi();
                    log.info("dernier envoi : {}",dateDernierEnvoi);
                }
                i--;
            }
            Date dateJour = new Date();
            long j30 = TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS);
            if (dateDernierEnvoi != null){
                if (dateJour.getTime() - dateDernierEnvoi.getTime() > j30){
                    envoi = true;
                }
            } else {
                envoi = true;
            }
        }

        if (bot && description && key){
            Bug bug = new Bug();
            bug.setUrl(dto.getUrl());
            bug.setDescription(dto.getDescription());
            bug.setCodeLocation(dto.getCodeLocation());
            bug.setOs(dto.getOs());
            bug.setBrowser(dto.getBrowser());
            bug.setScreenSize(dto.getScreenSize());
            bug.setDateCreation(new Date());
            bug.setEtatBug(EnumEtatBug.NEW);
            bug.setCompany(company);
            if (envoi){
                bug.setDateEnvoi(new Date());
            }
            log.info("bug :  {}", bug);
            try {
                Bug savedBug = bugRepository.save(bug);
                /* Vérification du compte gratuit, si oui -> si dernier envoi date de +30jours -> envoi du mail avec détail
                *   Si -30 jours, mail sans détail avec abonnez-vous pour le voir */
                if (company.getPlan().equals(EnumPlan.FREE) && envoi) {
                    log.info("mail à envoyer");
                    //Mail à modifier pour envoi forcé sinon company.getMail()
                    Response response = mailService.sendMailNewBugDetail(company.getMail(), savedBug);
                    if (response.getStatus().equals(EnumStatus.OK)) {
                        log.info("mail envoyé");
                    }
                } else if (company.getPlan().equals(EnumPlan.FREE)){
                    log.info("mail à envoyer sans détail");
                    //Mail à modifier pour envoi forcé sinon company.getMail()
                    Response response = mailService.sendMailNewBugNoDetail(company.getMail());
                    if (response.getStatus().equals(EnumStatus.OK)) {
                        log.info("mail envoyé");
                    }
                } else {
                    //TODO: envoyer mail lié au compte payant
                    log.info("compte payant");
                }
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
