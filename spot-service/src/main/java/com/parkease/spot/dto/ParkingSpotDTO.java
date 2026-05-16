package com.parkease.spot.dto;

import com.parkease.spot.entity.ParkingSpot;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingSpotDTO {
    private Long spotId;

    @NotNull
    private Long lotId;

    @NotBlank
    private String spotNumber;

    @NotBlank
    private String floor;

    @NotNull
    private ParkingSpot.SpotType spotType;

    @NotNull
    private ParkingSpot.VehicleType vehicleType;

    private ParkingSpot.SpotStatus status;
    private Boolean isEVCharging;
    private Boolean isHandicapped;

    @NotNull
    @Positive
    private BigDecimal pricePerHour;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
