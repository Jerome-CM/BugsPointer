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

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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


    public Response saveModal(ModalDTO dto){
        log.info("saveModal");
        log.info("in dto :  {}", dto);
        boolean test = false;
        boolean bot;
        boolean description;
        boolean key;
        boolean envoi = false;
        Company company;
        Date dateJour = new Date();
        Date dateDernierEnvoi = null;
        Date dateIpEnvoi;
        long timeSeconde = 60;

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

        if (dto.getKey().equals("C1estLaClePublicAModifier")) { //TODO:A modifier par la publicKey représentant la clé de la company test
            log.info("Envoi test");
            test = true;
        }

        Optional<Company> companyOptional = companyRepository.findByPublicKey(dto.getKey());
        if (companyOptional.isPresent()){
            log.info("Company exist : {}", companyOptional);

            company = companyOptional.get();
            if (company.isEnable())
            {
                if (company.getDomaine() != null && dto.getUrl().contains(company.getDomaine())) { //On vérifie que l'URL contient le nom de domaine (s'il est présent) où la modal est censé apparaitre.
                    key = true;
                } else {
                    log.info("URL ne correspondant pas au domaine transmis");
                    return new Response(EnumStatus.ERROR, null, "L'URL ne correspond pas au domaine transmis");
                }
            } else {
                return new Response(EnumStatus.ERROR, null, "company non valide");
            }

        } else {
            log.info("Key is not match with a company");
            return new Response(EnumStatus.ERROR, null, "company not exist");
        }

        if (dto.getAdresseIp() != null && !test){
            //On récupère la liste des bugs reçus de cette adresse Ip
            List<Bug> bugs =  bugRepository.findAllByAdresseIp(dto.getAdresseIp());

            if (!bugs.isEmpty()){
                int i = bugs.size()-1;
                dateIpEnvoi = bugs.get(i).getDateCreation();
                boolean ok = differenceDate(dateIpEnvoi, dateJour, timeSeconde);
                if (!ok){
                    return new Response(EnumStatus.ERROR, null, "Délai entre les envois trop court");
                }
            }
        }

        if (!test) {
            if (company.getPlan().equals(EnumPlan.FREE)) {
                //On récupère la liste des bugs reçus
                List<Bug> bugs = bugRepository.findAllByCompany(company);
                int i = bugs.size() - 1;

                //On cherche la dernière dateEnvoi
                while (i >= 0 && dateDernierEnvoi == null) {
                    if (bugs.get(i).getDateEnvoi() != null) {
                        dateDernierEnvoi = bugs.get(i).getDateEnvoi();
                        log.info("dernier envoi : {}", dateDernierEnvoi);
                    }
                    i--;
                }

                long j30 = TimeUnit.SECONDS.convert(30, TimeUnit.DAYS);
                if (dateDernierEnvoi != null) {
                    if (differenceDate(dateDernierEnvoi, dateJour, j30)) {
                        envoi = true;
                    }
                } else {
                    envoi = true;
                }
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
            bug.setDateCreation(dateJour);
            bug.setEtatBug(EnumEtatBug.NEW);
            bug.setCompany(company);
            bug.setAdresseIp(dto.getAdresseIp());
            if (envoi){
                bug.setDateEnvoi(dateJour);
            }
            try {
                if (!test) {
                    Bug savedBug = bugRepository.save(bug);
                    /* Vérification du compte gratuit, si oui → si dernier envoi date de +30jours → envoi du mail avec détail
                     *   Si -30 jours, mail sans détail avec 'abonnez-vous pour le voir' */
                    if (company.getPlan().equals(EnumPlan.FREE) && envoi) {

                        Response response = mailService.sendMailNewBugDetail(company.getMail(), savedBug);
                        if (response.getStatus().equals(EnumStatus.OK)) {
                            log.info("mail gratuit envoyé avec détails");
                        }
                    } else if (company.getPlan().equals(EnumPlan.FREE)) {


                        Response response = mailService.sendMailNewBugNoDetail(company.getMail());
                        if (response.getStatus().equals(EnumStatus.OK)) {
                            log.info("mail envoyer sans détail");
                        }
                    } else {
                        //TODO: envoyer mail lié au compte payant
                        log.info("compte payant");
                    }
                    log.info("bug saved :  {}", savedBug);
                } else {
                    String codeLoc = dto.getCodeLocation();
                    codeLoc = codeLoc.replace("<", "&lt;");
                    codeLoc = codeLoc.replace(">", "&gt;");
                    bug.setCodeLocation(codeLoc);
                    Response response = mailService.sendMailTest(dto.getMail(), bug);
                    if (response.getStatus().equals(EnumStatus.OK)) {
                        log.info("mail test envoyé");
                    }
                }
                return new Response(EnumStatus.OK, null, "Send successfully");
            }
            catch (Exception e){
                log.error("Error :  {}", e.getMessage());
                return new Response(EnumStatus.ERROR, null, "A error is present, Retry");
            }
        }

        return null;
    }

    private boolean differenceDate (Date date1, Date date2, long temps){
        Instant instant1 = date1.toInstant();
        Instant instant2 = date2.toInstant();
        Duration elapsedDuration = Duration.between(instant1, instant2);
        return elapsedDuration.getSeconds() > temps;
    }
}
