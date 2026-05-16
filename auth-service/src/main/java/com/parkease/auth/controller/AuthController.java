package com.parkease.auth.controller;

import com.parkease.auth.dto.*;
import com.parkease.auth.entity.User;
import com.parkease.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ── Authentication ──────────────────────────────────────────────────────

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /** Logout: stateless — client discards token; server-side hook for future blacklist. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token.replace("Bearer ", ""));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/oauth/{provider}")
    public ResponseEntity<AuthResponse> oauthCallback(@PathVariable String provider,
                                                      @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(authService.oauthLogin(provider, body.get("credential")));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(authService.refreshToken(token.replace("Bearer ", "")));
    }

    // ── Profile ─────────────────────────────────────────────────────────────

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> profile(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(authService.getUserById(userId));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDTO> updateProfile(@RequestHeader("X-User-Id") Long userId,
                                                        @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(authService.updateProfile(userId, request));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@RequestHeader("X-User-Id") Long userId,
                                               @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/deactivate")
    public ResponseEntity<Void> deactivate(@RequestHeader("X-User-Id") Long userId) {
        authService.deactivateAccount(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }

    // ── Admin ────────────────────────────────────────────────────────────────

    /** List all users by role (DRIVER / MANAGER / ADMIN). */
    @GetMapping("/admin/users")
    public ResponseEntity<List<UserProfileDTO>> usersByRole(@RequestParam User.Role role) {
        return ResponseEntity.ok(authService.getUsersByRole(role));
    }

    /** Get a single user profile by ID (admin use). */
    @GetMapping("/admin/users/{userId}")
    public ResponseEntity<UserProfileDTO> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(authService.getUserById(userId));
    }

    /** Suspend or reactivate a user account. */
    @PostMapping("/admin/users/{userId}/active")
    public ResponseEntity<Void> setActive(@PathVariable Long userId, @RequestParam boolean active) {
        authService.setAccountActive(userId, active);
        return ResponseEntity.noContent().build();
    }

    /** Permanently delete a user account. */
    @DeleteMapping("/admin/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        authService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    /** List all DRIVER accounts — admin picks from this list to promote. */
    @GetMapping("/admin/users/drivers")
    public ResponseEntity<List<UserProfileDTO>> getAllDrivers() {
        return ResponseEntity.ok(authService.getUsersByRole(User.Role.DRIVER));
    }

    /** Promote a DRIVER to MANAGER. */
    @PatchMapping("/admin/users/{userId}/promote")
    public ResponseEntity<UserProfileDTO> promoteToManager(@PathVariable Long userId) {
        return ResponseEntity.ok(authService.promoteToManager(userId));
    }

    /** Demote a MANAGER back to DRIVER. */
    @PatchMapping("/admin/users/{userId}/demote")
    public ResponseEntity<UserProfileDTO> demoteToDriver(@PathVariable Long userId) {
        return ResponseEntity.ok(authService.demoteToDriver(userId));
    }

    // ── Internal / inter-service ─────────────────────────────────────────────

    /** Look up a user by vehicle plate (called by Booking-Service). */
    @GetMapping("/internal/users/by-plate")
    public ResponseEntity<UserProfileDTO> getByVehiclePlate(@RequestParam String plate) {
        return authService.getProfileByVehiclePlate(plate)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Internal inter-service endpoint (no JWT required — permit-all via SecurityConfig).
     * Used by notification-service to fetch all users of a given role so it can
     * dispatch lot-approval notifications to every ADMIN.
     */
    @GetMapping("/internal/users-by-role")
    public ResponseEntity<List<UserProfileDTO>> getUsersByRoleInternal(@RequestParam User.Role role) {
        return ResponseEntity.ok(authService.getUsersByRole(role));
    }

    // ── Health ───────────────────────────────────────────────────────────────

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth Service is running");
    }
}
