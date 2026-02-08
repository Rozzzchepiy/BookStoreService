package com.epam.rd.autocode.spring.project.spec;

import com.epam.rd.autocode.spring.project.criteria.BookSearchRequest;
import com.epam.rd.autocode.spring.project.model.Book;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class BookSpecification {

    public static Specification<Book> filterBooks(BookSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(request.getSearch())) {
                String pattern = "%" + request.getSearch().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern),
                        cb.like(cb.lower(root.get("author")), pattern)
                ));
            }

            if (request.getAuthors() != null && !request.getAuthors().isEmpty()) {
                predicates.add(root.get("author").in(request.getAuthors()));
            }

            if (request.getGenres() != null && !request.getGenres().isEmpty()) {
                predicates.add(root.get("genre").in(request.getGenres()));
            }

            if (request.getLanguages() != null && !request.getLanguages().isEmpty()) {
                predicates.add(root.get("language").in(request.getLanguages()));
            }

            if (request.getAgeGroups() != null && !request.getAgeGroups().isEmpty()) {
                predicates.add(root.get("ageGroup").in(request.getAgeGroups()));
            }

            if (request.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), request.getMinPrice()));
            }

            if (request.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), request.getMaxPrice()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}