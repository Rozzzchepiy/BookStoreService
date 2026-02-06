package com.epam.rd.autocode.spring.project.spec;

import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BookSpecification {

    public static Specification<Book> filterBooks(String search, List<String> authors, List<String> genres,
                                                  List<Language> languages, List<AgeGroup> ageGroups, BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isEmpty()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }

            if (authors != null && !authors.isEmpty()) {
                predicates.add(root.get("author").in(authors));
            }

            if (genres != null && !genres.isEmpty()) {
                predicates.add(root.get("genre").in(genres));
            }

            if (ageGroups != null && !ageGroups.isEmpty()) {
                predicates.add(root.get("ageGroup").in(ageGroups));
            }

            if (languages != null && !languages.isEmpty()) {
                predicates.add(root.get("language").in(languages));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}