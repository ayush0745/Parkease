package com.parkease.booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_booking_user", columnList = "userId"),
        @Index(name = "idx_booking_lot", columnList = "lotId"),
        @Index(name = "idx_booking_spot", columnList = "spotId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long lotId;

    @Column(nullable = false)
    private Long spotId;

    @Column(nullable = false)
    private String vehiclePlate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType vehicleType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingType bookingType;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.RESERVED;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum BookingStatus { RESERVED, ACTIVE, COMPLETED, CANCELLED }
    public enum BookingType { PRE, WALK_IN }
    public enum VehicleType { TWO_WHEELER, FOUR_WHEELER, HEAVY }
}
