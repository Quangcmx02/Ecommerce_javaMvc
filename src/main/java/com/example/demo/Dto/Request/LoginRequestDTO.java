package com.example.demo.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDTO {
    @NotBlank(message = "Email không được để trống")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    @Override
    public String toString() {
        return "LoginRequestDTO{email='" + email + "', password='[PROTECTED]'}";
    }
}