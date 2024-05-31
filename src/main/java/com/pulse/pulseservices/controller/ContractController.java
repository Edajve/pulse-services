package com.pulse.pulseservices.controller;

import com.pulse.pulseservices.entity.Contract;
import com.pulse.pulseservices.model.CreateOrUpdateContractRequest;
import com.pulse.pulseservices.service.ContractService;
import jdk.jfr.Description;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/api/v1/contract")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    /**
     * TODO
     * some testing needs to be done but it looks to work the most part
     */
    @PostMapping("/create")
    public ResponseEntity<?> register(@RequestBody CreateOrUpdateContractRequest request) {
        try {
            contractService.createOrUpdateContract(request);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException | BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/valid/{account-id}")
    public ResponseEntity<?> allActiveContracts(@PathVariable("account-id") Long accountId) {
        try {
            Optional<List<Contract>> activeContracts = contractService.getActiveContractsById(accountId);

            if (activeContracts.isPresent()) return ResponseEntity.ok(activeContracts.get());
            else
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No active contracts found for account ID: " + accountId);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/inactive/{account-id}")
    public ResponseEntity<?> allInactiveContracts(@PathVariable("account-id") Long accountId) {
        try {
            Optional<List<Contract>> activeContracts = contractService.getInactiveContractsById(accountId);

            if (activeContracts.isPresent()) return ResponseEntity.ok(activeContracts.get());
            else
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No active contracts found for account ID: " + accountId);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/progress/{account-id}")
    public ResponseEntity<?> allInProgressContracts(@PathVariable("account-id") Long accountId) {
        try {
            Optional<List<Contract>> inProgressContracts = contractService.getInProgressContracts(accountId);

            if (inProgressContracts.isPresent()) return ResponseEntity.ok(inProgressContracts.get());
            else
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No in progress contracts found for account ID: " + accountId);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @Description("Gets all contracts for the users account")
    @GetMapping("/history/{account-id}")
    public ResponseEntity<?> allContractsThatAreNotActive(@PathVariable("account-id") Long accountId) {
        try {
            Optional<List<Contract>> contracts = contractService.getContractsThatAreNotActive(accountId);

            if (contracts.isPresent()) return ResponseEntity.ok(contracts.get());
            else
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No contracts found for account ID: " + accountId);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/{contract-id}")
    public ResponseEntity<?> getContract(@PathVariable("contract-id") Long contractId) {
        try {
            Optional<Contract> contract = contractService.getContract(contractId);

            if (contract.isPresent()) return ResponseEntity.ok(contract.get());
            else
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No contracts found for contract ID: " + contractId);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}
