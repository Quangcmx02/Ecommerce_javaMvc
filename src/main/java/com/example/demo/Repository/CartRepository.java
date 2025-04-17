package com.example.demo.Repository;

import com.example.demo.Entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserUserId(Long userId);
    @Query("SELECT c FROM Cart c JOIN c.cartItems ci WHERE ci.cartItem_Id = :cartItemId")
    Optional<Cart> findByCartItemId(@Param("cartItemId") Long cartItemId);
}
