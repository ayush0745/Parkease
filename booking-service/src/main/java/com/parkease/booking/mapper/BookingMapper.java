package com.parkease.booking.mapper;

import com.parkease.booking.dto.BookingDTO;
import com.parkease.booking.entity.Booking;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {
    public BookingDTO toDto(Booking booking) {
        return BookingDTO.builder()
                .bookingId(booking.getBookingId())
                .userId(booking.getUserId())
                .lotId(booking.getLotId())
                .spotId(booking.getSpotId())
                .vehiclePlate(booking.getVehiclePlate())
                .vehicleType(booking.getVehicleType())
                .bookingType(booking.getBookingType())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .checkInTime(booking.getCheckInTime())
                .checkOutTime(booking.getCheckOutTime())
                .status(booking.getStatus())
                .totalAmount(booking.getTotalAmount())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
