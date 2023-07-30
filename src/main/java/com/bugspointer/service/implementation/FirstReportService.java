package com.bugspointer.service.implementation;

import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.FirstReportDTO;
import com.bugspointer.dto.Response;
import com.bugspointer.entity.Company;
import com.bugspointer.entity.FirstReport;
import com.bugspointer.entity.enumLogger.Action;
import com.bugspointer.entity.enumLogger.Raison;
import com.bugspointer.entity.enumLogger.What;
import com.bugspointer.repository.CompanyRepository;
import com.bugspointer.repository.FirstReportRepository;
import com.bugspointer.utility.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class FirstReportService {

      private final FirstReportRepository firstReportRepository;

      private final CompanyRepository companyRepository;

    public FirstReportService(FirstReportRepository firstReportRepository, CompanyRepository companyRepository) {
        this.firstReportRepository = firstReportRepository;
        this.companyRepository = companyRepository;
    }

    public Response initFirstReport(String publicKey){

        Company company = companyRepository.findByPublicKey(publicKey).get();

        FirstReport firstReport = new FirstReport();

        firstReport.setCompanyId(company.getCompanyId());
        firstReport.setCompanyName(company.getCompanyName());
        firstReport.setDateConfirm(new Date());

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

    public Response saveReportSended(FirstReportDTO firstReportDTO){

        Optional<FirstReport> firstReportOptional = firstReportRepository.findById(firstReportDTO.getCompanyId());

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

    /*public List<FirstReportDTO> getCandidateForFirstReport(){

        Date dateNow = new Date();

        List<FirstReport> listCandidates = firstReportRepository.findFirstCandidates();

    }*/

}
