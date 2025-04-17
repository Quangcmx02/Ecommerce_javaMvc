package com.example.demo.Repository;

import com.example.demo.Entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
public interface ProductRepository extends JpaRepository<Product, Long> {

    Boolean existsByName(String name);

    Optional<Product> findByProductId(Long productId);

    List<Product> findByName(String name);

    Page<Product> findByIsActiveTrue(Pageable pageable);

    Page<Product> findByIsActiveFalse(Pageable pageable);


    Page<Product> findAll(Pageable pageable);

    Page<Product> findByCategory_CategoryId(Long categoryId, Pageable pageable);
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND p.isActive = true")
    Page<Product> findByNameContainingIgnoreCaseAndIsActiveTrue(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.category.categoryId = :categoryId AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND p.isActive = true")
    Page<Product> findByCategory_CategoryIdAndNameContainingIgnoreCase(
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword,
            Pageable pageable
    );
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND p.isActive = :isActive")
    Page<Product> findByNameContainingIgnoreCaseAndIsActive(
            @Param("keyword") String keyword,
            @Param("isActive") boolean isActive,
            Pageable pageable
    );


}