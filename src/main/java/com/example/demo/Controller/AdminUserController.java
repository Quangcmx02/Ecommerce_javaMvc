package com.example.demo.Controller;

import com.example.demo.Entity.User;
import com.example.demo.Service.LoggerService;
import com.example.demo.Service.UserService;
import com.example.demo.Utils.PageUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/user")
@PreAuthorize("hasRole('admin')")
public class AdminUserController {
    @Autowired
    private UserService userService;

    @Autowired
    private LoggerService loggerService;

    @GetMapping("/usermanagement")
    public String viewUsers(
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        try {
            int pageSize = 10;
            Pageable pageable = PageRequest.of(page, pageSize);
            Page<User> userPage = userService.getAllUsers(pageable);
            Long currentAdminId = getCurrentAdminId();

            model.addAttribute("users", userPage.getContent());
            model.addAttribute("currentPage", userPage.getNumber());
            model.addAttribute("pageNumbers", PageUtils.getPageNumbers(userPage));
            model.addAttribute("hasPrevious", PageUtils.hasPrevious(userPage));
            model.addAttribute("hasNext", PageUtils.hasNext(userPage));
            model.addAttribute("previousPage", PageUtils.getPreviousPage(userPage));
            model.addAttribute("nextPage", PageUtils.getNextPage(userPage));
            model.addAttribute("currentAdminId", currentAdminId);

            loggerService.logInfo("Displayed user management page");
        } catch (Exception e) {
            loggerService.logError("Failed to display user management page", e);
            model.addAttribute("errorMsg", "Không thể tải danh sách người dùng: " + e.getMessage());
        }
        return "admin/user/usermanagement";
    }

    @PostMapping("/update-role")
    public String updateUserRole(
            @RequestParam("userId") Long userId,
            @RequestParam("role") String role,
            RedirectAttributes redirectAttributes) {
        try {
            Long currentAdminId = getCurrentAdminId();
            userService.updateUserRole(userId, role, currentAdminId);
            redirectAttributes.addFlashAttribute("successMsg", "Cập nhật vai trò thành công!");
            loggerService.logInfo("Updated role for userId: " + userId + " to " + role);
        } catch (IllegalStateException | IllegalArgumentException e) {
            loggerService.logError("Failed to update role for userId: " + userId + ": " + e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        } catch (Exception e) {
            loggerService.logError("Failed to update role for userId: " + userId, e);
            redirectAttributes.addFlashAttribute("errorMsg", "Không thể cập nhật vai trò: " + e.getMessage());
        }
        return "redirect:/admin/user/usermanagement";
    }

    @PostMapping("/update-status")
    public String updateUserStatus(
            @RequestParam("userId") Long userId,
            @RequestParam("status") Boolean status,
            RedirectAttributes redirectAttributes) {
        try {
            Long currentAdminId = getCurrentAdminId();
            if (userId.equals(currentAdminId)) {
                loggerService.logWarning("Admin attempted to change own status: userId=" + userId);
                throw new IllegalStateException("Không thể thay đổi trạng thái của chính bạn");
            }
            boolean updated = userService.updateUserStatus(status, userId);
            if (!updated) {
                throw new EntityNotFoundException("User not found for userId: " + userId);
            }
            redirectAttributes.addFlashAttribute("successMsg", "Cập nhật trạng thái thành công!");
            loggerService.logInfo("Updated status for userId: " + userId + " to " + status);
        } catch (IllegalStateException | EntityNotFoundException e) {
            loggerService.logError("Failed to update status for userId: " + userId + ": " + e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        } catch (Exception e) {
            loggerService.logError("Failed to update status for userId: " + userId, e);
            redirectAttributes.addFlashAttribute("errorMsg", "Không thể cập nhật trạng thái: " + e.getMessage());
        }
        return "redirect:/admin/user/usermanagement";
    }

    private Long getCurrentAdminId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userService.getUserByEmail(email);
        if (user == null) {
            throw new IllegalStateException("Admin not found for email: " + email);
        }
        return user.getUserId();
    }
}
