package com.parkease.spot.mapper;

import com.parkease.spot.dto.ParkingSpotDTO;
import com.parkease.spot.entity.ParkingSpot;
import org.springframework.stereotype.Component;

@Component
public class ParkingSpotMapper {
    public ParkingSpotDTO toDto(ParkingSpot spot) {
        return ParkingSpotDTO.builder()
                .spotId(spot.getSpotId())
                .lotId(spot.getLotId())
                .spotNumber(spot.getSpotNumber())
                .floor(spot.getFloor())
                .spotType(spot.getSpotType())
                .vehicleType(spot.getVehicleType())
                .status(spot.getStatus())
                .isEVCharging(spot.getIsEVCharging())
                .isHandicapped(spot.getIsHandicapped())
                .pricePerHour(spot.getPricePerHour())
                .createdAt(spot.getCreatedAt())
                .updatedAt(spot.getUpdatedAt())
                .build();
    }

    public ParkingSpot toEntity(ParkingSpotDTO dto) {
        return ParkingSpot.builder()
                .spotId(dto.getSpotId())
                .lotId(dto.getLotId())
                .spotNumber(dto.getSpotNumber())
                .floor(dto.getFloor())
                .spotType(dto.getSpotType())
                .vehicleType(dto.getVehicleType())
                .status(dto.getStatus() == null ? ParkingSpot.SpotStatus.AVAILABLE : dto.getStatus())
                .isEVCharging(Boolean.TRUE.equals(dto.getIsEVCharging()))
                .isHandicapped(Boolean.TRUE.equals(dto.getIsHandicapped()))
                .pricePerHour(dto.getPricePerHour())
                .build();
    }
}
