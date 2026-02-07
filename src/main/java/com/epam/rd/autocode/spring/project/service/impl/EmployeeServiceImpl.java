package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.annotation.Loggable;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.EmployeeProfile;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeDTO> getAllEmployees(Pageable pageable, String keyword) {

        Page<User> employeesPage;
        if (keyword != null && !keyword.isBlank()) {
            employeesPage = userRepository.findAllByRolesContainingAndKeyword(Role.EMPLOYEE, keyword, pageable);
        } else {
            employeesPage = userRepository.findAllByRolesContaining(Role.EMPLOYEE, pageable);
        }

        return employeesPage.map(this::mapper);
    }

    @Override
    public EmployeeDTO getEmployeeById(Long id) {
        User employee = userRepository.findById(id)
                .orElseThrow(()->new NotFoundException("Employee not found"));
        if (!employee.getRoles().contains(Role.EMPLOYEE)) {
            throw new NotFoundException("This user is not an employee");
        }

        return mapper(employee);
    }

    @Override
    @Loggable
    @Transactional
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO employee) {
        User user = userRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Employee not found"));
        if (!user.getRoles().contains(Role.EMPLOYEE)) {
            throw new NotFoundException("This user is not an employee");
        }
        if (!user.getEmail().equals(employee.getEmail())) {
            if (userRepository.findByEmail(employee.getEmail()).isPresent()) {
                throw new AlreadyExistException("Employee with this email already exists");
            }
            user.setEmail(employee.getEmail());
        }

        user.setName(employee.getName());
        if (employee.getPassword() != null && !employee.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(employee.getPassword()));
        }

        user.getEmployeeProfile().setPhone(employee.getPhone());
        user.getEmployeeProfile().setBirthDate(employee.getBirthDate());

        User updatedUser = userRepository.save(user);

        return mapper(updatedUser);
    }

    @Override
    @Loggable
    @Transactional
    public void deleteEmployee(Long id) {
        User employee =  userRepository.findById(id)
                .orElseThrow(()->new NotFoundException("Employee not found"));
        userRepository.delete(employee);
    }

    @Override
    @Loggable
    @Transactional
    public EmployeeDTO addEmployee(EmployeeDTO employee) {
        if(userRepository.findByEmail(employee.getEmail()).isPresent()){
            throw new AlreadyExistException("Employee with this email already exists");
        }
        User user = new User();
        user.setEmail(employee.getEmail());
        user.setName(employee.getName());
        String encodedPassword = passwordEncoder.encode(employee.getPassword());
        user.setPassword(encodedPassword);
        user.setRoles(new HashSet<>(Set.of(Role.EMPLOYEE)));

        EmployeeProfile profile = new EmployeeProfile();
        profile.setPhone(employee.getPhone());
        profile.setBirthDate(employee.getBirthDate());

        profile.setUser(user);
        user.setEmployeeProfile(profile);

        User updatedUser = userRepository.save(user);
        return mapper(updatedUser);
    }

    private EmployeeDTO mapper(User user) {
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setId(user.getId());
        employeeDTO.setPassword(null);
        employeeDTO.setName(user.getName());
        employeeDTO.setEmail(user.getEmail());
        employeeDTO.setBirthDate(user.getEmployeeProfile().getBirthDate());
        employeeDTO.setPhone(user.getEmployeeProfile().getPhone());
        return employeeDTO;
    }
}
