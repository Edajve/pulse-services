package com.pulse.pulseservices.unit.service;

import com.pulse.pulseservices.entity.Contract;
import com.pulse.pulseservices.entity.User;
import com.pulse.pulseservices.model.AccountStats;
import com.pulse.pulseservices.repositories.AccountRepository;
import com.pulse.pulseservices.repositories.ContractRepository;
import com.pulse.pulseservices.repositories.UserRepository;
import com.pulse.pulseservices.service.AccountService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTests {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ContractRepository contractRepository;

    @InjectMocks
    private AccountService accountService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("secure-password")
                .build();
    }

    @Test
    void getAccountById_HappyPath_UserExists() {
        // Arrange
        Long userId = 1L;
        when(accountRepository.findById(userId.intValue())).thenReturn(Optional.of(testUser));

        // Act
        User foundUser = accountService.getAccountById(userId);

        // Assert
        assertNotNull(foundUser);
        assertEquals(testUser.getId(), foundUser.getId());
        assertEquals(testUser.getFirstName(), foundUser.getFirstName());
        assertEquals(testUser.getLastName(), foundUser.getLastName());
        assertEquals(testUser.getEmail(), foundUser.getEmail());

        // Verify interactions
        verify(accountRepository, times(1)).findById(userId.intValue());
    }

    @Test
    void getAccountById_BadPath_UserNotFound() {
        // Arrange
        Long userId = 999L;
        when(accountRepository.findById(userId.intValue())).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            accountService.getAccountById(userId);
        });

        assertEquals("User not found with id: 999", exception.getMessage());

        // Verify interactions
        verify(accountRepository, times(1)).findById(userId.intValue());
    }

    @Test
    void getStats_HappyPath() {
        // Arrange
        Long userId = 1L;

        // Create Users for Contracts
        User alice = User.builder().id(2).firstName("Alice").lastName("Smith").build();
        User bob = User.builder().id(3).firstName("Bob").lastName("Johnson").build();

        // Ensure Bob has MORE contracts than Alice
        Contract contract1 = new Contract();
        contract1.setParticipantOne(testUser);
        contract1.setParticipantTwo(bob); // Bob 1st contract

        Contract contract2 = new Contract();
        contract2.setParticipantOne(testUser);
        contract2.setParticipantTwo(bob); // Bob 2nd contract

        Contract contract3 = new Contract();
        contract3.setParticipantOne(testUser);
        contract3.setParticipantTwo(bob); // Bob 3rd contract

        Contract contract4 = new Contract();
        contract4.setParticipantOne(testUser);
        contract4.setParticipantTwo(alice); // Alice 1st contract

        Contract revokedContract = new Contract();
        revokedContract.setParticipantOne(testUser);
        revokedContract.setParticipantTwo(alice); // Alice revoked contract

        List<Contract> allContracts = List.of(contract1, contract2, contract3, contract4);
        List<Contract> revokedContracts = List.of(revokedContract);

        // Mock Repository Calls
        when(contractRepository.getTotalContractCount(userId)).thenReturn(5);
        when(contractRepository.getTotalContractRevokedCount(userId)).thenReturn(2);
        when(contractRepository.getTotalContracts(userId)).thenReturn(allContracts);
        when(contractRepository.getTotalRevokedContracts(userId)).thenReturn(revokedContracts);

        // Allow multiple calls for Alice and Bob
        when(accountRepository.findById(2)).thenReturn(Optional.of(alice));
        when(accountRepository.findById(3)).thenReturn(Optional.of(bob));

        // Act
        AccountStats stats = accountService.getStats(userId);

        // Assert
        assertNotNull(stats);
        assertEquals(5, stats.getTotalContracts());
        assertEquals(2, stats.getTotalContractsRevoked());
        assertEquals(1.5, stats.getSuccessfulToRevokedRatio()); // (5 - 2) / 2 = 1.5
        assertEquals("Bob Johnson", stats.getMostConsentedPartner()); // Bob has 3, Alice has 1
        assertEquals("Alice Smith", stats.getMostRevokedPartner()); // Alice revoked the most contracts

        // Verify interactions (adjusted)
        verify(contractRepository, times(1)).getTotalContractCount(userId);
        verify(contractRepository, times(1)).getTotalContractRevokedCount(userId);
        verify(contractRepository, times(1)).getTotalContracts(userId);
        verify(contractRepository, times(1)).getTotalRevokedContracts(userId);
        verify(accountRepository, times(1)).findById(2); // Alice looked up once
        verify(accountRepository, times(1)).findById(3); // Bob looked up once
    }

    @Test
    void verifySecurityQuestionAndAnswer_UserNotFound() {
        // Arrange
        String email = "user@example.com";
        when(accountRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        String result = accountService.verifySecurityQuestionAndAnswer("What is your pet's name?", "Fluffy", email);

        // Assert
        assertEquals("Invalid account", result);
        verify(accountRepository, times(1)).findByEmail(email);
    }

    @Test
    void verifySecurityQuestionAndAnswer_NoSecurityQuestion() {
        // Arrange
        String email = "user@example.com";
        User user = User.builder()
                .id(1)
                .email(email)
                .securityQuestion(null) // No security question
                .securityAnswer("Fluffy")
                .build();

        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                accountService.verifySecurityQuestionAndAnswer("What is your pet's name?", "Fluffy", email));

        assertEquals("This accountId: 1 does not have a security question", exception.getMessage());
    }

    @Test
    void verifySecurityQuestionAndAnswer_NoSecurityAnswer() {
        // Arrange
        String email = "user@example.com";
        User user = User.builder()
                .id(1)
                .email(email)
                .securityQuestion("What is your pet's name?")
                .securityAnswer(null) // No security answer
                .build();

        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                accountService.verifySecurityQuestionAndAnswer("What is your pet's name?", "Fluffy", email));

        assertEquals("This accountId: 1 does not have a security Answer", exception.getMessage());
    }

    @Test
    void verifySecurityQuestionAndAnswer_IncorrectSecurityQuestion() {
        // Arrange
        String email = "user@example.com";
        User user = User.builder()
                .id(1)
                .email(email)
                .securityQuestion("What is your pet's name?")
                .securityAnswer("Fluffy")
                .build();

        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        String result = accountService.verifySecurityQuestionAndAnswer("What is your favorite color?", "Fluffy", email);

        // Assert
        assertEquals("Security Question is incorrect", result);
    }

    @Test
    void verifySecurityQuestionAndAnswer_IncorrectSecurityAnswer() {
        // Arrange
        String email = "user@example.com";
        User user = User.builder()
                .id(1)
                .email(email)
                .securityQuestion("What is your pet's name?")
                .securityAnswer("Fluffy")
                .build();

        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        String result = accountService.verifySecurityQuestionAndAnswer("What is your pet's name?", "Max", email);

        // Assert
        assertEquals("Security Answer is incorrect", result);
    }

    @Test
    void verifySecurityQuestionAndAnswer_CorrectSecurityQuestionAndAnswer() {
        // Arrange
        String email = "user@example.com";
        User user = User.builder()
                .id(1)
                .email(email)
                .securityQuestion("What is your pet's name?")
                .securityAnswer("Fluffy")
                .build();

        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        String result = accountService.verifySecurityQuestionAndAnswer("What is your pet's name?", "Fluffy", email);

        // Assert
        assertEquals("verified", result);
    }

    @Test
    void updatePinSetting_HappyPath() {
        // Arrange
        Long accountId = 1L;
        boolean pinSetting = true;

        // Act
        accountService.updatePinSetting(accountId, pinSetting);

        // Assert (Verify that repository was called correctly)
        verify(accountRepository, times(1)).updatePinSetting(accountId, pinSetting);
    }

    @Test
    void updatePinSettingAndPinCode_HappyPath() {
        // Arrange
        Long accountId = 1L;
        boolean pinSetting = true;
        String rawPinCode = "1234";
        String encodedPinCode = "encoded1234";

        when(accountRepository.findById(accountId.intValue())).thenReturn(Optional.of(testUser)); // Mock getAccountById
        when(passwordEncoder.encode(rawPinCode)).thenReturn(encodedPinCode);
        doNothing().when(accountRepository).updatePinSetting(accountId, pinSetting); // Fix for void method

        // Act
        accountService.updatePinSettingAndPinCode(accountId, pinSetting, rawPinCode);

        // Assert
        assertEquals(encodedPinCode, testUser.getPinCode()); // Ensure password was encoded and set
        verify(accountRepository, times(1)).updatePinSetting(accountId, pinSetting);
        verify(accountRepository, times(1)).findById(accountId.intValue()); // Ensure user is retrieved
        verify(passwordEncoder, times(1)).encode(rawPinCode);
        verify(userRepository, times(1)).save(testUser);
    }
}