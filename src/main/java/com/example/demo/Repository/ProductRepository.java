package com.example.demo.Repository;

import com.example.demo.Entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

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





}