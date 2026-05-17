package com.bugspointer.service.implementation;

import com.bugspointer.entity.EnumViewCounterPage;
import com.bugspointer.entity.ViewCounter;
import com.bugspointer.repository.ViewCounterRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ViewCounterService {

    private final ViewCounterRepository viewCounterRepository;

    private final Set<String> excludedIps;

    public ViewCounterService(ViewCounterRepository viewCounterRepository,
                              @Value("${metrics.excluded-ips:}") String excludedIps) {
        this.viewCounterRepository = viewCounterRepository;
        this.excludedIps = Arrays.stream(excludedIps.split(","))
                .map(String::trim)
                .filter(ip -> !ip.isEmpty())
                .collect(Collectors.toSet());
    }

    public void addVisit(EnumViewCounterPage page, HttpServletRequest request){
        String adresseIp = getClientIp(request);
        if (excludedIps.contains(adresseIp)) {
            return;
        }

        ViewCounter view = new ViewCounter(page, new Date(), adresseIp);
        viewCounterRepository.save(view);

    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "";
        }

        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.trim().isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.trim().isEmpty()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }
}
