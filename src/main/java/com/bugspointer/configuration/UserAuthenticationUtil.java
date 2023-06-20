package com.bugspointer.configuration;

import com.bugspointer.jwtConfig.JwtTokenUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.Timestamp;
import java.util.Date;

@Component
public class UserAuthenticationUtil {

    private final JwtTokenUtil jwtTokenUtil;

    public UserAuthenticationUtil(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    public boolean isUserLoggedIn() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        HttpSession session = request.getSession();
        String headerToken = (String) session.getAttribute("token");
        if (headerToken != null) {
            String token = jwtTokenUtil.getTokenWithoutBearer(headerToken);
            Date dateNow = new Date();
            Date timeToToken = jwtTokenUtil.getExpirationDateFromToken(token);
            int comparisonResult = timeToToken.compareTo(dateNow);
            return comparisonResult > 0;
        }
        return false;
    }
}
