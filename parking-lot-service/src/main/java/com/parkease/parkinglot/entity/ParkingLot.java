package com.parkease.parkinglot.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "parking_lots", indexes = {
        @Index(name = "idx_lot_city", columnList = "city"),
        @Index(name = "idx_lot_manager", columnList = "managerId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingLot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lotId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Integer totalSpots;

    @Column(nullable = false)
    private Integer availableSpots;

    @Column(nullable = false)
    private Long managerId;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isOpen = true;

    private LocalTime openTime;
    private LocalTime closeTime;

    @Column(nullable = false)
    @Builder.Default
    private Double hourlyRate = 5.0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isApproved = false;

    private String imageUrl;
    private String rejectionReason;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    void prePersist() {
        if (availableSpots == null) {
            availableSpots = totalSpots;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
