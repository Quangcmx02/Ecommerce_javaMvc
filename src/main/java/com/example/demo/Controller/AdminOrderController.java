package com.example.demo.Controller;

import com.example.demo.Entity.Order;
import com.example.demo.Entity.Status;
import com.example.demo.Service.LoggerService;
import com.example.demo.Service.OrderService;
import com.example.demo.Utils.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/orders")
@PreAuthorize("hasRole('admin')")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private LoggerService loggerService;

    @GetMapping("/order")
    public String viewOrders(
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        try {
            int pageSize = 10;
            Pageable pageable = PageRequest.of(page, pageSize);
            Page<Order> orderPage = orderService.getAllOrders(pageable);

            model.addAttribute("orders", orderPage.getContent());
            model.addAttribute("currentPage", orderPage.getNumber());
            model.addAttribute("pageNumbers", PageUtils.getPageNumbers(orderPage));
            model.addAttribute("hasPrevious", PageUtils.hasPrevious(orderPage));
            model.addAttribute("hasNext", PageUtils.hasNext(orderPage));
            model.addAttribute("previousPage", PageUtils.getPreviousPage(orderPage));
            model.addAttribute("nextPage", PageUtils.getNextPage(orderPage));

            loggerService.logInfo("Displayed all orders for admin");
        } catch (Exception e) {
            loggerService.logError("Failed to display orders for admin", e);
            model.addAttribute("errorMsg", "Không thể tải danh sách đơn hàng: " + e.getMessage());
        }
        return "admin/orders/order";
    }

    @GetMapping("/order_detail/{id}")
    public String viewOrderDetail(@PathVariable("id") Long orderId, Model model) {
        try {
            Order order = orderService.getOrderById(orderId);
            model.addAttribute("order", order);
            model.addAttribute("availableStatuses", getAvailableStatuses(order.getStatus()));
            loggerService.logInfo("Displayed order detail for orderId: " + orderId + " for admin");
        } catch (Exception e) {
            loggerService.logError("Failed to display order detail for orderId: " + orderId + " for admin", e);
            model.addAttribute("errorMsg", "Không thể tải chi tiết đơn hàng: " + e.getMessage());
            return "redirect:/admin/orders/order";
        }
        return "admin/orders/order_detail";
    }

    @PostMapping("/update-status/{id}")
    public String updateOrderStatus(
            @PathVariable("id") Long orderId,
            @RequestParam("status") String status,
            RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(orderId, Status.valueOf(status));
            redirectAttributes.addFlashAttribute("successMsg", "Cập nhật trạng thái đơn hàng thành công!");
            loggerService.logInfo("Updated status for orderId: " + orderId + " to " + status);
        } catch (IllegalStateException e) {
            loggerService.logError("Failed to update status for orderId: " + orderId, e);
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        } catch (Exception e) {
            loggerService.logError("Failed to update status for orderId: " + orderId, e);
            redirectAttributes.addFlashAttribute("errorMsg", "Không thể cập nhật trạng thái đơn hàng: " + e.getMessage());
        }
        return "redirect:/admin/orders/order_detail/" + orderId;
    }

    @PostMapping("/cancel/{id}")
    public String cancelOrder(
            @PathVariable("id") Long orderId,
            RedirectAttributes redirectAttributes) {
        try {
            orderService.cancelOrder(orderId, null); // null userId cho admin
            redirectAttributes.addFlashAttribute("successMsg", "Hủy đơn hàng thành công!");
            loggerService.logInfo("Cancelled orderId: " + orderId + " by admin");
        } catch (IllegalStateException e) {
            loggerService.logError("Failed to cancel orderId: " + orderId + " by admin", e);
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        } catch (Exception e) {
            loggerService.logError("Failed to cancel orderId: " + orderId + " by admin", e);
            redirectAttributes.addFlashAttribute("errorMsg", "Không thể hủy đơn hàng: " + e.getMessage());
        }
        return "redirect:/admin/orders/detail/" + orderId;
    }

    private List<Status> getAvailableStatuses(Status currentStatus) {
        List<Status> availableStatuses = new ArrayList<>();
        switch (currentStatus) {
            case PENDING:
                availableStatuses.add(Status.PAID);
                availableStatuses.add(Status.DELIVERING);
                availableStatuses.add(Status.SHIPPED);
                break;
            case PAID:
                availableStatuses.add(Status.DELIVERING);
                availableStatuses.add(Status.SHIPPED);
                break;
            case DELIVERING:
                availableStatuses.add(Status.SHIPPED);
                break;
        }
        return availableStatuses;
    }
}
