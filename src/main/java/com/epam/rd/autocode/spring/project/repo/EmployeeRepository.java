package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.EmployeeProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<EmployeeProfile,Long> {
}
