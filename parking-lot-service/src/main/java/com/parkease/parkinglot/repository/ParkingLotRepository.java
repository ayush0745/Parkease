package com.parkease.parkinglot.repository;

import com.parkease.parkinglot.entity.ParkingLot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {

    // ── Required by class diagram ────────────────────────────────────────────

    /** findByLotId — wraps the default findById for diagram compliance. */
    Optional<ParkingLot> findByLotId(Long lotId);

    /** findByCity — approved + open lots in a city (paginated). */
    Page<ParkingLot> findByCityIgnoreCaseAndIsApprovedTrueAndIsOpenTrue(String city, Pageable pageable);

    /** findByManagerId — all lots belonging to a manager. */
    Page<ParkingLot> findByManagerId(Long managerId, Pageable pageable);

    /** findByIsOpen — filter lots by open/closed status. */
    Page<ParkingLot> findByIsOpen(Boolean isOpen, Pageable pageable);

    /** 
     * findPendingLots — specifically for lots that are NOT approved AND have no rejection reason.
     */
    @Query("SELECT p FROM ParkingLot p WHERE p.isApproved = false AND p.rejectionReason IS NULL")
    Page<ParkingLot> findPendingLots(Pageable pageable);

    /** findByIsApproved — general purpose lookup for approved vs unapproved. */
    Page<ParkingLot> findByIsApproved(Boolean isApproved, Pageable pageable);

    /** findByAvailableSpotsGreaterThan — lots that still have capacity. */
    Page<ParkingLot> findByAvailableSpotsGreaterThanAndIsApprovedTrueAndIsOpenTrue(
            Integer availableSpots, Pageable pageable);

    /** countByCity — aggregate count per city for analytics. */
    long countByCityIgnoreCase(String city);

    /** deleteByLotId — required by class diagram. */
    void deleteByLotId(Long lotId);

    // ── Custom JPQL queries ──────────────────────────────────────────────────

    /** Full-text search across name, address, and city. */
    @Query("SELECT p FROM ParkingLot p WHERE p.isApproved = true AND p.isOpen = true AND " +
            "(LOWER(p.city)    LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            " LOWER(p.address) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            " LOWER(p.name)    LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<ParkingLot> searchLots(@Param("query") String query, Pageable pageable);

    /**
     * findNearby — Haversine formula; returns lots within radiusKm of the given coordinates.
     * Only approved + open lots are returned.
     */
    @Query("SELECT p FROM ParkingLot p WHERE p.isApproved = true AND p.isOpen = true AND " +
            "(6371 * acos(" +
            "  cos(radians(:latitude)) * cos(radians(p.latitude)) " +
            "  * cos(radians(p.longitude) - radians(:longitude)) " +
            "  + sin(radians(:latitude)) * sin(radians(p.latitude))" +
            ")) <= :radiusKm")
    List<ParkingLot> findNearby(@Param("latitude") Double latitude,
                                @Param("longitude") Double longitude,
                                @Param("radiusKm") Double radiusKm);

    /**
     * decrementAvailable — atomic decrement; returns rows-affected (0 = no capacity left).
     */
    @Modifying
    @Query("UPDATE ParkingLot p SET p.availableSpots = p.availableSpots - 1 " +
            "WHERE p.lotId = :lotId AND p.availableSpots > 0")
    int decrementAvailable(@Param("lotId") Long lotId);

    /**
     * incrementAvailable — atomic increment; caps at totalSpots.
     */
    @Modifying
    @Query("UPDATE ParkingLot p SET p.availableSpots = " +
            "CASE WHEN p.availableSpots < p.totalSpots THEN p.availableSpots + 1 " +
            "     ELSE p.availableSpots END " +
            "WHERE p.lotId = :lotId")
    int incrementAvailable(@Param("lotId") Long lotId);
}
