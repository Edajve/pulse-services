package com.pulse.pulseservices.repositories;

import com.pulse.pulseservices.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
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
            SET pin_code = ?2
            WHERE id = ?1
            """, nativeQuery = true)
    void updatePinSetting(Long accountId, boolean pinSetting);


    @Query(value = """
        SELECT * 
        FROM account
        WHERE pin_code = ?1 
        AND local_hash = ?2
        LIMIT 1
        """, nativeQuery = true)
    Optional<User> getUserByPinAndHash(String pin, String localHash);

    @Query(value = """
        SELECT * 
        FROM account
        WHERE local_hash = ?1 
        LIMIT 1
        """, nativeQuery = true)
    Optional<User> getUserByHash(String localHash);

    @Query(value = """
        SELECT id 
        FROM account
        WHERE LOWER(first_name) = LOWER(?1)
        """, nativeQuery = true)
    List<Integer> getAllAccountsWithName(String name);
}
