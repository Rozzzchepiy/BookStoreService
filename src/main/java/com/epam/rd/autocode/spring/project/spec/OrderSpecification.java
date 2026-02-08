package com.epam.rd.autocode.spring.project.spec;

import com.epam.rd.autocode.spring.project.criteria.OrderSearchRequest;
import com.epam.rd.autocode.spring.project.model.Order;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class OrderSpecification {

    public static Specification<Order> filterOrders(OrderSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getClientId() != null) {
                predicates.add(cb.equal(root.get("client").get("id"), request.getClientId()));
            }
            if (request.getEmployeeId() != null) {
                predicates.add(cb.equal(root.get("employee").get("id"), request.getEmployeeId()));
            }

            if (StringUtils.hasText(request.getSearch())) {
                String pattern = "%" + request.getSearch().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("client").get("email")), pattern),
                        cb.like(root.get("id").as(String.class), pattern)
                ));
            }

            if (request.getStatuses() != null && !request.getStatuses().isEmpty()) {
                predicates.add(root.get("status").in(request.getStatuses()));
            }

            if (request.getDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("orderDate"), request.getDateFrom().atStartOfDay()));
            }
            if (request.getDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("orderDate"), request.getDateTo().atTime(23, 59, 59)));
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