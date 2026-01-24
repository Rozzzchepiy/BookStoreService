package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.ClientProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<ClientProfile,Long> {

}
