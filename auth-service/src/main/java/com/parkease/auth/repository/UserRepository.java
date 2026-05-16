package com.parkease.auth.repository;

import com.parkease.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /** findByEmail — login and duplicate-check lookups. */
    Optional<User> findByEmail(String email);

    /** findByUserId — explicit named method required by class diagram. */
    Optional<User> findByUserId(Long userId);

    /** findByPhone — phone uniqueness check. */
    Optional<User> findByPhone(String phone);

    /** findByVehiclePlate — quick-ref lookup for Booking-Service. */
    Optional<User> findByVehiclePlate(String vehiclePlate);

    /** findAllByRole — admin user management by role. */
    List<User> findAllByRole(User.Role role);

    /** existsByEmail — registration duplicate guard. */
    boolean existsByEmail(String email);

    /** deleteByUserId — admin permanent delete, required by class diagram. */
    @Transactional
    void deleteByUserId(Long userId);
}
