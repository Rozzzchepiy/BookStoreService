package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.criteria.EmployeeSearchRequest;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EmployeeService {

    Page<EmployeeDTO> getAllEmployees(EmployeeSearchRequest request);

    EmployeeDTO getEmployeeById(Long id);

    EmployeeDTO updateEmployee(Long id, EmployeeDTO employee);

    void deleteEmployee(Long id);

    EmployeeDTO addEmployee(EmployeeDTO employee);
}
