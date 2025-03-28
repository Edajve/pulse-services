package com.pulse.pulseservices.repositories;

import com.pulse.pulseservices.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ContractRepository extends JpaRepository<Contract, Long> {
    @Query(value = """
            SELECT *
            FROM contract
            WHERE (participant_one_id = ?1 OR participant_two_id = ?1)
            AND (participant_one_id = ?2 OR participant_two_id = ?2)
            AND status = 'ACTIVE'
            """, nativeQuery = true)
    Optional<Contract> findActiveContractBetweenParticipants(Long scanner, Long scannie);

    @Query(value = """
            SELECT *
            FROM contract
            WHERE (participant_one_id = ?1)
            AND (contract_number = ?2)
            AND status = 'PROGRESS'
            """, nativeQuery = true)
    Optional<Contract> findActiveContractWithAccountAndContractNumber(Long userId, int contractNumber);

    @Query(value = """
            SELECT contract_number
            FROM contract
            WHERE (id = ?1)
            """, nativeQuery = true)
    int findContractNameById(Long contractId);

    @Query(value = """
            SELECT *
            FROM contract
            WHERE (participant_one_id = ?1 OR participant_two_id = ?1)
            AND status = 'ACTIVE'
            """, nativeQuery = true)
    Optional<List<Contract>> getAllActiveContract(Long accountId);

    @Query(value = """
            SELECT *
            FROM contract
            WHERE (participant_one_id = ?1 OR participant_two_id = ?1)
            AND status != 'ACTIVE'
            AND status != 'PROGRESS'
            """, nativeQuery = true)
    Optional<List<Contract>> getAllNonActiveContracts(Long accountId);

    @Query(value = """
            SELECT *
            FROM contract
            WHERE status = 'ACTIVE'
            """, nativeQuery = true)
    List<Contract> findActiveContracts();

    @Query(value = """
            SELECT *
            FROM contract
            WHERE (participant_one_id = ?1 OR participant_two_id = ?1)
            AND status = 'PROGRESS'
            """, nativeQuery = true)
    Optional<List<Contract>> getAllInProgressContracts(Long accountId);

    @Transactional
    @Modifying
    @Query(value = """
        UPDATE contract
        SET participant_one_revoke_reason = ?1,
            did_participant_one_revoke = true,
            status = ?3
        WHERE id = ?2
        """, nativeQuery = true)
    void updateRevokeReasonForParticipantOne(String revokeReason, Long contractID, String newStatus);

    @Transactional
    @Modifying
    @Query(value = """
        UPDATE contract
        SET participant_two_revoke_reason = ?1,
            did_participant_two_revoke = true,
            status = ?3
        WHERE id = ?2
        """, nativeQuery = true)
    void updateRevokeReasonForParticipantTwo(String revokeReason, Long contractID, String newStatus);


    @Query(value = """
            SELECT *
            FROM contract
            WHERE status = 'ACTIVE'
            AND id = ?1
            """, nativeQuery = true)
    Optional<Contract> getContractById(Long contractId);

    @Transactional
    @Modifying
    @Query(value = """
        UPDATE contract
        SET contract_cancel_reason = ?2, status = ?3, end_time = ?4
        WHERE id = ?1
        """, nativeQuery = true)
    void updateContractStatusByID(Long contractID, String cancelReason, String newStatus, LocalDateTime endTime);

    @Query(value = """
            SELECT *
            FROM contract
            WHERE participant_one_id = ?1
            OR  participant_two_id = ?1
            """, nativeQuery = true)
    List<Contract> getAllContracts(Long id);

    @Query(value = """
            SELECT COUNT(*)
            FROM contract
            WHERE participant_one_id = ?1
            OR  participant_two_id = ?1
            """, nativeQuery = true)
    int getTotalContractCount(Long id);

    @Query(value = """
            SELECT COUNT(*)
            FROM contract
            WHERE (participant_one_id = ?1 OR participant_two_id = ?1)
            AND status = 'CANCELLED'
            """, nativeQuery = true)
    int getTotalContractRevokedCount(Long id);

    @Query(value = """
            SELECT *
            FROM contract
            WHERE participant_one_id = ?1
            OR  participant_two_id = ?1
            """, nativeQuery = true)
    List<Contract> getTotalContracts(Long id);

    @Query(value = """
    SELECT *
    FROM contract
    WHERE (participant_one_id = ?1 OR participant_two_id = ?1)
    AND status = 'CANCELLED'
    """, nativeQuery = true)
    List<Contract> getTotalRevokedContracts(Long id);

    @Query(value = """
    SELECT * 
    FROM contract 
    WHERE 
    (participant_one_id = ?2 AND participant_two_id IN ?1)
    OR 
    (participant_two_id = ?2 AND participant_one_id IN ?1)
""", nativeQuery = true)
    Optional<List<Contract>> getSearchedContractsByNameForUserId(List<Integer> idsWithGivenName, int userId);

}
