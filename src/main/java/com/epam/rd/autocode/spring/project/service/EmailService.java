package com.epam.rd.autocode.spring.project.service;

public interface EmailService {
    void sendEmail(String to, String subject, String text);
}
