package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.annotation.Loggable;
import com.epam.rd.autocode.spring.project.criteria.EmployeeSearchRequest;
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
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @Override
    public Page<EmployeeDTO> getAllEmployees(EmployeeSearchRequest request) {
        Page<User> employeesPage;

        if (StringUtils.hasText(request.getKeyword())) {
            employeesPage = userRepository.findAllByRolesContainingAndKeyword(
                    Role.EMPLOYEE, request.getKeyword(), request.getPageable());
        } else {
            employeesPage = userRepository.findAllByRolesContaining(
                    Role.EMPLOYEE, request.getPageable());
        }

        return employeesPage.map(this::mapToDto);
    }

    @Override
    public EmployeeDTO getEmployeeById(Long id) {
        User employee = getEmployeeEntityById(id);
        return mapToDto(employee);
    }

    @Override
    @Loggable
    @Transactional
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO employeeDTO) {
        User user = getEmployeeEntityById(id);

        if (!user.getEmail().equals(employeeDTO.getEmail())) {
            if (userRepository.findByEmail(employeeDTO.getEmail()).isPresent()) {
                throw new AlreadyExistException("Employee with this email already exists");
            }
            user.setEmail(employeeDTO.getEmail());
        }

        user.setName(employeeDTO.getName());

        if (StringUtils.hasText(employeeDTO.getPassword())) {
            user.setPassword(passwordEncoder.encode(employeeDTO.getPassword()));
        }

        if (user.getEmployeeProfile() == null) {
            user.setEmployeeProfile(new EmployeeProfile());
            user.getEmployeeProfile().setUser(user);
        }
        user.getEmployeeProfile().setPhone(employeeDTO.getPhone());
        user.getEmployeeProfile().setBirthDate(employeeDTO.getBirthDate());

        return mapToDto(userRepository.save(user));
    }

    @Override
    @Loggable
    @Transactional
    public void deleteEmployee(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("Employee not found");
        }
        userRepository.deleteById(id);
    }

    @Override
    @Loggable
    @Transactional
    public EmployeeDTO addEmployee(EmployeeDTO employeeDTO) {
        if (userRepository.findByEmail(employeeDTO.getEmail()).isPresent()) {
            throw new AlreadyExistException("Employee with this email already exists");
        }

        User user = new User();
        user.setEmail(employeeDTO.getEmail());
        user.setName(employeeDTO.getName());
        user.setPassword(passwordEncoder.encode(employeeDTO.getPassword()));
        user.setRoles(Set.of(Role.EMPLOYEE));

        EmployeeProfile profile = new EmployeeProfile();
        profile.setPhone(employeeDTO.getPhone());
        profile.setBirthDate(employeeDTO.getBirthDate());

        profile.setUser(user);
        user.setEmployeeProfile(profile);

        return mapToDto(userRepository.save(user));
    }

    private User getEmployeeEntityById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee not found"));
        if (!user.getRoles().contains(Role.EMPLOYEE)) {
            throw new NotFoundException("This user is not an employee");
        }
        return user;
    }

    private EmployeeDTO mapToDto(User user) {
        EmployeeDTO dto = modelMapper.map(user, EmployeeDTO.class);
        dto.setPassword(null);
        if (user.getEmployeeProfile() != null) {
            dto.setBirthDate(user.getEmployeeProfile().getBirthDate());
            dto.setPhone(user.getEmployeeProfile().getPhone());
        }
        return dto;
    }
}
