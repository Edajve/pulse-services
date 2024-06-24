package com.pulse.pulseservices.service;

import com.pulse.pulseservices.entity.Contract;
import com.pulse.pulseservices.entity.User;
import com.pulse.pulseservices.enums.ContractStatus;
import com.pulse.pulseservices.model.CreateOrUpdateContractRequest;
import com.pulse.pulseservices.model.contract.UpdateContractRequest;
import com.pulse.pulseservices.repositories.ContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountService accountService;

    // Create contract table if these users have does not already have an active open contract,
    // if not then update contract with other user
    public void createOrUpdateContract(CreateOrUpdateContractRequest request) {
        Long scannerId = request.getScannerUserId();
        Long scannieId = request.getScannieUserId();
        String scannerPassword = request.getUsersPassword();
        int contractNumber = request.getContractNumber();

        if (Objects.equals(scannerId, scannieId)) {
            throw new IllegalStateException("User can not create a contract with them selves");
        }

        Optional<Contract> activeContract = hasActiveContractBetweenUsers(scannerId, scannieId);
        if (activeContract.isPresent())
            throw new IllegalStateException("Active contract already exists between the participants, can not create another until this is closed");

        Optional<Contract> scannerActiveContract =
                contractRepository.findActiveContractWithAccountAndContractNumber(scannerId, contractNumber);

        if (scannerActiveContract.isPresent()) {
            throw new IllegalStateException("Scanner tried to create an active contract with the " +
                                            "same contract number. Use a different contract number, or wait until that contract is finished");
        } else {
            Optional<Contract> scannieActiveContract =
                    contractRepository.findActiveContractWithAccountAndContractNumber(scannieId, contractNumber);

            if (scannieActiveContract.isEmpty()) {
                String storedPassword = accountService.getStoredPassword(scannerId);
                if (!passwordEncoder.matches(scannerPassword, storedPassword))
                    throw new BadCredentialsException("Invalid password");

                // Creating a new contract
                Contract contract = new Contract();
                contract.setParticipantOne(accountService.getAccountById(scannerId));
                contract.setStartTime(LocalDateTime.now());
                contract.setStatus(ContractStatus.PROGRESS);
                contract.setContractNumber(contractNumber);
                contract.setDurationMinutes(60); // this will changed depending on the users functionality
                contractRepository.save(contract);
            } else {
                Contract contract = scannieActiveContract.get();

                String storedPassword = accountService.getStoredPassword(scannerId);
                if (!passwordEncoder.matches(scannerPassword, storedPassword))
                    throw new BadCredentialsException("Invalid password");

                int contractNumberInContractRecord = getContractNumber(contract.getId());
                if (contractNumberInContractRecord != contractNumber) {
                    throw new BadCredentialsException("User did not pass in correct contract number to participate in contract");
                }

                Optional<Contract> allById = contractRepository.findById(contract.getId());
                if (allById.isPresent()) {
                    Contract updatedContract = allById.get();
                    updatedContract.setParticipantTwo(accountService.getAccountById(scannerId));
                    updatedContract.setStatus(ContractStatus.ACTIVE);

                    contractRepository.save(updatedContract);
                }
            }
        }
    }

    private Optional<Contract> hasActiveContractBetweenUsers(Long scanner, Long scannie) {
        return contractRepository.findActiveContractBetweenParticipants(scanner, scannie);
    }

    private int getContractNumber(Long contractId) {
        return contractRepository.findContractNameById(contractId);
    }

    public Optional<List<Contract>> getActiveContractsById(Long accountId) {
        return contractRepository.getAllActiveContract(accountId);
    }

    public Optional<List<Contract>> getContractsThatAreNotActive(Long accountId) {
        return contractRepository.getAllNonActiveContracts(accountId);
    }

    public Optional<Contract> getContract(Long contractId) {
        return contractRepository.findById(contractId);
    }

    public Optional<List<Contract>> getInactiveContractsById(Long accountId) {
        return contractRepository.getAllNonActiveContracts(accountId);
    }

    public Optional<List<Contract>> getInProgressContracts(Long accountId) {
        return contractRepository.getAllInProgressContracts(accountId);
    }

    public void updateContract(Long contractId, Contract updatedContract) {
        Optional<Contract> contractOptional = getContract(contractId);

        if (contractOptional.isEmpty())
            throw new IllegalStateException("This contract id of " + contractId + " does not exist");

        Contract contract = contractOptional.get();

        if (updatedContract.getParticipantOne() != null)
            contract.setParticipantOne(updatedContract.getParticipantOne());
        if (updatedContract.getParticipantTwo() != null)
            contract.setParticipantTwo(updatedContract.getParticipantTwo());
        if (updatedContract.getStartTime() != null) contract.setStartTime(updatedContract.getStartTime());
        if (updatedContract.getEndTime() != null) contract.setEndTime(updatedContract.getEndTime());
        if (updatedContract.getStatus() != null) contract.setStatus(updatedContract.getStatus());
        contract.setDidParticipantOneRevoke(updatedContract.isDidParticipantOneRevoke());
        contract.setDidParticipantTwoRevoke(updatedContract.isDidParticipantTwoRevoke());
        if (updatedContract.getParticipantOneRevokeContractReason() != null)
            contract.setParticipantOneRevokeContractReason(updatedContract.getParticipantOneRevokeContractReason());
        if (updatedContract.getParticipantTwoRevokeContractReason() != null)
            contract.setParticipantTwoRevokeContractReason(updatedContract.getParticipantTwoRevokeContractReason());
        if (updatedContract.getContractCancelReason() != null)
            contract.setContractCancelReason(updatedContract.getContractCancelReason());
        if (updatedContract.getDurationMinutes() != 0)
            contract.setDurationMinutes(updatedContract.getDurationMinutes());
        if (updatedContract.getContractNumber() != 0) contract.setContractNumber(updatedContract.getContractNumber());

        contractRepository.save(contract);
    }

    public void revokeContract(
            Long contractId
            , Long userId
            , UpdateContractRequest updateContractRequest
    ) {
        Optional<Contract> contractOptional = contractRepository.getContractById(contractId);

        if (contractOptional.isEmpty())
            throw new IllegalStateException("This contract number of " + contractId + " does not exist");

        Contract contract = contractOptional.get();
        User account = accountService.getAccountById(userId);

        boolean doesUserMatchFirstParticipant = contract.getParticipantOne().getId().equals(account.getId());
        boolean doesUserMatchSecondParticipant = contract.getParticipantTwo().getId().equals(account.getId());

        if (doesUserMatchFirstParticipant)
            contractRepository.updateRevokeReasonForParticipantOne(updateContractRequest.getRevokeReason(), contract.getId());
        else if (doesUserMatchSecondParticipant)
            contractRepository.updateRevokeReasonForParticipantTwo(updateContractRequest.getRevokeReason(), contract.getId());
        else
            throw new IllegalStateException(String.format("This account id of: %s is not on this contract number of %s", account.getId(), contract.getContractNumber()));

        // only cancelled contract once both revokes, or one revokes and the other denies the revoke
        Optional<Contract> grabContractOptionalAgain = contractRepository.getContractById(contractId);
        Contract updatedContract = grabContractOptionalAgain.get();
        boolean bothUsersHasRevoked = updatedContract.isDidParticipantOneRevoke()
                                      || updatedContract.isDidParticipantTwoRevoke();
        LocalDateTime nowTime = LocalDateTime.now();
        if (bothUsersHasRevoked)
            contractRepository.updateContractStatusByID(updatedContract.getId(), "Both Parties Revoked", "CANCELLED", nowTime);
    }
}
