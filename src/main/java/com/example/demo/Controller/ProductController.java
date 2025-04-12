package com.example.demo.Controller;

import com.example.demo.Entity.Product;
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

    @GetMapping
    public String listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "true") boolean active,
            Model model) {
        int pageSize = 10; // Số sản phẩm mỗi trang
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Product> productPage;

        // Lọc theo trạng thái active
        if (active) {
            productPage = productService.findByActiveTrue(pageable);
        } else {
            productPage = productService.findByActiveFalse(pageable);
        }

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", productPage.getNumber());
        model.addAttribute("pageNumbers", PageUtils.getPageNumbers(productPage));
        model.addAttribute("hasPrevious", PageUtils.hasPrevious(productPage));
        model.addAttribute("hasNext", PageUtils.hasNext(productPage));
        model.addAttribute("previousPage", PageUtils.getPreviousPage(productPage));
        model.addAttribute("nextPage", PageUtils.getNextPage(productPage));
        model.addAttribute("activeFilter", active); // Để giữ trạng thái bộ lọc trên UI

        return "index";
    }

    // Hiển thị form tạo sản phẩm mới
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new Product());
        return "admin/product/create";
    }

    // Xử lý tạo sản phẩm mới
    @PostMapping("/create")
    public String createProduct(
            @Valid @ModelAttribute("product") Product product,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/product/create";
        }

        product.setActive(true); // Mặc định sản phẩm mới là active
        productService.save(product);
        redirectAttributes.addFlashAttribute("success", "Tạo sản phẩm thành công!");
        return "redirect:/admin/product";
    }

    // Hiển thị form chỉnh sửa sản phẩm
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Product> product = productService.findById(id);
        if (product.isPresent()) {
            model.addAttribute("product", product.get());
            return "admin/products/edit";
        } else {
            redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại!");
            return "redirect:/admin/product";
        }
    }

    // Xử lý cập nhật sản phẩm
    @PostMapping("/edit/{id}")
    public String updateProduct(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("product") Product updatedProduct,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/product/edit";
        }

        Optional<Product> updated = productService.update(id, updatedProduct);
        if (updated.isPresent()) {
            redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không thể cập nhật sản phẩm!");
        }
        return "redirect:/admin/product";
    }

    // Kích hoạt/vô hiệu hóa sản phẩm
    @PostMapping("/toggle-active/{id}")
    public String toggleActive(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes) {
        Optional<Product> productOpt = productService.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setActive(!product.isActive()); // Đổi trạng thái active
            productService.save(product);
            redirectAttributes.addFlashAttribute("success",
                    product.isActive() ? "Kích hoạt sản phẩm thành công!" : "Vô hiệu hóa sản phẩm thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại!");
        }
        return "redirect:/admin/product";
    }
}
