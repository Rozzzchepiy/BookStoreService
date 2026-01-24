package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.ClientProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientRepository extends JpaRepository<ClientProfile,Long> {

}
