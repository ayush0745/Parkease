package com.parkease.vehicle.service;

import com.parkease.vehicle.dto.VehicleDTO;
import com.parkease.vehicle.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface VehicleService {
    VehicleDTO registerVehicle(Long ownerId, VehicleDTO dto);
    Optional<VehicleDTO> getVehicleById(Long vehicleId);
    List<VehicleDTO> getVehiclesByOwner(Long ownerId);
    Page<VehicleDTO> getVehiclesByOwner(Long ownerId, Pageable pageable);
    VehicleDTO getByLicensePlate(String plate);
    VehicleDTO updateVehicle(Long vehicleId, VehicleDTO dto);
    void deleteVehicle(Long vehicleId);
    Vehicle.VehicleType getVehicleType(Long vehicleId);
    Vehicle.VehicleType getVehicleType(String plate);
    boolean isEVVehicle(Long vehicleId);
    boolean isEVVehicle(String plate);
    List<VehicleDTO> getAllVehicles();
    List<VehicleDTO> getByVehicleType(Vehicle.VehicleType vehicleType);
    List<VehicleDTO> getEVVehicles();
}
