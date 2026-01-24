package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.BookItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<BookItem,Long> {
}
