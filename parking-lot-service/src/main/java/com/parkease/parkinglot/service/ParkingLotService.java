package com.parkease.parkinglot.service;

import com.parkease.parkinglot.dto.ParkingLotDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * ParkingLotService — business contract (Java Interface).
 * Required by the class diagram; ParkingLotServiceImpl provides the implementation.
 */
public interface ParkingLotService {

    // ── CRUD ─────────────────────────────────────────────────────────────────
    ParkingLotDTO createLot(ParkingLotDTO dto);
    ParkingLotDTO getLotById(Long lotId);
    ParkingLotDTO updateLot(Long lotId, ParkingLotDTO dto);
    void deleteLot(Long lotId);

    // ── Discovery / search ────────────────────────────────────────────────────
    Page<ParkingLotDTO> getLotsByCity(String city, Pageable pageable);
    List<ParkingLotDTO> getNearbyLots(Double latitude, Double longitude, Double radiusKm);
    Page<ParkingLotDTO> getLotsByManager(Long managerId, Pageable pageable);
    Page<ParkingLotDTO> searchLots(String query, Pageable pageable);
    Page<ParkingLotDTO> getLotsByAvailability(Integer minAvailable, Pageable pageable);

    // ── Status management ─────────────────────────────────────────────────────
    ParkingLotDTO toggleOpen(Long lotId, boolean open);

    // ── Admin approval ────────────────────────────────────────────────────────
    ParkingLotDTO approveLot(Long lotId);
    ParkingLotDTO rejectLot(Long lotId, String reason);
    Page<ParkingLotDTO> getPendingLots(Pageable pageable);

    // ── Availability counter (called by Booking-Service) ──────────────────────
    void decrementAvailable(Long lotId);
    void incrementAvailable(Long lotId);
}
