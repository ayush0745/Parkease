package com.parkease.analytics.controller;

import com.parkease.analytics.dto.AnalyticsDTO;
import com.parkease.analytics.dto.LotReport;
import com.parkease.analytics.dto.PlatformSummary;
import com.parkease.analytics.service.AnalyticsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping({"/api/v1/analytics", "/analytics"})
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService service;

    @PostMapping("/occupancy")
    public ResponseEntity<AnalyticsDTO> logOccupancy(@Valid @RequestBody AnalyticsDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.logOccupancy(dto));
    }

    @GetMapping("/{logId}")
    public ResponseEntity<AnalyticsDTO> getLog(@PathVariable @Positive Long logId) {
        return service.getLog(logId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/lots/{lotId}")
    public ResponseEntity<Page<AnalyticsDTO>> byLot(@PathVariable @Positive Long lotId, Pageable pageable) {
        return ResponseEntity.ok(service.getByLot(lotId, pageable));
    }

    @GetMapping("/lots/{lotId}/range")
    public ResponseEntity<List<AnalyticsDTO>> range(@PathVariable @Positive Long lotId,
                                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(service.getByLotAndRange(lotId, start, end));
    }

    @GetMapping({"/occupancyRate", "/lots/{lotId}/occupancy-rate"})
    public ResponseEntity<Double> occupancyRate(@PathVariable(name = "lotId", required = false) Long lotIdPath,
                                                @RequestParam(required = false) Long lotId) {
        return ResponseEntity.ok(service.getOccupancyRate(resolveLotId(lotIdPath, lotId)));
    }

    @GetMapping({"/byHour", "/lots/{lotId}/by-hour"})
    public ResponseEntity<Map<Integer, Double>> byHour(@PathVariable(name = "lotId", required = false) Long lotIdPath,
                                                       @RequestParam(required = false) Long lotId) {
        return ResponseEntity.ok(service.getOccupancyByHour(resolveLotId(lotIdPath, lotId)));
    }

    @GetMapping({"/peakHours", "/lots/{lotId}/peak-hours"})
    public ResponseEntity<List<Integer>> peakHours(@PathVariable(name = "lotId", required = false) Long lotIdPath,
                                                   @RequestParam(required = false) Long lotId) {
        return ResponseEntity.ok(service.getPeakHours(resolveLotId(lotIdPath, lotId)));
    }

    @GetMapping("/revenue")
    public ResponseEntity<BigDecimal> revenue(@RequestParam Long lotId,
                                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(service.getRevenueByLot(lotId, start, end));
    }

    @GetMapping("/revenueByDay")
    public ResponseEntity<Map<LocalDate, BigDecimal>> revenueByDay(@RequestParam Long lotId) {
        return ResponseEntity.ok(service.getRevenueByDay(lotId));
    }

    @GetMapping("/spotTypes")
    public ResponseEntity<Map<String, Long>> spotTypes(@RequestParam Long lotId) {
        return ResponseEntity.ok(service.getMostUsedSpotTypes(lotId));
    }

    @GetMapping("/avgDuration")
    public ResponseEntity<Double> avgDuration(@RequestParam Long lotId) {
        return ResponseEntity.ok(service.getAvgDuration(lotId));
    }

    @GetMapping({"/platformSummary", "/platform-summary"})
    public ResponseEntity<PlatformSummary> platformSummary() {
        return ResponseEntity.ok(service.getPlatformSummary());
    }

    @GetMapping({"/dailyReport", "/lots/{lotId}/daily-report"})
    public ResponseEntity<LotReport> dailyReport(@PathVariable(name = "lotId", required = false) Long lotIdPath,
                                                 @RequestParam(required = false) Long lotId,
                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(service.generateDailyReport(resolveLotId(lotIdPath, lotId), date));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Analytics Service is running");
    }

    private Long resolveLotId(Long lotIdPath, Long lotIdParam) {
        Long resolved = lotIdPath != null ? lotIdPath : lotIdParam;
        if (resolved == null || resolved <= 0) {
            throw new IllegalArgumentException("lotId must be provided and positive");
        }
        return resolved;
    }
}
