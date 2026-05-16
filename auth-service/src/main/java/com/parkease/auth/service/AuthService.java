package com.parkease.auth.service;

import com.parkease.auth.dto.*;
import com.parkease.auth.entity.User;

import java.util.List;
import java.util.Optional;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void logout(String token);

    boolean validateToken(String token);

    AuthResponse refreshToken(String refreshToken);

    UserProfileDTO getUserByEmail(String email);

    UserProfileDTO getUserById(Long userId);

    UserProfileDTO updateProfile(Long userId, UpdateProfileRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void deactivateAccount(Long userId);

    // ── Extended operations used by controller ───────────────────────────────

    AuthResponse oauthLogin(String provider, String credential);

    Optional<UserProfileDTO> getProfileByVehiclePlate(String vehiclePlate);

    void setAccountActive(Long userId, boolean active);

    void deleteUser(Long userId);

    UserProfileDTO promoteToManager(Long userId);

    UserProfileDTO demoteToDriver(Long userId);

    List<UserProfileDTO> getUsersByRole(User.Role role);
}
