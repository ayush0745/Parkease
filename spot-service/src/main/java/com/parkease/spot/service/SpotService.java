package com.parkease.spot.service;

import com.parkease.spot.dto.BulkCreateSpotRequest;
import com.parkease.spot.dto.ParkingSpotDTO;
import com.parkease.spot.entity.ParkingSpot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * SpotService — business contract (Java Interface).
 * Required by the class diagram (.spot.service); SpotServiceImpl provides the implementation.
 */
public interface SpotService {

    // ── Create ────────────────────────────────────────────────────────────────
    ParkingSpotDTO addSpot(ParkingSpotDTO dto);
    List<ParkingSpotDTO> addBulkSpots(BulkCreateSpotRequest request);

    // ── Read ──────────────────────────────────────────────────────────────────
    ParkingSpotDTO getSpotById(Long spotId);
    Page<ParkingSpotDTO> getSpotsByLot(Long lotId, Pageable pageable);
    List<ParkingSpotDTO> getAvailableSpots(Long lotId);
    List<ParkingSpotDTO> getByTypeAndLot(Long lotId, ParkingSpot.SpotType spotType);
    List<ParkingSpotDTO> getByVehicleTypeAndLot(Long lotId, ParkingSpot.VehicleType vehicleType);
    List<ParkingSpotDTO> getEVSpots(Long lotId);
    List<ParkingSpotDTO> getHandicappedSpots(Long lotId);

    // ── Status transitions ────────────────────────────────────────────────────
    ParkingSpotDTO reserveSpot(Long spotId);
    ParkingSpotDTO occupySpot(Long spotId);
    ParkingSpotDTO releaseSpot(Long spotId);

    // ── Update / Delete ───────────────────────────────────────────────────────
    ParkingSpotDTO updateSpot(Long spotId, ParkingSpotDTO dto);
    void deleteSpot(Long spotId);

    // ── Count ─────────────────────────────────────────────────────────────────
    int countAvailable(Long lotId);
    int countByStatus(Long lotId, ParkingSpot.SpotStatus status);
}
