package com.bugspointer.service.implementation;

import com.bugspointer.entity.Bug;
import com.bugspointer.repository.BugRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Optional;

@Service
@Slf4j
public class BugService {

    private final BugRepository bugRepository;

    public BugService(BugRepository bugRepository) {
        this.bugRepository = bugRepository;
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
}
