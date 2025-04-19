package com.example.demo.Controller;

import com.example.demo.Dto.Request.PasswordChangeDto;
import com.example.demo.Dto.Request.UserProfileUpdateDto;
import com.example.demo.Entity.User;
import com.example.demo.Service.LoggerService;
import com.example.demo.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private LoggerService loggerService;

    @GetMapping("/account")
    public String viewAccount(Model model) {
        Long userId = getCurrentUserId();
        try {
            User user = userService.getUserById(userId);
            UserProfileUpdateDto profileDto = new UserProfileUpdateDto();
            profileDto.setFirstName(user.getFirstName());
            profileDto.setLastName(user.getLastName());
            profileDto.setImgLink(user.getImgLink());
            profileDto.setAddress(user.getAdress());

            model.addAttribute("user", user);
            model.addAttribute("profileDto", profileDto);
            model.addAttribute("passwordDto", new PasswordChangeDto());
            loggerService.logInfo("Displayed account page for userId: " + userId);
        } catch (Exception e) {
            loggerService.logError("Failed to display account page for userId: " + userId, e);
            model.addAttribute("errorMsg", "Không thể tải thông tin tài khoản: " + e.getMessage());
        }
        return "/user/account";
    }

    @PostMapping("/update-profile")
    public String updateProfile(
            @Valid @ModelAttribute("profileDto") UserProfileUpdateDto profileDto,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        Long userId = getCurrentUserId();
        if (result.hasErrors()) {
            loggerService.logWarning("Profile update validation errors for userId: " + userId);
            return "/user/account";
        }

        try {
            userService.updateUserProfile(userId, profileDto);
            redirectAttributes.addFlashAttribute("successMsg", "Cập nhật thông tin thành công!");
            loggerService.logInfo("Profile updated for userId: " + userId);
        } catch (Exception e) {
            loggerService.logError("Failed to update profile for userId: " + userId, e);
            redirectAttributes.addFlashAttribute("errorMsg", "Không thể cập nhật thông tin: " + e.getMessage());
        }
        return "redirect:/user/account";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @Valid @ModelAttribute("passwordDto") PasswordChangeDto passwordDto,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        Long userId = getCurrentUserId();
        if (result.hasErrors()) {
            loggerService.logWarning("Password change validation errors for userId: " + userId);
            return "/user/account";
        }

        try {
            userService.changeUserPassword(userId, passwordDto);
            redirectAttributes.addFlashAttribute("successMsg", "Thay đổi mật khẩu thành công!");
            loggerService.logInfo("Password changed for userId: " + userId);
        } catch (IllegalArgumentException e) {
            loggerService.logWarning("Password change failed for userId: " + userId + ": " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        } catch (Exception e) {
            loggerService.logError("Failed to change password for userId: " + userId, e);
            redirectAttributes.addFlashAttribute("errorMsg", "Không thể thay đổi mật khẩu: " + e.getMessage());
        }
        return "redirect:/user/account";
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
}
