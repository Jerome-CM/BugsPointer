package com.bugspointer.service.implementation;

import com.bugspointer.dto.AdminScraperResourceDTO;
import com.bugspointer.dto.AdminScraperResultDTO;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
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
public class AdminScraperService {

    private static final int MAX_INTERNAL_PAGES = 100;
    private static final int MAX_RESOURCES = 600;
    private static final int TIMEOUT_MS = 8000;
    private static final String USER_AGENT = "BugsPointer-admin-scraper/1.0";

    public AdminScraperResultDTO scan(String rawUrl) {
        AdminScraperResultDTO result = new AdminScraperResultDTO();
        String startUrl = normalizeStartUrl(rawUrl);
        result.setStartUrl(startUrl);

        URI startUri;
        try {
            startUri = new URI(startUrl);
            if (!isHttpUrl(startUri)) {
                result.setGlobalError("Merci de saisir une URL en http ou https.");
                return result;
            }
        } catch (URISyntaxException e) {
            result.setGlobalError("URL invalide.");
            return result;
        }

        Queue<String> pagesToVisit = new ArrayDeque<>();
        Set<String> visitedPages = new HashSet<>();
        Set<String> checkedLinks = new HashSet<>();
        Set<String> checkedImages = new HashSet<>();
        URI normalizedStartUri = normalizeUrl(startUri.toString());
        if (normalizedStartUri == null) {
            result.setGlobalError("URL invalide.");
            return result;
        }
        pagesToVisit.add(normalizedStartUri.toString());

        while (!pagesToVisit.isEmpty() && visitedPages.size() < MAX_INTERNAL_PAGES && checkedLinks.size() + checkedImages.size() < MAX_RESOURCES) {
            String pageUrl = pagesToVisit.poll();
            if (!visitedPages.add(pageUrl)) {
                continue;
            }

            Document document;
            try {
                document = fetchDocument(pageUrl);
                result.setCheckedPageCount(visitedPages.size());
            } catch (HttpStatusException e) {
                result.addError(new AdminScraperResourceDTO(pageUrl, pageUrl, "Page", e.getStatusCode(), "Réponse HTTP " + e.getStatusCode(), true));
                continue;
            } catch (UnsupportedMimeTypeException e) {
                result.addError(new AdminScraperResourceDTO(pageUrl, pageUrl, "Page", 0, "Contenu non HTML", true));
                continue;
            } catch (IOException | IllegalArgumentException e) {
                result.addError(new AdminScraperResourceDTO(pageUrl, pageUrl, "Page", 0, e.getMessage(), true));
                continue;
            }

            collectLinks(document, pageUrl, startUri, pagesToVisit, visitedPages, checkedLinks, result);
            collectImages(document, pageUrl, startUri, checkedImages, result);
        }

        result.setCheckedPageCount(visitedPages.size());
        result.setCheckedLinkCount(checkedLinks.size());
        result.setCheckedImageCount(checkedImages.size());
        result.setLimitReached(!pagesToVisit.isEmpty() || visitedPages.size() >= MAX_INTERNAL_PAGES || checkedLinks.size() + checkedImages.size() >= MAX_RESOURCES);
        return result;
    }

    private void collectLinks(Document document,
                              String pageUrl,
                              URI startUri,
                              Queue<String> pagesToVisit,
                              Set<String> visitedPages,
                              Set<String> checkedLinks,
                              AdminScraperResultDTO result) {
        for (Element link : document.select("a[href]")) {
            String href = link.absUrl("href");
            URI uri = normalizeUrl(href);
            if (uri == null || !isHttpUrl(uri)) {
                continue;
            }
            String normalizedUrl = uri.toString();
            if (!checkedLinks.add(normalizedUrl)) {
                continue;
            }

            boolean internal = isInternal(startUri, uri);
            StatusCheck statusCheck = checkStatus(normalizedUrl);
            if (statusCheck.statusCode != 200) {
                result.addError(new AdminScraperResourceDTO(pageUrl, normalizedUrl, internal ? "Lien interne" : "Lien externe", statusCheck.statusCode, statusCheck.error, internal));
            }
            if (internal && statusCheck.statusCode == 200 && shouldCrawlAsPage(uri, statusCheck.contentType) && !visitedPages.contains(normalizedUrl)) {
                pagesToVisit.add(normalizedUrl);
            }
        }
    }

    private void collectImages(Document document,
                               String pageUrl,
                               URI startUri,
                               Set<String> checkedImages,
                               AdminScraperResultDTO result) {
        for (Element image : document.select("img[src]")) {
            String src = image.absUrl("src");
            URI uri = normalizeUrl(src);
            if (uri == null || !isHttpUrl(uri)) {
                continue;
            }
            String normalizedUrl = uri.toString();
            if (!checkedImages.add(normalizedUrl)) {
                continue;
            }

            StatusCheck statusCheck = checkStatus(normalizedUrl);
            if (statusCheck.statusCode != 200) {
                result.addError(new AdminScraperResourceDTO(pageUrl, normalizedUrl, "Image", statusCheck.statusCode, statusCheck.error, isInternal(startUri, uri)));
            }
        }
    }

    private Document fetchDocument(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .followRedirects(true)
                .ignoreHttpErrors(false)
                .get();
    }

    private StatusCheck checkStatus(String url) {
        try {
            Connection.Response response = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .followRedirects(true)
                    .ignoreHttpErrors(true)
                    .ignoreContentType(true)
                    .method(Connection.Method.HEAD)
                    .execute();
            int statusCode = response.statusCode();
            if (statusCode == 405 || statusCode == 403) {
                return checkStatusWithGet(url);
            }
            return new StatusCheck(statusCode, statusCode == 200 ? "" : "Réponse HTTP " + statusCode, response.contentType());
        } catch (IOException | IllegalArgumentException e) {
            return checkStatusWithGet(url);
        }
    }

    private StatusCheck checkStatusWithGet(String url) {
        try {
            Connection.Response response = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .followRedirects(true)
                    .ignoreHttpErrors(true)
                    .ignoreContentType(true)
                    .maxBodySize(1024)
                    .execute();
            int statusCode = response.statusCode();
            return new StatusCheck(statusCode, statusCode == 200 ? "" : "Réponse HTTP " + statusCode, response.contentType());
        } catch (IOException | IllegalArgumentException e) {
            return new StatusCheck(0, e.getMessage(), "");
        }
    }

    private boolean shouldCrawlAsPage(URI uri, String contentType) {
        if (contentType != null && !contentType.isEmpty()) {
            return contentType.toLowerCase(Locale.ROOT).contains("text/html");
        }
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

    private String normalizeStartUrl(String rawUrl) {
        if (rawUrl == null) {
            return "";
        }
        String url = rawUrl.trim();
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
            URI uri = removeFragment(new URI(rawUrl.trim())).normalize();
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
            int port = uri.getPort();
            URI normalized = new URI(scheme, uri.getUserInfo(), host, port, uri.getPath(), uri.getQuery(), null);
            return normalized.normalize();
        } catch (URISyntaxException | IllegalArgumentException e) {
            return null;
        }
    }

    private URI removeFragment(URI uri) throws URISyntaxException {
        return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), null).normalize();
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

    private static class StatusCheck {
        private final int statusCode;
        private final String error;
        private final String contentType;

        private StatusCheck(int statusCode, String error, String contentType) {
            this.statusCode = statusCode;
            this.error = error;
            this.contentType = contentType;
        }
    }
}
