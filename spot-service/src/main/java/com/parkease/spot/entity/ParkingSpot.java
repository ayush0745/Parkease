package com.parkease.spot.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "parking_spots", uniqueConstraints = @UniqueConstraint(columnNames = {"lotId", "spotNumber"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingSpot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long spotId;

    @Column(nullable = false)
    private Long lotId;

    @Column(nullable = false)
    private String spotNumber;

    @Column(nullable = false)
    private String floor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpotType spotType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType vehicleType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SpotStatus status = SpotStatus.AVAILABLE;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isEVCharging = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isHandicapped = false;

    @Column(nullable = false)
    private BigDecimal pricePerHour;

    @Version
    private Integer version;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum SpotStatus { AVAILABLE, RESERVED, OCCUPIED }
    public enum SpotType { COMPACT, STANDARD, LARGE, MOTORBIKE, EV }
    public enum VehicleType { TWO_WHEELER, FOUR_WHEELER, HEAVY }
}
