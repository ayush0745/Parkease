package com.parkease.spot.dto;

import com.parkease.spot.entity.ParkingSpot;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BulkCreateSpotRequest {
    @NotNull
    private Long lotId;
    @Min(1)
    private int count;
    @NotBlank
    private String floor;
    @NotNull
    private ParkingSpot.SpotType spotType;
    @NotNull
    private ParkingSpot.VehicleType vehicleType;
    private Boolean isEVCharging = false;
    private Boolean isHandicapped = false;
    @NotNull
    @Positive
    private BigDecimal pricePerHour;
    private String prefix;
}
