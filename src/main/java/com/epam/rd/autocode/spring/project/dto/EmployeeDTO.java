package com.epam.rd.autocode.spring.project.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDTO {
    private Long id;

    @NotBlank(message = "{validation.required}")
    @Email(message = "{validation.email}")
    private String email;

    @NotBlank(message = "{validation.required}")
    @Pattern(regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{6,25}$",
            message = "{validation.password.complexity}")
    private String password;

    @NotBlank(message = "{validation.required}")
    private String name;

    @NotBlank(message = "{validation.required}")
    private String phone;

    @NotNull(message = "{validation.required}")
    @Past(message = "{validation.date.past}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
}