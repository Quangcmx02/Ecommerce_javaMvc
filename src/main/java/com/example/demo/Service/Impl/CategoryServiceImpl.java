package com.example.demo.Service.Impl;

import com.example.demo.Entity.Category;
import com.example.demo.Repository.CategoryRepository;
import com.example.demo.Service.CategoryService;
import com.example.demo.Service.LoggerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private LoggerService loggerService;
    @Override
    public Page<Category> findByActiveTrue(Pageable pageable) {
        return categoryRepository.findByIsActiveTrue(pageable);
    }

    @Override
    public Page<Category> findByActiveFalse(Pageable pageable) {
        return categoryRepository.findByIsActiveFalse(pageable);
    }

    @Override
    public Category create(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new IllegalArgumentException("Category name already exists: " + category.getName());
        }
        if (category.getIsActive() == null) {
            category.setIsActive(true);
        }
        return categoryRepository.save(category);
    }

    @Override
    public Category edit(Long categoryId, Category updatedCategory) {
        Optional<Category> existingCategoryOpt = categoryRepository.findById(categoryId);
        if (!existingCategoryOpt.isPresent()) {
            throw new IllegalArgumentException("Category not found with ID: " + categoryId);
        }

        Category existingCategory = existingCategoryOpt.get();

        if (!existingCategory.getName().equals(updatedCategory.getName()) &&
                categoryRepository.existsByName(updatedCategory.getName())) {
            throw new IllegalArgumentException("Category name already exists: " + updatedCategory.getName());
        }

        existingCategory.setName(updatedCategory.getName());
        existingCategory.setDescription(updatedCategory.getDescription());
        existingCategory.setImageLink(updatedCategory.getImageLink());
        existingCategory.setIsActive(updatedCategory.getIsActive());

        return categoryRepository.save(existingCategory);
    }

    @Override
    public Page<Category> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new IllegalArgumentException("Category not found with ID: " + id);
        }
        categoryRepository.deleteById(id);
    }

    @Override
    public Page<Category> searchCategories(String keyword, Boolean isActive, Pageable pageable) {
        if (keyword == null) {
            keyword = "";
        }
        if (isActive == null) {
            return categoryRepository.findByNameContainingIgnoreCase(keyword, pageable);
        }
        return categoryRepository.findByIsActiveAndNameContainingIgnoreCase(isActive, keyword, pageable);
    }
    @Override
    public long count() {
        try {
            return categoryRepository.count();
        } catch (Exception e) {
            loggerService.logError("Lỗi khi đếm tổng số danh mục", e);
            return 0;
        }
    }
}