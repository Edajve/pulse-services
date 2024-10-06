package com.pulse.pulseservices.repositories;

import com.pulse.pulseservices.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<User, Integer> {
    @Query(value = """
            SELECT *
            FROM account
            WHERE email = ?1
            """, nativeQuery = true)
    Optional<User> findByEmail(String email);
}
