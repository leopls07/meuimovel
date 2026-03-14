package com.api.meuimovel.controller;

import com.api.meuimovel.dto.auth.AuthResponseDTO;
import com.api.meuimovel.dto.auth.AuthResponseDTO.UserInfoDTO;
import com.api.meuimovel.dto.auth.GoogleAuthRequestDTO;
import com.api.meuimovel.model.User;
import com.api.meuimovel.security.GoogleTokenVerifier;
import com.api.meuimovel.security.JwtService;
import com.api.meuimovel.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Autenticação via Google OAuth2")
public class AuthController {

    private final GoogleTokenVerifier googleTokenVerifier;
    private final UserService userService;
    private final JwtService jwtService;

    @Operation(
        summary = "Login com Google",
        description = "Recebe o ID Token do Google (obtido no frontend via Google Sign-In), " +
                      "valida, cria/encontra o usuário e retorna um JWT da aplicação."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Autenticado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token do Google inválido ou expirado"),
        @ApiResponse(responseCode = "400", description = "Requisição inválida")
    })
    @PostMapping("/google")
    public ResponseEntity<AuthResponseDTO> loginWithGoogle(
            @Valid @RequestBody GoogleAuthRequestDTO request) {

        // 1. Valida o ID Token do Google
        GoogleTokenVerifier.GoogleUserInfo googleInfo = googleTokenVerifier.verify(request.idToken());

        // 2. Cria ou atualiza o usuário no banco
        User user = userService.findOrCreateFromGoogle(googleInfo);

        // 3. Gera nosso próprio JWT
        String token = jwtService.generateToken(user);

        AuthResponseDTO response = new AuthResponseDTO(
                token,
                "Bearer",
                jwtService.getExpirationMs() / 1000,
                new UserInfoDTO(user.getId(), user.getEmail(), user.getName(), user.getPictureUrl())
        );

        return ResponseEntity.ok(response);
    }
}
