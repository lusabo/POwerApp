package com.powerapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank
    @Size(max = 255)
    public String name;

    @NotBlank
    @Email
    @Size(max = 255)
    public String email;

    @NotBlank
    @Size(min = 8, max = 255)
    public String password;
}
