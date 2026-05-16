package com.parkease.spot.repository;

import com.parkease.spot.entity.ParkingSpot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {

    // ── Required by class diagram ────────────────────────────────────────────

    /** findBySpotId — explicit named lookup (diagram compliance). */
    Optional<ParkingSpot> findBySpotId(Long spotId);

    /** findByLotId — all spots in a lot (list and paginated). */
    List<ParkingSpot> findByLotId(Long lotId);
    Page<ParkingSpot> findByLotId(Long lotId, Pageable pageable);

    /** findByLotIdAndStatus — spots in a lot filtered by status. */
    List<ParkingSpot> findByLotIdAndStatus(Long lotId, ParkingSpot.SpotStatus status);

    /** findByLotIdAndSpotType — spots in a lot filtered by type. */
    List<ParkingSpot> findByLotIdAndSpotType(Long lotId, ParkingSpot.SpotType spotType);

    /** findByLotIdAndVehicleType — spots in a lot filtered by vehicle type. */
    List<ParkingSpot> findByLotIdAndVehicleType(Long lotId, ParkingSpot.VehicleType vehicleType);

    /** findByIsEVCharging — all EV-charging spots (platform-wide or filtered below). */
    List<ParkingSpot> findByIsEVChargingTrue();

    /** Lot-scoped EV charging spots. */
    List<ParkingSpot> findByLotIdAndIsEVChargingTrue(Long lotId);

    /** Lot-scoped handicapped-accessible spots. */
    List<ParkingSpot> findByLotIdAndIsHandicappedTrue(Long lotId);

    /** countByLotIdAndStatus — required by diagram; used for analytics. */
    int countByLotIdAndStatus(Long lotId, ParkingSpot.SpotStatus status);

    /** deleteBySpotId — required by diagram. */
    @Modifying
    void deleteBySpotId(Long spotId);

    // ── Duplicate-check ──────────────────────────────────────────────────────
    Optional<ParkingSpot> findByLotIdAndSpotNumber(Long lotId, String spotNumber);

    // ── Optimistic-locking lookup (for occupy/release) ───────────────────────
    @Lock(LockModeType.OPTIMISTIC)
    Optional<ParkingSpot> findWithLockBySpotId(Long spotId);

    // ── Convenience count queries ────────────────────────────────────────────
    @Query("SELECT COUNT(p) FROM ParkingSpot p WHERE p.lotId = :lotId AND p.status = 'AVAILABLE'")
    int countAvailableSpots(@Param("lotId") Long lotId);

    @Query("SELECT COUNT(p) FROM ParkingSpot p WHERE p.lotId = :lotId AND p.status = 'OCCUPIED'")
    int countOccupiedSpots(@Param("lotId") Long lotId);
}
