package com.pulse.pulseservices.repositories;

import com.pulse.pulseservices.model.FeedBack.FeedBack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedBackRepository extends JpaRepository<FeedBack, Integer> {

    /*

    @Query(value = "SELECT generated_qr_id FROM qr WHERE user_id = ?1", nativeQuery = true)
    byte[] getUUIDById(Integer id);
     */
}
