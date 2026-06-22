package com.bugspointer.service.implementation;

import com.bugspointer.dto.WidgetInstallationScanDTO;
import com.bugspointer.entity.Company;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

@Service
public class WidgetInstallationScanService {

    private static final int TIMEOUT_MS = 8000;
    private static final String USER_AGENT = "Bugspointer installation checker";

    public WidgetInstallationScanDTO scan(Company company, String rawPageUrl) {
        WidgetInstallationScanDTO result = new WidgetInstallationScanDTO();
        if (company == null) {
            result.setErrorMessage("Compte introuvable.");
            return result;
        }

        String pageUrl = normalizeStartUrl(rawPageUrl);
        result.setDomain(normalizeStartUrl(company.getDomaine()));
        result.setPublicKey(company.getPublicKey());

        URI pageUri = normalizeUrl(pageUrl);
        if (pageUri == null || !isHttpUrl(pageUri)) {
            result.setErrorMessage("Merci de saisir une URL de page valide.");
            return result;
        }
        result.setScannedUrl(pageUri.toString());

        Document document;
        try {
            document = fetchDocument(pageUri.toString());
        } catch (IOException | IllegalArgumentException e) {
            result.setErrorMessage("Impossible de vérifier cette page: " + e.getMessage());
            return result;
        }

        result.setCheckedPageCount(1);
        if (containsFloatingWidget(document)) {
            result.getWidgetUrls().add(pageUri.toString());
        }
        if (containsBugspointerLink(document)) {
            result.getLinkUrls().add(pageUri.toString());
        }
        return result;
    }

    private Document fetchDocument(String url) throws IOException {
        Connection.Response response = configureRequest(Jsoup.connect(url))
                .followRedirects(false)
                .ignoreHttpErrors(true)
                .execute();
        if (response.statusCode() >= 300 && response.statusCode() < 400) {
            throw new IOException("l'URL répond par une redirection HTTP " + response.statusCode() + ". Merci de vérifier l'URL exacte.");
        }
        if (response.statusCode() != 200) {
            throw new IOException("l'URL répond en HTTP " + response.statusCode() + ".");
        }
        String contentType = response.contentType();
        if (contentType != null && !contentType.toLowerCase(Locale.ROOT).contains("text/html")) {
            throw new IOException("l'URL ne renvoie pas une page HTML.");
        }
        return response.parse();
    }

    private boolean containsFloatingWidget(Document document) {
        for (Element script : document.select("script[src]")) {
            String src = script.absUrl("src");
            String rawSrc = script.attr("src");
            String buttonStyle = script.attr("data-button-style");
            boolean isBugspointerScript = containsIgnoreCase(src, "modalPointer.js") || containsIgnoreCase(rawSrc, "modalPointer.js");
            boolean isLinkMode = "custom".equalsIgnoreCase(buttonStyle);
            if (isBugspointerScript && !isLinkMode) {
                return true;
            }
        }
        return false;
    }

    private boolean containsBugspointerLink(Document document) {
        boolean hasBugspointerScript = false;
        for (Element script : document.select("script[src]")) {
            String src = script.absUrl("src");
            String rawSrc = script.attr("src");
            if (containsIgnoreCase(src, "modalPointer.js") || containsIgnoreCase(rawSrc, "modalPointer.js")) {
                hasBugspointerScript = true;
                break;
            }
        }
        return hasBugspointerScript && !document.select("[data-bugspointer-open]").isEmpty();
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
            String path = uri.getPath();
            if (path == null || path.trim().isEmpty()) {
                path = "/";
            }
            URI withoutFragment = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), path, uri.getQuery(), null);
            String scheme = withoutFragment.getScheme() == null ? "" : withoutFragment.getScheme().toLowerCase(Locale.ROOT);
            String host = withoutFragment.getHost() == null ? "" : withoutFragment.getHost().toLowerCase(Locale.ROOT);
            return new URI(scheme, withoutFragment.getUserInfo(), host, withoutFragment.getPort(), withoutFragment.getPath(), withoutFragment.getQuery(), null).normalize();
        } catch (URISyntaxException | IllegalArgumentException e) {
            return null;
        }
    }

    private boolean isHttpUrl(URI uri) {
        return "http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme());
    }

    private boolean containsIgnoreCase(String value, String expected) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(expected.toLowerCase(Locale.ROOT));
    }
}
