package com.parkease.parkinglot.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingLotDTO {
    private Long lotId;

    @NotBlank
    private String name;

    @NotBlank
    private String address;

    @NotBlank
    private String city;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    @NotNull
    @PositiveOrZero
    private Integer totalSpots;

    private Integer availableSpots;

    @NotNull
    private Long managerId;

    private Boolean isOpen;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Double hourlyRate;
    private Boolean isApproved;
    private String imageUrl;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
