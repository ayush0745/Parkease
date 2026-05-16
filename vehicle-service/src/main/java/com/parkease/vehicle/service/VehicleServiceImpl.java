package com.parkease.vehicle.service;

import com.parkease.vehicle.dto.VehicleDTO;
import com.parkease.vehicle.entity.Vehicle;
import com.parkease.vehicle.mapper.VehicleMapper;
import com.parkease.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository repository;
    private final VehicleMapper mapper;

    @Override
    public VehicleDTO registerVehicle(Long ownerId, VehicleDTO dto) {
        String plate = normalizePlate(dto.getLicensePlate());
        
        Optional<Vehicle> existingVehicle = repository.findByOwnerIdAndLicensePlate(ownerId, plate);
        if (existingVehicle.isPresent()) {
            Vehicle vehicle = existingVehicle.get();
            if (vehicle.getIsActive()) {
                throw new IllegalStateException("Vehicle already registered and active for this owner");
            }
            // Reactivate and update with new details
            vehicle.setIsActive(true);
            vehicle.setMake(dto.getMake());
            vehicle.setModel(dto.getModel());
            vehicle.setColor(dto.getColor());
            vehicle.setVehicleType(dto.getVehicleType());
            vehicle.setIsEV(Boolean.TRUE.equals(dto.getIsEV()));
            return mapper.toDto(repository.save(vehicle));
        }
        dto.setLicensePlate(plate);
        return mapper.toDto(repository.save(mapper.toEntity(ownerId, dto)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VehicleDTO> getVehicleById(Long vehicleId) {
        return repository.findByVehicleId(vehicleId).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleDTO> getVehiclesByOwner(Long ownerId) {
        return repository.findByOwnerIdAndIsActiveTrue(ownerId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VehicleDTO> getVehiclesByOwner(Long ownerId, Pageable pageable) {
        return repository.findByOwnerIdAndIsActiveTrue(ownerId, pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleDTO getByLicensePlate(String plate) {
        return repository.findByLicensePlate(normalizePlate(plate))
                .map(mapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));
    }

    @Override
    public VehicleDTO updateVehicle(Long vehicleId, VehicleDTO dto) {
        Vehicle vehicle = repository.findByVehicleId(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        String plate = normalizePlate(dto.getLicensePlate());
        if (!vehicle.getLicensePlate().equals(plate)
                && repository.existsByOwnerIdAndLicensePlate(vehicle.getOwnerId(), plate)) {
            throw new IllegalStateException("Vehicle already registered for this owner");
        }

        vehicle.setLicensePlate(plate);
        vehicle.setMake(dto.getMake());
        vehicle.setModel(dto.getModel());
        vehicle.setColor(dto.getColor());
        vehicle.setVehicleType(dto.getVehicleType());
        vehicle.setIsEV(Boolean.TRUE.equals(dto.getIsEV()));
        if (dto.getIsActive() != null) {
            vehicle.setIsActive(dto.getIsActive());
        }

        return mapper.toDto(repository.save(vehicle));
    }

    @Override
    public void deleteVehicle(Long vehicleId) {
        Vehicle vehicle = repository.findByVehicleId(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));
        vehicle.setIsActive(false);
        repository.save(vehicle);
    }

    @Override
    @Transactional(readOnly = true)
    public Vehicle.VehicleType getVehicleType(Long vehicleId) {
        return repository.findByVehicleId(vehicleId)
                .map(Vehicle::getVehicleType)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Vehicle.VehicleType getVehicleType(String plate) {
        return repository.findByLicensePlate(normalizePlate(plate))
                .map(Vehicle::getVehicleType)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEVVehicle(Long vehicleId) {
        return repository.findByVehicleId(vehicleId)
                .map(Vehicle::getIsEV)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEVVehicle(String plate) {
        return repository.findByLicensePlate(normalizePlate(plate))
                .map(Vehicle::getIsEV)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleDTO> getAllVehicles() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleDTO> getByVehicleType(Vehicle.VehicleType vehicleType) {
        return repository.findByVehicleType(vehicleType).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleDTO> getEVVehicles() {
        return repository.findByIsEVTrue().stream()
                .map(mapper::toDto)
                .toList();
    }

    private String normalizePlate(String plate) {
        if (plate == null || plate.isBlank()) {
            throw new IllegalArgumentException("licensePlate is required");
        }
        return plate.trim().toUpperCase();
    }
}
