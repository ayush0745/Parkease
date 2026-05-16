package com.parkease.auth.dto;

import com.parkease.auth.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String fullName;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 6)
    private String password;

    private String phone;

    /** Optional quick-reference vehicle plate — stored for driver accounts. */
    private String vehiclePlate;
}
