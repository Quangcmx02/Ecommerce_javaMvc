package com.example.demo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class AuthController {
    @GetMapping("/auth/login")
    public String login() {

        return "auth/login";
    }

    @GetMapping("/auth/register")
    public String register() {

        return "auth/register";
    }

}
