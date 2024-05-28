package com.pulse.pulseservices.repositories;

import com.pulse.pulseservices.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
            """, nativeQuery = true)
    Optional<List<Contract>> getAllNonActiveContracts(Long accountId);

    @Query(value = """
            SELECT *
            FROM contract
            WHERE status = 'ACTIVE'
            """, nativeQuery = true)
    List<Contract> findNonActiveContracts();

}
