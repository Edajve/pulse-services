package com.pulse.pulseservices.repositories;

import com.pulse.pulseservices.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<User, Integer> {
    @Query(value = """
            SELECT *
            FROM account
            WHERE LOWER(email) = LOWER(?1)
            """, nativeQuery = true)
    Optional<User> findByEmail(String email);

    @Modifying
    @Query(value = """
            UPDATE account
            SET biometric_login = ?2
            WHERE id = ?1
            """, nativeQuery = true)
    void updatePinSetting(Long accountId, boolean pinSetting);
}
