package com.parkease.parkinglot.service;

import com.parkease.parkinglot.dto.ParkingLotDTO;
import com.parkease.parkinglot.entity.ParkingLot;
import com.parkease.parkinglot.mapper.ParkingLotMapper;
import com.parkease.parkinglot.repository.ParkingLotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

/**
 * ParkingLotServiceImpl — implements ParkingLotService (class diagram: .parkinglot.service).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ParkingLotServiceImpl implements ParkingLotService {

    private final ParkingLotRepository parkingLotRepository;
    private final ParkingLotMapper mapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final LotNotificationService lotNotificationService;

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @Override
    public ParkingLotDTO createLot(ParkingLotDTO dto) {
        ParkingLot lot = mapper.toEntity(dto);
        lot.setIsApproved(false); // always starts pending
        ParkingLotDTO saved = mapper.toDto(parkingLotRepository.save(lot));
        log.info("Created parking lot id={} managerId={}", saved.getLotId(), saved.getManagerId());
        // Notify all admins about the new pending lot
        lotNotificationService.notifyAdminsLotPending(saved.getLotId(), saved.getName(), saved.getManagerId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "parking-lots", key = "#lotId")
    public ParkingLotDTO getLotById(Long lotId) {
        return parkingLotRepository.findByLotId(lotId)
                .map(mapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Parking lot not found: " + lotId));
    }

    @Override
    @CacheEvict(value = "parking-lots", key = "#lotId")
    public ParkingLotDTO updateLot(Long lotId, ParkingLotDTO dto) {
        ParkingLot lot = parkingLotRepository.findByLotId(lotId)
                .orElseThrow(() -> new IllegalArgumentException("Parking lot not found: " + lotId));
        lot.setName(dto.getName());
        lot.setAddress(dto.getAddress());
        lot.setCity(dto.getCity());
        lot.setLatitude(dto.getLatitude());
        lot.setLongitude(dto.getLongitude());
        lot.setTotalSpots(dto.getTotalSpots());
        if (dto.getAvailableSpots() != null) {
            lot.setAvailableSpots(dto.getAvailableSpots());
        }
        lot.setOpenTime(dto.getOpenTime());
        lot.setCloseTime(dto.getCloseTime());
        if (dto.getHourlyRate() != null) {
            lot.setHourlyRate(dto.getHourlyRate());
        }
        lot.setImageUrl(dto.getImageUrl());
        evictNearbyCache();
        return mapper.toDto(parkingLotRepository.save(lot));
    }

    @Override
    @CacheEvict(value = "parking-lots", key = "#lotId")
    public void deleteLot(Long lotId) {
        if (!parkingLotRepository.existsById(lotId)) {
            throw new IllegalArgumentException("Parking lot not found: " + lotId);
        }
        parkingLotRepository.deleteByLotId(lotId);
        evictNearbyCache();
        log.info("Deleted parking lot id={}", lotId);
    }

    // ── Discovery / search ────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<ParkingLotDTO> getLotsByCity(String city, Pageable pageable) {
        return parkingLotRepository
                .findByCityIgnoreCaseAndIsApprovedTrueAndIsOpenTrue(city, pageable)
                .map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParkingLotDTO> getNearbyLots(Double latitude, Double longitude, Double radiusKm) {
        String key = "lots:nearby:" + latitude + ":" + longitude + ":" + radiusKm;
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof List<?> list) {
            return list.stream().map(ParkingLotDTO.class::cast).toList();
        }
        List<ParkingLotDTO> lots = parkingLotRepository
                .findNearby(latitude, longitude, radiusKm)
                .stream().map(mapper::toDto).toList();
        redisTemplate.opsForValue().set(key, lots, Duration.ofMinutes(5));
        return lots;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ParkingLotDTO> getLotsByManager(Long managerId, Pageable pageable) {
        return parkingLotRepository.findByManagerId(managerId, pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ParkingLotDTO> searchLots(String query, Pageable pageable) {
        return parkingLotRepository.searchLots(query, pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ParkingLotDTO> getLotsByAvailability(Integer minAvailable, Pageable pageable) {
        return parkingLotRepository
                .findByAvailableSpotsGreaterThanAndIsApprovedTrueAndIsOpenTrue(minAvailable, pageable)
                .map(mapper::toDto);
    }

    // ── Status management ─────────────────────────────────────────────────────

    @Override
    @CacheEvict(value = "parking-lots", key = "#lotId")
    public ParkingLotDTO toggleOpen(Long lotId, boolean open) {
        ParkingLot lot = parkingLotRepository.findByLotId(lotId)
                .orElseThrow(() -> new IllegalArgumentException("Parking lot not found: " + lotId));
        lot.setIsOpen(open);
        log.info("Toggled lot id={} isOpen={}", lotId, open);
        return mapper.toDto(parkingLotRepository.save(lot));
    }

    // ── Admin approval ────────────────────────────────────────────────────────

    @Override
    @CacheEvict(value = "parking-lots", key = "#lotId")
    public ParkingLotDTO approveLot(Long lotId) {
        ParkingLot lot = parkingLotRepository.findByLotId(lotId)
                .orElseThrow(() -> new IllegalArgumentException("Parking lot not found: " + lotId));
        lot.setIsApproved(true);
        lot.setRejectionReason(null);
        ParkingLotDTO saved = mapper.toDto(parkingLotRepository.save(lot));
        log.info("Approved parking lot id={}", lotId);
        // Notify the manager their lot was approved
        lotNotificationService.notifyManagerLotApproved(saved.getLotId(), saved.getName(), saved.getManagerId());
        return saved;
    }

    @Override
    @CacheEvict(value = "parking-lots", key = "#lotId")
    public ParkingLotDTO rejectLot(Long lotId, String reason) {
        ParkingLot lot = parkingLotRepository.findByLotId(lotId)
                .orElseThrow(() -> new IllegalArgumentException("Parking lot not found: " + lotId));
        lot.setIsApproved(false);
        lot.setRejectionReason(reason);
        ParkingLotDTO saved = mapper.toDto(parkingLotRepository.save(lot));
        log.info("Rejected parking lot id={} reason={}", lotId, reason);
        // Notify the manager their lot was rejected
        lotNotificationService.notifyManagerLotRejected(saved.getLotId(), saved.getName(), saved.getManagerId(), reason);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ParkingLotDTO> getPendingLots(Pageable pageable) {
        return parkingLotRepository.findPendingLots(pageable).map(mapper::toDto);
    }

    // ── Availability counter ──────────────────────────────────────────────────

    @Override
    @CacheEvict(value = "parking-lots", key = "#lotId")
    public void decrementAvailable(Long lotId) {
        int updated = parkingLotRepository.decrementAvailable(lotId);
        if (updated == 0) {
            throw new IllegalStateException("No available spots in lot " + lotId);
        }
    }

    @Override
    @CacheEvict(value = "parking-lots", key = "#lotId")
    public void incrementAvailable(Long lotId) {
        parkingLotRepository.incrementAvailable(lotId);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Evict all nearby-cache keys when lot data changes. */
    private void evictNearbyCache() {
        try {
            var keys = redisTemplate.keys("lots:nearby:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("Failed to evict nearby cache: {}", e.getMessage());
        }
    }
}
