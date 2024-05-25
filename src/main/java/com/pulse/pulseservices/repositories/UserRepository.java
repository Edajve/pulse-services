package com.pulse.pulseservices.repositories;

import com.pulse.pulseservices.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    @Query(value = "SELECT * FROM account WHERE email = ?1", nativeQuery = true)
    Optional<User> isUserAlreadyRegistered(String email);
}
