package com.epam.rd.autocode.spring.project.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "employee_profiles")
@NoArgsConstructor
@Data
public class EmployeeProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone", unique = true, nullable = false)
    private String phone;

    @Column(name = "birth_date",  nullable = false)
    private LocalDate birthDate;

    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @OneToOne
    private User user;
}
