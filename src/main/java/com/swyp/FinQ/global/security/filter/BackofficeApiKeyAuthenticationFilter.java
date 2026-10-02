package com.swyp.FinQ.global.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Component
public class BackofficeApiKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER_NAME = "X-Backoffice-Api-Key";
    private static final String BACKOFFICE_PATH = "/internal/backoffice";
    private static final int MINIMUM_API_KEY_BYTES = 32;

    private final byte[] expectedApiKey;

    public BackofficeApiKeyAuthenticationFilter(
            @Value("${backoffice.api-key:}") String expectedApiKey
    ) {
        this.expectedApiKey = expectedApiKey.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestPath = request.getRequestURI().substring(request.getContextPath().length());
        return !requestPath.equals(BACKOFFICE_PATH)
                && !requestPath.startsWith(BACKOFFICE_PATH + "/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (expectedApiKey.length < MINIMUM_API_KEY_BYTES) {
            response.sendError(HttpStatus.SERVICE_UNAVAILABLE.value());
            return;
        }

        String providedApiKey = request.getHeader(HEADER_NAME);
        byte[] providedBytes = providedApiKey == null
                ? new byte[0]
                : providedApiKey.getBytes(StandardCharsets.UTF_8);

        if (!MessageDigest.isEqual(expectedApiKey, providedBytes)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        "backoffice",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_BACKOFFICE"))
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        try {
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
