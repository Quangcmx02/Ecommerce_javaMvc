package com.example.demo.Controller;

import com.example.demo.Entity.Category;
import com.example.demo.Service.CategoryService;
import com.example.demo.Service.LoggerService;
import com.example.demo.Utils.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
@Controller
@RequestMapping("/admin/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private LoggerService loggerService;
    // Hiển thị danh sách danh mục với phân trang, lọc, và tìm kiếm
    @GetMapping("/index")
    public String index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive,
            Model model) {
        try {
            Pageable pageable = PageRequest.of(page, 10);
            Page<Category> categories = categoryService.searchCategories(keyword, isActive, pageable);

            model.addAttribute("categories", categories);
            model.addAttribute("pageNumbers", PageUtils.getPageNumbers(categories));
            model.addAttribute("hasPrevious", PageUtils.hasPrevious(categories));
            model.addAttribute("hasNext", PageUtils.hasNext(categories));
            model.addAttribute("previousPage", PageUtils.getPreviousPage(categories));
            model.addAttribute("nextPage", PageUtils.getNextPage(categories));
            model.addAttribute("keyword", keyword);
            model.addAttribute("isActive", isActive);

            loggerService.logInfo("Loaded category list for admin, page: " + page + ", keyword: " + keyword + ", isActive: " + isActive);
            return "admin/category/index";
        } catch (Exception ex) {
            loggerService.logError("Error loading category list", ex);
            model.addAttribute("error", "Unable to load categories");
            return "admin/category/index";
        }
    }

    // Hiển thị form tạo danh mục mới
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("category", new Category());
        return "admin/category/create";
    }

    // Xử lý tạo danh mục mới
    @PostMapping
    public String create(@ModelAttribute Category category, Model model) {
        try {
            categoryService.create(category);
            loggerService.logInfo("Created new category: " + category.getName());
            return "redirect:/admin/category/index";
        } catch (IllegalArgumentException ex) {
            loggerService.logError("Failed to create category: " + category.getName(), ex);
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("category", category);
            return "admin/category/create";
        }
    }

    // Hiển thị form chỉnh sửa danh mục
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        try {
            Category category = categoryService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            model.addAttribute("category", category);
            return "admin/category/edit";
        } catch (IllegalArgumentException ex) {
            loggerService.logError("Category not found for ID: " + id, ex);
            model.addAttribute("error", "Category not found");
            return "redirect:/admin/category/index";
        }
    }

    // Xử lý chỉnh sửa danh mục
    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id, @ModelAttribute Category category, Model model) {
        try {
            categoryService.edit(id, category);
            loggerService.logInfo("Updated category ID: " + id);
            return "redirect:/admin/category/index";
        } catch (IllegalArgumentException ex) {
            loggerService.logError("Failed to update category ID: " + id, ex);
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("category", category);
            return "admin/category/edit";
        }
    }

    // Xử lý xóa danh mục
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, Model model) {
        try {
            categoryService.deleteById(id);
            loggerService.logInfo("Deleted category ID: " + id);
            return "redirect:/admin/category/index";
        } catch (IllegalArgumentException ex) {
            loggerService.logError("Failed to delete category ID: " + id, ex);
            model.addAttribute("error", ex.getMessage());
            return "redirect:/admin/category/index";
        }
    }}