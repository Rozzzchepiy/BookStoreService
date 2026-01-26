package com.epam.rd.autocode.spring.project.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDTO{
    private Long id;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 25)
    private String password;

    @NotBlank
    private String name;

    @NotBlank
    private String phone;

    @NotNull
    @Past
    private LocalDate birthDate;

}
