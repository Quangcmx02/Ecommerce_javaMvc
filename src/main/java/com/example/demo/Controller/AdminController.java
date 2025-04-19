package com.example.demo.Controller;

import com.example.demo.Service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private LoggerService loggerService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private UserService userService;

    @GetMapping("/home")
    public String adminHome(Model model) {
        try {

            long totalProducts = productService.count();
            long totalCategories = categoryService.count();

            long totalOrders = orderService.count() ;
            long totalUsers = userService.count();

            model.addAttribute("totalProducts", totalProducts);
            model.addAttribute("totalCategories", totalCategories);
            model.addAttribute("totalOrders", totalOrders);
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("currentPage", "home");
        } catch (Exception e) {
            loggerService.logError("Lỗi khi hiển thị dashboard admin", e);
            model.addAttribute("error", "Đã xảy ra lỗi khi tải dữ liệu dashboard.");
        }

        return "admin/home";
    }
}