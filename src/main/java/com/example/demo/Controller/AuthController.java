package com.example.demo.Controller;

import ch.qos.logback.classic.Logger;
import com.example.demo.Dto.Request.LoginRequestDTO;
import com.example.demo.Dto.Request.RegisterRequestDTO;
import com.example.demo.Entity.User;
import com.example.demo.Service.LoggerService;
import com.example.demo.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;


@Controller

public class AuthController {
    @Autowired
    UserService userService;
    @Autowired
    private LoggerService loggerService;


    @GetMapping("/auth/login")
    public String login(Model model) {
        model.addAttribute("loginRequest", new LoginRequestDTO());
        return "auth/login";
    }
    @GetMapping("/auth/register")
    public String register(Model model) {
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO();
        model.addAttribute("registerRequest", registerRequestDTO);
        loggerService.logInfo("GET /auth/register - Initial RegisterRequestDTO: " + registerRequestDTO.toString());
        return "auth/register";
    }

    @PostMapping("/auth/register")
    public String saveUserDetails(
            @Valid @ModelAttribute("registerRequest") RegisterRequestDTO registerRequestDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model,
            HttpServletRequest request) {
        // Debug dữ liệu thô từ request
        loggerService.logInfo("Controller - Raw request parameters: " + request.getParameterMap().toString());
        loggerService.logInfo("Controller - Parameter 'email': " + request.getParameter("email"));
        loggerService.logInfo("Controller - Parameter 'firstName': " + request.getParameter("firstName"));
        loggerService.logInfo("Controller - Parameter 'lastName': " + request.getParameter("lastName"));
        loggerService.logInfo("Controller - Parameter 'password': " + request.getParameter("password"));
        loggerService.logInfo("Controller - Parameter 'confirmPassword': " + request.getParameter("confirmPassword"));

        // Debug dữ liệu bind vào DTO
        loggerService.logInfo("Received registration request: " + registerRequestDTO.toString());

        // Kiểm tra lỗi validation
        if (result.hasErrors()) {
            loggerService.logWarning("Validation errors detected: " + result.getAllErrors());
            model.addAttribute("errorMsg", "Vui lòng điền đầy đủ và đúng các trường bắt buộc.");
            model.addAttribute("registerRequest", registerRequestDTO);
            return "auth/register";
        }

        // Kiểm tra password và confirmPassword
        if (!registerRequestDTO.getPassword().equals(registerRequestDTO.getConfirmPassword())) {
            loggerService.logWarning("Password and confirmPassword do not match");
            model.addAttribute("confirmPasswordError", "Mật khẩu xác nhận không khớp.");
            model.addAttribute("registerRequest", registerRequestDTO);
            return "auth/register";
        }

        // Chuyển từ DTO sang User entity
        User user = new User();
        user.setEmail(registerRequestDTO.getEmail());
        user.setFirstName(registerRequestDTO.getFirstName());
        user.setLastName(registerRequestDTO.getLastName());
        user.setPassword(registerRequestDTO.getPassword());

        try {
            User savedUser = userService.saveUser(user);
            if (!ObjectUtils.isEmpty(savedUser)) {
                loggerService.logInfo("User registered successfully: " + savedUser.getEmail());
                redirectAttributes.addFlashAttribute("successMsg", "Đăng ký tài khoản thành công!");
            } else {
                loggerService.logError("Failed to save user: empty result");
                redirectAttributes.addFlashAttribute("errorMsg", "Có lỗi xảy ra trên server!");
            }
        } catch (Exception e) {
            loggerService.logError("Registration failed: " + e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMsg", "Đăng ký thất bại: " + e.getMessage());
        }
        return "redirect:/auth/register";
    }
}