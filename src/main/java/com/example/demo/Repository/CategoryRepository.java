package com.example.demo.Repository;

import com.example.demo.Entity.Category;
import com.example.demo.Entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Page<Category> findByIsActiveTrue(Pageable pageable);

    Page<Category> findByIsActiveFalse(Pageable pageable);


Boolean existsByName(String categoryName);


    Page<Category> findAll(Pageable pageable);

    List<Category> findAll();
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Category> findByNameContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT c FROM Category c WHERE c.isActive = :isActive AND LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Category> findByIsActiveAndNameContainingIgnoreCase(@Param("isActive") Boolean isActive, @Param("keyword") String keyword, Pageable pageable);

}