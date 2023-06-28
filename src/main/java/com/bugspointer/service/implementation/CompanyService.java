package com.bugspointer.service.implementation;

import com.bugspointer.dto.*;
import com.bugspointer.entity.*;
import com.bugspointer.jwtConfig.JwtTokenUtil;
import com.bugspointer.repository.BugRepository;
import com.bugspointer.repository.CompanyPreferencesRepository;
import com.bugspointer.service.ICompany;
import com.bugspointer.repository.CompanyRepository;
import com.bugspointer.utility.Utility;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
public class CompanyService implements ICompany {

    private final CompanyRepository companyRepository;
    private final BugRepository bugRepository;
    private final CompanyPreferencesRepository preferencesRepository;

    private final MailService mailService;

    private final CompanyTokenService companyTokenService;

    private final Utility utility;
    private final PasswordEncoder passwordEncoder;

    private final JwtTokenUtil jwtTokenUtil;

    private final ModelMapper modelMapper;

    public CompanyService(CompanyRepository companyRepository, BugRepository bugRepository, CompanyPreferencesRepository preferencesRepository, MailService mailService, CompanyTokenService companyTokenService, Utility utility, PasswordEncoder passwordEncoder, JwtTokenUtil jwtTokenUtil, ModelMapper modelMapper) {
        this.companyRepository = companyRepository;
        this.bugRepository = bugRepository;
        this.preferencesRepository = preferencesRepository;
        this.mailService = mailService;
        this.companyTokenService = companyTokenService;
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
        //TODO: A transformer en DTO
        return companyGet;
    }

    public Company getCompanyByPublicKey(String publicKey){
        Optional<Company> companyOptional = companyRepository.findByPublicKey(publicKey);
        Company company = null;

        if (companyOptional.isPresent()){
            company = companyOptional.get();
        }
        //TODO: A transformer en DTO
        return company;
    }

    public String createPublicKey() {
        return utility.createPublicKey(25);
    }

    @Override
    public Response saveCompany(AuthRegisterCompanyDTO dto) {
        log.warn("Want save company : name : {}, mail : {},", dto.getCompanyName(), dto.getMail());
        boolean mail;
        boolean pw;
        boolean name;

        if (dto.getCompanyName().isEmpty() || dto.getMail().isEmpty() ||
        dto.getPassword().isEmpty()) {
            return new Response(EnumStatus.ERROR, null, "Merci de compléter tous les champs");
        }

        if (dto.getPassword().equals(dto.getConfirmPassword())){
            pw = true;
        } else {
            return new Response(EnumStatus.ERROR, null, "Les mots de passes ne sont pas identiques");
        }

        if (dto.getMail().equals(dto.getConfirmMail())) {
            Optional<Company> companyOptional = companyRepository.findByMail(dto.getMail());
            if(companyOptional.isPresent()){
                if (!companyOptional.get().isEnable()) {
                    log.warn("Company is disable : {}",companyOptional.get().getMail());
                    return new Response(EnumStatus.ERROR, null, "Le compte existe mais est fermé, pour le réactiver veuillez envoyer un mail à contact@bugspointer.com");
                }
                return new Response(EnumStatus.ERROR, null, "Ce mail existe déjà");
            }
            else{
                mail = true;
            }
        } else {
            return new Response(EnumStatus.ERROR, null, "Les mails ne sont pas identiques");
        }

        //TODO optimisation à faire entre les 2 recherches de company, besoin d'unicité pour le nom ???
        Optional<Company> companyOptional = companyRepository.findByCompanyName(dto.getCompanyName());
        if (companyOptional.isPresent()){
            log.info("CompanyName is exist :  {}", dto.getCompanyName());
            return new Response(EnumStatus.ERROR, null, "Ce nom d'entreprise existe déjà");
        } else {
            name = true;
        }

        if(mail && pw && name){
            Company company = new Company();
            company.setCompanyName(dto.getCompanyName());
            company.setMail(dto.getMail());
            company.setPassword(passwordEncoder.encode(dto.getPassword()));
            company.setPublicKey(createPublicKey());
            company.setMotifEnable(EnumMotif.CONFIRMATION);
            CompanyPreferences preferences = new CompanyPreferences();
            preferences.setCompany(company);
            preferences.setMailNewBug(true);
            preferences.setMailInactivity(true);
            try {
                Company savedCompany = companyRepository.save(company);
                log.info("\r -------  ");
                log.info("New Company : #{} - {}", savedCompany.getCompanyId(), savedCompany.getCompanyName());
                log.info("\r -------  ");
                CompanyPreferences savedPreferences = preferencesRepository.save(preferences);

                // TODO BETA Auto connect joindre le mail a UserDetailsServiceJwt

                return new Response(EnumStatus.OK, null, "Compte enregistré, merci. Vous pouvez maintenant vous connecter");
            }
            catch (Exception e){
                log.error("Error to save a new company: {}", e.getMessage());
                return new Response(EnumStatus.ERROR, null, "Erreur lors de l'enregistrement");
            }
        }
        return new Response(EnumStatus.ERROR, null, "Une erreur est présente, merci de recommencer");
    }

