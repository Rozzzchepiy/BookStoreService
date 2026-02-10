package com.epam.rd.autocode.spring.project.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    void sendEmail_ShouldSendSimpleMailMessage() {
        String to = "test@example.com";
        String subject = "Test Subject";
        String text = "Test Content";

        emailService.sendEmail(to, subject, text);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();

        assertEquals(to, Objects.requireNonNull(capturedMessage.getTo())[0]);
        assertEquals(subject, capturedMessage.getSubject());
        assertEquals(text, capturedMessage.getText());
        assertEquals("noreply@bookservice.com", capturedMessage.getFrom());
    }
}