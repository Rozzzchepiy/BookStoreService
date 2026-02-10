package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.model.RefreshToken;

import java.util.Optional;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(String email);
    Optional<RefreshToken> findByToken(String token);
    RefreshToken verifyExpiration(RefreshToken token);
    void deleteByUserId(Long userId);
}