    public Company getCompanyWithToken(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String token = (String) session.getAttribute("token");

        String realToken = token.substring(7);

        Optional<Company> companyOptional = companyRepository.findByMail(jwtTokenUtil.getUsernameFromToken(realToken));
        if (companyOptional.isPresent()){
            return companyOptional.get();
        }

        return null;
    }

    public AccountDTO getAccountDto(Company company) {
        AccountDTO dto;
        dto = modelMapper.map(company, AccountDTO.class);
        return dto;
    }

    public AccountDeleteDTO getAccountDeleteDto(Company company) {
        AccountDeleteDTO dto;
        dto = modelMapper.map(company, AccountDeleteDTO.class);
        int nbNewBug = bugRepository.findAllByCompanyAndEtatBug(company, EnumEtatBug.NEW).size();
        int nbPendingBug = bugRepository.findAllByCompanyAndEtatBug(company, EnumEtatBug.PENDING).size();
        dto.setNbNewBug(nbNewBug);
        dto.setNbPendingBug(nbPendingBug);

        return dto;
    }

    public DashboardDTO getDashboardDto(Company company) {
        DashboardDTO dto;
        dto = modelMapper.map(company, DashboardDTO.class);
        if (company.getPlan().equals(EnumPlan.FREE)){
            return dto;
        }
        int nbNewBug = bugRepository.findAllByCompanyAndEtatBug(company, EnumEtatBug.NEW).size();
        int nbPendingBug = bugRepository.findAllByCompanyAndEtatBug(company, EnumEtatBug.PENDING).size();
        int nbSolvedBug = bugRepository.findAllByCompanyAndEtatBug(company, EnumEtatBug.SOLVED).size();
        int nbIgnoredBug = bugRepository.findAllByCompanyAndEtatBug(company, EnumEtatBug.IGNORED).size();
        dto.setNbNewBug(nbNewBug);
        dto.setNbPendingBug(nbPendingBug);
        dto.setNbSolvedBug(nbSolvedBug);
        dto.setNbIgnoredBug(nbIgnoredBug);

        return dto;
    }

    public Response mailUpdate(AccountDTO dto) {
        log.info("mailUpdate dto received : {}", dto.getMail());
        boolean mail = false;
        boolean pw = false;

        Optional<Company> companyOptional = companyRepository.findByPublicKey(dto.getPublicKey());
        Company company = new Company();
        if (companyOptional.isPresent()){
            company = companyOptional.get();
            if (dto.getPassword().isEmpty()){
                return new Response(EnumStatus.ERROR, null, "Mot de passe non rempli");
            } else {
                if (passwordEncoder.matches(dto.getPassword(), company.getPassword())){
                    pw = true;
                }
                else {
                    log.info("Error password to update mail");
                    return new Response(EnumStatus.ERROR, null, "Mauvais mot de passe");
                }
            }

            if (dto.getMail().isEmpty()) {
                return new Response(EnumStatus.ERROR, null, "Mail non rempli");
            } else {
                if (company.getMail().equals(dto.getMail())) {
                    return new Response(EnumStatus.ERROR, null, "Nouveau mail identique à l'ancien");
                } else {
                    Optional<Company> companyMail = companyRepository.findByMail(dto.getMail());
                    if (companyMail.isPresent()){
                        log.info("Mail already taken : {}", companyMail.get().getMail());
                        return new Response(EnumStatus.ERROR, null, "Impossible d'utiliser ce mail, merci d'en saisir un autre");
                    }
                }
                mail = true;
            }
        }

        if (pw && mail) {
            company.setMail(dto.getMail());
            return companyTryRegistration(company, "Mail modifié avec succès");
        }
        return new Response(EnumStatus.ERROR, null, "Erreur inconnue");
    }

