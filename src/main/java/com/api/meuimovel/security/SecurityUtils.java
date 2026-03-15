package com.api.meuimovel.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Utilitário estático para acessar o ID do usuário autenticado a partir
 * de qualquer camada da aplicação sem precisar injetar dependências.
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    /**
     * Retorna o ID do usuário autenticado na requisição atual.
     * O ID é o {@code username} do {@link UserDetails} — que foi definido
     * como {@code user.getId()} em {@link JwtAuthFilter}.
     *
     * @throws IllegalStateException se não houver autenticação no contexto
     */
    public static String currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(auth);
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("Nenhum usuário autenticado no contexto atual");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails ud) {
            return ud.getUsername();
        }
        return principal.toString();
    }
}
