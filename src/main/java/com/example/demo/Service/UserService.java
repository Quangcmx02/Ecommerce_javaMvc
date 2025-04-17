package com.example.demo.Service;

import com.example.demo.Entity.User;


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
}
