package com.bugspointer.service.implementation;

import com.bugspointer.dto.AdminScraperResourceDTO;
import com.bugspointer.dto.AdminScraperJobDTO;
import com.bugspointer.dto.AdminScraperResultDTO;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class AdminScraperService {

    private static final int MAX_INTERNAL_PAGES = 100;
    private static final int MAX_RESOURCES = 600;
    private static final int TIMEOUT_MS = 8000;
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36";
    private static final long JOB_TTL_MS = 30 * 60 * 1000;

    private final ExecutorService scanExecutor = Executors.newFixedThreadPool(3);

    private final Map<String, AdminScraperJobDTO> jobs = new ConcurrentHashMap<>();

    private final Map<String, Future<?>> runningTasks = new ConcurrentHashMap<>();

    public AdminScraperJobDTO startScan(String rawUrl) {
        cleanOldJobs();
        String jobId = UUID.randomUUID().toString();
        AdminScraperJobDTO job = new AdminScraperJobDTO(jobId, normalizeStartUrl(rawUrl));
        jobs.put(jobId, job);

        Future<?> task = scanExecutor.submit(() -> {
            try {
                job.complete(scan(rawUrl, job));
            } catch (Exception e) {
                job.fail(e.getMessage());
            } finally {
                runningTasks.remove(jobId);
            }
        });
        runningTasks.put(jobId, task);

        return job;
    }

    public AdminScraperJobDTO getJob(String jobId) {
        if (jobId == null || jobId.trim().isEmpty()) {
            return null;
        }
        cleanOldJobs();
        return jobs.get(jobId);
    }

    public boolean cancelScan(String jobId) {
        AdminScraperJobDTO job = getJob(jobId);
        if (job == null || !job.isRunning()) {
            return false;
        }

        job.cancel();
        Future<?> task = runningTasks.remove(jobId);
        if (task != null) {
            task.cancel(true);
        }
        return true;
    }

    @PreDestroy
    public void shutdown() {
        scanExecutor.shutdownNow();
    }

    private void cleanOldJobs() {
        long now = System.currentTimeMillis();
        jobs.entrySet().removeIf(entry -> !entry.getValue().isRunning()
                && entry.getValue().getCompletedAt() > 0
                && now - entry.getValue().getCompletedAt() > JOB_TTL_MS);
    }

    public AdminScraperResultDTO scan(String rawUrl) {
        return scan(rawUrl, null);
    }

    private AdminScraperResultDTO scan(String rawUrl, AdminScraperJobDTO job) {
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

        while (!isCancelled(job) && !pagesToVisit.isEmpty() && visitedPages.size() < MAX_INTERNAL_PAGES && checkedLinks.size() + checkedImages.size() < MAX_RESOURCES) {
            String pageUrl = pagesToVisit.poll();
            if (!visitedPages.add(pageUrl)) {
                continue;
            }

            Document document;
            try {
                document = fetchDocument(pageUrl);
                result.setCheckedPageCount(visitedPages.size());
            } catch (HttpStatusException e) {
                result.addError(new AdminScraperResourceDTO(pageUrl, pageUrl, "Page", e.getStatusCode(), getHttpErrorMessage(e.getStatusCode()), true));
                continue;
            } catch (UnsupportedMimeTypeException e) {
                result.addError(new AdminScraperResourceDTO(pageUrl, pageUrl, "Page", 0, "Contenu non HTML", true));
                continue;
            } catch (IOException | IllegalArgumentException e) {
                result.addError(new AdminScraperResourceDTO(pageUrl, pageUrl, "Page", 0, e.getMessage(), true));
                continue;
            }

            collectLinks(document, pageUrl, startUri, pagesToVisit, visitedPages, checkedLinks, result, job);
            collectImages(document, pageUrl, startUri, checkedImages, result, job);
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
                              AdminScraperResultDTO result,
                              AdminScraperJobDTO job) {
        for (Element link : document.select("a[href]")) {
            if (isCancelled(job)) {
                return;
            }
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
                               AdminScraperResultDTO result,
                               AdminScraperJobDTO job) {
        for (Element image : document.select("img[src]")) {
            if (isCancelled(job)) {
                return;
            }
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
        Connection.Response response = configureRequest(Jsoup.connect(url))
                .followRedirects(true)
                .ignoreHttpErrors(true)
                .execute();

        if (response.statusCode() != 200) {
            throw new HttpStatusException(getHttpErrorMessage(response.statusCode()), response.statusCode(), url);
        }

        return response.parse();
    }

    private boolean isCancelled(AdminScraperJobDTO job) {
        return Thread.currentThread().isInterrupted() || (job != null && job.isCancelled());
    }

    private StatusCheck checkStatus(String url) {
        try {
            Connection.Response response = configureRequest(Jsoup.connect(url))
                    .followRedirects(true)
                    .ignoreHttpErrors(true)
                    .ignoreContentType(true)
                    .method(Connection.Method.HEAD)
                    .execute();
            int statusCode = response.statusCode();
            if (statusCode < 200 || statusCode >= 400) {
                return checkStatusWithGet(url);
            }
            return new StatusCheck(statusCode, "", response.contentType());
        } catch (IOException | IllegalArgumentException e) {
            return checkStatusWithGet(url);
        }
    }

    private StatusCheck checkStatusWithGet(String url) {
        try {
            Connection.Response response = configureRequest(Jsoup.connect(url))
                    .followRedirects(true)
                    .ignoreHttpErrors(true)
                    .ignoreContentType(true)
                    .maxBodySize(1024)
                    .execute();
            int statusCode = response.statusCode();
            return new StatusCheck(statusCode, statusCode == 200 ? "" : getHttpErrorMessage(statusCode), response.contentType());
        } catch (IOException | IllegalArgumentException e) {
            return new StatusCheck(0, e.getMessage(), "");
        }
    }

    private Connection configureRequest(Connection connection) {
        return connection
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                .header("Accept-Language", "fr-FR,fr;q=0.9,en;q=0.8")
                .header("Cache-Control", "no-cache")
                .referrer("https://www.google.com/");
    }

    private String getHttpErrorMessage(int statusCode) {
        if (statusCode == 401 || statusCode == 403 || statusCode == 429) {
            return "Accès bloqué par le site (HTTP " + statusCode + ")";
        }
        if (statusCode == 404) {
            return "Page ou fichier introuvable (HTTP 404)";
        }
        if (statusCode >= 500) {
            return "Erreur serveur (HTTP " + statusCode + ")";
        }
        return "Réponse HTTP " + statusCode;
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
