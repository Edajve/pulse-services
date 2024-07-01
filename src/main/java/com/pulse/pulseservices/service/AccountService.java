package com.pulse.pulseservices.service;

import com.pulse.pulseservices.entity.Contract;
import com.pulse.pulseservices.entity.User;
import com.pulse.pulseservices.model.AccountStats;
import com.pulse.pulseservices.repositories.AccountRepository;
import com.pulse.pulseservices.repositories.ContractRepository;
import com.pulse.pulseservices.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final ContractRepository contractRepository;
    private PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;


    @Autowired
    public AccountService(
            AccountRepository accountRepository
            , ContractRepository contractRepository
            , PasswordEncoder passwordEncoder,
            UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.contractRepository = contractRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public User getAccountById(Long id) {
        return accountRepository.findById(Math.toIntExact(id))
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    public String getStoredPassword(Long userId) {
        return accountRepository.findById(Math.toIntExact(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getPassword();
    }

    public AccountStats getStats(Long id) {
        int totalContractsCount = contractRepository.getTotalContractCount(id);
        int totalContractsRevoked = contractRepository.getTotalContractRevokedCount(id);
        double crRatio = completedToRevokedContractRatio(totalContractsCount, totalContractsRevoked);
        List<Contract> contracts = contractRepository.getTotalContracts(id);
        String partnerContractedWithTheMost = getMostOccurringPartner(contracts, id);
        List<Contract> revokedContracts = contractRepository.getTotalRevokedContracts(id);
        String partnerRevokedWithTheMost = getMostOccurringPartner(revokedContracts, id);

        return AccountStats.builder()
                .totalContracts(totalContractsCount)
                .totalContractsRevoked(totalContractsRevoked)
                .successfulToRevokedRatio(crRatio)
                .mostConsentedPartner(partnerContractedWithTheMost)
                .mostRevokedPartner(partnerRevokedWithTheMost)
                .build();
    }

    private String getMostOccurringPartner(List<Contract> contracts, Long id) {
        if (contracts.isEmpty()) return "None";

        // go through and get the most contracted partners
        Map<Integer, Integer> occurrences = new HashMap<>();
        for (Contract contract : contracts) {
            if ((long) contract.getParticipantOne().getId() == id) {
                // track user participant two
                if (occurrences.containsKey(contract.getParticipantTwo().getId())) {
                    occurrences.put(
                            contract.getParticipantTwo().getId(),
                            occurrences.get(contract.getParticipantTwo().getId()) + 1
                    );
                } else {
                    occurrences.put(contract.getParticipantTwo().getId(), 1);
                }
            } else {
                // track user of participant one
                if (occurrences.containsKey(contract.getParticipantOne().getId())) {
                    occurrences.put(
                            contract.getParticipantOne().getId(),
                            occurrences.get(contract.getParticipantOne().getId()) + 1
                    );
                } else {
                    occurrences.put(contract.getParticipantOne().getId(), 1);
                }
            }
        }

        Integer mostContractedPartnerId = 0;
        Integer maxOccurrences = 0;
        // which id has the highest number
        for (Map.Entry<Integer, Integer> contract : occurrences.entrySet()) {
            Integer currentKey = contract.getKey();
            Integer currentValue = contract.getValue();
            if (currentValue >= maxOccurrences) {
                maxOccurrences = currentValue;
                mostContractedPartnerId = currentKey;
            }
        }

        // what is the users name of this id
        User mostContractedPartner = getAccountById(Long.valueOf(mostContractedPartnerId));
        return String.format("%s %s", mostContractedPartner.getFirstName(), mostContractedPartner.getLastName());
    }

    private double completedToRevokedContractRatio(int totalContracts, int totalContractsRevoked) {
        if (totalContractsRevoked == 0) return 0;
        double completedContracts = totalContracts - totalContractsRevoked;
        double ratio = completedContracts / totalContractsRevoked;
        return Math.round(ratio * 100.0) / 100.0;
    }

    public void resetPassword(int accountId, String password) {
        // reset password
        try {
            User account = getAccountById((long) accountId);
            account.setPassword(passwordEncoder.encode(password));
            userRepository.save(account);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
