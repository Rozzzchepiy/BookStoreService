package com.epam.rd.autocode.spring.project.conf;

import com.epam.rd.autocode.spring.project.model.EmployeeProfile;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        String adminEmail = "admin@employee.com";

        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            createEmployee(adminEmail);
        }
    }


    private void createEmployee(String email) {
        User user = new User();
        user.setEmail(email);
        user.setName("Admin");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRoles(Set.of(Role.EMPLOYEE));

        EmployeeProfile profile = new EmployeeProfile();
        profile.setPhone("0999999999");
        profile.setBirthDate(LocalDate.of(2005, 11, 9));
        profile.setUser(user);
        user.setEmployeeProfile(profile);

        userRepository.save(user);

        System.out.println("Default EMPLOYEE created: " + email + " / password");
    }
}
