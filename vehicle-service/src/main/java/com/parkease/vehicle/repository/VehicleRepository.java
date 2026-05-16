package com.parkease.vehicle.repository;

import com.parkease.vehicle.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByOwnerId(Long ownerId);
    List<Vehicle> findByOwnerIdAndIsActiveTrue(Long ownerId);
    Page<Vehicle> findByOwnerIdAndIsActiveTrue(Long ownerId, Pageable pageable);
    Optional<Vehicle> findByLicensePlate(String licensePlate);
    Optional<Vehicle> findByVehicleId(Long vehicleId);
    List<Vehicle> findByVehicleType(Vehicle.VehicleType vehicleType);
    List<Vehicle> findByIsEV(Boolean isEV);
    List<Vehicle> findByIsEVTrue();
    boolean existsByLicensePlate(String licensePlate);
    boolean existsByOwnerIdAndLicensePlate(Long ownerId, String licensePlate);
    Optional<Vehicle> findByOwnerIdAndLicensePlate(Long ownerId, String licensePlate);
    void deleteByVehicleId(Long vehicleId);
}
