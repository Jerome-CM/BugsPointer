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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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

        Date date = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        calendar.add(Calendar.DAY_OF_MONTH, -3);
        String dateLessThreeDay = dateFormat.format(calendar.getTime());

        calendar.add(Calendar.DAY_OF_MONTH, -10);
        String dateLessTenDay = dateFormat.format(calendar.getTime());

        List<FirstReport> listCandidates = firstReportRepository.findFirstCandidates(dateLessTenDay, dateLessThreeDay);
        log.info("jointure : {}", listCandidates);

        for (FirstReport first : listCandidates){
            FirstReportDTO dto = new FirstReportDTO();
            dto.setId(first.getId());
            dto.setCompanyId(first.getCompanyId());
            dto.setCompanyName(first.getCompanyName());
            dto.setDomaine("http://www."+first.getDomaine());
            dto.setSendIsChecked(first.isFirstReport());
            dto.setDescription(first.getFirstDescription());
            listCandidatesFormatted.add(dto);
        }
        return listCandidatesFormatted;
    }


    public List<FirstReportDTO> getCandidateForSecondReport(){

        List<FirstReportDTO> listCandidatesFormatted = new ArrayList<>();

        Date date = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        calendar.add(Calendar.DAY_OF_MONTH, -15);
        String dateLessThreeDay = dateFormat.format(calendar.getTime());

        calendar.add(Calendar.DAY_OF_MONTH, -25);
        String dateLessTenDay = dateFormat.format(calendar.getTime());

        List<FirstReport> listCandidates = firstReportRepository.findFirstCandidates(dateLessTenDay, dateLessThreeDay);
        log.info("jointure : {}", listCandidates);

        for (FirstReport first : listCandidates){
            FirstReportDTO dto = new FirstReportDTO();
            dto.setId(first.getId());
            dto.setCompanyId(first.getCompanyId());
            dto.setCompanyName(first.getCompanyName());
            dto.setDomaine("http://www."+first.getDomaine());
            dto.setSendIsChecked(first.isFirstReport());
            dto.setDescription(first.getFirstDescription());
            listCandidatesFormatted.add(dto);
        }
        return listCandidatesFormatted;
    }

}
