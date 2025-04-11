package com.example.demo.Service.Impl;

import com.example.demo.Entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

@Component
public class AuthSuccessImpl implements AuthenticationSuccessHandler {
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String email = authentication.getName();
        User user = userDetailsService.getUserEntity(email);
        request.getSession().setAttribute("currentUser", user); // Lưu User vào session

        Collection<? extends org.springframework.security.core.GrantedAuthority> authorities = authentication.getAuthorities();
        Set<String> roles = AuthorityUtils.authorityListToSet(authorities);
        System.out.println("ROLES :" + roles.toString());


            response.sendRedirect("/home");

    }
    }

