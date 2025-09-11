package com.pm.todoapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class SecurityConfig {

    private final String jwtSecret;
    private final AuthEntryPoint authEntryPoint;

    public SecurityConfig(
            @Value("${jwt.secret}") String jwtSecret
            , AuthEntryPoint authEntryPoint) {

        this.jwtSecret = jwtSecret;
        this.authEntryPoint = authEntryPoint;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtCookieAuthenticationFilter jwtCookieAuthenticationFilter(JwtDecoder jwtDecoder) {
        return new JwtCookieAuthenticationFilter(jwtDecoder);
    }

    @Bean
    public FilterRegistrationBean<JwtCookieAuthenticationFilter> jwtCookieAuthenticationFilterRegistration(JwtCookieAuthenticationFilter filter) {
        FilterRegistrationBean<JwtCookieAuthenticationFilter> registrationBean = new FilterRegistrationBean<>(filter);
        registrationBean.setEnabled(false);
        return registrationBean;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/h2-console/**");
    }

    @Bean
    @Order(1)
    public SecurityFilterChain publicEndpointsSecurityFilterChain(HttpSecurity http) throws Exception {

        http.securityMatcher(
                        "/auth/**",
                        "/css/**",
                        "/js/**",
                        "/error",
                        "/ws/**",
                        "/h2-console/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html")
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain protectedEndpointsSecurityFilterChain(HttpSecurity http, JwtCookieAuthenticationFilter jwtCookieAuthenticationFilter) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        authorize -> authorize.anyRequest().authenticated())
                .addFilterBefore(jwtCookieAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(authEntryPoint));

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret.getBytes(StandardCharsets.UTF_8));
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");

        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }

}
