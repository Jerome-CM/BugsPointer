package com.bugspointer.jwtConfig;

import com.bugspointer.configuration.LoginRedirectUtil;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.security.core.AuthenticationException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.net.URLEncoder;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, Serializable {

    private static final long serialVersionUID = -7858869558953243875L;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        String redirectPath = LoginRedirectUtil.getSafeRedirectPath(request);
        request.getSession().setAttribute("redirectAfterLogin", redirectPath);
        response.sendRedirect("/authentication?status=ERROR&message=Vous%20devez%20vous%20connecter&redirect=" + URLEncoder.encode(redirectPath, "UTF-8"));
    }
}
