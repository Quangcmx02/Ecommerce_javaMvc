package com.example.demo.Controller;

import com.example.demo.Entity.Product;
import com.example.demo.Service.CategoryService;
import com.example.demo.Service.LoggerService;
import com.example.demo.Service.ProductService;
import com.example.demo.Utils.PageUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/admin/product")
public class ProductController {
    @Autowired
    private ProductService productService;
@Autowired

private LoggerService loggerService;
@Autowired
private CategoryService categoryService;
    @GetMapping("/index")
    public String listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "true") boolean active,
            @RequestParam(required = false) String keyword,
            Model model) {
        try {
            int pageSize = 10;
            Pageable pageable = PageRequest.of(page, pageSize);
            Page<Product> productPage = productService.searchProductsForAdmin(keyword, active, pageable);

            model.addAttribute("products", productPage.getContent());
            model.addAttribute("currentPage", productPage.getNumber());
            model.addAttribute("pageNumbers", PageUtils.getPageNumbers(productPage));
            model.addAttribute("hasPrevious", PageUtils.hasPrevious(productPage));
            model.addAttribute("hasNext", PageUtils.hasNext(productPage));
            model.addAttribute("previousPage", PageUtils.getPreviousPage(productPage));
            model.addAttribute("nextPage", PageUtils.getNextPage(productPage));
            model.addAttribute("activeFilter", active);
            model.addAttribute("keyword", keyword);
            model.addAttribute("currentPageName", "products"); // Cho sidebar active

        } catch (Exception e) {
            loggerService.logError("Lỗi khi hiển thị danh sách sản phẩm", e);
            model.addAttribute("error", "Đã xảy ra lỗi khi tải danh sách sản phẩm.");
        }

        return "admin/product/index";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        try {
            model.addAttribute("product", new Product());
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("currentPage", "products");

        } catch (Exception e) {
            loggerService.logError("Lỗi khi hiển thị form tạo sản phẩm", e);
            model.addAttribute("error", "Đã xảy ra lỗi khi tải form tạo sản phẩm.");
        }

        return "admin/product/create";
    }

    @PostMapping("/create")
    public String createProduct(
            @Valid @ModelAttribute("product") Product product,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            if (result.hasErrors()) {
                model.addAttribute("categories", categoryService.findAll());
                return "admin/product/create";
            }

            productService.save(product);
            redirectAttributes.addFlashAttribute("message", "Tạo sản phẩm thành công!");
            return "redirect:/admin/product/index";

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categories", categoryService.findAll());
            return "admin/product/create";
        } catch (Exception e) {
            loggerService.logError("Lỗi khi tạo sản phẩm", e);
            model.addAttribute("error", "Đã xảy ra lỗi khi tạo sản phẩm.");
            model.addAttribute("categories", categoryService.findAll());
            return "admin/product/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        try {
            Optional<Product> product = productService.findById(id);
            if (product.isPresent()) {
                model.addAttribute("product", product.get());
                model.addAttribute("categories", categoryService.findAll());
                model.addAttribute("currentPage", "products");
                return "admin/product/edit";
            } else {
                return "redirect:/admin/product/index";
            }

        } catch (Exception e) {
            loggerService.logError("Lỗi khi hiển thị form chỉnh sửa sản phẩm ID: " + id, e);
            model.addAttribute("error", "Đã xảy ra lỗi khi tải form chỉnh sửa.");
            return "redirect:/admin/product/index";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("product") Product product,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            if (result.hasErrors()) {
                model.addAttribute("categories", categoryService.findAll());
                return "admin/product/edit";
            }

            Optional<Product> updatedProduct = productService.update(id, product);
            if (updatedProduct.isPresent()) {
                redirectAttributes.addFlashAttribute("message", "Cập nhật sản phẩm thành công!");
                return "redirect:/admin/product/index";
            } else {
                model.addAttribute("error", "Không tìm thấy sản phẩm để cập nhật.");
                model.addAttribute("categories", categoryService.findAll());
                return "admin/product/edit";
            }

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categories", categoryService.findAll());
            return "admin/product/edit";
        } catch (Exception e) {
            loggerService.logError("Lỗi khi cập nhật sản phẩm ID: " + id, e);
            model.addAttribute("error", "Đã xảy ra lỗi khi cập nhật sản phẩm.");
            model.addAttribute("categories", categoryService.findAll());
            return "admin/product/edit";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            boolean deleted = productService.delete(id);
            if (deleted) {
                redirectAttributes.addFlashAttribute("message", "Xóa sản phẩm thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm để xóa.");
            }
        } catch (Exception e) {
            loggerService.logError("Lỗi khi xóa sản phẩm ID: " + id, e);
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi xóa sản phẩm.");
        }
        return "redirect:/admin/product/index";
    }
}