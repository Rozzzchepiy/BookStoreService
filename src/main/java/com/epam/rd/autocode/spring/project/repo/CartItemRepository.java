package com.epam.rd.autocode.spring.project.repo;


import com.epam.rd.autocode.spring.project.model.CartItem;
import com.epam.rd.autocode.spring.project.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Page<CartItem> findByUser(User user, Pageable pageable);

    Optional<CartItem> findByUserAndBookId(User user, Long bookId);

    void deleteByUser(User user);

    void deleteByUserAndBookId(User user, Long bookId);

    @Query("SELECT COALESCE(SUM(c.quantity * c.book.price), 0) FROM CartItem c WHERE c.user = :user")
    BigDecimal getTotalPriceByUser(@Param("user") User user);
}
