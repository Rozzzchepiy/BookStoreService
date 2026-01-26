package com.epam.rd.autocode.spring.project.conf;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig{

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(a -> a
                .requestMatchers("/", "/books", "/books/{id}", "/client/register", "/login", "/css/**", "/js/**", "/h2-console/**").permitAll()
                .requestMatchers("/books/add", "/books/edit/**", "/books/delete/**", "/clients/**", "/employees/**", "/orders/employee/**", "/orders/client/**").hasRole("EMPLOYEE")
                .requestMatchers("/profile/**" ).hasRole("CLIENT")
                .anyRequest().authenticated()
        )
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))

                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .formLogin(f->f
                        .loginPage("/login")
                        .defaultSuccessUrl("/books",  true)
                        .permitAll()
                )
                .logout(l -> l
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/books")
                        .permitAll()
                 );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
