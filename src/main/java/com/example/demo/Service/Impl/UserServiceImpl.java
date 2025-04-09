package com.example.demo.Service.Impl;

import com.example.demo.Entity.User;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Service.UserService;
import com.example.demo.Utils.UserAuthUtlis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User saveUser(User user) {
        System.out.println("user obje :"+user.toString());
        user.setRole("ROLE_USER");
        user.setIsEnable(true);
        user.setAccountStatusNonLocked(true);
        user.setAccountFailedAttemptCount(0);
        user.setAccountLockTime(null);
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        try {
            User saveUser = userRepository.save(user);

            return saveUser;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create user", e);
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






}
