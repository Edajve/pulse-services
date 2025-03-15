package com.pulse.pulseservices.service;

import com.pulse.pulseservices.entity.Contract;
import com.pulse.pulseservices.entity.User;
import com.pulse.pulseservices.exception.UserNotFoundException;
import com.pulse.pulseservices.model.AccountStats;
import com.pulse.pulseservices.repositories.AccountRepository;
import com.pulse.pulseservices.repositories.ContractRepository;
import com.pulse.pulseservices.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final ContractRepository contractRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

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

        Map<Integer, Integer> occurrences = new LinkedHashMap<>(); // Use LinkedHashMap to preserve order

        for (Contract contract : contracts) {
            int partnerId;
            if ((long) contract.getParticipantOne().getId() == id) {
                partnerId = contract.getParticipantTwo().getId();
            } else {
                partnerId = contract.getParticipantOne().getId();
            }

            occurrences.put(partnerId, occurrences.getOrDefault(partnerId, 0) + 1);
        }

        Integer mostContractedPartnerId = null;
        Integer maxOccurrences = 0;

        for (Map.Entry<Integer, Integer> entry : occurrences.entrySet()) {
            if (entry.getValue() > maxOccurrences ||
                (entry.getValue().equals(maxOccurrences) && (mostContractedPartnerId == null || entry.getKey() < mostContractedPartnerId))) {
                // Tie-breaker: Pick the partner with the smaller ID
                maxOccurrences = entry.getValue();
                mostContractedPartnerId = entry.getKey();
            }
        }

        if (mostContractedPartnerId == null) return "None";


        User mostContractedPartner = getAccountById(Long.valueOf(mostContractedPartnerId));
        return String.format("%s %s", mostContractedPartner.getFirstName(), mostContractedPartner.getLastName());
    }

    private double completedToRevokedContractRatio(int totalContracts, int totalContractsRevoked) {
        if (totalContractsRevoked == 0) return 0;
        double completedContracts = totalContracts - totalContractsRevoked;
        double ratio = completedContracts / totalContractsRevoked;
        return Math.round(ratio * 100.0) / 100.0;
    }

    public void resetPassword(int accountId, String newPassword) {
        try {
            logger.info("Password reset request received for User ID = {}", accountId);
            User account = getAccountById((long) accountId);
            account.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(account);
            logger.info("Password successfully changed for User ID = {}", accountId);
        } catch (Exception e) {
            logger.error("Error while resetting password for User ID = {}: {}", accountId, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public Optional<User> getUserByEmail(String email) {
        return accountRepository.findByEmail(email);
    }

    public boolean isEmailInDataBase(String email) {
        Optional<User> byEmail = accountRepository.findByEmail(email);
        return byEmail.isPresent();
    }

    public String verifySecurityQuestionAndAnswer(String securityQuestion, String securityAnswer, String email) {
        Optional<User> userOptional = accountRepository.findByEmail((email));
        if (userOptional.isEmpty()) {
            return "Invalid account";
        }

        User user = userOptional.get();

        if (Objects.isNull(user.getSecurityQuestion())) {
            throw new RuntimeException(String.format("This accountId: %s does not have a security question", user.getId()));
        }

        if (Objects.isNull(user.getSecurityAnswer())) {
            throw new RuntimeException(String.format("This accountId: %s does not have a security Answer", user.getId()));
        }

        if (!securityQuestion.trim().equals(user.getSecurityQuestion().trim())) {
            return "Security Question is incorrect";
        }

        if (!securityAnswer.trim().equals(user.getSecurityAnswer().trim())) {
            return "Security Answer is incorrect";
        }

        return "verified";
    }

    public void updatePinSetting(Long accountId, boolean pinSetting) {
        accountRepository.updatePinSetting(accountId, pinSetting);
    }

    public void updatePinSettingAndPinCode(Long accountId, boolean pinSetting, String pinCode) {
        accountRepository.updatePinSetting(accountId, pinSetting);

        User account = getAccountById(accountId);
        account.setPinCode(passwordEncoder.encode(pinCode));
        userRepository.save(account);
    }

    public String getAuthMethodByLocalHash(String localHash) {
        return userRepository.findByLocalHash(localHash)
                .map(User::getAuthMethod)
                .orElseThrow(() -> new UserNotFoundException("No user found with this local hash"));
    }

    @Transactional
    public Optional<User> updateUser(int accountId, User request) {
        User user = userRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isUserTryingUpdatePIFields =   request.getPassword() != null ||
                                                  request.getLocalHash() != null;
        if (isUserTryingUpdatePIFields) {
            throw new IllegalArgumentException("Updating password, pinCode, or localHash is not allowed due to security reasons.");
        }

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getSecurityQuestion() != null) user.setSecurityQuestion(request.getSecurityQuestion());
        if (request.getSecurityAnswer() != null) user.setSecurityAnswer(request.getSecurityAnswer());
        if (request.getSex() != null) user.setSex(request.getSex());
        if (request.getDateOfBirth() != null) user.setDateOfBirth(request.getDateOfBirth());
        if (request.getCountryRegion() != null) user.setCountryRegion(request.getCountryRegion());
        if (request.getAuthMethod() != null) user.setAuthMethod(request.getAuthMethod());
        if (request.getPinCode() != null) user.setPinCode(request.getPinCode());
        if (request.getHasUserBeenAskedAuthMethod() != null) user.setHasUserBeenAskedAuthMethod(request.getHasUserBeenAskedAuthMethod());

        // might want to return only the correct properties
        return Optional.of(userRepository.save(user));
    }
}
