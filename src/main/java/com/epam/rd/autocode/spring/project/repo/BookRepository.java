package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book,Long>, JpaSpecificationExecutor<Book> {
    Optional<Book> findByNameIgnoreCase(String name);

    @Query("SELECT DISTINCT b.author FROM Book b")
    List<String> findAllAuthors();

    @Query("SELECT DISTINCT b.genre FROM Book b")
    List<String> findAllGenres();

    @Query("SELECT DISTINCT b.language FROM Book b")
    List<String> findAllLanguages();
}
