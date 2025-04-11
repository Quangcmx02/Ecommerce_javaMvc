package com.example.demo.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
@Configuration
public class SecurityConfig {
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    AuthenticationSuccessHandler authenticationSuccessHandler;

    @Autowired
    @Lazy
    AuthenticationFailureHandler authenticationFailureHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // Gắn UserDetailsService
        authProvider.setPasswordEncoder(passwordEncoder());     // Gắn PasswordEncoder
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .authenticationProvider(authenticationProvider()) // Dùng DaoAuthenticationProvider
                .authorizeHttpRequests(req -> req
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/home").permitAll()
                        .requestMatchers("/user/**").hasRole("user")
                        .requestMatchers("/admin/**").hasRole("user")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .failureHandler(authenticationFailureHandler)
                        .successHandler(authenticationSuccessHandler)
                )
                .logout(logout -> logout.permitAll());
        return http.build();
    }}