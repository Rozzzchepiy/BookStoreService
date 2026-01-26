package com.epam.rd.autocode.spring.project.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientDTO{

    private Long id;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 25)
    private String password;

    @NotBlank
    private String name;

    @NotNull
    @Min(0)
    private BigDecimal balance;
}
