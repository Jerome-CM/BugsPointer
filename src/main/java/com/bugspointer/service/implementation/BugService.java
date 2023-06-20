package com.bugspointer.service.implementation;

import com.bugspointer.entity.EnumPlan;
import com.bugspointer.dto.BugDTO;
import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.Response;
import com.bugspointer.entity.Bug;
import com.bugspointer.entity.Company;
import com.bugspointer.entity.EnumEtatBug;
import com.bugspointer.repository.BugRepository;
import com.bugspointer.repository.CompanyRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class BugService {

    private final BugRepository bugRepository;

    private final CompanyRepository companyRepository;

    private final ModelMapper modelMapper;

    public BugService(BugRepository bugRepository, CompanyRepository companyRepository, ModelMapper modelMapper) {
        this.bugRepository = bugRepository;
        this.companyRepository = companyRepository;
        this.modelMapper = modelMapper;
    }

    public String codeBlockFormatter(Long idBug){

        Optional<Bug> bug = bugRepository.findById(idBug);
        if(bug.isPresent()){
            String codeHtml = bug.get().getCodeLocation();
            // Charge HTML in Document JSoupo bjet
            Document document = Jsoup.parse(codeHtml);

            // Use output method to get indented HTML
            document.outputSettings().indentAmount(4).prettyPrint();

            // Get new HTML code
            String codeHtmlIndente = document.html();
            codeHtmlIndente = codeHtmlIndente.replace("<html>", "");
            codeHtmlIndente = codeHtmlIndente.replace("</html>", "");
            codeHtmlIndente = codeHtmlIndente.replace("<head>", "");
            codeHtmlIndente = codeHtmlIndente.replace("</head>", "");
            codeHtmlIndente = codeHtmlIndente.replace("<body>", "");
            codeHtmlIndente = codeHtmlIndente.replace("</body>", "");

            return codeHtmlIndente;
        }

        return "";
    }

    public String codeBlockFormatter(String code){

        if (code == null){
            return "";
        }
        // Charge HTML in Document JSoup objet
        Document document = Jsoup.parse(code);

        // Use output method to get indented HTML
        document.outputSettings().indentAmount(4).prettyPrint();

        // Get new HTML code
        String codeHtmlIndente = document.html();
        codeHtmlIndente = codeHtmlIndente.replace("<html>", "");
        codeHtmlIndente = codeHtmlIndente.replace("</html>", "");
        codeHtmlIndente = codeHtmlIndente.replace("<head>", "");
        codeHtmlIndente = codeHtmlIndente.replace("</head>", "");
        codeHtmlIndente = codeHtmlIndente.replace("<body>", "");
        codeHtmlIndente = codeHtmlIndente.replace("</body>", "");

        return codeHtmlIndente;

    }


    public Response viewBug(Long idBug, Long idCompany, EnumPlan plan){
        //Vérifier compte non FREE, company de bug correspondant à la company de la session

        if (plan != EnumPlan.FREE) {
            Optional<Bug> bugOptional = bugRepository.findById(idBug);
            Bug bug;
            if (bugOptional.isPresent()){
                bug = bugOptional.get();
                if (bug.getCompany().getCompanyId().equals(idCompany)){
                    return new Response(EnumStatus.OK, bug, "Détail bug");
                } else {
                    return new Response(EnumStatus.ERROR, null, "Ce bug ne vous appartient pas");
                }
            } else {
                return new Response(EnumStatus.ERROR, null, "Ce bug n'existe pas");
            }
        } else {
            return new Response(EnumStatus.ERROR, null, "Vous ne pouvez pas accéder au détail de bug");
        }
    }

    public void bugPending(Long idBug, Long idCompany, String plan){
        Optional<Bug> bugOptional = bugRepository.findById(idBug);
        Bug bug;
        if (bugOptional.isPresent() && bugOptional.get().getCompany().getCompanyId().equals(idCompany) && !plan.equals("FREE")) {
            bug = bugOptional.get();
            if (bug.getEtatBug().equals(EnumEtatBug.PENDING)){
                bugSolved(bug);
            } else {
                Date jour = new Date();
                if (bug.getDateView() == null) {
                    bug.setDateView(jour);
                }
                bug.setEtatBug(EnumEtatBug.PENDING);
                Bug bugSaved = bugRepository.save(bug);
                log.info("Bug update : id = {}, company = {}, etat = {} date view = {}", bugSaved.getId(), bugSaved.getCompany().getMail(), bugSaved.getEtatBug().name(), bugSaved.getDateView());
            }
        }
    }

    public void bugSolved(Bug bug){
        Date jour = new Date();
        if (bug.getDateSolved() == null) {
            bug.setDateSolved(jour);
            bug.setEtatBug(EnumEtatBug.SOLVED);
            Bug bugSaved = bugRepository.save(bug);
            log.info("Bug update : id = {}, company = {}, etat = {} date view = {}", bugSaved.getId(), bugSaved.getCompany().getMail(), bugSaved.getEtatBug().name(), bugSaved.getDateView());
        }
    }

    public void bugIgnored(Long idBug, Long idCompany, String plan) {
        Optional<Bug> bugOptional = bugRepository.findById(idBug);
        Bug bug;
        if (bugOptional.isPresent() && bugOptional.get().getCompany().getCompanyId().equals(idCompany) && !plan.equals("FREE")) {
            bug = bugOptional.get();

            Date jour = new Date();
            if (bug.getDateView() == null) {
                bug.setDateView(jour);
            }
            if (bug.getDateSolved() == null) {
                bug.setDateSolved(jour);
                bug.setEtatBug(EnumEtatBug.IGNORED);
                Bug bugSaved = bugRepository.save(bug);
                log.info("Bug update : id = {}, company = {}, etat = {} date view = {}", bugSaved.getId(), bugSaved.getCompany().getMail(), bugSaved.getEtatBug().name(), bugSaved.getDateView());
            }
        }
    }

    public String getTitle(String state, boolean isList){
        String title;
        if(isList){
            switch(state){
                case "new":
                    title = "Nouveaux rapports";
                    break;
                case "pending":
                    title = "Rapports en cours";
                    break;
                case "solved":
                    title = "Rapports résolus";
                    break;
                case "ignored":
                    title = "Rapports ignorés";
                    break;
                default:
                    title = null;
                    break;
            }
        } else {
            switch(state){
                case "new":
                    title = "Nouveau rapport";
                    break;
                case "pending":
                    title = "Rapport en cours";
                    break;
                case "solved":
                    title = "Rapport résolu";
                    break;
                case "ignored":
                    title = "Rapport ignoré";
                    break;
                default:
                    title = null;
                    break;
            }
        }

       return title;
    }

    public Response getBugDTOByCompanyAndState(String publicKey, String state){

        Optional<Company> company = companyRepository.findByPublicKey(publicKey);
        EnumEtatBug stateBug;

        switch(state){
            case "new":
                stateBug = EnumEtatBug.NEW;
                break;
            case "pending":
                stateBug = EnumEtatBug.PENDING;
                break;
            case "solved":
                stateBug = EnumEtatBug.SOLVED;
                break;
            case "ignored":
                stateBug = EnumEtatBug.IGNORED;
                break;
            default:
                stateBug = null;
                break;
        }

        if(company.isPresent() && stateBug != null){

            List<Bug> bugList = bugRepository.findAllByCompanyAndEtatBug(company.get(), stateBug);

            if(!bugList.isEmpty()){
                List<BugDTO> bugDTOList = new ArrayList<>();
                for (Bug bug : bugList) {
                    BugDTO bugDTO = modelMapper.map(bug, BugDTO.class);
                    bugDTOList.add(bugDTO);
                }
                return new Response(EnumStatus.OK, bugDTOList, "");
            }
            return new Response(EnumStatus.OK, null, "La liste des bugs pour cette compagnie est vide");
        }
        return new Response(EnumStatus.ERROR, null, "La compagnie ou l'état du bug n'est pas bon");

    }
}
