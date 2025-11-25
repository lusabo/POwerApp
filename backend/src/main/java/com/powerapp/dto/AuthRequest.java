package com.powerapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthRequest {

    @NotBlank
    @Email
    public String email;

    @NotBlank
    public String password;
}
