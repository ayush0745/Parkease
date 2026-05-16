package com.parkease.analytics.controller;

import com.parkease.analytics.dto.AdminDashboardDTO;
import com.parkease.analytics.dto.PlatformSummary;
import com.parkease.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final AnalyticsService analyticsService;
    
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardDTO> getDashboard(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        requireAdmin(role);
        return ResponseEntity.ok(analyticsService.getAdminDashboard());
    }
    
    @GetMapping("/platform-summary")
    public ResponseEntity<PlatformSummary> getPlatformSummary(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        requireAdmin(role);
        return ResponseEntity.ok(analyticsService.getPlatformSummary(start, end));
    }
    
    @GetMapping("/revenue/total")
    public ResponseEntity<BigDecimal> getTotalRevenue(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        requireAdmin(role);
        return ResponseEntity.ok(analyticsService.getTotalRevenue(start, end));
    }
    
    @GetMapping("/revenue/by-lot")
    public ResponseEntity<Map<String, BigDecimal>> getRevenueByLot(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        requireAdmin(role);
        return ResponseEntity.ok(analyticsService.getRevenueByLot(start, end));
    }
    
    @GetMapping("/bookings/stats")
    public ResponseEntity<Map<String, Long>> getBookingStats(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        requireAdmin(role);
        return ResponseEntity.ok(analyticsService.getBookingStats(start, end));
    }
    
    @GetMapping("/occupancy/peak-hours")
    public ResponseEntity<Map<Integer, Double>> getPeakHours(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        requireAdmin(role);
        return ResponseEntity.ok(analyticsService.getPeakHours(start, end));
    }
    
    @GetMapping("/lots/performance")
    public ResponseEntity<Map<String, Object>> getLotPerformance(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        requireAdmin(role);
        return ResponseEntity.ok(analyticsService.getLotPerformance(start, end));
    }
    
    private void requireAdmin(String role) {
        if (!"ADMIN".equalsIgnoreCase(role) && !"SYSTEM".equalsIgnoreCase(role)) {
            throw new SecurityException("Admin role is required");
        }
    }
}