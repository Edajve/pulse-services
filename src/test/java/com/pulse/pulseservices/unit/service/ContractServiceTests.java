package com.pulse.pulseservices.unit.service;

import com.pulse.pulseservices.entity.Contract;
import com.pulse.pulseservices.entity.User;
import com.pulse.pulseservices.enums.ContractStatus;
import com.pulse.pulseservices.model.contract.CreateOrUpdateContractRequest;
import com.pulse.pulseservices.repositories.ContractRepository;
import com.pulse.pulseservices.service.AccountService;
import com.pulse.pulseservices.service.ContractService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Disabled
class ContractServiceTests {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private ContractService contractService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateNewContractWhenNoActiveContractExists() {
        // Given
        CreateOrUpdateContractRequest request = new CreateOrUpdateContractRequest(1, "2", 10L, 123L);

        when(contractRepository.findActiveContractBetweenParticipants(10L, 123L)).thenReturn(Optional.empty());
        when(contractRepository.findActiveContractWithAccountAndContractNumber(10L, 1)).thenReturn(Optional.empty());
        when(contractRepository.findActiveContractWithAccountAndContractNumber(123L, 1)).thenReturn(Optional.empty());

        when(accountService.getStoredPassword(10L)).thenReturn("encodedPassword");
        when(passwordEncoder.matches("securePassword", "encodedPassword")).thenReturn(true);

        User scanner = new User();
        scanner.setId(10L);
        when(accountService.getAccountById(10L)).thenReturn(scanner);

        ArgumentCaptor<Contract> contractCaptor = ArgumentCaptor.forClass(Contract.class);

        // Debugging Print Statements
        System.out.println("Stored Password: " + accountService.getStoredPassword(10L));
        System.out.println("Password Matches: " + passwordEncoder.matches("securePassword", "encodedPassword"));

        // When
        contractService.createOrUpdateContract(request);

        // Then
        verify(contractRepository).save(contractCaptor.capture());
        Contract savedContract = contractCaptor.getValue();

        assertNotNull(savedContract);
        assertEquals(scanner, savedContract.getParticipantOne());
        assertEquals(ContractStatus.PROGRESS, savedContract.getStatus());
        assertEquals(1, savedContract.getContractNumber());
        assertEquals(60, savedContract.getDurationMinutes());
    }

    @Test
    void shouldThrowExceptionWhenUserTriesToCreateContractWithThemselves() {
        // Given
        CreateOrUpdateContractRequest request = new CreateOrUpdateContractRequest(1, "2", 10L, 123L);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> contractService.createOrUpdateContract(request));

        assertEquals("User can not create a contract with themselves", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenActiveContractAlreadyExistsBetweenUsers() {
        // Given
        CreateOrUpdateContractRequest request = new CreateOrUpdateContractRequest(1, "2", 10L, 123L);

        when(contractRepository.findActiveContractBetweenParticipants(1L, 2L)).thenReturn(Optional.of(new Contract()));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> contractService.createOrUpdateContract(request));

        assertEquals("Active contract already exists between the participants, can not create another until this is closed", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenScannerTriesToReuseContractNumber() {
        // Given
        CreateOrUpdateContractRequest request = new CreateOrUpdateContractRequest(1, "2", 10L, 123L);

        when(contractRepository.findActiveContractWithAccountAndContractNumber(1L, 123)).thenReturn(Optional.of(new Contract()));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> contractService.createOrUpdateContract(request));

        assertEquals("Scanner tried to create an active contract with the same contract number. Use a different contract number, or have your consenting partner enter into this contract with this number", exception.getMessage());
    }

    @Test
    void shouldUpdateExistingContractWhenScannieAlreadyHasOne() {
        // Given
        CreateOrUpdateContractRequest request = new CreateOrUpdateContractRequest(1, "2", 10L, 123L);

        Contract existingContract = new Contract();
        existingContract.setId(10L);
        existingContract.setContractNumber(123);

        when(contractRepository.findActiveContractWithAccountAndContractNumber(2L, 123)).thenReturn(Optional.of(existingContract));
        when(accountService.getStoredPassword(1L)).thenReturn("encodedPassword");
        when(passwordEncoder.matches("securePassword", "encodedPassword")).thenReturn(true);
        when(contractRepository.findContractNameById(10L)).thenReturn(123);
        when(contractRepository.findById(10L)).thenReturn(Optional.of(existingContract));

        User scanner = new User();
        scanner.setId(1L);
        when(accountService.getAccountById(1L)).thenReturn(scanner);

        ArgumentCaptor<Contract> contractCaptor = ArgumentCaptor.forClass(Contract.class);

        // When
        contractService.createOrUpdateContract(request);

        // Then
        verify(contractRepository).save(contractCaptor.capture());
        Contract updatedContract = contractCaptor.getValue();

        assertNotNull(updatedContract);
        assertEquals(scanner, updatedContract.getParticipantTwo());
        assertEquals(ContractStatus.ACTIVE, updatedContract.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsIncorrect() {
        // Given
        CreateOrUpdateContractRequest request = new CreateOrUpdateContractRequest(1, "2", 10L, 123L);

        when(contractRepository.findActiveContractWithAccountAndContractNumber(2L, 123)).thenReturn(Optional.empty());
        when(accountService.getStoredPassword(1L)).thenReturn("encodedPassword");
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // When & Then
        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> contractService.createOrUpdateContract(request));

        assertEquals("Invalid password", exception.getMessage());
    }
}