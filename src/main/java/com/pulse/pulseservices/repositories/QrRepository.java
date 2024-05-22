package com.pulse.pulseservices.repositories;

import com.pulse.pulseservices.entity.Qr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface QrRepository extends JpaRepository<Qr, Integer> {
    @Query(value = "SELECT generated_qr_id FROM qr WHERE user_id = ?1", nativeQuery = true)
    UUID getUUIDById(Integer id);
}
