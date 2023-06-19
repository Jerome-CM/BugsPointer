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

    private final Utility utility;
    private final PasswordEncoder passwordEncoder;

    private final JwtTokenUtil jwtTokenUtil;

    private final ModelMapper modelMapper;

    public CompanyService(CompanyRepository companyRepository, BugRepository bugRepository, CompanyPreferencesRepository preferencesRepository, MailService mailService, Utility utility, PasswordEncoder passwordEncoder, JwtTokenUtil jwtTokenUtil, ModelMapper modelMapper) {
        this.companyRepository = companyRepository;
        this.bugRepository = bugRepository;
        this.preferencesRepository = preferencesRepository;
        this.mailService = mailService;
        this.utility = utility;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
        this.modelMapper = modelMapper;
    }

    public Company getCompanyByMail(String mail){//TODO: A transformer en DTO
        Optional<Company> company = companyRepository.findByMail(mail);
        Company companyGet = null;

        if (company.isPresent()){
            companyGet = company.get();
        }

        return companyGet;
    }

    public Company getCompanyByPublicKey(String publicKey){
        Optional<Company> companyOptional = companyRepository.findByPublicKey(publicKey);
        Company company = null;

        if (companyOptional.isPresent()){
            company = companyOptional.get();
        }

        return company;
    }

    public String createPublicKey() {
        return utility.createPublicKey(25);
    }

    @Override
    public Response saveCompany(AuthRegisterCompanyDTO dto) {
        log.info("SaveCompany : {}", dto);
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
                log.info("CompanyMail is exist :  {}", dto.getMail());
                return new Response(EnumStatus.ERROR, null, "Ce mail existe déjà");
            }
            else{
                mail = true;
            }
        } else {
            return new Response(EnumStatus.ERROR, null, "Les mails ne sont pas identiques");
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
            company.setMotifEnable(EnumMotif.CONFIRMATION);
            log.info("company :  {}", company);
            CompanyPreferences preferences = new CompanyPreferences();
            preferences.setCompany(company);
            preferences.setMailNewBug(true);
            preferences.setMailInactivity(true);
            log.info("company preferences : {}", preferences);
            try {
                Company savedCompany = companyRepository.save(company);
                log.info("Company saved :  {}", savedCompany);
                CompanyPreferences savedPreferences = preferencesRepository.save(preferences);
                log.info("Company preferences saved : {}", savedPreferences);
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

    public AccountDeleteDTO getAccountDeleteDto(Company company) {
        log.info("AccountDeleteDTO : company : {}", company);
        AccountDeleteDTO dto;
        dto = modelMapper.map(company, AccountDeleteDTO.class);
        int nbNewBug = bugRepository.findAllByCompanyAndEtatBug(company, EnumEtatBug.NEW).size();
        int nbPendingBug = bugRepository.findAllByCompanyAndEtatBug(company, EnumEtatBug.PENDING).size();
        log.info("nbNewBug : {}", nbNewBug);
        log.info("nbPendingBug : {}", nbNewBug);
        dto.setNbNewBug(nbNewBug);
        dto.setNbPendingBug(nbPendingBug);

        return dto;
    }

    public DashboardDTO getDashboardDto(Company company) {
        log.info("DashboardDTO : company : {}", company);
        DashboardDTO dto;
        dto = modelMapper.map(company, DashboardDTO.class);
        if (company.getPlan().equals(EnumPlan.FREE)){
            return dto;
        }
        int nbNewBug = bugRepository.findAllByCompanyAndEtatBug(company, EnumEtatBug.NEW).size();
        int nbPendingBug = bugRepository.findAllByCompanyAndEtatBug(company, EnumEtatBug.PENDING).size();
        int nbSolvedBug = bugRepository.findAllByCompanyAndEtatBug(company, EnumEtatBug.SOLVED).size();
        int nbIgnoredBug = bugRepository.findAllByCompanyAndEtatBug(company, EnumEtatBug.IGNORED).size();
        log.info("nbNewBug : {}", nbNewBug);
        log.info("nbPendingBug : {}", nbPendingBug);
        log.info("nbSolvedBug : {}", nbSolvedBug);
        log.info("nbIgnoredBug : {}", nbIgnoredBug);
        dto.setNbNewBug(nbNewBug);
        dto.setNbPendingBug(nbPendingBug);
        dto.setNbSolvedBug(nbSolvedBug);
        dto.setNbIgnoredBug(nbIgnoredBug);

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
                if (company.getMail().equals(dto.getMail())) {
                    log.info("Mail identique");
                    return new Response(EnumStatus.ERROR, null, "Mail identique");
                } else {
                    Optional<Company> companyMail = companyRepository.findByMail(dto.getMail());
                    if (companyMail.isPresent()){
                        log.info("Mail déjà utilisé");
                        return new Response(EnumStatus.ERROR, null, "Ce mail est déjà utilisée veuillez en saisir un autre");
                    }
                }
                log.info("Mail completed");
                mail = true;
            }
        }

        if (pw && mail) {
            log.info("mail at modify : {}", dto.getMail());
            company.setMail(dto.getMail());
            log.info("Company mail update : {}", company);

            return companyTryRegistration(company, "Mail updated");
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
                        return companyTryRegistration(company, "Password updated");
                    } else {
                        log.info("Password not identical");
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
                    log.info("Company update :  {}", savedCompany);
                    CompanyPreferences savedPreference = preferencesRepository.save(preferences);
                    log.info("Company preferences saved : {}", savedPreference);
                    return new Response(EnumStatus.OK, null, "Phone number delete");
                }
                catch (Exception e){
                    log.error("Error :  {}", e.getMessage());
                    return new Response(EnumStatus.ERROR, null, "Error in update");
                }
            } else {
                if (dto.getPhoneNumber().length()==10 && (dto.getPhoneNumber().startsWith("06") || dto.getPhoneNumber().startsWith("07"))) {
                    EnumIndicatif indicatif = dto.getIndicatif();
                    if (indicatif != null) {
                        company.setIndicatif(indicatif);
                    } else {
                        return new Response(EnumStatus.ERROR, null, "the code doesn't exist");
                    }

                    company.setPhoneNumber(dto.getPhoneNumber());
                    return companyTryRegistration(company, "Phone number updated");
                } else {
                    return new Response(EnumStatus.ERROR, null, "It's not a phone number");
                }
            }
        }

        return new Response(EnumStatus.ERROR, null, "Error in the process");
    }

    private Response companyTryRegistration(Company company, String message) {
        try {
            Company savedCompany = companyRepository.save(company);
            log.info("Company update :  {}", savedCompany);
            return new Response(EnumStatus.OK, null, message);
        }
        catch (Exception e){
            log.error("Error :  {}", e.getMessage());
            return new Response(EnumStatus.ERROR, null, "Error in update");
        }
    }

    public Company companyTryUpdateLastVisit(Company company) {
        try {
            Company savedCompany = companyRepository.save(company);
            log.info("Company update :  {} - {}", savedCompany.getCompanyId(), savedCompany.getLastVisit());
            return savedCompany;
        }
        catch (Exception e){
            log.error("Error :  {}", e.getMessage());
            return null;
        }
    }

    public Response delete(AccountDeleteDTO dto){
        log.info("delete Account");
        log.info("dto : {}", dto);

        Optional<Company> companyOptional=companyRepository.findByPublicKey(dto.getPublicKey());
        Company company;
        if (companyOptional.isPresent()){
            company = companyOptional.get();
            log.info("Company : {}", company);

            if (dto.getPassword().isEmpty()){
                return new Response(EnumStatus.ERROR, null, "Password empty");
            } else {
                if (passwordEncoder.matches(dto.getPassword(), company.getPassword())){
                    company.setEnable(false);//Mise en désactivé
                    company.setMotifEnable(EnumMotif.SUPPRESSION);//Désactivation lié à la suppression du compte par le client
                    company.setDateCloture(new Date());// Ajoute la date du jour de la suppression du compte
                    log.info("company at modify");
                    return companyTryRegistration(company, "Company disabled");
                } else {
                    return new Response(EnumStatus.ERROR, null, "Wrong password");
                }
            }
        }

        return new Response(EnumStatus.ERROR, null, "Error in the process");
    }

    public Response validateRegister(String publicKey){
        log.info("validate register :");
        log.info("publicKey : {}", publicKey);

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
        log.info("register domaine :");
        log.info("dto: {}", dto);

        Company company = getCompanyByPublicKey(dto.getPublicKey());
        log.info("company : {}", company);

        boolean isValid = Utility.domaineValidate.isValid(dto.getDomaine());

        if (!isValid){
            return new Response(EnumStatus.ERROR, null, "Le site renseigné ne correspond pas au format www.monsite.extension");
        }

        if (company != null){
            log.info("Company : {}", company);

            company.setDomaine(dto.getDomaine());
            return companyTryRegistration(company, "Nom de domaine enregistré");
        }

        return new Response(EnumStatus.ERROR, null, "Error in the process");
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
        log.info("Password Lost :");
        log.info("dto: {}", dto);

        Optional<Company> companyOptional = companyRepository.findByMail(dto.getMail());
        log.info("company : {}", companyOptional);
        Company company;

        if (companyOptional.isPresent()){
            company = companyOptional.get();
            return mailService.sendMailLostPassword(dto.getMail(), company.getPublicKey());
        }

        return new Response(EnumStatus.OK, null, "Si votre mail nous est connu, un mail vient d'être envoyé à : "+dto.getMail());
    }

    public Response resetPassword(String publicKey, AccountDTO dto){
        log.info("resetPasseword : ");
        log.info("publicKey : {}", publicKey);
        log.info("dto : {}", dto);

        Company company = getCompanyByPublicKey(publicKey);

        if (company != null){
            if (dto.getPassword().isEmpty() || dto.getConfirmPassword().isEmpty()) {
                return new Response(EnumStatus.ERROR, null, "Password empty");
            } else {
                if (dto.getPassword().equals(dto.getConfirmPassword())) {
                    company.setPassword(passwordEncoder.encode(dto.getPassword()));
                    log.info("company modified :  {}", company);
                    return companyTryRegistration(company, "Password updated");
                } else {
                    log.info("Password not identical");
                    return new Response(EnumStatus.ERROR, null, "Password not identical");
                }
            }
        }

        return new Response(EnumStatus.ERROR, null, "Error in the process");
    }

    public Response updatePlan(Company company, Date dateLine, EnumPlan plan){
        log.info("Plan : {}", plan);
        log.info("DateLine Facture : {}", dateLine);
        company.setPlan(plan);
        company.setDateLineFacturePlan(dateLine);

        return companyTryRegistration(company, "Plan update");

    }
}
