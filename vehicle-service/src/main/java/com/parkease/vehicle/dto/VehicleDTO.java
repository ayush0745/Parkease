package com.parkease.vehicle.dto;

import com.parkease.vehicle.entity.Vehicle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleDTO {
    private Long vehicleId;
    private Long ownerId;

    @NotBlank
    private String licensePlate;
    @NotBlank
    private String make;
    @NotBlank
    private String model;
    @NotBlank
    private String color;
    @NotNull
    private Vehicle.VehicleType vehicleType;
    private Boolean isEV;
    private LocalDateTime registeredAt;
    private Boolean isActive;
}
