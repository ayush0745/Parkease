package com.parkease.parkinglot.mapper;

import com.parkease.parkinglot.dto.ParkingLotDTO;
import com.parkease.parkinglot.entity.ParkingLot;
import org.springframework.stereotype.Component;

@Component
public class ParkingLotMapper {
    public ParkingLotDTO toDto(ParkingLot lot) {
        return ParkingLotDTO.builder()
                .lotId(lot.getLotId())
                .name(lot.getName())
                .address(lot.getAddress())
                .city(lot.getCity())
                .latitude(lot.getLatitude())
                .longitude(lot.getLongitude())
                .totalSpots(lot.getTotalSpots())
                .availableSpots(lot.getAvailableSpots())
                .managerId(lot.getManagerId())
                .isOpen(lot.getIsOpen())
                .openTime(lot.getOpenTime())
                .closeTime(lot.getCloseTime())
                .hourlyRate(lot.getHourlyRate())
                .isApproved(lot.getIsApproved())
                .imageUrl(lot.getImageUrl())
                .rejectionReason(lot.getRejectionReason())
                .createdAt(lot.getCreatedAt())
                .updatedAt(lot.getUpdatedAt())
                .build();
    }

    public ParkingLot toEntity(ParkingLotDTO dto) {
        return ParkingLot.builder()
                .lotId(dto.getLotId())
                .name(dto.getName())
                .address(dto.getAddress())
                .city(dto.getCity())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .totalSpots(dto.getTotalSpots())
                .availableSpots(dto.getAvailableSpots() == null ? dto.getTotalSpots() : dto.getAvailableSpots())
                .managerId(dto.getManagerId())
                .isOpen(dto.getIsOpen() == null || dto.getIsOpen())
                .openTime(dto.getOpenTime())
                .closeTime(dto.getCloseTime())
                .hourlyRate(dto.getHourlyRate() == null ? 5.0 : dto.getHourlyRate())
                .isApproved(dto.getIsApproved() != null && dto.getIsApproved())
                .imageUrl(dto.getImageUrl())
                .rejectionReason(dto.getRejectionReason())
                .build();
    }
}
