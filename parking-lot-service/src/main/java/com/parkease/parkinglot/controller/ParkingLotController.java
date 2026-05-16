package com.parkease.parkinglot.controller;

import com.parkease.parkinglot.dto.ParkingLotDTO;
import com.parkease.parkinglot.service.ParkingLotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ParkingLotResource (REST layer) — exposes /api/v1/lots endpoints.
 * Matches: ParkingLotResource in the class diagram (.parkinglot.resource).
 */
@RestController
@RequestMapping("/api/v1/lots")
@RequiredArgsConstructor
public class ParkingLotController {

    private final ParkingLotService parkingLotService;

    // ── Create ────────────────────────────────────────────────────────────────

    /** POST /api/v1/lots — Lot Manager registers a new lot (starts pending). */
    @PostMapping
    public ResponseEntity<ParkingLotDTO> createLot(@Valid @RequestBody ParkingLotDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(parkingLotService.createLot(dto));
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    /** GET /api/v1/lots/{lotId} — Fetch lot details by ID. */
    @GetMapping("/{lotId}")
    public ResponseEntity<ParkingLotDTO> getLot(@PathVariable Long lotId) {
        return ResponseEntity.ok(parkingLotService.getLotById(lotId));
    }

    /** GET /api/v1/lots/city/{city} — Approved + open lots in a city (paginated). */
    @GetMapping("/city/{city}")
    public ResponseEntity<Page<ParkingLotDTO>> getByCity(@PathVariable String city, Pageable pageable) {
        return ResponseEntity.ok(parkingLotService.getLotsByCity(city, pageable));
    }

    /** GET /api/v1/lots/nearby?latitude=&longitude=&radiusKm= — GPS proximity search. */
    @GetMapping("/nearby")
    public ResponseEntity<List<ParkingLotDTO>> nearby(@RequestParam Double latitude,
                                                      @RequestParam Double longitude,
                                                      @RequestParam(defaultValue = "3") Double radiusKm) {
        return ResponseEntity.ok(parkingLotService.getNearbyLots(latitude, longitude, radiusKm));
    }

    /** GET /api/v1/lots/search?q= — Full-text search across name/address/city. */
    @GetMapping("/search")
    public ResponseEntity<Page<ParkingLotDTO>> search(@RequestParam String q, Pageable pageable) {
        return ResponseEntity.ok(parkingLotService.searchLots(q, pageable));
    }

    /** GET /api/v1/lots/manager/{managerId} — All lots owned by a manager. */
    @GetMapping("/manager/{managerId}")
    public ResponseEntity<Page<ParkingLotDTO>> byManager(@PathVariable Long managerId, Pageable pageable) {
        return ResponseEntity.ok(parkingLotService.getLotsByManager(managerId, pageable));
    }

    /**
     * GET /api/v1/lots/available?minSpots= — Lots with at least minSpots capacity.
     * Supports Driver "Filter by availability" use case.
     */
    @GetMapping("/available")
    public ResponseEntity<Page<ParkingLotDTO>> byAvailability(
            @RequestParam(defaultValue = "1") Integer minSpots, Pageable pageable) {
        return ResponseEntity.ok(parkingLotService.getLotsByAvailability(minSpots, pageable));
    }

    // ── Update ────────────────────────────────────────────────────────────────

    /** PUT /api/v1/lots/{lotId} — Lot Manager updates lot details. */
    @PutMapping("/{lotId}")
    public ResponseEntity<ParkingLotDTO> update(@PathVariable Long lotId,
                                                @Valid @RequestBody ParkingLotDTO dto) {
        return ResponseEntity.ok(parkingLotService.updateLot(lotId, dto));
    }

    /** PATCH /api/v1/lots/{lotId}/open?open= — Toggle lot open/closed in real time. */
    @PatchMapping("/{lotId}/open")
    public ResponseEntity<ParkingLotDTO> toggleOpen(@PathVariable Long lotId,
                                                    @RequestParam boolean open) {
        return ResponseEntity.ok(parkingLotService.toggleOpen(lotId, open));
    }

    // ── Admin approval ────────────────────────────────────────────────────────

    /** GET /api/v1/lots/admin/pending — Lists lots awaiting admin approval. */
    @GetMapping("/admin/pending")
    public ResponseEntity<Page<ParkingLotDTO>> pending(Pageable pageable) {
        return ResponseEntity.ok(parkingLotService.getPendingLots(pageable));
    }

    /** PATCH /api/v1/lots/{lotId}/approve — Admin approves a lot registration. */
    @PatchMapping("/{lotId}/approve")
    public ResponseEntity<ParkingLotDTO> approve(@PathVariable Long lotId) {
        return ResponseEntity.ok(parkingLotService.approveLot(lotId));
    }

    /** PATCH /api/v1/lots/{lotId}/reject?reason= — Admin rejects a lot with feedback. */
    @PatchMapping("/{lotId}/reject")
    public ResponseEntity<ParkingLotDTO> reject(@PathVariable Long lotId,
                                                @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(parkingLotService.rejectLot(lotId, reason));
    }

    // ── Availability (inter-service, called by Booking-Service) ───────────────

    /** POST /api/v1/lots/{lotId}/availability/decrement — Atomic spot decrement on booking. */
    @PostMapping("/{lotId}/availability/decrement")
    public ResponseEntity<Void> decrement(@PathVariable Long lotId) {
        parkingLotService.decrementAvailable(lotId);
        return ResponseEntity.noContent().build();
    }

    /** POST /api/v1/lots/{lotId}/availability/increment — Atomic spot increment on checkout/cancel. */
    @PostMapping("/{lotId}/availability/increment")
    public ResponseEntity<Void> increment(@PathVariable Long lotId) {
        parkingLotService.incrementAvailable(lotId);
        return ResponseEntity.noContent().build();
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    /** DELETE /api/v1/lots/{lotId} — Permanently removes a lot. */
    @DeleteMapping("/{lotId}")
    public ResponseEntity<Void> delete(@PathVariable Long lotId) {
        parkingLotService.deleteLot(lotId);
        return ResponseEntity.noContent().build();
    }

    // ── Health ────────────────────────────────────────────────────────────────

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Parking Lot Service is running");
    }
}
