package com.projectflow.service;

import com.projectflow.config.AppProperties;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class CookieService {

    private final AppProperties appProperties;

    public CookieService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public void setAccessToken(HttpServletResponse response, String token) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie(token, appProperties.getJwt().getExpirationMs()).toString());
    }

    public void clearAccessToken(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie("", 0).toString());
    }

    private ResponseCookie buildCookie(String value, long maxAgeMs) {
        return ResponseCookie.from(appProperties.getJwt().getCookieName(), value)
                .httpOnly(true)
                .secure(appProperties.isCookieSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofMillis(maxAgeMs))
                .build();
    }
}
