package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.EmployeeProfile;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final UserRepository userRepository;

    @Override
    public List<EmployeeDTO> getAllEmployees() {
        List<User> employees = userRepository.findAllByRolesContaining(Role.EMPLOYEE);

        return employees.stream()
                .map(this::mapper)
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeDTO getEmployeeByEmail(String email) {
        User employee = userRepository.findByEmail(email)
                .orElseThrow(()->new NotFoundException("Employee not found"));
        if (!employee.getRoles().contains(Role.EMPLOYEE)) {
            throw new NotFoundException("This user is not an employee");
        }

        return mapper(employee);
    }

    @Override
    @Transactional
    public EmployeeDTO updateEmployeeByEmail(String email, EmployeeDTO employee) {
        User user = userRepository.findByEmail(email)
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
        user.setPassword(employee.getPassword());

        user.getEmployeeProfile().setPhone(employee.getPhone());
        user.getEmployeeProfile().setBirthDate(employee.getBirthDate());

        User updatedUser = userRepository.save(user);

        return mapper(updatedUser);
    }

    @Override
    @Transactional
    public void deleteEmployeeByEmail(String email) {
        User employee =  userRepository.findByEmail(email)
                .orElseThrow(()->new NotFoundException("Employee not found"));
        userRepository.delete(employee);
    }

    @Override
    @Transactional
    public EmployeeDTO addEmployee(EmployeeDTO employee) {
        if(userRepository.findByEmail(employee.getEmail()).isPresent()){
            throw new AlreadyExistException("Employee with this email already exists");
        }
        User user = new User();
        user.setEmail(employee.getEmail());
        user.setName(employee.getName());
        user.setPassword(employee.getPassword());
        user.setRoles(Set.of(Role.EMPLOYEE));

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
        employeeDTO.setPassword(user.getPassword());
        employeeDTO.setName(user.getName());
        employeeDTO.setEmail(user.getEmail());
        employeeDTO.setBirthDate(user.getEmployeeProfile().getBirthDate());
        employeeDTO.setPhone(user.getEmployeeProfile().getPhone());
        return employeeDTO;
    }
}
