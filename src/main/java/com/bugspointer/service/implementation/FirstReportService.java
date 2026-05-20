package com.bugspointer.service.implementation;

import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.FirstReportDTO;
import com.bugspointer.dto.Response;
import com.bugspointer.entity.Bug;
import com.bugspointer.entity.Company;
import com.bugspointer.entity.FirstReport;
import com.bugspointer.entity.enumLogger.Action;
import com.bugspointer.entity.enumLogger.What;
import com.bugspointer.repository.BugRepository;
import com.bugspointer.repository.CompanyRepository;
import com.bugspointer.repository.FirstReportRepository;
import com.bugspointer.utility.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class FirstReportService {

      private final FirstReportRepository firstReportRepository;

      private final CompanyRepository companyRepository;

      private final BugRepository bugRepository;

    public FirstReportService(FirstReportRepository firstReportRepository, CompanyRepository companyRepository, BugRepository bugRepository) {
        this.firstReportRepository = firstReportRepository;
        this.companyRepository = companyRepository;
        this.bugRepository = bugRepository;
    }

    public Response initFirstReport(String publicKey){
        Optional<Company> companyOptional = companyRepository.findByPublicKey(publicKey);
        if (!companyOptional.isPresent()) {
            return new Response(EnumStatus.ERROR, null, "Company not found");
        }
        return initFirstReport(companyOptional.get());
    }

    public Response initFirstReport(Company company){
        if (company == null || company.getCompanyId() == null) {
            return new Response(EnumStatus.ERROR, null, "Company not found");
        }

        FirstReport firstReport = firstReportRepository.findByCompanyId(company.getCompanyId()).orElse(new FirstReport());
        firstReport.setCompanyId(company.getCompanyId());
        firstReport.setCompanyName(company.getCompanyName());
        firstReport.setDomaine(company.getDomaine());

        if (!firstReport.isFirstReport()) {
            firstReport.setDateConfirm(company.getDomainVerifiedAt() != null ? company.getDomainVerifiedAt() : new Date());
        } else if (firstReport.getDateConfirm() == null) {
            firstReport.setDateConfirm(company.getDomainVerifiedAt() != null ? company.getDomainVerifiedAt() : new Date());
        }

        try{
            firstReportRepository.save(firstReport);
            Utility.saveLog(firstReport.getCompanyId(), Action.INITIALISE, What.REPORT, "table", null, null);
            log.info("Company #{} init firstReport",firstReport.getCompanyId());
            return new Response(EnumStatus.OK,null,null);
        } catch (Exception e){
            log.error("Impossible init firstReport table : {}", e.getMessage());
            return new Response(EnumStatus.ERROR, null, null);
        }
    }

    public void markFirstReportReceived(Company company, Date reportArrivalDate, String description) {
        if (company == null || company.getCompanyId() == null) {
            return;
        }

        FirstReport firstReport = firstReportRepository.findByCompanyId(company.getCompanyId()).orElse(new FirstReport());
        firstReport.setCompanyId(company.getCompanyId());
        firstReport.setCompanyName(company.getCompanyName());
        firstReport.setDomaine(company.getDomaine());
        if (firstReport.getDateConfirm() == null) {
            firstReport.setDateConfirm(company.getDomainVerifiedAt() != null ? company.getDomainVerifiedAt() : new Date());
        }

        if (!firstReport.isFirstReport()) {
            firstReport.setFirstReport(true);
            firstReport.setFirstSend(reportArrivalDate != null ? reportArrivalDate : new Date());
            firstReport.setFirstDescription(limitDescription(description));

            try {
                firstReportRepository.save(firstReport);
                log.info("Company #{} received first user report", firstReport.getCompanyId());
                Utility.saveLog(firstReport.getCompanyId(), Action.HAVE, What.REPORT, "first user report", null, null);
            } catch (Exception e) {
                log.error("Impossible to mark first user report for company #{}: {}", company.getCompanyId(), e.getMessage());
            }
        }
    }

    public Response saveReportSended(FirstReportDTO firstReportDTO){

        Optional<FirstReport> firstReportOptional = firstReportRepository.findById(firstReportDTO.getId());

        if(firstReportOptional.isPresent()){
            FirstReport firstReport = firstReportOptional.get();
            if(!firstReport.isFirstReport()){
                firstReport.setFirstReport(true);
                firstReport.setFirstDescription(firstReportDTO.getDescription());
                firstReport.setFirstSend(new Date());
            } else {
                firstReport.setSecondReport(true);
                firstReport.setSecondDescription(firstReportDTO.getDescription());
                firstReport.setSecondSend(new Date());
            }

            try{
                firstReportRepository.save(firstReport);
                log.info("Company #{} have a report by Bugspointer agent", firstReport.getCompanyId());
                Utility.saveLog(firstReport.getCompanyId(), Action.HAVE, What.REPORT, "by Bugspointer agent", null, null);
                return new Response(EnumStatus.OK, null, "");
            } catch (Exception e){
                log.error("Impossible to  : {}", e.getMessage());
            }
        }

        return new Response(EnumStatus.ERROR, null, "Impossible to save a firstReport state");
    }


    public List<FirstReportDTO> getCandidateForFirstReport(){

        List<FirstReportDTO> listCandidatesFormatted = new ArrayList<>();

        Iterable<FirstReport> listCandidates = firstReportRepository.findAll();

        for (FirstReport first : listCandidates){
            Optional<Company> companyOptional = companyRepository.findById(first.getCompanyId());
            if (companyOptional.isPresent()) {
                Company company = companyOptional.get();
                if (company.isDomainVerified() && bugRepository.countByCompany(company) == 0) {
                    FirstReportDTO dto = toDTO(first, company);
                    dto.setDateConfirm(company.getDomainVerifiedAt() != null ? company.getDomainVerifiedAt() : first.getDateConfirm());
                    listCandidatesFormatted.add(dto);
                }
            }
        }
        return listCandidatesFormatted;
    }

    public List<FirstReportDTO> getCandidateForSecondReport(){

        List<FirstReportDTO> listCandidatesFormatted = new ArrayList<>();

        Iterable<FirstReport> listCandidates = firstReportRepository.findAll();

        for (FirstReport first : listCandidates){
            Optional<Company> companyOptional = companyRepository.findById(first.getCompanyId());
            if (companyOptional.isPresent()) {
                Company company = companyOptional.get();
                if (company.isDomainVerified() && bugRepository.countByCompany(company) == 1) {
                    Optional<Bug> firstBug = bugRepository.findTopByCompanyOrderByDateCreationAsc(company);
                    FirstReportDTO dto = toDTO(first, company);
                    dto.setSend(firstBug.map(Bug::getDateCreation).orElse(first.getFirstSend()));
                    listCandidatesFormatted.add(dto);
                }
            }
        }
        return listCandidatesFormatted;
    }

    private FirstReportDTO toDTO(FirstReport first, Company company) {
        FirstReportDTO dto = new FirstReportDTO();
        dto.setId(first.getId());
        dto.setCompanyId(company.getCompanyId());
        dto.setCompanyName(company.getCompanyName());
        dto.setDomaine(formatDomainUrl(company.getDomaine() != null ? company.getDomaine() : first.getDomaine()));
        return dto;
    }

    private String formatDomainUrl(String domain) {
        if (domain == null || domain.trim().isEmpty()) {
            return "#";
        }
        if (domain.startsWith("http://") || domain.startsWith("https://")) {
            return domain;
        }
        return "https://" + domain;
    }

    private String limitDescription(String description) {
        if (description == null) {
            return null;
        }
        return description.length() > 500 ? description.substring(0, 500) : description;
    }

}
