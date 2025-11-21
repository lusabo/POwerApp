package com.powerapp.dto;

public class AuthResponse {
    public String token;
    public String name;
    public String email;

    public AuthResponse(String token, String name, String email) {
        this.token = token;
        this.name = name;
        this.email = email;
    }
}
