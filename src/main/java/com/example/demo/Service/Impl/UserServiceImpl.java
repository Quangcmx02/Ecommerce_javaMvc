package com.example.demo.Service.Impl;

import com.example.demo.Entity.User;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Service.EmailService;
import com.example.demo.Service.LoggerService;
import com.example.demo.Service.UserService;
import com.example.demo.Utils.UserAuthUtlis;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    private LoggerService loggerService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private  EmailService emailService;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private static final int PASSWORD_LENGTH = 12;
    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found for userId: " + userId));
    }
    @Override
    public User saveUser(User user) {
        loggerService.logInfo("Attempting to save user: email=" + user.getEmail());
        user.setRole("admin");
        user.setIsEnable(true);
        user.setAccountStatusNonLocked(true);
        user.setAccountFailedAttemptCount(0);
        user.setAccountLockTime(null);
        user.setActivationToken(UUID.randomUUID().toString());
        // Validation đã được thực hiện ở controller, nhưng giữ kiểm tra này cho an toàn
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            loggerService.logError("Password is null or empty");
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        try {
            User savedUser = userRepository.save(user);
            loggerService.logInfo("User saved successfully: email=" + savedUser.getEmail());
            return savedUser;
        } catch (DataIntegrityViolationException e) {
            loggerService.logError("Email already exists: " + user.getEmail(), e);
            throw new RuntimeException("Email đã được sử dụng");
        } catch (Exception e) {
            loggerService.logError("Failed to save user: " + e.getMessage(), e);
            throw new RuntimeException("Không thể tạo người dùng: " + e.getMessage(), e);
        }
    }

    @Override
    public User getUserByEmail(String email) {

        return userRepository.findByEmail(email);
    }
    @Override
    public User getAllUsersByRole(String role) {
        // TODO Auto-generated method stub
        return userRepository.findByRole(role);
    }

    @Override
    public Boolean updateUserStatus(Boolean status, Long id) {
        Optional<User> userById = userRepository.findById(id);
        if (userById.isPresent()) {
            User user = userById.get();
            user.setIsEnable(status);
            userRepository.save(user);

            return true;
        } else {
            return false;
        }

    }

    @Override
    public void userFailedAttemptIncrease(User user) {
        int userAttempt= user.getAccountFailedAttemptCount()+1;
        user.setAccountFailedAttemptCount(userAttempt);
        userRepository.save(user);

    }

    @Override
    public void userAccountLock(User user) {
        user.setAccountStatusNonLocked(false);
        user.setAccountLockTime(new Date());
        userRepository.save(user);
    }

    @Override
    public boolean isUnlockAccountTimeExpired(User user) {
        long accountLockTime = user.getAccountLockTime().getTime();
        System.out.println("Account LockTime: "+accountLockTime);
        long  accountUnlockTime = accountLockTime+ UserAuthUtlis.UNLOCK_DURATION_TIME;
        System.out.println("Account Unlock Time :"+accountUnlockTime);

        long currentTime = System.currentTimeMillis();
        System.out.println("currentTime :"+currentTime);

        if(accountUnlockTime < currentTime) {
            user.setAccountStatusNonLocked(true);
            user.setAccountFailedAttemptCount(0);
            user.setAccountLockTime(null);
            userRepository.save(user);
            return true;
        }

        return false;
    }

    @Override
    public void userFailedAttempt(int userId) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean activateUser(String token) {
        try {
            Optional<User> userOpt = userRepository.findByActivationToken(token);
            if (userOpt.isEmpty()) {
                loggerService.logError("Invalid activation token: " + token);
                return false;
            }
            User user = userOpt.get();
            user.setIsActive(true);
            user.setActivationToken(null);
            userRepository.save(user);
            loggerService.logInfo("User activated successfully: email=" + user.getEmail());
            return true;
        } catch (Exception e) {
            loggerService.logError("Failed to activate user with token: " + token, e);
            return false;
        }
    }
    @Override
    public boolean resetPassword(String email) {
        try {
            User user = userRepository.findByEmail(email);
            if (user == null) {
                loggerService.logWarning("No user found with email: " + email);
                return false;
            }

            // Tạo mật khẩu ngẫu nhiên
            String newPassword = generateRandomPassword();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            // Gửi email chứa mật khẩu mới
            emailService.sendNewPasswordEmail(email, "Mật khẩu mới RED WINGS", newPassword);
            loggerService.logInfo("New password sent to: " + email);
            return true;
        } catch (Exception e) {
            loggerService.logError("Failed to reset password for: " + email, e);
            return false;
        }
    }

    private String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            password.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return password.toString();
    }



}
