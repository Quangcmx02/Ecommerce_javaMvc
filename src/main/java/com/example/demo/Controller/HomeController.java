package com.example.demo.Controller;

import com.example.demo.Entity.Product;
import com.example.demo.Service.LoggerService;
import com.example.demo.Service.ProductService;
import com.example.demo.Utils.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class HomeController {
    @Autowired
    private ProductService productService;

    @Autowired
    private LoggerService loggerService;

    @GetMapping("/home")
    public String home(Model model,
                       @RequestParam(defaultValue = "0") int page) {
        try {
            int pageSize = 12;
            Pageable pageable = PageRequest.of(page, pageSize);
            Page<Product> productPage = productService.findByActiveTrue(pageable);

            model.addAttribute("products", productPage.getContent());
            model.addAttribute("currentPage", productPage.getNumber());
            model.addAttribute("pageNumbers", PageUtils.getPageNumbers(productPage));
            model.addAttribute("hasPrevious", PageUtils.hasPrevious(productPage));
            model.addAttribute("hasNext", PageUtils.hasNext(productPage));
            model.addAttribute("previousPage", PageUtils.getPreviousPage(productPage));
            model.addAttribute("nextPage", PageUtils.getNextPage(productPage));

        } catch (Exception e) {
            loggerService.logError("Lỗi khi hiển thị sản phẩm đang hoạt động trên trang chủ", e);
            model.addAttribute("error", "Đã xảy ra lỗi khi tải sản phẩm.");
        }

        return "home";
    }
    @GetMapping("/product/detail/{id}")
    public String showProductDetail(@PathVariable("id") Long id, Model model) {
        Optional<Product> product = productService.findById(id);

        if (product.isPresent()) {
            model.addAttribute("product", product.get());
            return "product/detail";
        } else {
            return "redirect:/product/list"; // hoặc trang báo lỗi riêng
        }
    }


}