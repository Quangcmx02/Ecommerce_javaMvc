package com.example.demo.Service.Impl;

import com.example.demo.Entity.User;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Service.LoggerService;
import com.example.demo.Service.UserService;
import com.example.demo.Utils.UserAuthUtlis;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class AuthFailImpl extends SimpleUrlAuthenticationFailureHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoggerService loggerService;

    @Autowired
    private UserService userService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String email = request.getParameter("username");
        loggerService.logWarning("Đăng nhập thất bại cho email: " + email);

        User user = userRepository.findByEmail(email);
        String errorMessage;

        if (user != null) {
            // Kiểm tra trạng thái kích hoạt
            if (user.getIsActive() == null || !user.getIsActive()) {
                errorMessage = "Tài khoản chưa được kích hoạt. Vui lòng kiểm tra email để kích hoạt.";
                loggerService.logWarning("Tài khoản chưa kích hoạt: " + email);
            } else if (user.getIsEnable()) {
                // Tài khoản đã kích hoạt và không bị vô hiệu hóa
                if (user.getAccountStatusNonLocked()) {
                    // Tài khoản không bị khóa
                    if (user.getAccountFailedAttemptCount() < UserAuthUtlis.ATTEMPT_COUNT) {
                        userService.userFailedAttemptIncrease(user);
                        long remainingAttempts = UserAuthUtlis.ATTEMPT_COUNT - user.getAccountFailedAttemptCount();
                        errorMessage = "Email hoặc mật khẩu không đúng. Còn " + remainingAttempts + " lần thử.";
                        loggerService.logInfo("Tăng số lần đăng nhập thất bại cho: " + email + ", còn " + remainingAttempts + " lần");
                    } else {
                        userService.userAccountLock(user);
                        errorMessage = "Tài khoản của bạn đã bị khóa do nhập sai mật khẩu 3 lần. Vui lòng thử lại sau 5 phút.";
                        loggerService.logWarning("Tài khoản bị khóa do vượt quá số lần thử: " + email);
                    }
                } else {
                    // Tài khoản bị khóa
                    if (userService.isUnlockAccountTimeExpired(user)) {
                        errorMessage = "Tài khoản của bạn đã được mở khóa. Vui lòng thử đăng nhập lại.";
                        loggerService.logInfo("Tài khoản được mở khóa: " + email);
                    } else {
                        errorMessage = "Tài khoản của bạn đang bị khóa. Vui lòng thử lại sau 5 phút.";
                        loggerService.logWarning("Tài khoản đang bị khóa: " + email);
                    }
                }
            } else {
                // Tài khoản bị vô hiệu hóa
                errorMessage = "Tài khoản của bạn đã bị vô hiệu hóa.";
                loggerService.logWarning("Tài khoản bị vô hiệu hóa: " + email);
            }
        } else {
            errorMessage = "Email hoặc mật khẩu không đúng.";
            loggerService.logWarning("Không tìm thấy người dùng với email: " + email);
        }

        // Lưu thông báo lỗi vào session để hiển thị trên trang login
        request.getSession().setAttribute("errorMsg", errorMessage);
        super.setDefaultFailureUrl("/auth/login?error=true");
        super.onAuthenticationFailure(request, response, new AuthenticationException(errorMessage, exception) {});
    }
}