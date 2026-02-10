package com.epam.rd.autocode.spring.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PasswordResetDTO {

    private String token;

    @NotBlank(message = "{validation.required}")
    @Pattern(regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{6,25}$",
            message = "{validation.password.complexity}")
    private String password;
}