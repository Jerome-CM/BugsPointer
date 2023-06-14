package com.bugspointer.service.implementation;

import com.bugspointer.entity.Link;
import com.bugspointer.entity.LinkType;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScrapService {

    private static final int MAX_PAGES_TO_CRAWL = 200;

    private static Set<String> visitedUrls = new HashSet<>();

    private static List<String> errorLinks = new ArrayList<>();

    public static List<Link> crawlPage(String url) {
        List<Link> extractedLinks = new ArrayList<>();

        if (!visitedUrls.contains(url)) {
            visitedUrls.add(url);
            extractedLinks.addAll(processPage(url));

            try {
                RequestConfig requestConfig = RequestConfig.custom()
                        .setConnectTimeout(5000)  // Temps d'attente de connexion
                        .setSocketTimeout(5000)   // Temps d'attente pour les opérations de lecture/écriture
                        .build();

                CloseableHttpClient httpClient = HttpClients.custom()
                        .disableAutomaticRetries()  // Désactiver la stratégie de réessai
                        .setDefaultRequestConfig(requestConfig)
                        .build();

                Document document = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
                        .get();

                Elements links = document.select("a[href]");
                Elements images = document.select("img[src]");

                for (Element link : links) {
                    String linkHref = link.attr("abs:href");
                    LinkType linkType = getLinkType(url, linkHref);
                    int responseCode = getResponseCode(httpClient, linkHref);
                    Link linkObj = new Link(linkHref, responseCode, linkType);
                    extractedLinks.add(linkObj);
                }

                for (Element image : images) {
                    String imageSrc = image.attr("abs:src");
                    int responseCode = getResponseCode(httpClient, imageSrc);
                    Link imageLink = new Link(imageSrc, responseCode, LinkType.IMAGE);
                    extractedLinks.add(imageLink);
                }

                // Trier les liens par type
                extractedLinks.sort(Comparator.comparing(Link::getType));

                List<Link> uniqueLinks = extractedLinks.stream()
                        .distinct()
                        .collect(Collectors.toList());

                return uniqueLinks;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }


    private static void crawlPageRecursively(String url) throws IOException {
        if (visitedUrls.size() >= MAX_PAGES_TO_CRAWL) {
            return;
        }

        crawlPage(url);
    }

    private static List<Link> processPage(String url) {
        List<Link> extractedLinks = new ArrayList<>();

        try {
            Document document = Jsoup.connect(url).get();
            Elements links = document.select("a[href]");
            Elements images = document.select("img[src]");

            for (Element link : links) {
                String linkHref = link.attr("abs:href");
                int responseCode = getResponseCode(linkHref);
                LinkType linkType = getLinkType(url, linkHref);
                Link linkObj = new Link(linkHref, responseCode, linkType);
                extractedLinks.add(linkObj);
            }

            for (Element image : images) {
                String imageSrc = image.attr("abs:src");
                int responseCode = getResponseCode(imageSrc);
                Link imageLink = new Link(imageSrc, responseCode, LinkType.IMAGE);
                extractedLinks.add(imageLink);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return extractedLinks;
    }

    private static int getResponseCode(CloseableHttpClient httpClient, String url) throws IOException {
        HttpHead httpHead = new HttpHead(url);
        try (CloseableHttpResponse response = httpClient.execute(httpHead)) {
            return response.getStatusLine().getStatusCode();
        }
    }

    private static int getResponseCode(String url) throws IOException {
        return getResponseCode(HttpClients.createDefault(), url);
    }

    private static LinkType getLinkType(String baseUrl, String linkUrl) {
        if (linkUrl.startsWith(baseUrl)) {
            return LinkType.INTERNAL;
        } else if (linkUrl.contains("://")) {
            return LinkType.EXTERNAL;
        } else if (linkUrl.toLowerCase().endsWith(".pdf")) {
            return LinkType.PDF;
        } else if (linkUrl.toLowerCase().endsWith(".zip")) {
            return LinkType.ZIP;
        } else if (linkUrl.toLowerCase().endsWith(".css")) {
            return LinkType.CSS;
        } else if (linkUrl.toLowerCase().endsWith(".js")) {
            return LinkType.JS;
        } else {
            return LinkType.INTERNAL;
        }
    }
}
