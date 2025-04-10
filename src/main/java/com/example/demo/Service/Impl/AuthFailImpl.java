package com.example.demo.Service.Impl;

import com.example.demo.Entity.User;
import com.example.demo.Repository.UserRepository;
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
   UserRepository userRepository;

    @Autowired
    UserService userService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String email = request.getParameter("username");
        User user = userRepository.findByEmail(email);

        if(user != null) {
            if(user.getIsEnable()) {
                //account is active

                if(user.getAccountStatusNonLocked()) {
                    //Non-locked / Unlocked
                    if(user.getAccountFailedAttemptCount() < UserAuthUtlis.ATTEMPT_COUNT) {
                        userService.userFailedAttemptIncrease(user);
                    }else {
                        //
                        userService.userAccountLock(user);
                        exception = new LockedException("Your account is Locked! Failed Attempt 3");
                    }

                }else {
                    //Locked
                    if(userService.isUnlockAccountTimeExpired(user)) {
                        exception = new LockedException("Your account is UnLocked, Now you can login to system");
                    }else {
                        exception = new LockedException("Your account is Locked! Please try after sometimes");
                    }
                }

            }else {
                //account is inactive
                exception = new LockedException("Your account is inactive");
            }
        }else {
            exception = new LockedException("Email & Password Invalid!");
        }

        super.setDefaultFailureUrl("/auth/login?error=true");
        super.onAuthenticationFailure(request, response, exception);
    }
}
