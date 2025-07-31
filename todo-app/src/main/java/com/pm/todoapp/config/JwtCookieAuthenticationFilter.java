package com.pm.todoapp.config;

import com.pm.todoapp.exceptions.InvalidTokenException;
import com.pm.todoapp.service.AuthService;
import com.pm.todoapp.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.persistence.Access;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

public class JwtCookieAuthenticationFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;

    @Autowired
    public JwtCookieAuthenticationFilter(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Optional<String> userCookie = extractCookie(request, "userCookie");

        if (userCookie.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Jwt jwt = jwtDecoder.decode(userCookie.get());

            String userIdString = jwt.getClaimAsString("id");
            UUID userId = UUID.fromString(userIdString);

            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception e) {
           throw new InvalidTokenException("Could not set user authentication from token: ");
        }

        filterChain.doFilter(request, response);
    }

    private Optional<String> extractCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}
