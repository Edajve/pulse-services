package com.pulse.pulseservices.repositories;

import com.pulse.pulseservices.entity.Qr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface QrRepository extends JpaRepository<Qr, Integer> {
    @Query(value = "SELECT image_bytes FROM qr WHERE user_id = ?1", nativeQuery = true)
    byte[] getQrById(Integer id);
}
