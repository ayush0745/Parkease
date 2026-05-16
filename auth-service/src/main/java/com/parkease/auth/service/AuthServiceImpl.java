package com.parkease.auth.service;

import com.parkease.auth.dto.*;
import com.parkease.auth.entity.PasswordResetToken;
import com.parkease.auth.entity.User;
import com.parkease.auth.repository.PasswordResetTokenRepository;
import com.parkease.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.google.client-id}")
    private String googleClientId;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .vehiclePlate(request.getVehiclePlate())
                .role(User.Role.DRIVER)
                .isActive(true)
                .build();
        return generateAuthResponse(userRepository.save(user));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new IllegalStateException("Account is inactive");
        }
        return generateAuthResponse(user);
    }

    /**
     * Logout: validates the token is legitimate before accepting the request.
     * Stateless — client must discard the token after this call.
     * Future: add token to a Redis blacklist keyed by jti/expiry.
     */
    @Override
    public void logout(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
        // Stateless logout — client discards the token.
        // Future: blacklist token in Redis until its expiry.
    }

    @Override
    public AuthResponse oauthLogin(String provider, String credential) {
        if (!"google".equalsIgnoreCase(provider)) {
            throw new IllegalArgumentException("Unsupported OAuth provider");
        }

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(credential);
            if (idToken == null) {
                throw new IllegalArgumentException("Invalid Google ID token.");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String subject = payload.getSubject();
            String fullName = (String) payload.get("name");

            User user = userRepository.findByEmail(email).orElseGet(() -> userRepository.save(User.builder()
                    .email(email)
                    .fullName(fullName == null || fullName.isBlank() ? email : fullName)
                    .passwordHash(passwordEncoder.encode(provider + ":" + subject))
                    .oauthProvider(provider)
                    .oauthSubject(subject)
                    .role(User.Role.DRIVER)
                    .isActive(true)
                    .build()));
            return generateAuthResponse(user);
        } catch (Exception e) {
            throw new IllegalArgumentException("Google token verification failed", e);
        }
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        Long userId = jwtTokenProvider.extractUserId(refreshToken);
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return generateAuthResponse(user);
    }

    @Override
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDTO getUserByEmail(String email) {
        return userRepository.findByEmail(email).map(this::mapProfile)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDTO getUserById(Long userId) {
        return userRepository.findByUserId(userId).map(this::mapProfile)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserProfileDTO> getProfileByVehiclePlate(String vehiclePlate) {
        return userRepository.findByVehiclePlate(vehiclePlate).map(this::mapProfile);
    }

    @Override
    public UserProfileDTO updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setVehiclePlate(request.getVehiclePlate());
        user.setProfilePicUrl(request.getProfilePicUrl());
        return mapProfile(userRepository.save(user));
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void deactivateAccount(Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Override
    public void setAccountActive(Long userId, boolean active) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setIsActive(active);
        userRepository.save(user);
    }

    @Override
    public void deleteUser(Long userId) {
        if (!userRepository.findByUserId(userId).isPresent()) {
            throw new IllegalArgumentException("User not found");
        }
        userRepository.deleteByUserId(userId);
    }

    @Override
    public UserProfileDTO promoteToManager(Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getRole() == User.Role.ADMIN) {
            throw new IllegalStateException("Cannot change role of an ADMIN account");
        }
        user.setRole(User.Role.MANAGER);
        user.setVehiclePlate(null);
        return mapProfile(userRepository.save(user));
    }

    @Override
    public UserProfileDTO demoteToDriver(Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getRole() == User.Role.ADMIN) {
            throw new IllegalStateException("Cannot change role of an ADMIN account");
        }
        user.setRole(User.Role.DRIVER);
        return mapProfile(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileDTO> getUsersByRole(User.Role role) {
        return userRepository.findAllByRole(role).stream().map(this::mapProfile).toList();
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Delete existing tokens for this user
        passwordResetTokenRepository.deleteByUserId(user.getUserId());
        
        // Generate new reset token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .userId(user.getUserId())
                .expiryDate(LocalDateTime.now().plusHours(1)) // 1 hour expiry
                .build();
        
        passwordResetTokenRepository.save(resetToken);

        // Publish event to RabbitMQ for notification-service to send email
        PasswordResetEvent event = PasswordResetEvent.builder()
                .email(user.getEmail())
                .token(token)
                .build();
        rabbitTemplate.convertAndSend("parkease.events", "user.password_reset", event);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenAndUsedFalse(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));
        
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Reset token has expired");
        }
        
        User user = userRepository.findByUserId(resetToken.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        // Mark token as used
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getEmail(), user.getUserId(), user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(
                user.getEmail(), user.getUserId());
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getUserId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .expiresIn(jwtTokenProvider.getExpirationTime())
                .build();
    }

    private UserProfileDTO mapProfile(User user) {
        return UserProfileDTO.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .vehiclePlate(user.getVehiclePlate())
                .role(user.getRole().name())
                .isActive(user.getIsActive())
                .profilePicUrl(user.getProfilePicUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
