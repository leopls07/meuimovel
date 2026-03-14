package com.api.meuimovel.service;

import com.api.meuimovel.model.User;
import com.api.meuimovel.security.GoogleTokenVerifier.GoogleUserInfo;

public interface UserService {
    /**
     * Busca o usuário pelo googleId.
     * Se não existir, cria um novo a partir das informações do Google.
     */
    User findOrCreateFromGoogle(GoogleUserInfo googleUserInfo);
}
