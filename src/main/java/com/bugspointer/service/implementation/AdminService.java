package com.bugspointer.service.implementation;

import be.woutschoovaerts.mollie.Client;
import be.woutschoovaerts.mollie.data.common.Pagination;
import be.woutschoovaerts.mollie.data.mandate.MandateListResponse;
import be.woutschoovaerts.mollie.data.mandate.MandateResponse;
import be.woutschoovaerts.mollie.exception.MollieException;
import com.bugspointer.dto.*;
import com.bugspointer.entity.*;
import com.bugspointer.entity.enumLogger.Action;
import com.bugspointer.entity.enumLogger.What;
import com.bugspointer.repository.BugRepository;
import com.bugspointer.repository.AdminBillingRepository;
import com.bugspointer.repository.CompanyRepository;
import com.bugspointer.repository.HomeLoggerRepository;
import com.bugspointer.utility.Utility;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import java.util.stream.Collectors;


@Service
@Slf4j
public class AdminService {

    private final CompanyRepository companyRepository;

    private final HomeLoggerRepository homeLoggerRepository;

    private final BugRepository bugRepository;

    private final AdminBillingRepository adminBillingRepository;

    private final ModelMapper modelMapper;

    private final Client client;

    public AdminService(CompanyRepository companyRepository, HomeLoggerRepository homeLoggerRepository, BugRepository bugRepository, AdminBillingRepository adminBillingRepository, ModelMapper modelMapper, Client client) {
        this.companyRepository = companyRepository;
        this.homeLoggerRepository = homeLoggerRepository;
        this.bugRepository = bugRepository;
        this.adminBillingRepository = adminBillingRepository;
        this.modelMapper = modelMapper;
        this.client = client;
    }

    public List<AdminBilling> getBillings() {
        return adminBillingRepository.findAllByOrderByBillingDateDesc();
    }

    public Response saveBilling(AdminBillingDTO dto) {
        AdminBilling billing = modelMapper.map(dto, AdminBilling.class);
        try {
            adminBillingRepository.save(billing);
            return new Response(EnumStatus.OK, null, "Dépense ajoutée");
        } catch (Exception e) {
            log.error("Unable to save admin billing: {}", e.getMessage(), e);
            return new Response(EnumStatus.ERROR, null, "Impossible d'ajouter cette dépense");
        }
    }

