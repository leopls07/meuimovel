package com.api.meuimovel.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record GoogleAuthRequestDTO(
        @NotBlank(message = "idToken é obrigatório")
        String idToken
) {}
