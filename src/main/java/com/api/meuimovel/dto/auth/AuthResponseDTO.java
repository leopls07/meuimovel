package com.api.meuimovel.dto.auth;

public record AuthResponseDTO(
        String token,
        String tokenType,
        long expiresIn,
        UserInfoDTO user
) {
    public record UserInfoDTO(
            String id,
            String email,
            String name,
            String pictureUrl
    ) {}
}
