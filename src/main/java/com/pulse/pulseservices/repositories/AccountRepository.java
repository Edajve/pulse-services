package com.pulse.pulseservices.repositories;

import com.pulse.pulseservices.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<User, Integer> {
}
