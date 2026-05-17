package com.parkease.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserProfileDTO {
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private String vehiclePlate;
    private String role;
    private Boolean isActive;
    private String profilePicUrl;
    private LocalDateTime createdAt;
}
