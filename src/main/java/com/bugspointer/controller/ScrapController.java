package com.bugspointer.controller;

import com.bugspointer.entity.Link;
import com.bugspointer.service.implementation.ScrapService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Date;
import java.util.List;


@RestController
public class ScrapController {

    private final ScrapService scrapService;

    public ScrapController(ScrapService scrapService) {
        this.scrapService = scrapService;
    }

    @GetMapping(value="/scrap")
    public List<Link> getLinkAndImage(){

        System.out.println(new Date());
    /*
        String siteUrl = "https://www.community-technologie.fr"; // Remplacez l'URL par le site que vous souhaitez parcourir

        try {
            Document document = Jsoup.connect(siteUrl).get();

            // Extraire les liens HTML
            Elements links = document.select("a[href]");
            for (Element link : links) {
                String linkText = link.text();
                String linkHref = link.attr("abs:href");

                if (isInternalLink(siteUrl, linkHref)) {
                    System.out.println("Lien : " + linkText);
                    System.out.println("Adresse : " + linkHref);
                    System.out.println();
                }
            }

            // Extraire les adresses des images
            Elements images = document.select("img[src]");
            for (Element image : images) {
                String imageSrc = image.attr("abs:src");
                System.out.println("Image : " + imageSrc);
                System.out.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isInternalLink(String siteUrl, String link) {
        try {
            URI siteUri = new URI(siteUrl);
            URI linkUri = new URI(link);
            return siteUri.getHost().equals(linkUri.getHost());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return false;

    }*/
        List<Link> extractedLinks = scrapService.crawlPage("https://www.axereal.com/");
        //ToDO Dans crawPage, prévoir que les liens internal sont ceux qui commence par les 14 premiers caractéres
        //TODO Impossible d'arrêter les retry de connexion 443.
        return extractedLinks;
    }
}


