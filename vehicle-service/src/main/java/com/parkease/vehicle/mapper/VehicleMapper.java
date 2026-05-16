package com.parkease.vehicle.mapper;

import com.parkease.vehicle.dto.VehicleDTO;
import com.parkease.vehicle.entity.Vehicle;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {
    public VehicleDTO toDto(Vehicle vehicle) {
        return VehicleDTO.builder()
                .vehicleId(vehicle.getVehicleId())
                .ownerId(vehicle.getOwnerId())
                .licensePlate(vehicle.getLicensePlate())
                .make(vehicle.getMake())
                .model(vehicle.getModel())
                .color(vehicle.getColor())
                .vehicleType(vehicle.getVehicleType())
                .isEV(vehicle.getIsEV())
                .registeredAt(vehicle.getRegisteredAt())
                .isActive(vehicle.getIsActive())
                .build();
    }

    public Vehicle toEntity(Long ownerId, VehicleDTO dto) {
        return Vehicle.builder()
                .vehicleId(dto.getVehicleId())
                .ownerId(ownerId)
                .licensePlate(dto.getLicensePlate())
                .make(dto.getMake())
                .model(dto.getModel())
                .color(dto.getColor())
                .vehicleType(dto.getVehicleType())
                .isEV(Boolean.TRUE.equals(dto.getIsEV()))
                .isActive(dto.getIsActive() == null || dto.getIsActive())
                .build();
    }
}