    public Response passwordUpdate(AccountDTO dto) {
        log.info("passwordUpdate method");

        Optional<Company> companyOptional = companyRepository.findByPublicKey(dto.getPublicKey());
        Company company;
        if (companyOptional.isPresent()) {
            company = companyOptional.get();
            if (dto.getPassword().isEmpty() || dto.getNewPassword().isEmpty() || dto.getConfirmPassword().isEmpty()) {
                return new Response(EnumStatus.ERROR, null, "Un ou plusieurs champs ne sont pas remplis");
            } else {
                if (passwordEncoder.matches(dto.getPassword(), company.getPassword())) {
                    if (dto.getNewPassword().equals(dto.getConfirmPassword())){
                        company.setPassword(passwordEncoder.encode(dto.getNewPassword()));

                        return companyTryRegistration(company, "Mot de passe modifié avec succès");
                    } else {
                        return new Response(EnumStatus.ERROR, null, "Mot de passe non identique");
                    }
                } else {
                    return new Response(EnumStatus.ERROR, null, "Mauvais mot de passe");
                }
            }
        }

        return new Response(EnumStatus.ERROR, null, "Erreur inconnue");
    }

    public Response smsUpdate(AccountDTO dto){
        log.info("SmsUpdate dto received : {}", dto.getPhoneNumber());

        Optional<Company> companyOptional = companyRepository.findByPublicKey(dto.getPublicKey());
        Company company;
        if (companyOptional.isPresent()) {
            company = companyOptional.get();
            if (dto.getPhoneNumber().isEmpty()){
                company.setPhoneNumber(null);
                Optional<CompanyPreferences> preferencesOptional = preferencesRepository.findByCompany_PublicKey(dto.getPublicKey());
                CompanyPreferences preferences = new CompanyPreferences();
                if (preferencesOptional.isPresent()) {
                    preferences = preferencesOptional.get();
                } else {
                    preferences.setCompany(company);
                    preferences.setMailNewBug(true);
                }
                preferences.setSmsInactivity(false);
                preferences.setSmsNewBug(false);
                preferences.setSmsNewFeature(false);

                try {
                    Company savedCompany = companyRepository.save(company);
                    CompanyPreferences savedPreference = preferencesRepository.save(preferences);
                    return new Response(EnumStatus.OK, null, "Préférences mises à jour");
                }
                catch (Exception e){
                    log.error("Error to update phone number for sms : {}", e.getMessage());
                    return new Response(EnumStatus.ERROR, null, "Erreur dans la mise à jour");
                }
            } else {
                if (dto.getPhoneNumber().length()==10 && (dto.getPhoneNumber().startsWith("06") || dto.getPhoneNumber().startsWith("07"))) {
                    EnumIndicatif indicatif = dto.getIndicatif();
                    if (indicatif != null) {
                        company.setIndicatif(indicatif);
                    } else {
                        return new Response(EnumStatus.ERROR, null, "Cet indicatif n'existe pas chez nous");
                    }

                    company.setPhoneNumber(dto.getPhoneNumber());
                    return companyTryRegistration(company, "Numéro de téléphone mis à jour");
                } else {
                    return new Response(EnumStatus.ERROR, null, "Merci de joindre un numéro valide");
                }
            }
        }

        return new Response(EnumStatus.ERROR, null, "Erreur inconnue");
    }

    private Response companyTryRegistration(Company company, String message) {
        try {
            Company savedCompany = companyRepository.save(company);
            log.info("Company #{} - {}", savedCompany.getCompanyId(), message);
            return new Response(EnumStatus.OK, null, message);
        }
        catch (Exception e){
            log.error("Error : {}", e.getMessage());
            return new Response(EnumStatus.ERROR, null, "Erreur lors de la mise à jour");
        }
    }

    public Company companyTryUpdateLastVisit(Company company) {
        try {
            Company savedCompany = companyRepository.save(company);
            log.info("Company #{} connected at {}", savedCompany.getCompanyId(), savedCompany.getLastVisit());
            return savedCompany;
        }
        catch (Exception e){
            log.error("Error : {}", e.getMessage());
            return null;
        }
    }

