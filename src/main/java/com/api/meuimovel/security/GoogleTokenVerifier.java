package com.api.meuimovel.security;

import com.api.meuimovel.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

/**
 * Valida o Google ID Token usando a JWKS pública do Google.
 * Não faz chamada à tokeninfo — valida localmente com a chave pública.
 */
@Component
public class GoogleTokenVerifier {

    private static final String GOOGLE_JWKS_URI = "https://www.googleapis.com/oauth2/v3/certs";
    private static final String GOOGLE_ISSUER_1 = "https://accounts.google.com";
    private static final String GOOGLE_ISSUER_2 = "accounts.google.com";

    private final String clientId;
    private final JwtDecoder googleJwtDecoder;

    public GoogleTokenVerifier(@Value("${security.google.client-id}") String clientId) {
        this.clientId = clientId;
        this.googleJwtDecoder = NimbusJwtDecoder.withJwkSetUri(GOOGLE_JWKS_URI).build();
    }

    public GoogleUserInfo verify(String idToken) {
        Jwt jwt;
        try {
            jwt = googleJwtDecoder.decode(idToken);
        } catch (JwtException e) {
            throw new UnauthorizedException("Google ID token inválido: " + e.getMessage());
        }

        // Validar issuer
        String issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : "";
        if (!GOOGLE_ISSUER_1.equals(issuer) && !GOOGLE_ISSUER_2.equals(issuer)) {
            throw new UnauthorizedException("Issuer inválido no token do Google");
        }

        // Validar audience (client_id da aplicação)
        if (jwt.getAudience() == null || !jwt.getAudience().contains(clientId)) {
            throw new UnauthorizedException("Audience inválida no token do Google");
        }

        String sub = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String name = jwt.getClaimAsString("name");
        String picture = jwt.getClaimAsString("picture");

        if (sub == null || email == null) {
            throw new UnauthorizedException("Token do Google não contém sub ou email");
        }

        return new GoogleUserInfo(sub, email, name, picture);
    }

    public record GoogleUserInfo(
            String googleId,
            String email,
            String name,
            String pictureUrl
    ) {}
}
