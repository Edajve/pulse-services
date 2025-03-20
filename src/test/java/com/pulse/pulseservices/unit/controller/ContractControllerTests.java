package com.pulse.pulseservices.unit.controller;

import com.pulse.pulseservices.controller.ContractController;
import com.pulse.pulseservices.model.contract.CreateOrUpdateContractRequest;
import com.pulse.pulseservices.model.contract.UpdateContractRequest;
import com.pulse.pulseservices.service.ContractService;
import com.pulse.pulseservices.entity.Contract;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class ContractControllerTests {

    @Mock
    private ContractService contractService;

    @InjectMocks
    private ContractController contractController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void ContractController_testAllContractsThatAreNotActive_Success() {
        // Arrange
        Long accountId = 1L;
        List<com.pulse.pulseservices.entity.Contract> mockContracts = new ArrayList<>();
        when(contractService.getContractsThatAreNotActive(accountId)).thenReturn(Optional.of(mockContracts));

        // Act
        ResponseEntity<?> response = contractController.allContractsThatAreNotActive(accountId);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockContracts, response.getBody());
    }

    @Test
    void ContractController_testAllContractsThatAreNotActive_NotFound() {
        // Arrange
        Long accountId = 2L;
        when(contractService.getContractsThatAreNotActive(accountId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = contractController.allContractsThatAreNotActive(accountId);

        // Assert
        assertEquals(404, response.getStatusCodeValue());
        assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("No contracts found for account ID: " + accountId));
    }

    @Test
    void ContractController_testAllContractsThatAreNotActive_InternalServerError() {
        // Arrange
        Long accountId = 3L;
        when(contractService.getContractsThatAreNotActive(accountId)).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<?> response = contractController.allContractsThatAreNotActive(accountId);

        // Assert
        assertEquals(500, response.getStatusCodeValue());
        assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("Error: Database error"));
    }

    @Test
    void ContractController_testCreateContract_Success() {
        // Arrange
        CreateOrUpdateContractRequest request = new CreateOrUpdateContractRequest();
        doNothing().when(contractService).createOrUpdateContract(request);

        // Act
        ResponseEntity<?> response = contractController.register(request);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void ContractController_testCreateContract_ConflictError() {
        // Arrange
        CreateOrUpdateContractRequest request = new CreateOrUpdateContractRequest();
        doThrow(new IllegalStateException("Contract already exists")).when(contractService).createOrUpdateContract(request);

        // Act
        ResponseEntity<?> response = contractController.register(request);

        // Assert
        assertEquals(409, response.getStatusCodeValue());
        assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("Contract already exists"));
    }

    @Test
    void ContractController_testCreateContract_BadCredentialsError() {
        // Arrange
        CreateOrUpdateContractRequest request = new CreateOrUpdateContractRequest();
        doThrow(new org.springframework.security.authentication.BadCredentialsException("Invalid credentials"))
                .when(contractService).createOrUpdateContract(request);

        // Act
        ResponseEntity<?> response = contractController.register(request);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("Invalid credentials"));
    }

    @Test
    void ContractController_testCreateContract_InternalServerError() {
        // Arrange
        CreateOrUpdateContractRequest request = new CreateOrUpdateContractRequest();
        doThrow(new RuntimeException("Unexpected error")).when(contractService).createOrUpdateContract(request);

        // Act
        ResponseEntity<?> response = contractController.register(request);

        // Assert
        assertEquals(500, response.getStatusCodeValue());
        assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("An unexpected error occurred"));
    }

    @Test
    void ContractController_testAllActiveContracts_Success() {
        // Arrange
        Long accountId = 1L;
        List<com.pulse.pulseservices.entity.Contract> mockContracts = new ArrayList<>();
        when(contractService.getActiveContractsById(accountId)).thenReturn(Optional.of(mockContracts));

        // Act
        ResponseEntity<?> response = contractController.allActiveContracts(accountId);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockContracts, response.getBody());
    }

    @Test
    void ContractController_testAllActiveContracts_NotFound() {
        // Arrange
        Long accountId = 2L;
        when(contractService.getActiveContractsById(accountId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = contractController.allActiveContracts(accountId);

        // Assert
        assertEquals(404, response.getStatusCodeValue());
        assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("No active contracts found for account ID: " + accountId));
    }

    @Test
    void ContractController_testAllActiveContracts_InternalServerError() {
        // Arrange
        Long accountId = 3L;
        when(contractService.getActiveContractsById(accountId)).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<?> response = contractController.allActiveContracts(accountId);

        // Assert
        assertEquals(500, response.getStatusCodeValue());
        assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("Error: Database error"));
    }

    @Test
    void ContractController_testAllInactiveContracts_Success() {
        // Arrange
        Long accountId = 1L;
        List<com.pulse.pulseservices.entity.Contract> mockContracts = new ArrayList<>();
        when(contractService.getInactiveContractsById(accountId)).thenReturn(Optional.of(mockContracts));

        // Act
        ResponseEntity<?> response = contractController.allInactiveContracts(accountId);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockContracts, response.getBody());
    }

    @Test
    void ContractController_testAllInactiveContracts_NotFound() {
        // Arrange
        Long accountId = 2L;
        when(contractService.getInactiveContractsById(accountId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = contractController.allInactiveContracts(accountId);

        // Assert
        assertEquals(404, response.getStatusCodeValue());
        assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("No active contracts found for account ID: " + accountId));
    }

    @Test
    void ContractController_testAllInactiveContracts_InternalServerError() {
        // Arrange
        Long accountId = 3L;
        when(contractService.getInactiveContractsById(accountId)).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<?> response = contractController.allInactiveContracts(accountId);

        // Assert
        assertEquals(500, response.getStatusCodeValue());
        assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("Error: Database error"));
    }

    @Test
    void ContractController_testAllInProgressContracts_Success() {
        // Arrange
        Long accountId = 1L;
        List<com.pulse.pulseservices.entity.Contract> mockContracts = new ArrayList<>();
        when(contractService.getInProgressContracts(accountId)).thenReturn(Optional.of(mockContracts));

        // Act
        ResponseEntity<?> response = contractController.allInProgressContracts(accountId);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockContracts, response.getBody());
    }

    @Test
    void ContractController_testAllInProgressContracts_NotFound() {
        // Arrange
        Long accountId = 2L;
        when(contractService.getInProgressContracts(accountId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = contractController.allInProgressContracts(accountId);

        // Assert
        assertEquals(404, response.getStatusCodeValue());
        assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("No in progress contracts found for account ID: " + accountId));
    }

    @Test
    void ContractController_testAllInProgressContracts_InternalServerError() {
        // Arrange
        Long accountId = 3L;
        when(contractService.getInProgressContracts(accountId)).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<?> response = contractController.allInProgressContracts(accountId);

        // Assert
        assertEquals(500, response.getStatusCodeValue());
        assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("Error: Database error"));
    }

    @Test
    void ContractController_testGetContract_Success() {
        // Arrange
        Long contractId = 1L;
        Contract mockContract = new Contract();
        when(contractService.getContract(contractId)).thenReturn(Optional.of(mockContract));

        // Act
        ResponseEntity<?> response = contractController.getContract(contractId);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockContract, response.getBody());
    }

    @Test
    void ContractController_testGetContract_NotFound() {
        // Arrange
        Long contractId = 2L;
        when(contractService.getContract(contractId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = contractController.getContract(contractId);

        // Assert
        assertEquals(404, response.getStatusCodeValue());
        assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("No contracts found for contract ID: " + contractId));
    }

    @Test
    void ContractController_testGetContract_InternalServerError() {
        // Arrange
        Long contractId = 3L;
        when(contractService.getContract(contractId)).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<?> response = contractController.getContract(contractId);

        // Assert
        assertEquals(500, response.getStatusCodeValue());
        assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("Error: Database error"));
    }

    @Test
    void ContractController_testUpdateContract_Success() {
        // Arrange
        Long contractId = 1L;
        Contract updatedContract = new Contract();
        doNothing().when(contractService).updateContract(contractId, updatedContract);

        // Act
        ResponseEntity<?> response = contractController.updateContract(contractId, updatedContract);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Contract updated", response.getBody());
    }

    @Test
    void ContractController_testUpdateContract_InternalServerError() {
        // Arrange
        Long contractId = 2L;
        Contract updatedContract = new Contract();
        doThrow(new IllegalStateException("Update failed")).when(contractService).updateContract(contractId, updatedContract);

        // Act
        ResponseEntity<?> response = contractController.updateContract(contractId, updatedContract);

        // Assert
        assertEquals(500, response.getStatusCodeValue());
        assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("Error: Update failed"));
    }

    @Test
    void ContractController_testRevokeContractByUserId_Success() {
        // Arrange
        Long contractId = 1L;
        Long userId = 10L;
        UpdateContractRequest updateContractRequest = new UpdateContractRequest();
        doNothing().when(contractService).revokeContract(contractId, userId, updateContractRequest);

        // Act
        ResponseEntity<?> response = contractController.revokeContractByUserId(contractId, userId, updateContractRequest);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Contract updated", response.getBody());
    }

    @Test
    void ContractController_testRevokeContractByUserId_InternalServerError() {
        // Arrange
        Long contractId = 2L;
        Long userId = 20L;
        UpdateContractRequest updateContractRequest = new UpdateContractRequest();
        doThrow(new IllegalStateException("Revoke failed")).when(contractService).revokeContract(contractId, userId, updateContractRequest);

        // Act
        ResponseEntity<?> response = contractController.revokeContractByUserId(contractId, userId, updateContractRequest);

        // Assert
        assertEquals(500, response.getStatusCodeValue());
        assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("Error: Revoke failed"));
    }

    @Test
    void ContractController_testGetContractsByNameSearch_Success() {
        // Arrange
        String name = "TestContract";
        int userId = 1;
        List<Contract> mockContracts = new ArrayList<>();
        when(contractService.findByNameAndUserId(name, userId)).thenReturn(Optional.of(mockContracts));

        // Act
        ResponseEntity<List<Contract>> response = contractController.getContractsByNameSearch(name, userId);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockContracts, response.getBody());
    }

    @Test
    void ContractController_testGetContractsByNameSearch_NotFound() {
        // Arrange
        String name = "NonExistent";
        int userId = 2;
        when(contractService.findByNameAndUserId(name, userId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<List<Contract>> response = contractController.getContractsByNameSearch(name, userId);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNull(response.getBody());
    }
}
