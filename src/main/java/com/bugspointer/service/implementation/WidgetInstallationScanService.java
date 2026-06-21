package com.bugspointer.service.implementation;

import com.bugspointer.dto.WidgetInstallationScanDTO;
import com.bugspointer.entity.Company;
import com.bugspointer.entity.EnumPlan;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;

@Service
public class WidgetInstallationScanService {

    private static final int TIMEOUT_MS = 8000;
    private static final String USER_AGENT = "Bugspointer installation scanner";

    public WidgetInstallationScanDTO scan(Company company) {
        WidgetInstallationScanDTO result = new WidgetInstallationScanDTO();
        if (company == null || company.getDomaine() == null || company.getDomaine().trim().isEmpty()) {
            result.setErrorMessage("Aucun domaine n'est enregistré pour ce compte.");
            return result;
        }

        String startUrl = normalizeStartUrl(company.getDomaine());
        result.setDomain(startUrl);
        result.setPublicKey(company.getPublicKey());

        URI startUri = normalizeUrl(startUrl);
        if (startUri == null || !isHttpUrl(startUri)) {
            result.setErrorMessage("Le domaine enregistré n'est pas une URL valide.");
            return result;
        }

        int pageLimit = company.getPlan() == EnumPlan.FREE
                ? AdminScraperService.FREE_INTERNAL_PAGE_LIMIT
                : AdminScraperService.TARGET_INTERNAL_PAGE_LIMIT;

        Queue<String> pagesToVisit = new ArrayDeque<>();
        Set<String> visitedPages = new HashSet<>();
        pagesToVisit.add(startUri.toString());

        while (!pagesToVisit.isEmpty() && visitedPages.size() < pageLimit) {
            String pageUrl = pagesToVisit.poll();
            if (!visitedPages.add(pageUrl)) {
                continue;
            }

            Document document;
            try {
                document = fetchDocument(pageUrl);
            } catch (IOException | IllegalArgumentException e) {
                if (visitedPages.size() == 1) {
                    result.setErrorMessage("Impossible de scanner la page d'accueil: " + e.getMessage());
                }
                continue;
            }

            result.setCheckedPageCount(visitedPages.size());
            if (containsWidget(document, company.getPublicKey())) {
                result.getWidgetUrls().add(pageUrl);
            }
            if (containsBugspointerLink(document, company.getPublicKey())) {
                result.getLinkUrls().add(pageUrl);
            }
            collectInternalPages(document, startUri, visitedPages, pagesToVisit);
        }

        result.setCheckedPageCount(visitedPages.size());
        result.setLimitReached(!pagesToVisit.isEmpty() || visitedPages.size() >= pageLimit);
        return result;
    }

    private Document fetchDocument(String url) throws IOException {
        return configureRequest(Jsoup.connect(url))
                .followRedirects(true)
                .ignoreHttpErrors(false)
                .get();
    }

    private boolean containsWidget(Document document, String publicKey) {
        for (Element script : document.select("script[src]")) {
            String src = script.absUrl("src");
            String rawSrc = script.attr("src");
            String key = script.attr("data-public-key");
            boolean isBugspointerScript = containsIgnoreCase(src, "modalPointer.js") || containsIgnoreCase(rawSrc, "modalPointer.js");
            boolean hasCurrentKey = publicKey != null && publicKey.equals(key);
            if (isBugspointerScript && hasCurrentKey) {
                return true;
            }
        }
        return false;
    }

    private boolean containsBugspointerLink(Document document, String publicKey) {
        for (Element element : document.select("[data-bugspointer-open], [data-bugspointer-key]")) {
            String key = element.attr("data-bugspointer-key");
            if (key == null || key.trim().isEmpty() || publicKey == null || publicKey.equals(key.trim())) {
                return true;
            }
        }
        return false;
    }

    private void collectInternalPages(Document document, URI startUri, Set<String> visitedPages, Queue<String> pagesToVisit) {
        for (Element link : document.select("a[href]")) {
            URI uri = normalizeUrl(link.absUrl("href"));
            if (uri == null || !isHttpUrl(uri) || !isInternal(startUri, uri) || !shouldCrawlAsPage(uri)) {
                continue;
            }
            String normalizedUrl = uri.toString();
            if (!visitedPages.contains(normalizedUrl) && !pagesToVisit.contains(normalizedUrl)) {
                pagesToVisit.add(normalizedUrl);
            }
        }
    }

    private Connection configureRequest(Connection connection) {
        return connection
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "fr-FR,fr;q=0.9,en;q=0.8")
                .referrer("https://bugspointer.com/");
    }

    private String normalizeStartUrl(String rawUrl) {
        String url = rawUrl == null ? "" : rawUrl.trim();
        if (!url.toLowerCase(Locale.ROOT).startsWith("http://") && !url.toLowerCase(Locale.ROOT).startsWith("https://")) {
            return "https://" + url;
        }
        return url;
    }

    private URI normalizeUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            return null;
        }
        try {
            URI uri = new URI(rawUrl.trim());
            URI withoutFragment = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), null);
            String scheme = withoutFragment.getScheme() == null ? "" : withoutFragment.getScheme().toLowerCase(Locale.ROOT);
            String host = withoutFragment.getHost() == null ? "" : withoutFragment.getHost().toLowerCase(Locale.ROOT);
            return new URI(scheme, withoutFragment.getUserInfo(), host, withoutFragment.getPort(), withoutFragment.getPath(), withoutFragment.getQuery(), null).normalize();
        } catch (URISyntaxException | IllegalArgumentException e) {
            return null;
        }
    }

    private boolean shouldCrawlAsPage(URI uri) {
        String path = uri.getPath();
        if (path == null || path.isEmpty() || path.endsWith("/")) {
            return true;
        }
        String lowerPath = path.toLowerCase(Locale.ROOT);
        return !(lowerPath.endsWith(".pdf")
                || lowerPath.endsWith(".jpg")
                || lowerPath.endsWith(".jpeg")
                || lowerPath.endsWith(".png")
                || lowerPath.endsWith(".gif")
                || lowerPath.endsWith(".webp")
                || lowerPath.endsWith(".svg")
                || lowerPath.endsWith(".zip")
                || lowerPath.endsWith(".css")
                || lowerPath.endsWith(".js"));
    }

    private boolean isHttpUrl(URI uri) {
        return "http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme());
    }

    private boolean isInternal(URI startUri, URI uri) {
        return normalizeHost(startUri).equals(normalizeHost(uri));
    }

    private String normalizeHost(URI uri) {
        String host = uri.getHost();
        if (host == null) {
            return "";
        }
        host = host.toLowerCase(Locale.ROOT);
        return host.startsWith("www.") ? host.substring(4) : host;
    }

    private boolean containsIgnoreCase(String value, String expected) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(expected.toLowerCase(Locale.ROOT));
    }
}
