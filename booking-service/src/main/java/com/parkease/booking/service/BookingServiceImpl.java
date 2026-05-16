package com.parkease.booking.service;

import com.parkease.booking.config.RabbitConfig;
import com.parkease.booking.dto.BookingDTO;
import com.parkease.booking.dto.CreateBookingRequest;
import com.parkease.booking.dto.ExtendBookingRequest;
import com.parkease.booking.dto.ProcessPaymentRequest;
import com.parkease.booking.entity.Booking;
import com.parkease.booking.feign.ParkingLotServiceClient;
import com.parkease.booking.feign.PaymentServiceClient;
import com.parkease.booking.feign.SpotServiceClient;
import com.parkease.booking.mapper.BookingMapper;
import com.parkease.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {
    private final BookingRepository repository;
    private final BookingMapper mapper;
    private final SpotServiceClient spotClient;
    private final ParkingLotServiceClient lotClient;
    private final PaymentServiceClient paymentClient;
    private final RabbitTemplate rabbitTemplate;

    public BookingDTO createBooking(Long userId, CreateBookingRequest request) {
        LocalDateTime end = request.getEndTime() == null ? request.getStartTime().plusHours(1) : request.getEndTime();
        if (!repository.findConflictingBookings(request.getSpotId(), request.getStartTime(), end).isEmpty()) {
            throw new IllegalStateException("Spot is already booked for this time window");
        }
        spotClient.reserveSpot(request.getSpotId());
        lotClient.decrementAvailable(request.getLotId());
        Booking booking = Booking.builder()
                .userId(userId)
                .lotId(request.getLotId())
                .spotId(request.getSpotId())
                .vehiclePlate(request.getVehiclePlate())
                .vehicleType(request.getVehicleType())
                .bookingType(request.getBookingType())
                .startTime(request.getStartTime())
                .endTime(end)
                .status(Booking.BookingStatus.RESERVED)
                .totalAmount(calculateAmount(request.getSpotId(), request.getStartTime(), end))
                .build();
        Booking saved = repository.save(booking);
        
        if (request.getPaymentMethod() != null) {
            String spotNumber = "Spot " + saved.getSpotId();
            String lotName = "Lot " + saved.getLotId();
            try {
                var spot = spotClient.getSpot(saved.getSpotId());
                if (spot != null) spotNumber = (String) spot.get("spotNumber");
                var lot = lotClient.getLot(saved.getLotId());
                if (lot != null) lotName = (String) lot.get("name");
            } catch (Exception e) {
                log.warn("Failed to fetch spot/lot details for payment: {}", e.getMessage());
            }

            paymentClient.processPayment(userId, ProcessPaymentRequest.builder()
                .bookingId(saved.getBookingId())
                .lotId(saved.getLotId())
                .spotId(saved.getSpotId())
                .spotNumber(spotNumber)
                .lotName(lotName)
                .amount(saved.getTotalAmount())
                .mode(request.getPaymentMethod())
                .currency("INR")
                .build());
        }

        publish("booking.created", saved);
        return mapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public BookingDTO getBookingById(Long bookingId) {
        return repository.findByBookingId(bookingId).map(mapper::toDto).orElseThrow(() -> new IllegalArgumentException("Booking not found"));
    }

    @Transactional(readOnly = true)
    public BookingDTO getBooking(Long bookingId) {
        return getBookingById(bookingId);
    }

    @Transactional(readOnly = true)
    public Page<BookingDTO> getUserBookings(Long userId, Pageable pageable) {
        return repository.findByUserId(userId, pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<BookingDTO> getLotBookings(Long lotId, Pageable pageable) {
        return repository.findByLotId(lotId, pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<BookingDTO> getByStatus(Booking.BookingStatus status, Pageable pageable) {
        return repository.findByStatus(status, pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAvailableSpotsForTime(Long lotId, LocalDateTime start, LocalDateTime end) {
        List<Map<String, Object>> allAvailableSpots = spotClient.getAvailableSpots(lotId);
        if (start == null || end == null) return allAvailableSpots;

        return allAvailableSpots.stream()
                .filter(spot -> {
                    try {
                        Long spotId = Long.valueOf(String.valueOf(spot.get("spotId")));
                        return repository.findConflictingBookings(spotId, start, end).isEmpty();
                    } catch (Exception e) {
                        return false;
                    }
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingDTO> getActiveBookings(Long lotId) {
        return repository.findByLotId(lotId).stream()
                .filter(booking -> booking.getStatus() == Booking.BookingStatus.RESERVED
                        || booking.getStatus() == Booking.BookingStatus.ACTIVE)
                .map(mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingDTO> getBookingHistory(Long userId) {
        return repository.findByUserId(userId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingDTO> getBookingsBySpot(Long spotId) {
        return repository.findBySpotId(spotId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingDTO> getBookingsByVehiclePlate(String vehiclePlate) {
        return repository.findByVehiclePlate(vehiclePlate).stream()
                .map(mapper::toDto)
                .toList();
    }

    public BookingDTO checkIn(Long bookingId) {
        Booking booking = repository.findById(bookingId).orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (booking.getStatus() != Booking.BookingStatus.RESERVED) throw new IllegalStateException("Only reserved bookings can check in");
        booking.setStatus(Booking.BookingStatus.ACTIVE);
        booking.setCheckInTime(LocalDateTime.now());
        spotClient.occupySpot(booking.getSpotId());
        Booking saved = repository.save(booking);
        publish("booking.checkin", saved);
        return mapper.toDto(saved);
    }

    public BookingDTO checkOut(Long bookingId, String paymentMode) {
        Booking booking = repository.findById(bookingId).orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (booking.getStatus() != Booking.BookingStatus.ACTIVE) throw new IllegalStateException("Only active bookings can check out");
        LocalDateTime now = LocalDateTime.now();
        booking.setCheckOutTime(now);
        booking.setEndTime(now);
        booking.setTotalAmount(calculateAmount(booking.getSpotId(), booking.getCheckInTime(), now));
        booking.setStatus(Booking.BookingStatus.COMPLETED);
        spotClient.releaseSpot(booking.getSpotId());
        lotClient.incrementAvailable(booking.getLotId());
        Booking saved = repository.save(booking);
        String spotNumber = "Spot " + saved.getSpotId();
        String lotName = "Lot " + saved.getLotId();
        try {
            var spot = spotClient.getSpot(saved.getSpotId());
            if (spot != null) spotNumber = (String) spot.get("spotNumber");
            var lot = lotClient.getLot(saved.getLotId());
            if (lot != null) lotName = (String) lot.get("name");
        } catch (Exception e) {
            log.warn("Failed to fetch spot/lot details for payment: {}", e.getMessage());
        }

        paymentClient.processPayment(saved.getUserId(), ProcessPaymentRequest.builder()
            .bookingId(saved.getBookingId())
            .lotId(saved.getLotId())
            .spotId(saved.getSpotId())
            .spotNumber(spotNumber)
            .lotName(lotName)
            .amount(saved.getTotalAmount())
            .mode(paymentMode == null ? "CASH" : paymentMode)
            .currency("INR")
            .build());
        publish("booking.checkout", saved);
        return mapper.toDto(saved);
    }

    public BookingDTO extendBooking(Long bookingId, ExtendBookingRequest request) {
        Booking booking = repository.findById(bookingId).orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED || booking.getStatus() == Booking.BookingStatus.COMPLETED) {
            throw new IllegalStateException("Completed or cancelled bookings cannot be extended");
        }
        if (!repository.findConflictingBookings(booking.getSpotId(), booking.getEndTime(), request.getNewEndTime()).isEmpty()) {
            throw new IllegalStateException("Spot is not available for extension window");
        }
        booking.setEndTime(request.getNewEndTime());
        booking.setTotalAmount(calculateAmount(booking.getSpotId(), booking.getStartTime(), request.getNewEndTime()));
        Booking saved = repository.save(booking);
        publish("booking.extended", saved);
        return mapper.toDto(saved);
    }

    public void cancelBooking(Long bookingId) {
        Booking booking = repository.findById(bookingId).orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (booking.getStatus() == Booking.BookingStatus.COMPLETED) throw new IllegalStateException("Completed bookings cannot be cancelled");
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        spotClient.releaseSpot(booking.getSpotId());
        lotClient.incrementAvailable(booking.getLotId());
        repository.save(booking);
        publish("booking.cancelled", booking);
    }

    public BigDecimal calculateAmount(Long spotId, LocalDateTime start, LocalDateTime end) {
        BigDecimal price = extractPrice(spotClient.getSpot(spotId));
        long hours = Math.max(1, ChronoUnit.MINUTES.between(start, end) );
        return price.multiply(BigDecimal.valueOf(Math.max(1, (long) Math.ceil(hours / 60.0)))).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal extractPrice(Map<String, Object> spot) {
        Object value = spot.get("pricePerHour");
        if (value instanceof Number number) return BigDecimal.valueOf(number.doubleValue());
        if (value instanceof String string) return new BigDecimal(string);
        return BigDecimal.valueOf(50);
    }

    private void publish(String routingKey, Booking booking) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("eventType", routingKey);
        event.put("bookingId", booking.getBookingId());
        event.put("userId", booking.getUserId());
        event.put("lotId", booking.getLotId());
        event.put("spotId", booking.getSpotId());
        event.put("vehicleType", booking.getVehicleType().name());
        event.put("spotType", resolveSpotType(booking.getSpotId()));
        event.put("amount", booking.getTotalAmount());
        event.put("startTime", toIso(booking.getStartTime()));
        event.put("endTime", toIso(booking.getEndTime()));
        event.put("checkInTime", toIso(booking.getCheckInTime()));
        event.put("checkOutTime", toIso(booking.getCheckOutTime()));
        event.put("durationMinutes", durationMinutes(booking));
        event.put("timestamp", LocalDateTime.now().toString());
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, routingKey, event);
    }

    private String resolveSpotType(Long spotId) {
        try {
            Object spotType = spotClient.getSpot(spotId).get("spotType");
            return spotType == null ? null : String.valueOf(spotType);
        } catch (Exception ex) {
            log.debug("Unable to resolve spotType for analytics event, spotId={}: {}", spotId, ex.getMessage());
            return null;
        }
    }

    private Long durationMinutes(Booking booking) {
        LocalDateTime start = booking.getCheckInTime() == null ? booking.getStartTime() : booking.getCheckInTime();
        LocalDateTime end = booking.getCheckOutTime() == null ? booking.getEndTime() : booking.getCheckOutTime();
        if (start == null || end == null || !start.isBefore(end)) {
            return null;
        }
        return ChronoUnit.MINUTES.between(start, end);
    }

    private String toIso(LocalDateTime value) {
        return value == null ? null : value.toString();
    }

    @Scheduled(fixedDelayString = "${booking.expiry-check-ms:300000}")
    public void cancelExpiredPreBookings() {
        List<Booking> expired = repository.findExpiredPreBookings(LocalDateTime.now().minusMinutes(15));
        expired.forEach(booking -> {
            booking.setStatus(Booking.BookingStatus.CANCELLED);
            spotClient.releaseSpot(booking.getSpotId());
            lotClient.incrementAvailable(booking.getLotId());
            publish("booking.expired", booking);
        });
        if (!expired.isEmpty()) repository.saveAll(expired);
    }
}
