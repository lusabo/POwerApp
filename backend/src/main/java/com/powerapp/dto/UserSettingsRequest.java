package com.powerapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UserSettingsRequest {

    @NotBlank
    @Email
    public String jiraApiEmail;

    @NotBlank
    public String jiraApiToken;
}
