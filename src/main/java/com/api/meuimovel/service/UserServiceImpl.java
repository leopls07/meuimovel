package com.api.meuimovel.service;

import com.api.meuimovel.model.User;
import com.api.meuimovel.repository.UserRepository;
import com.api.meuimovel.security.GoogleTokenVerifier.GoogleUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User findOrCreateFromGoogle(GoogleUserInfo info) {
        return userRepository.findByGoogleId(info.googleId())
                .map(existing -> syncIfChanged(existing, info))
                .orElseGet(() -> createUser(info));
    }

    private User createUser(GoogleUserInfo info) {
        User user = User.builder()
                .googleId(info.googleId())
                .email(info.email())
                .name(info.name())
                .pictureUrl(info.pictureUrl())
                .build();
        return userRepository.save(user);
    }

    /** Atualiza nome/foto caso o perfil do Google tenha mudado */
    private User syncIfChanged(User existing, GoogleUserInfo info) {
        boolean changed = false;
        if (!equal(existing.getName(), info.name())) {
            existing.setName(info.name());
            changed = true;
        }
        if (!equal(existing.getPictureUrl(), info.pictureUrl())) {
            existing.setPictureUrl(info.pictureUrl());
            changed = true;
        }
        return changed ? userRepository.save(existing) : existing;
    }

    private boolean equal(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
}
