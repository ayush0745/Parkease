package com.parkease.analytics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "occupancy_logs", indexes = {
        @Index(name = "idx_occupancy_lot", columnList = "lotId"),
        @Index(name = "idx_occupancy_lot_timestamp", columnList = "lotId,timestamp"),
        @Index(name = "idx_occupancy_vehicle_type", columnList = "vehicleType"),
        @Index(name = "idx_occupancy_event_type", columnList = "eventType")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OccupancyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @Column(nullable = false)
    private Long lotId;

    private Long spotId;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(nullable = false)
    private Double occupancyRate;

    @Column(nullable = false)
    private Integer availableSpots;

    @Column(nullable = false)
    private Integer totalSpots;

    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;

    private String spotType;
    private String eventType;
    private Long bookingId;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 3)
    @Builder.Default
    private String currency = "INR";

    private Long durationMinutes;

    private BigDecimal revenue;

    public enum VehicleType { TWO_WHEELER, FOUR_WHEELER, HEAVY }
}