    public BigDecimal getBillingTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (AdminBilling billing : getBillings()) {
            if (billing.getAmount() != null) {
                total = total.add(billing.getAmount());
            }
        }
        return total;
    }

    public long getPaidCompanyCount() {
        return StreamSupport.stream(companyRepository.findAll().spliterator(), false)
                .filter(company -> company.getPlan() != null && company.getPlan() != EnumPlan.FREE)
                .count();
    }

    public long getTotalCompanyCount() {
        return StreamSupport.stream(companyRepository.findAll().spliterator(), false).count();
    }

    public long getConfirmedCompanyCount() {
        return StreamSupport.stream(companyRepository.findAll().spliterator(), false)
                .filter(company -> company.getMotifEnable() == EnumMotif.VALIDATE)
                .count();
    }

    public long getFreeCompanyCount() {
        return getPlanCount("FREE");
    }

    public long getTargetCompanyCount() {
        return getPlanCount("TARGET");
    }

    public long getUltimateCompanyCount() {
        return getPlanCount("ULTIMATE");
    }

    private long getPlanCount(String planName) {
        return StreamSupport.stream(companyRepository.findAll().spliterator(), false)
                .filter(company -> company.getPlan() != null && company.getPlan().name().equals(planName))
                .count();
    }

    public long getVerifiedDomainCount() {
        return StreamSupport.stream(companyRepository.findAll().spliterator(), false)
                .filter(Company::isDomainVerified)
                .count();
    }

    public long getMissingDomainCount() {
        return StreamSupport.stream(companyRepository.findAll().spliterator(), false)
                .filter(company -> company.getDomaine() == null || company.getDomaine().trim().isEmpty())
                .count();
    }

    public Long getTotalBugCount() {
        return bugRepository.allBugCounted();
    }

    public long getBugCount(EnumEtatBug status) {
        if (status == null) {
            return 0;
        }
        Long count = bugRepository.countByEtatBug(status);
        return count != null ? count : 0;
    }

    public BigDecimal getEstimatedAnnualRevenue() {
        return BigDecimal.valueOf(getPaidCompanyCount()).multiply(BigDecimal.valueOf(15));
    }

    public BigDecimal getEstimatedProfit() {
        return getEstimatedAnnualRevenue().subtract(getBillingTotal());
    }

    public List<CompanyListDTO> getAllCompanyForList(){

        Iterable<Company> companies = companyRepository.findAll();

        List<CompanyListDTO> companiesDTO = new ArrayList<>();
        for(Company company : companies){
            CompanyListDTO compDTO = new CompanyListDTO();
            // Basics info
            compDTO.setCompanyId(company.getCompanyId());
            compDTO.setCompanyName(company.getCompanyName());
            compDTO.setDateDownload(Utility.dateFormator(company.getDateDownload(), "dd/MM/yyyy"));
            compDTO.setDomainVerified(company.isDomainVerified());
            compDTO.setCreationDate(Utility.dateFormator(company.getDateCreation(), "dd/MM/yyyy"));
            compDTO.setMotifEnable(StringUtils.capitalize(String.valueOf(company.getMotifEnable()).toLowerCase()));
            compDTO.setPlan(StringUtils.capitalize(String.valueOf(company.getPlan()).toLowerCase()));

            // Total bug
            List<Bug> news = bugRepository.findAllByCompanyAndEtatBug(company,EnumEtatBug.NEW);
            List<Bug> pending = bugRepository.findAllByCompanyAndEtatBug(company,EnumEtatBug.PENDING);
            List<Bug> solved = bugRepository.findAllByCompanyAndEtatBug(company,EnumEtatBug.SOLVED);
            List<Bug> ignored = bugRepository.findAllByCompanyAndEtatBug(company,EnumEtatBug.IGNORED);

            compDTO.setNbrTotalBug(news.size() + pending.size() + solved.size() + ignored.size());
            compDTO.setNbrSolved(solved.size() + ignored.size());

            companiesDTO.add(compDTO);
        }
        return companiesDTO;

    }

    public List<LogDTO> getAllLogByCompany(Long companyId){

        Optional<Company> company = companyRepository.findById(companyId);
        List<LogDTO> listOfLogs = new ArrayList<>();

        if(company.isPresent()){
            List<HomeLogger> dataLogs = homeLoggerRepository.findAllByCompanyId(companyId);

            for (HomeLogger log : dataLogs){
                String identifier = log.getIdentifier() == null ? "" : log.getIdentifier();
                String adjective = log.getAdjective() == null ? "" : String.valueOf(log.getAdjective());
                String raison = log.getRaison() == null ? "" : String.valueOf(log.getRaison());

                String logConstructor = "Company #"+ log.getCompanyId() + " "
                        + log.getAction() + " "
                        + log.getWhat() + " "
                        + identifier + " "
                        + adjective + " "
                        + raison;
                logConstructor = logConstructor.replace("VOID", "");

                LogDTO logFormated = new LogDTO();
                logFormated.setDate(String.valueOf(Utility.dateFormator(log.getDateLog(), "dd/MM/yyyy HH:mm")));
                logFormated.setLog(logConstructor.replace("  ", " ").toLowerCase());

                listOfLogs.add(logFormated);
            }
            return listOfLogs;
        }

        return listOfLogs;
    }

    public CompanyDetailsDTO getCompanyInfo(Long companyId) throws MollieException {

        CompanyDetailsDTO finalDTO = new CompanyDetailsDTO();

        Company company = companyRepository.findById(companyId).get();

        // Add company infos
        finalDTO = modelMapper.map(company, CompanyDetailsDTO.class);

        // Add bugs infos
        finalDTO.setNbNewBug(bugRepository.findAllByCompanyAndEtatBug(company, EnumEtatBug.NEW).size());
        finalDTO.setNbPendingBug(bugRepository.findAllByCompanyAndEtatBug(company, EnumEtatBug.PENDING).size());
        finalDTO.setNbSolvedBug(bugRepository.findAllByCompanyAndEtatBug(company, EnumEtatBug.SOLVED).size());
        finalDTO.setNbIgnoredBug(bugRepository.findAllByCompanyAndEtatBug(company, EnumEtatBug.IGNORED).size());


        // Add date last bug
        List<Bug> bugsList = bugRepository.findAllByCompany(company);

        bugsList = bugsList.stream().sorted(Comparator.comparing(Bug::getDateCreation).reversed()).collect(Collectors.toList());
        log.info("----- buglist after sorted : {}",bugsList);

        if(bugsList.size() != 0){
            Bug lastBug = bugsList.get(0);
            finalDTO.setLastReport(lastBug.getDateCreation());
        }

        // Customer section
        if(company.getCustomer() != null) {

            // Add customer infos
            finalDTO.setCustomerId(company.getCustomer().getCustomerId());

            List<MandateResponse> mandates = client.mandates().listMandates(company.getCustomer().getCustomerId()).getEmbedded().getMandates();

            if(mandates.size() > 0){
                // Add date last mandate
                finalDTO.setDateLastMandate(String.valueOf(mandates.get(0).getSignatureDate()));

                // Add mandatesList
                List<MandateDTO> mandateListDTO = new ArrayList<>();
                for (MandateResponse mandate : mandates) {
                    MandateDTO mandateDTO = new MandateDTO();
                    mandateDTO.setMandateId(mandate.getId());
                    mandateDTO.setStatus(String.valueOf(mandate.getStatus()));
                    mandateDTO.setSignatureDate(String.valueOf(mandate.getSignatureDate()));
                    mandateListDTO.add(mandateDTO);
                }

                finalDTO.setMandatesList(mandateListDTO);
            }

        }
        return finalDTO;
    }

    public Response changeEnableCompanyStatus(Long id){
        Optional<Company> companyOpt = companyRepository.findById(id);

        if(companyOpt.isPresent()){
            Company company = companyOpt.get();
            String motifLog = null;
            if(company.isEnable() == true){
                company.setEnable(false);
                company.setMotifEnable(EnumMotif.ADMIN);
                motifLog = "for isEnable, Account disable by ADMIN"; // TODO peut-être mettre un vrai raison ?
            } else {
                company.setEnable(true);
                company.setMotifEnable(EnumMotif.VALIDATE); //TODO peux-être mettre CONFIRMATION pour que le compte sois reconfirmer
                motifLog = "for isEnable. Account re-open";
            }

            try{
                companyRepository.save(company);
                Utility.saveLog(company.getCompanyId(), Action.UPDATE, What.ACCOUNT, motifLog, null, null);
                log.info(" Company#{} isEnable status is changed by admin",company.getCompanyId());
            } catch (Exception e){
                log.error("Impossible to change isEnable status for company #{} : {}", company.getCompanyId(), e.getMessage());
            }
        }
        return new Response(EnumStatus.ERROR, null, null);
    }

}
