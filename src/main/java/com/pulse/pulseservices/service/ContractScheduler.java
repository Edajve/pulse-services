package com.pulse.pulseservices.service;

import com.pulse.pulseservices.entity.Contract;
import com.pulse.pulseservices.enums.ContractStatus;
import com.pulse.pulseservices.repositories.ContractRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ContractScheduler {

    private final ContractRepository contractRepository;

    public ContractScheduler(ContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    @Scheduled(fixedRate = 60000) // Every 60 seconds
    public void checkAndUpdateContracts() {
        System.out.println("Running Schedule for out of time contracts");
        List<Contract> contracts = contractRepository.findActiveContracts();
        contracts.forEach(contract -> {
            contractRepository.save(contract);
            updateCancelReasonIfRequired(contract);
            if (isContractOutOfDate(contract)) contractRepository.save(contract);
        });
    }

    private void updateCancelReasonIfRequired(Contract contract) {
        boolean didOneRevoke = contract.isDidParticipantOneRevoke();
        boolean didTwoRevoke = contract.isDidParticipantTwoRevoke();

        if (!didOneRevoke && !didTwoRevoke) return;

        String message;
        if (didOneRevoke && didTwoRevoke) {
            message = "Both Parties Revoked";
        } else {
            String usersName = didOneRevoke ? contract.getParticipantOne().getFirstName() : contract.getParticipantTwo().getFirstName();
            message = usersName + " Revoked";
        }

        contract.setContractCancelReason(message);
    }

    private boolean isContractOutOfDate(Contract contract) {
        LocalDateTime startTime = contract.getStartTime();
        int duration = contract.getDurationMinutes(); // In minutes

        LocalDateTime calculatedEndTime = startTime.plusMinutes(duration);
        LocalDateTime currentTime = LocalDateTime.now();
        boolean isContractDurationOver = currentTime.isAfter(calculatedEndTime);

        if (isContractDurationOver) {
            contract.setEndTime(currentTime);
            contract.setStatus(ContractStatus.COMPLETED);
        }

        return isContractDurationOver;
    }
}
