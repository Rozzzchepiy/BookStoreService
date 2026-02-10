package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.PasswordResetDTO;
import com.epam.rd.autocode.spring.project.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/forgot-password")
@RequiredArgsConstructor
public class ForgotPasswordController {

    private final PasswordResetService passwordResetService;

    @GetMapping
    public String showForgotPasswordForm() {
        return "forgot_password";
    }

    @PostMapping
    public String processForgotPassword(@RequestParam("email") String email,
                                        HttpServletRequest request) {

        String appUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        passwordResetService.initiatePasswordReset(email, appUrl);
        return "redirect:/forgot-password?status=sent";
    }

    @GetMapping("/reset")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        boolean isValid = passwordResetService.validatePasswordResetToken(token);

        if (!isValid) {
            model.addAttribute("error", "invalid_token");
            return "forgot_password";
        }
        PasswordResetDTO resetRequest = new PasswordResetDTO();
        resetRequest.setToken(token);

        model.addAttribute("resetForm", resetRequest);

        return "reset_password";
    }

    @PostMapping("/reset")
    public String processResetPassword(@Valid @ModelAttribute("resetForm") PasswordResetDTO resetRequest,
                                       BindingResult bindingResult,
                                       Model model) {

        if (bindingResult.hasErrors()) {
            return "reset_password";
        }

        try {
            passwordResetService.updatePassword(resetRequest.getToken(), resetRequest.getPassword());
            return "redirect:/login?msg=password_changed";
        } catch (Exception e) {
            model.addAttribute("error", "update_failed");
            return "reset_password";
        }
    }
}