    public Response delete(AccountDeleteDTO dto){


        Optional<Company> companyOptional=companyRepository.findByPublicKey(dto.getPublicKey());
        Company company;
        if (companyOptional.isPresent()){
            log.info("Company #{} want delete your account", companyOptional.get().getCompanyId());
            company = companyOptional.get();
            if (dto.getPassword().isEmpty()){
                return new Response(EnumStatus.ERROR, null, "Mot de passe vide");
            } else {
                if (passwordEncoder.matches(dto.getPassword(), company.getPassword())){
                    company.setEnable(false);//Mise en désactivé
                    company.setMotifEnable(EnumMotif.SUPPRESSION);//Désactivation liée à la suppression du compte par le client
                    company.setDateCloture(new Date());// Ajoute la date du jour de la suppression du compte

                    return companyTryRegistration(company, "Compte désactivé");
                } else {
                    return new Response(EnumStatus.ERROR, null, "Mauvais mot de passe");
                }
            }
        }

        return new Response(EnumStatus.ERROR, null, "Erreur inconnue");
    }

    public Response validateRegister(String publicKey){
        Company company = getCompanyByPublicKey(publicKey);

        if (company != null) {
            if (!company.isEnable() && company.getMotifEnable() == EnumMotif.CONFIRMATION){
                company.setEnable(true);
                company.setMotifEnable(EnumMotif.VALIDATE);

                return companyTryRegistration(company, "Compte validé");
            }

            return new Response(EnumStatus.ERROR, null, "Ce compte a déjà été validé, veuillez vous connecter");
        }

        return new Response(EnumStatus.ERROR, null, "Echec de la validation");
    }

    public Response registerDomaine(AccountDTO dto){

        Company company = getCompanyByPublicKey(dto.getPublicKey());

        boolean isValid = Utility.domaineValidate.isValid(dto.getDomaine());

        if (!isValid){
            return new Response(EnumStatus.ERROR, null, "Le site renseigné ne correspond pas au format www.monsite.extension");
        }

        if (company != null){
            company.setDomaine(dto.getDomaine());
            return companyTryRegistration(company, "Nom de domaine enregistré");
        }

        return new Response(EnumStatus.ERROR, null, "Erreur inconnue");
    }

    public Response updateDomaine(AccountDTO dto){

        Company company = getCompanyByPublicKey(dto.getPublicKey());

        if (passwordEncoder.matches(dto.getPassword(), company.getPassword())){
            return registerDomaine(dto);
        } else {
            return new Response(EnumStatus.ERROR, null, "Erreur de mot de passe");
        }
    }

    public Response sendPwLost(AccountDTO dto){

        Optional<Company> companyOptional = companyRepository.findByMail(dto.getMail());
        Company company;

        if (companyOptional.isPresent()){
            company = companyOptional.get();
            String token = utility.createPublicKey(60);
            companyTokenService.saveCompanyToken(company, token);
            log.info("Company #{} get new password", company.getCompanyId());
            return mailService.sendMailLostPassword(dto.getMail(), company.getPublicKey(), token);
        }

        return new Response(EnumStatus.OK, null, "Si votre mail nous est connu, un mail vient d'être envoyé à : "+dto.getMail());
    }

    public Response resetPassword(String publicKey, AccountDTO dto, String token){
        Company company = getCompanyByPublicKey(publicKey);
        boolean checkToken = companyTokenService.checkToken(token, publicKey);

        if (checkToken) {
            if (company != null) {
                if (dto.getPassword().isEmpty() || dto.getConfirmPassword().isEmpty()) {
                    return new Response(EnumStatus.ERROR, null, "Mot de passe vide");
                } else {
                    if (dto.getPassword().equals(dto.getConfirmPassword())) {
                        company.setPassword(passwordEncoder.encode(dto.getPassword()));
                        companyTokenService.deleteToken(company);
                        return companyTryRegistration(company, "Mot de passe modifié");
                    } else {
                        return new Response(EnumStatus.ERROR, null, "Mots de passes différents");
                    }
                }
            } else {
                return new Response(EnumStatus.ERROR, null, "Erreur inconnue");
            }
        } else {
            return new Response(EnumStatus.ERROR, null, "Votre demande de renouvellement de mot de passe n'a pas été prise en compte");
        }


    }

    public Response updatePlan(Company company, Date dateLine, EnumPlan plan){
        log.info("Company #{} want change plan : {} with dateLineFacture : {}", company.getCompanyId(), plan, dateLine);
        company.setPlan(plan);
        company.setDateLineFacturePlan(dateLine);

        return companyTryRegistration(company, "Offre modifié avec succès");

    }
}
