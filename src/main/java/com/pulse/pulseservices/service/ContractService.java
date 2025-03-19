package com.pulse.pulseservices.service;

import com.pulse.pulseservices.entity.Contract;
import com.pulse.pulseservices.entity.User;
import com.pulse.pulseservices.enums.ContractStatus;
import com.pulse.pulseservices.model.contract.CreateOrUpdateContractRequest;
import com.pulse.pulseservices.model.contract.UpdateContractRequest;
import com.pulse.pulseservices.repositories.ContractRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(ContractService.class);


    // Create contract table if these users have does not already have an active open contract,
// if not then update contract with other user
    public void createOrUpdateContract(CreateOrUpdateContractRequest request) {
        Long scannerId = request.getScannerUserId();
        Long scannieId = request.getScannieUserId();
        String scannerPassword = request.getUsersPassword();
        int contractNumber = request.getContractNumber();

        logger.info("Received request to create/update contract: Scanner ID = {}, Scannie ID = {}, Contract Number = {}",
                scannerId, scannieId, contractNumber);

        if (Objects.equals(scannerId, scannieId)) {
            logger.warn("User {} attempted to create a contract with themselves", scannerId);
            throw new IllegalStateException("User can not create a contract with themselves");
        }

        Optional<Contract> activeContract = hasActiveContractBetweenUsers(scannerId, scannieId);
        if (activeContract.isPresent()) {
            logger.warn("Active contract already exists between Scanner ID = {} and Scannie ID = {}", scannerId, scannieId);
            throw new IllegalStateException("Active contract already exists between the participants, can not create another until this is closed");
        }

        Optional<Contract> scannerActiveContract =
                contractRepository.findActiveContractWithAccountAndContractNumber(scannerId, contractNumber);

        if (scannerActiveContract.isPresent()) {
            logger.warn("Scanner ID = {} tried to create an active contract with the same contract number {}", scannerId, contractNumber);
            throw new IllegalStateException("Scanner tried to create an active contract with the same contract number. Use a different contract number, or have your consenting partner enter into this contract with this number");
        } else {
            Optional<Contract> scannieActiveContract =
                    contractRepository.findActiveContractWithAccountAndContractNumber(scannieId, contractNumber);

            if (scannieActiveContract.isEmpty()) {
                String storedPassword = accountService.getStoredPassword(scannerId);
                if (!passwordEncoder.matches(scannerPassword, storedPassword)) {
                    logger.error("Invalid password attempt for Scanner ID = {}", scannerId);
                    throw new BadCredentialsException("Invalid password");
                }

                // Creating a new contract
                Contract contract = new Contract();
                contract.setParticipantOne(accountService.getAccountById(scannerId));
                contract.setStartTime(LocalDateTime.now());
                contract.setStatus(ContractStatus.PROGRESS);
                contract.setContractNumber(contractNumber);
                contract.setDurationMinutes(60); // this will change depending on the users functionality
                contractRepository.save(contract);

                logger.info("New contract created successfully: Contract Number = {}, Scanner ID = {}", contractNumber, scannerId);
            } else {
                Contract contract = scannieActiveContract.get();

                String storedPassword = accountService.getStoredPassword(scannerId);
                if (!passwordEncoder.matches(scannerPassword, storedPassword)) {
                    logger.error("Invalid password attempt for Scanner ID = {} when trying to update contract", scannerId);
                    throw new BadCredentialsException("Invalid password");
                }

                int contractNumberInContractRecord = getContractNumber(contract.getId());
                if (contractNumberInContractRecord != contractNumber) {
                    logger.error("Scanner ID = {} provided incorrect contract number {} for existing contract {}",
                            scannerId, contractNumber, contract.getId());
                    throw new BadCredentialsException("User did not pass in correct contract number to participate in contract");
                }

                Optional<Contract> allById = contractRepository.findById(contract.getId());
                if (allById.isPresent()) {
                    Contract updatedContract = allById.get();
                    updatedContract.setParticipantTwo(accountService.getAccountById(scannerId));
                    updatedContract.setStatus(ContractStatus.ACTIVE);

                    contractRepository.save(updatedContract);
                    logger.info("Contract ID = {} updated successfully. Second participant (Scanner ID = {}) added.", contract.getId(), scannerId);
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
        logger.info("Received request to update contract: Contract ID = {}", contractId);

        Optional<Contract> contractOptional = getContract(contractId);
        if (contractOptional.isEmpty()) {
            logger.warn("Attempted to update a non-existent contract ID = {}", contractId);
            throw new IllegalStateException("This contract id of " + contractId + " does not exist");
        }

        Contract contract = contractOptional.get();

        contract.setParticipantOne(updatedContract.getParticipantOne());
        contract.setParticipantTwo(updatedContract.getParticipantTwo());
        contract.setStartTime(updatedContract.getStartTime());
        if (updatedContract.getEndTime() != null) contract.setEndTime(updatedContract.getEndTime());
        contract.setStatus(updatedContract.getStatus());
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
        logger.info("Contract ID = {} updated successfully", contractId);
    }


    public void revokeContract(
            Long contractId, Long userId, UpdateContractRequest updateContractRequest
    ) {
        logger.info("Revocation request received: Contract ID = {}, User ID = {}", contractId, userId);

        Optional<Contract> contractOptional = contractRepository.getContractById(contractId);
        if (contractOptional.isEmpty()) {
            logger.warn("Attempted to revoke a non-existent contract ID = {}", contractId);
            throw new IllegalStateException("This contract number of " + contractId + " does not exist");
        }

        Contract contract = contractOptional.get();
        User account = accountService.getAccountById(userId);

        boolean doesUserMatchFirstParticipant = contract.getParticipantOne().getId().equals(account.getId());
        boolean doesUserMatchSecondParticipant = contract.getParticipantTwo().getId().equals(account.getId());

        if (doesUserMatchFirstParticipant) {
            logger.info("User ID = {} is revoking as Participant One for Contract ID = {}", userId, contractId);
            contractRepository.updateRevokeReasonForParticipantOne(updateContractRequest.getRevokeReason(), contract.getId(), "CANCELLED");
        } else if (doesUserMatchSecondParticipant) {
            logger.info("User ID = {} is revoking as Participant Two for Contract ID = {}", userId, contractId);
            contractRepository.updateRevokeReasonForParticipantTwo(updateContractRequest.getRevokeReason(), contract.getId(), "CANCELLED");
        } else {
            logger.error("User ID = {} attempted to revoke a contract (ID = {}) they are not a participant of", userId, contractId);
            throw new IllegalStateException(String.format("This account id of: %s is not on this contract number of %s", account.getId(), contract.getContractNumber()));
        }

        Optional<Contract> grabContractOptionalAgain = contractRepository.getContractById(contractId);
        Contract updatedContract = grabContractOptionalAgain.get();
        boolean bothUsersHasRevoked = updatedContract.isDidParticipantOneRevoke() || updatedContract.isDidParticipantTwoRevoke();
        LocalDateTime nowTime = LocalDateTime.now();

        if (bothUsersHasRevoked) {
            contractRepository.updateContractStatusByID(updatedContract.getId(), "Both Parties Revoked", "CANCELLED", nowTime);
            logger.info("Contract ID = {} has been cancelled as both participants revoked", contractId);
        }
    }

    public int getTotalContractsCount(Long id) {
        return contractRepository.getTotalContractCount(id);
    }

    public int getTotalContractsRevokedCount(Long id) {
        return contractRepository.getTotalContractRevokedCount(id);
    }

    public List<Contract> getAllContracts(Long id) {
        return contractRepository.getTotalContracts(id);
    }

    public List<Contract> getTotalRevokedContracts(Long id) {
        return contractRepository.getTotalRevokedContracts(id);
    }

    public Optional<List<Contract>> findByNameAndUserId(String name, int userId) {

       List<Integer> idsWithGivenName = accountService.getAccountsWithName(name);

        return contractRepository.getSearchedContractsByNameForUserId(idsWithGivenName, userId);
    }
}
