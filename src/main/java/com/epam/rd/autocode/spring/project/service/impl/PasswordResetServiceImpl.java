package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.model.PasswordResetToken;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.repo.PasswordResetTokenRepository;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import com.epam.rd.autocode.spring.project.service.EmailService;
import com.epam.rd.autocode.spring.project.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void initiatePasswordReset(String email, String appUrl) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            log.warn("Password reset requested for non-existent email: {}", email);
            return;
        }

        User user = userOptional.get();


        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(30));

        tokenRepository.save(resetToken);

        String resetLink = appUrl + "/forgot-password/reset?token=" + token;

        emailService.sendEmail(
                user.getEmail(),
                "Відновлення пароля",
                "Щоб змінити пароль, перейдіть за посиланням: " + resetLink
        );
        log.info("Password reset email sent to: {}", email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validatePasswordResetToken(String token) {
        Optional<PasswordResetToken> tokenOptional = tokenRepository.findByToken(token);
        return tokenOptional.isPresent() && !tokenOptional.get().isExpired();
    }

    @Override
    @Transactional
    public void updatePassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (resetToken.isExpired()) {
            throw new IllegalArgumentException("Token expired");
        }

        User user = resetToken.getUser();

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.delete(resetToken);


        log.info("Password successfully reset for user: {}", user.getEmail());
    }
}