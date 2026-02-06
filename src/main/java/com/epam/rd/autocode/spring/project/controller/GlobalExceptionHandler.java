package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.security.Principal;
import java.util.Locale;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoHandlerFoundException(NoHandlerFoundException ex, Model model, Principal principal, Locale locale) {
        String user = (principal != null) ? principal.getName() : "Anonymous";
        log.warn("Page not found: {} | User: {}", ex.getRequestURL(), user);

        model.addAttribute("status", 404);
        String errorMsg = messageSource.getMessage("error.404.page", null, locale);
        model.addAttribute("error", errorMsg);

        return "error";
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFoundException(NotFoundException ex, Model model, Principal principal, Locale locale) {
        String user = (principal != null) ? principal.getName() : "Anonymous";
        log.warn("Resource not found: {} | User: {}", ex.getMessage(), user);

        model.addAttribute("status", 404);

        String errorMsg = messageSource.getMessage("error.404.resource", null, locale);
        model.addAttribute("error", errorMsg);

        return "error";
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDeniedException(AccessDeniedException ex, Model model, Principal principal, Locale locale) {
        String user = (principal != null) ? principal.getName() : "Anonymous";
        log.warn("Access Denied: {} | User: {}", ex.getMessage(), user);

        model.addAttribute("status", 403);

        String errorMsg = messageSource.getMessage("error.403", null, locale);
        model.addAttribute("error", errorMsg);

        return "error";
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleRuntimeException(RuntimeException ex, Model model, Principal principal, Locale locale) {
        String user = (principal != null) ? principal.getName() : "Anonymous";
        log.error("Runtime error: {} | User: {}", ex.getMessage(), user, ex);

        model.addAttribute("status", 400);

        String errorMsg = messageSource.getMessage("error.400", null, locale);
        model.addAttribute("error", errorMsg);

        return "error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGlobalException(Exception ex, Model model, Principal principal, Locale locale) {
        String user = (principal != null) ? principal.getName() : "Anonymous";
        log.error("Internal Server Error: {} | User: {}", ex.getMessage(), user, ex);

        model.addAttribute("status", 500);

        String errorMsg = messageSource.getMessage("error.500", null, locale);
        model.addAttribute("error", errorMsg);

        return "error";
    }
}