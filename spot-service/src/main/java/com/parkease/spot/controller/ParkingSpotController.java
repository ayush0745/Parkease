package com.parkease.spot.controller;

import com.parkease.spot.dto.BulkCreateSpotRequest;
import com.parkease.spot.dto.ParkingSpotDTO;
import com.parkease.spot.entity.ParkingSpot;
import com.parkease.spot.service.SpotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SpotResource (REST layer) — exposes /api/v1/spots endpoints.
 * Matches SpotResource in the class diagram (.spot.resource).
 */
@RestController
@RequestMapping("/api/v1/spots")
@RequiredArgsConstructor
public class ParkingSpotController {

    private final SpotService spotService;

    // ── Create ────────────────────────────────────────────────────────────────

    /** POST /api/v1/spots — Add a single spot to a lot. */
    @PostMapping
    public ResponseEntity<ParkingSpotDTO> addSpot(@Valid @RequestBody ParkingSpotDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(spotService.addSpot(dto));
    }

    /** POST /api/v1/spots/bulk — Bulk-create spots for faster lot onboarding. */
    @PostMapping("/bulk")
    public ResponseEntity<List<ParkingSpotDTO>> addBulk(@Valid @RequestBody BulkCreateSpotRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(spotService.addBulkSpots(request));
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    /** GET /api/v1/spots/{spotId} — Get a spot by ID. */
    @GetMapping("/{spotId}")
    public ResponseEntity<ParkingSpotDTO> getById(@PathVariable Long spotId) {
        return ResponseEntity.ok(spotService.getSpotById(spotId));
    }

    /** GET /api/v1/spots/lot/{lotId} — All spots in a lot (paginated). */
    @GetMapping("/lot/{lotId}")
    public ResponseEntity<Page<ParkingSpotDTO>> getByLot(@PathVariable Long lotId, Pageable pageable) {
        return ResponseEntity.ok(spotService.getSpotsByLot(lotId, pageable));
    }

    /** GET /api/v1/spots/lot/{lotId}/available — Available spots in a lot. */
    @GetMapping("/lot/{lotId}/available")
    public ResponseEntity<List<ParkingSpotDTO>> getAvailable(@PathVariable Long lotId) {
        return ResponseEntity.ok(spotService.getAvailableSpots(lotId));
    }

    /** GET /api/v1/spots/lot/{lotId}/type?spotType= — Spots filtered by spot type. */
    @GetMapping("/lot/{lotId}/type")
    public ResponseEntity<List<ParkingSpotDTO>> getByType(@PathVariable Long lotId,
                                                          @RequestParam ParkingSpot.SpotType spotType) {
        return ResponseEntity.ok(spotService.getByTypeAndLot(lotId, spotType));
    }

    /** GET /api/v1/spots/lot/{lotId}/vehicle?vehicleType= — Spots compatible with a vehicle type. */
    @GetMapping("/lot/{lotId}/vehicle")
    public ResponseEntity<List<ParkingSpotDTO>> getByVehicleType(@PathVariable Long lotId,
                                                                  @RequestParam ParkingSpot.VehicleType vehicleType) {
        return ResponseEntity.ok(spotService.getByVehicleTypeAndLot(lotId, vehicleType));
    }

    /** GET /api/v1/spots/lot/{lotId}/ev — EV-charging spots in a lot. */
    @GetMapping("/lot/{lotId}/ev")
    public ResponseEntity<List<ParkingSpotDTO>> getEV(@PathVariable Long lotId) {
        return ResponseEntity.ok(spotService.getEVSpots(lotId));
    }

    /** GET /api/v1/spots/lot/{lotId}/handicapped — Handicapped-accessible spots in a lot. */
    @GetMapping("/lot/{lotId}/handicapped")
    public ResponseEntity<List<ParkingSpotDTO>> getHandicapped(@PathVariable Long lotId) {
        return ResponseEntity.ok(spotService.getHandicappedSpots(lotId));
    }

    // ── Status transitions ────────────────────────────────────────────────────

    /** PATCH /api/v1/spots/{spotId}/reserve — Mark spot as Reserved (on booking). */
    @PatchMapping("/{spotId}/reserve")
    public ResponseEntity<ParkingSpotDTO> reserve(@PathVariable Long spotId) {
        return ResponseEntity.ok(spotService.reserveSpot(spotId));
    }

    /** PATCH /api/v1/spots/{spotId}/occupy — Mark spot as Occupied (on check-in). */
    @PatchMapping("/{spotId}/occupy")
    public ResponseEntity<ParkingSpotDTO> occupy(@PathVariable Long spotId) {
        return ResponseEntity.ok(spotService.occupySpot(spotId));
    }

    /** PATCH /api/v1/spots/{spotId}/release — Mark spot as Available (on checkout/cancellation). */
    @PatchMapping("/{spotId}/release")
    public ResponseEntity<ParkingSpotDTO> release(@PathVariable Long spotId) {
        return ResponseEntity.ok(spotService.releaseSpot(spotId));
    }

    // ── Update / Delete ───────────────────────────────────────────────────────

    /** PUT /api/v1/spots/{spotId} — Update spot details (pricing, type, flags). */
    @PutMapping("/{spotId}")
    public ResponseEntity<ParkingSpotDTO> update(@PathVariable Long spotId,
                                                 @Valid @RequestBody ParkingSpotDTO dto) {
        return ResponseEntity.ok(spotService.updateSpot(spotId, dto));
    }

    /** DELETE /api/v1/spots/{spotId} — Remove a spot. */
    @DeleteMapping("/{spotId}")
    public ResponseEntity<Void> delete(@PathVariable Long spotId) {
        spotService.deleteSpot(spotId);
        return ResponseEntity.noContent().build();
    }

    // ── Count ─────────────────────────────────────────────────────────────────

    /** GET /api/v1/spots/lot/{lotId}/count/available — Count available spots. */
    @GetMapping("/lot/{lotId}/count/available")
    public ResponseEntity<Integer> countAvailable(@PathVariable Long lotId) {
        return ResponseEntity.ok(spotService.countAvailable(lotId));
    }

    /** GET /api/v1/spots/lot/{lotId}/count?status= — Count spots by any status. */
    @GetMapping("/lot/{lotId}/count")
    public ResponseEntity<Integer> countByStatus(@PathVariable Long lotId,
                                                  @RequestParam ParkingSpot.SpotStatus status) {
        return ResponseEntity.ok(spotService.countByStatus(lotId, status));
    }

    // ── Health ────────────────────────────────────────────────────────────────

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Spot Service is running");
    }
}
