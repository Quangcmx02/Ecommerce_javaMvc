package com.example.demo.Service;

import com.example.demo.Dto.Request.PasswordChangeDto;
import com.example.demo.Dto.Request.UserProfileUpdateDto;
import com.example.demo.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface UserService {
        User saveUser(User user);
        User getUserByEmail(String email);
        User getAllUsersByRole(String role);
        Boolean updateUserStatus(Boolean status, Long id);
        void userFailedAttemptIncrease(User user);
        void userAccountLock(User user);
        boolean isUnlockAccountTimeExpired(User user);
        void userFailedAttempt(int userId);
        boolean resetPassword(String email);
        boolean activateUser(String token);
        User getUserById(Long userId);
        void updateUserProfile(Long userId, UserProfileUpdateDto dto);
        void changeUserPassword(Long userId, PasswordChangeDto dto);
        Page<User> getAllUsers(Pageable pageable);
        void updateUserRole(Long userId, String newRole, Long currentAdminId);
        long count();
}
