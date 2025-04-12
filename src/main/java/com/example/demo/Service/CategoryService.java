package com.example.demo.Service;

import com.example.demo.Entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    Page<Category> findByActiveTrue(Pageable pageable);

    Page<Category> findByActiveFalse(Pageable pageable);

    Category create(Category category);

    Category edit(Long categoryId, Category category);

    Page<Category> findAll(Pageable pageable);

    List<Category> findAll();

    Optional<Category> findById(Long id);

    void deleteById(Long id);

    Page<Category> searchCategories(String keyword, Boolean isActive, Pageable pageable);
}
