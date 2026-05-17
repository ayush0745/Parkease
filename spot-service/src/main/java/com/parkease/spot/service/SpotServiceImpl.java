package com.parkease.spot.service;

import com.parkease.spot.dto.BulkCreateSpotRequest;
import com.parkease.spot.dto.ParkingSpotDTO;
import com.parkease.spot.entity.ParkingSpot;
import com.parkease.spot.mapper.ParkingSpotMapper;
import com.parkease.spot.repository.ParkingSpotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * SpotServiceImpl — implements SpotService (class diagram: .spot.service).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SpotServiceImpl implements SpotService {

    private final ParkingSpotRepository repository;
    private final ParkingSpotMapper mapper;

    // ── Create ────────────────────────────────────────────────────────────────

    @Override
    public ParkingSpotDTO addSpot(ParkingSpotDTO dto) {
        repository.findByLotIdAndSpotNumber(dto.getLotId(), dto.getSpotNumber())
                .ifPresent(e -> { throw new IllegalStateException("Spot number already exists in this lot"); });
        ParkingSpotDTO saved = mapper.toDto(repository.save(mapper.toEntity(dto)));
        log.info("Added spot {} to lot {}", saved.getSpotNumber(), saved.getLotId());
        return saved;
    }

    @Override
    public List<ParkingSpotDTO> addBulkSpots(BulkCreateSpotRequest request) {
        String prefix = (request.getPrefix() == null || request.getPrefix().isBlank())
                ? request.getSpotType().name().substring(0, 1)
                : request.getPrefix();

        List<ParkingSpot> spots = new ArrayList<>();
        for (int i = 1; i <= request.getCount(); i++) {
            spots.add(ParkingSpot.builder()
                    .lotId(request.getLotId())
                    .spotNumber(prefix + String.format("%03d", i))
                    .floor(request.getFloor())
                    .spotType(request.getSpotType())
                    .vehicleType(request.getVehicleType())
                    .isEVCharging(Boolean.TRUE.equals(request.getIsEVCharging()))
                    .isHandicapped(Boolean.TRUE.equals(request.getIsHandicapped()))
                    .pricePerHour(request.getPricePerHour())
                    .status(ParkingSpot.SpotStatus.AVAILABLE)
                    .build());
        }
        List<ParkingSpotDTO> created = repository.saveAll(spots).stream().map(mapper::toDto).toList();
        log.info("Bulk created {} spots for lot {}", created.size(), request.getLotId());
        return created;
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ParkingSpotDTO getSpotById(Long spotId) {
        return repository.findBySpotId(spotId)
                .map(mapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Spot not found: " + spotId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ParkingSpotDTO> getSpotsByLot(Long lotId, Pageable pageable) {
        return repository.findByLotId(lotId, pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParkingSpotDTO> getAvailableSpots(Long lotId) {
        return repository.findByLotIdAndStatus(lotId, ParkingSpot.SpotStatus.AVAILABLE)
                .stream().map(mapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParkingSpotDTO> getByTypeAndLot(Long lotId, ParkingSpot.SpotType spotType) {
        return repository.findByLotIdAndSpotType(lotId, spotType)
                .stream().map(mapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParkingSpotDTO> getByVehicleTypeAndLot(Long lotId, ParkingSpot.VehicleType vehicleType) {
        return repository.findByLotIdAndVehicleType(lotId, vehicleType)
                .stream().map(mapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParkingSpotDTO> getEVSpots(Long lotId) {
        return repository.findByLotIdAndIsEVChargingTrue(lotId)
                .stream().map(mapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParkingSpotDTO> getHandicappedSpots(Long lotId) {
        return repository.findByLotIdAndIsHandicappedTrue(lotId)
                .stream().map(mapper::toDto).toList();
    }

    // ── Status transitions ────────────────────────────────────────────────────

    @Override
    public ParkingSpotDTO reserveSpot(Long spotId) {
        ParkingSpot spot = repository.findWithLockBySpotId(spotId)
                .orElseThrow(() -> new IllegalArgumentException("Spot not found: " + spotId));
        if (spot.getStatus() != ParkingSpot.SpotStatus.AVAILABLE) {
            throw new IllegalStateException("Spot " + spotId + " is not available (status=" + spot.getStatus() + ")");
        }
        spot.setStatus(ParkingSpot.SpotStatus.RESERVED);
        log.info("Reserved spot {}", spotId);
        return mapper.toDto(repository.save(spot));
    }

    @Override
    public ParkingSpotDTO occupySpot(Long spotId) {
        ParkingSpot spot = repository.findWithLockBySpotId(spotId)
                .orElseThrow(() -> new IllegalArgumentException("Spot not found: " + spotId));
        if (spot.getStatus() == ParkingSpot.SpotStatus.OCCUPIED) {
            throw new IllegalStateException("Spot " + spotId + " is already occupied");
        }
        spot.setStatus(ParkingSpot.SpotStatus.OCCUPIED);
        log.info("Occupied spot {}", spotId);
        return mapper.toDto(repository.save(spot));
    }

    @Override
    public ParkingSpotDTO releaseSpot(Long spotId) {
        ParkingSpot spot = repository.findWithLockBySpotId(spotId)
                .orElseThrow(() -> new IllegalArgumentException("Spot not found: " + spotId));
        spot.setStatus(ParkingSpot.SpotStatus.AVAILABLE);
        log.info("Released spot {}", spotId);
        return mapper.toDto(repository.save(spot));
    }

    // ── Update / Delete ───────────────────────────────────────────────────────

    @Override
    public ParkingSpotDTO updateSpot(Long spotId, ParkingSpotDTO dto) {
        ParkingSpot spot = repository.findBySpotId(spotId)
                .orElseThrow(() -> new IllegalArgumentException("Spot not found: " + spotId));
        spot.setSpotNumber(dto.getSpotNumber());
        spot.setFloor(dto.getFloor());
        spot.setSpotType(dto.getSpotType());
        spot.setVehicleType(dto.getVehicleType());
        spot.setIsEVCharging(Boolean.TRUE.equals(dto.getIsEVCharging()));
        spot.setIsHandicapped(Boolean.TRUE.equals(dto.getIsHandicapped()));
        spot.setPricePerHour(dto.getPricePerHour());
        return mapper.toDto(repository.save(spot));
    }

    @Override
    public void deleteSpot(Long spotId) {
        if (!repository.existsById(spotId)) {
            throw new IllegalArgumentException("Spot not found: " + spotId);
        }
        repository.deleteBySpotId(spotId);
        log.info("Deleted spot {}", spotId);
    }

    // ── Count ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public int countAvailable(Long lotId) {
        return repository.countAvailableSpots(lotId);
    }

    @Override
    @Transactional(readOnly = true)
    public int countByStatus(Long lotId, ParkingSpot.SpotStatus status) {
        return repository.countByLotIdAndStatus(lotId, status);
    }
}
