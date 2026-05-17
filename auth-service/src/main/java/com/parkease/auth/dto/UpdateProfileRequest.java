package com.parkease.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @NotBlank
    private String fullName;
    @Email
    private String email;
    private String phone;
    private String vehiclePlate;
    private String profilePicUrl;
}
