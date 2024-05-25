package com.pulse.pulseservices.repositories;

import com.pulse.pulseservices.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractRepository   extends JpaRepository<Contract, Integer> {
}
