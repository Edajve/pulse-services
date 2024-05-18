package com.pulse.pulseservices.service;

import com.pulse.pulseservices.entity.User;
import com.pulse.pulseservices.repositories.AccountRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService (AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public User getAccountById(Long id) {
        return accountRepository.findById(Math.toIntExact(id))
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }
}
