package com.parkease.vehicle.controller;

import com.parkease.vehicle.dto.VehicleDTO;
import com.parkease.vehicle.entity.Vehicle;
import com.parkease.vehicle.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService service;

    @PostMapping
    public ResponseEntity<VehicleDTO> register(@RequestHeader("X-User-Id") Long ownerId,
                                               @Valid @RequestBody VehicleDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registerVehicle(ownerId, dto));
    }

    @GetMapping("/{vehicleId}")
    public ResponseEntity<VehicleDTO> get(@PathVariable Long vehicleId) {
        return service.getVehicleById(vehicleId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/owner/me")
    public ResponseEntity<List<VehicleDTO>> mine(@RequestHeader("X-User-Id") Long ownerId) {
        return ResponseEntity.ok(service.getVehiclesByOwner(ownerId));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<Page<VehicleDTO>> byOwner(@PathVariable Long ownerId, Pageable pageable) {
        return ResponseEntity.ok(service.getVehiclesByOwner(ownerId, pageable));
    }

    @GetMapping("/all")
    public ResponseEntity<List<VehicleDTO>> all() {
        return ResponseEntity.ok(service.getAllVehicles());
    }

    @GetMapping("/plate/{plate}")
    public ResponseEntity<VehicleDTO> byPlate(@PathVariable String plate) {
        return ResponseEntity.ok(service.getByLicensePlate(plate));
    }

    @GetMapping("/plate/{plate}/type")
    public ResponseEntity<Vehicle.VehicleType> type(@PathVariable String plate) {
        return ResponseEntity.ok(service.getVehicleType(plate));
    }

    @GetMapping("/plate/{plate}/ev")
    public ResponseEntity<Boolean> ev(@PathVariable String plate) {
        return ResponseEntity.ok(service.isEVVehicle(plate));
    }

    @GetMapping("/{vehicleId}/type")
    public ResponseEntity<Vehicle.VehicleType> typeById(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(service.getVehicleType(vehicleId));
    }

    @GetMapping("/{vehicleId}/ev")
    public ResponseEntity<Boolean> evById(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(service.isEVVehicle(vehicleId));
    }

    @GetMapping("/type/{vehicleType}")
    public ResponseEntity<List<VehicleDTO>> byType(@PathVariable Vehicle.VehicleType vehicleType) {
        return ResponseEntity.ok(service.getByVehicleType(vehicleType));
    }

    @GetMapping("/ev")
    public ResponseEntity<List<VehicleDTO>> evVehicles() {
        return ResponseEntity.ok(service.getEVVehicles());
    }

    @PutMapping("/{vehicleId}")
    public ResponseEntity<VehicleDTO> update(@PathVariable Long vehicleId,
                                             @Valid @RequestBody VehicleDTO dto) {
        return ResponseEntity.ok(service.updateVehicle(vehicleId, dto));
    }

    @DeleteMapping("/{vehicleId}")
    public ResponseEntity<Void> delete(@PathVariable Long vehicleId) {
        service.deleteVehicle(vehicleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Vehicle Service is running");
    }
}
