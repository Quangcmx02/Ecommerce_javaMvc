package com.example.demo.Controller;

import com.example.demo.Entity.Order;
import com.example.demo.Entity.User;
import com.example.demo.Service.LoggerService;
import com.example.demo.Service.OrderService;
import com.example.demo.Service.UserService;
import com.example.demo.Utils.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private LoggerService loggerService;
    @GetMapping("/order")
    public String viewOrders(
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        Long userId = getCurrentUserId();
        try {
            int pageSize = 5;
            Pageable pageable = PageRequest.of(page, pageSize);
            Page<Order> orderPage = orderService.getOrdersByUserId(userId, pageable);

            model.addAttribute("orders", orderPage.getContent());
            model.addAttribute("currentPage", orderPage.getNumber());
            model.addAttribute("pageNumbers", PageUtils.getPageNumbers(orderPage));
            model.addAttribute("hasPrevious", PageUtils.hasPrevious(orderPage));
            model.addAttribute("hasNext", PageUtils.hasNext(orderPage));
            model.addAttribute("previousPage", PageUtils.getPreviousPage(orderPage));
            model.addAttribute("nextPage", PageUtils.getNextPage(orderPage));

            loggerService.logInfo("Displayed orders for userId: " + userId);
        } catch (Exception e) {
            loggerService.logError("Failed to display orders for userId: " + userId, e);
            model.addAttribute("errorMsg", "Không thể tải danh sách đơn hàng: " + e.getMessage());
        }
        return "orders/order";
    }

    @GetMapping("/order_detail/{id}")
    public String viewOrderDetail(@PathVariable("id") Long orderId, Model model) {
        Long userId = getCurrentUserId();
        try {
            Order order = orderService.getOrderById(orderId);
            if (!order.getUser().getUserId().equals(userId)) {
                throw new SecurityException("Bạn không có quyền xem đơn hàng này");
            }
            model.addAttribute("order", order);
            loggerService.logInfo("Displayed order detail for orderId: " + orderId + ", userId: " + userId);
        } catch (Exception e) {
            loggerService.logError("Failed to display order detail for orderId: " + orderId + ", userId: " + userId, e);
            model.addAttribute("errorMsg", "Không thể tải chi tiết đơn hàng: " + e.getMessage());
            return "redirect:/orders";
        }
        return "orders/order_detail";
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userService.getUserByEmail(email);
        if (user == null) {
            throw new IllegalStateException("User not found for email: " + email);
        }
        return user.getUserId();
    }
    @PostMapping("/cancel/{id}")
    public String cancelOrder(
            @PathVariable("id") Long orderId,
            RedirectAttributes redirectAttributes) {
        Long userId = getCurrentUserId();
        try {
            orderService.cancelOrder(orderId, userId);
            redirectAttributes.addFlashAttribute("successMsg", "Hủy đơn hàng thành công!");
            loggerService.logInfo("Cancelled orderId: " + orderId + " for userId: " + userId);
        } catch (SecurityException | IllegalStateException e) {
            loggerService.logError("Failed to cancel orderId: " + orderId + " for userId: " + userId, e);
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        } catch (Exception e) {
            loggerService.logError("Failed to cancel orderId: " + orderId + " for userId: " + userId, e);
            redirectAttributes.addFlashAttribute("errorMsg", "Không thể hủy đơn hàng: " + e.getMessage());
        }
        return "redirect:/orders/order_detail/" + orderId;
    }
}
