package com.bugspointer.service.implementation;

import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.ModalDTO;
import com.bugspointer.dto.Response;
import com.bugspointer.entity.*;
import com.bugspointer.entity.enumLogger.Action;
import com.bugspointer.entity.enumLogger.What;
import com.bugspointer.repository.BugRepository;
import com.bugspointer.repository.CompanyPreferencesRepository;
import com.bugspointer.repository.CompanyRepository;
import com.bugspointer.service.IModal;
import com.bugspointer.utility.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
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

    private final CompanyPreferencesRepository companyPreferencesRepository;

    public ModalService(BugRepository bugRepository, CompanyRepository companyRepository, MailService mailService, CompanyPreferencesRepository companyPreferencesRepository) {
        this.bugRepository = bugRepository;
        this.companyRepository = companyRepository;
        this.mailService = mailService;
        this.companyPreferencesRepository = companyPreferencesRepository;
    }


    public Response saveModal(ModalDTO dto){
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
        boolean wantNewBugNotif = false;

        if (dto.getBot().isEmpty())
        {
            bot = true;
        } else {
            log.warn("Modal completed by BOT");
            return new Response(EnumStatus.ERROR, null, "");
        }

        if (dto.getDescription().length()>10){
            if(dto.getDescription().contains("<script>")){
                description = false;
                return new Response(EnumStatus.ERROR, null, "Merci de ne pas signaler de balise <script> dans la description");
            } else {
                description = true;
            }
        } else {
            return new Response(EnumStatus.ERROR, null, "Description incomplete");
        }

        if (dto.getKey().equals("LaClefDeTest")) {
            test = true;
        }

        Optional<Company> companyOptional = companyRepository.findByPublicKey(dto.getKey());
        if (companyOptional.isPresent()){

            company = companyOptional.get();
            if (company.isEnable()){
                if (company.getDomaine() != null && dto.getUrl().contains(company.getDomaine())) { //On vérifie que l'URL contient le nom de domaine (s'il est présent) où la modal est censé apparaitre.
                    key = true;
                } else {
                    return new Response(EnumStatus.ERROR, null, "L'URL ne correspond pas au domaine transmis");
                }
            } else {
                return new Response(EnumStatus.ERROR, null, "Votre compte est désactivé. Contactez-nous pour le réactiver : contact@bugspointer.com");
            }

        } else {
            log.error("Key {} isn't in bdd", dto.getKey());
            return new Response(EnumStatus.ERROR, null, "Erreur avec votre clé public, assurez-vous quelle soit identique à celle inscrit dans votre compte ( Dashboard > Account )");
        }

        if (dto.getAdresseIp() != null && !test){
            //On récupère la liste des bugs reçus de cette adresse Ip
            List<Bug> bugs = bugRepository.findAllByAdresseIp(dto.getAdresseIp());

            if (!bugs.isEmpty()){
                int i = bugs.size()-1;
                dateIpEnvoi = bugs.get(i).getDateCreation();
                boolean ok = differenceDate(dateIpEnvoi, dateJour, timeSeconde);
                if (!ok){
                    return new Response(EnumStatus.ERROR, null, "Merci de ne pas envoyer plus d'un rapport toutes les minutes");
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
                    }
                    i--;
                }

                // Si la différence est bien de + de 30jours
                if (dateDernierEnvoi != null) {
                    if (differenceDate(dateDernierEnvoi, dateJour, TimeUnit.SECONDS.convert(30, TimeUnit.DAYS))) {
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
                    Bug savedBug = new Bug();
                    try{
                        savedBug = bugRepository.save(bug);
                        Utility.saveLog(bug.getCompany().getCompanyId(), Action.SAVE, What.BUG, "#"+ savedBug.getId(), null, null);
                        log.info("Company #{} save a new bug #{}",savedBug.getCompany().getCompanyId(), savedBug.getId());
                    } catch (Exception e){
                        log.error("Impossible to save a bug for company#{} : {}", bug.getCompany().getCompanyId(), e.getMessage());
                    }

                    // Notification new mail si notification activée
                    Optional<CompanyPreferences> notifOpt = companyPreferencesRepository.findByCompany(company);

                    if(notifOpt.isPresent()){
                        CompanyPreferences notif = notifOpt.get();
                        wantNewBugNotif = notif.isMailNewBug();
                    }

                    if(wantNewBugNotif){
                        /* Vérification du compte gratuit, si oui → si dernier envoi date de +30jours → envoi du mail avec détail
                         *   Si -30 jours, mail sans détails avec 'abonnez-vous pour le voir' */
                        if (company.getPlan().equals(EnumPlan.FREE) && envoi && savedBug.getEtatBug() != null) {

                            Response response = mailService.sendMailNewBugDetail(company.getMail(), savedBug);
                            if (response.getStatus().equals(EnumStatus.OK)) {
                                log.info("Mail gratuit envoyé avec détails");
                            }
                        } else if (company.getPlan().equals(EnumPlan.FREE)) {

                            Response response = mailService.sendMailNewBugNoDetail(company.getMail());
                            if (response.getStatus().equals(EnumStatus.OK)) {
                                log.info("Mail gratuit envoyé sans détails");
                            }
                        } else {
                            Response response = mailService.sendMailNewBugForNotification(company.getMail());
                            log.info("Mail new bug avec offre envoyé");

                        }
                    }

                } else {
                    String codeLoc = dto.getCodeLocation();
                    codeLoc = codeLoc.replace("<", "&lt;");
                    codeLoc = codeLoc.replace(">", "&gt;");
                    bug.setCodeLocation(codeLoc);
                    Response response = mailService.sendMailTest(dto.getMail(), bug);
                    if (response.getStatus().equals(EnumStatus.OK)) {
                        log.info("TestPage report send");
                    }
                }
                return new Response(EnumStatus.OK, null, "Envoie avec succès");
            }
            catch (Exception e){
                log.error("Error : {}", e.getMessage());
                return new Response(EnumStatus.ERROR, null, "Une erreur est survenue, merci de recommencer");
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
