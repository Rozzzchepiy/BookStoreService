package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.PasswordResetToken;
import com.epam.rd.autocode.spring.project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUser(User user);
}