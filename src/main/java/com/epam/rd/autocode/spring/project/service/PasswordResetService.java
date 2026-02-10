package com.epam.rd.autocode.spring.project.service;

public interface PasswordResetService {
    void initiatePasswordReset(String email, String appUrl);

    boolean validatePasswordResetToken(String token);

    void updatePassword(String token, String newPassword);
}