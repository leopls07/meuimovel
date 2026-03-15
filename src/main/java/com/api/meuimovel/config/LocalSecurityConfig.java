package com.api.meuimovel.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Configuração de segurança exclusiva para o profile "local".
 * Injeta automaticamente um userId em todas as requisições via header X-Local-User-Id,
 * dispensando o token JWT e o login Google.
 *
 * Ative com: -Dspring.profiles.active=local
 * ou SPRING_PROFILES_ACTIVE=local
 */
@Configuration
@EnableWebSecurity
@Profile("local")
public class LocalSecurityConfig {

    public static final String LOCAL_USER_ID = "local-user-dev";

    @Bean
    public SecurityFilterChain localSecurityFilterChain(HttpSecurity http) throws Exception {
        // Filtro criado inline — NÃO é @Bean para evitar que o Spring Boot
        // o registre também como servlet filter fora da cadeia de segurança.
        // Se fosse @Bean, o SecurityContextHolderFilter limparia o contexto
        // após o filtro rodar como servlet filter, desfazendo a autenticação.
        OncePerRequestFilter localAuthFilter = new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain filterChain) throws ServletException, IOException {
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    String headerUserId = request.getHeader("X-Local-User-Id");
                    String userId = (headerUserId != null && !headerUserId.isBlank())
                            ? headerUserId
                            : LOCAL_USER_ID;

                    UserDetails userDetails = User.builder()
                            .username(userId)
                            .password("")
                            .authorities(Collections.emptyList())
                            .build();

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
                filterChain.doFilter(request, response);
            }
        };

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .addFilterBefore(localAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Desabilita o registro automático do JwtAuthFilter como servlet filter no profile local.
     * Como JwtAuthFilter é @Component, o Spring Boot o registraria fora da cadeia de segurança,
     * causando execução duplicada e comportamento imprevisível.
     */
    @Bean
    public FilterRegistrationBean<com.api.meuimovel.security.JwtAuthFilter> jwtFilterRegistration(
            com.api.meuimovel.security.JwtAuthFilter jwtAuthFilter) {
        FilterRegistrationBean<com.api.meuimovel.security.JwtAuthFilter> reg =
                new FilterRegistrationBean<>(jwtAuthFilter);
        reg.setEnabled(false);
        return reg;
    }
}