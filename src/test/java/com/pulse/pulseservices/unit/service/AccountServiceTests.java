package com.pulse.pulseservices.unit.service;

import com.pulse.pulseservices.entity.User;
import com.pulse.pulseservices.enums.ContractStatus;
import com.pulse.pulseservices.enums.Country;
import com.pulse.pulseservices.enums.Role;
import com.pulse.pulseservices.enums.Sex;
import com.pulse.pulseservices.model.AccountStats;
import com.pulse.pulseservices.model.contract.Contract;
import com.pulse.pulseservices.repositories.AccountRepository;
import com.pulse.pulseservices.repositories.ContractRepository;
import com.pulse.pulseservices.repositories.UserRepository;
import com.pulse.pulseservices.service.AccountService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTests {

    @InjectMocks
    AccountService accountService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    public void AccountService_resetPassword_Successful() {

        // Arrange
        String initialPassword = "securePassword123";
        String newPassword = "newSecurePassword";
        String hashedPassword = "hashedPassword123";

        User user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("johndoe@example.com")
                .password(initialPassword)
                .role(Role.USER)
                .securityQuestion("What is your favorite color?")
                .securityAnswer("Blue")
                .accountCreatedDate(LocalDateTime.now())
                .sex(Sex.MALE)
                .dateOfBirth("1990-05-15")
                .countryRegion(Country.US)
                .pinCode("1234")
                .localHash("hashedValue123")
                .authMethod("PIN")
                .hasUserBeenAskedAuthMethod(false)
                .build();

        // Mock repository behavior
        when(accountRepository.findById(Math.toIntExact(user.getId()))).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(newPassword)).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        accountService.resetPassword(user.getId(), newPassword);

        // Assert
        Mockito.verify(userRepository, Mockito.times(1)).save(user);
        Assertions.assertNotEquals(initialPassword, user.getPassword());
        Assertions.assertEquals(hashedPassword, user.getPassword()); // Check if password was hashed
    }

    private User createUser() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("test@example.com");
        user.setSecurityQuestion("What is your favorite color?");
        user.setSecurityAnswer("Blue");
        user.setSex(Sex.FEMALE);
        user.setDateOfBirth("1990-01-01");
        user.setCountryRegion(Country.US);
        user.setAuthMethod("password");
        user.setHasUserBeenAskedAuthMethod(true);
        return user;
    }

    private User createUserWithParams(Long id, String firstName, String lastName) {
        User user = new User();
        user.setId(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        return user;
    }

    private Contract createContract(User participantOne, User participantTwo, ContractStatus status, boolean p1Revoked, boolean p2Revoked) {
        return Contract.builder()
                .participantOne(participantOne)
                .participantTwo(participantTwo)
                .status(status)
                .didParticipantOneRevoke(p1Revoked)
                .didParticipantTwoRevoke(p2Revoked)
                .build();
    }

    @Disabled
    @Test
    void AccountService_getStats_Success() {
        Long accountId = 1L;
        User user1 = createUserWithParams(1L, "Alice", "Smith");
        User user2 = createUserWithParams(2L, "Bob", "Johnson");
        User user3 = createUserWithParams(3L, "Charlie", "Brown");

        List<Contract> contracts = List.of(
                createContract(user1, user2, ContractStatus.ACTIVE, false, false),
                createContract(user1, user2, ContractStatus.ACTIVE, false, false),
                createContract(user1, user3, ContractStatus.ACTIVE, false, false)
        );

        List<Contract> revokedContracts = List.of(
                createContract(user1, user3, ContractStatus.CANCELLED, true, false),
                createContract(user1, user3, ContractStatus.CANCELLED, true, false)
        );

        when(contractRepository.getTotalContractCount(accountId)).thenReturn(contracts.size() + revokedContracts.size());
        when(contractRepository.getTotalContractRevokedCount(accountId)).thenReturn(revokedContracts.size());
        when(contractRepository.getTotalContracts(accountId)).thenReturn(List.of());
        when(contractRepository.getTotalRevokedContracts(accountId)).thenReturn(List.of());
        when(accountRepository.findById(2)).thenReturn(Optional.of(user2));
        when(accountRepository.findById(3)).thenReturn(Optional.of(user3));

        AccountStats stats = accountService.getStats(accountId);

        Assertions.assertEquals(5, stats.getTotalContracts());
        Assertions.assertEquals(2, stats.getTotalContractsRevoked());
        Assertions.assertEquals(1.5, stats.getSuccessfulToRevokedRatio());
        Assertions.assertEquals("Bob Johnson", stats.getMostConsentedPartner());
        Assertions.assertEquals("Charlie Brown", stats.getMostRevokedPartner());
    }

    @Test
    void AccountService_getStats_NoContracts() {
        Long accountId = 1L;
        when(contractRepository.getTotalContractCount(accountId)).thenReturn(0);
        when(contractRepository.getTotalContractRevokedCount(accountId)).thenReturn(0);
        when(contractRepository.getTotalContracts(accountId)).thenReturn(List.of());
        when(contractRepository.getTotalRevokedContracts(accountId)).thenReturn(List.of());

        AccountStats stats = accountService.getStats(accountId);

        Assertions.assertEquals(0, stats.getTotalContracts());
        Assertions.assertEquals(0, stats.getTotalContractsRevoked());
        Assertions.assertEquals(0.0, stats.getSuccessfulToRevokedRatio());
        Assertions.assertEquals("None", stats.getMostConsentedPartner());
        Assertions.assertEquals("None", stats.getMostRevokedPartner());
    }

    @Test
    void AccountService_completedToRevokedContractRatio_ValidCases() {
        Assertions.assertEquals(2.0, accountService.completedToRevokedContractRatio(6, 2));
        Assertions.assertEquals(1.5, accountService.completedToRevokedContractRatio(5, 2));
        Assertions.assertEquals(0.0, accountService.completedToRevokedContractRatio(0, 0));
    }

    @Disabled
    @Test
    void AccountService_getMostOccurringPartner_Success() {
        Long accountId = 1L;
        User user1 = createUserWithParams(1L, "Alice", "Smith");
        User user2 = createUserWithParams(2L, "Bob", "Johnson");
        User user3 = createUserWithParams(3L, "Charlie", "Brown");

        List<Contract> contracts = new ArrayList<>(Arrays.asList(
                createContract(user1, user2, ContractStatus.ACTIVE, false, false),
                createContract(user1, user2, ContractStatus.ACTIVE, false, false),
                createContract(user1, user3, ContractStatus.ACTIVE, false, false)
        ));

        List<Contract> revokedContracts = List.of();

        when(contractRepository.getTotalContractCount(accountId)).thenReturn(contracts.size());
        when(contractRepository.getTotalContractRevokedCount(accountId)).thenReturn(0);
//        when(contractRepository.getTotalContracts(accountId)).thenReturn(contracts);
        // Mocking accountRepository so accountService.getAccountById() returns a valid user
        when(accountRepository.findById(2)).thenReturn(Optional.of(user2));
        when(accountRepository.findById(3)).thenReturn(Optional.of(user3));

        AccountStats stats = accountService.getStats(accountId);

        Assertions.assertEquals("Bob Johnson", stats.getMostConsentedPartner());
    }

    @Test
    void AccountService_getMostOccurringPartner_NoContracts() {
        Long accountId = 1L;
        when(contractRepository.getTotalContractCount(accountId)).thenReturn(0);
        when(contractRepository.getTotalContractRevokedCount(accountId)).thenReturn(0);
        when(contractRepository.getTotalContracts(accountId)).thenReturn(List.of());
        when(contractRepository.getTotalRevokedContracts(accountId)).thenReturn(List.of());

        AccountStats stats = accountService.getStats(accountId);

        Assertions.assertEquals("None", stats.getMostConsentedPartner());
    }

    @Test
    void AccountService_updateUser_Success() {
        User existingUser = createUser();
        User request = new User();
        request.setFirstName("Jane");
        request.setLastName("Smith");

        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        Optional<User> updatedUser = accountService.updateUser(1, request);

        Assertions.assertTrue(updatedUser.isPresent());
        Assertions.assertEquals("Jane", updatedUser.get().getFirstName());
        Assertions.assertEquals("Smith", updatedUser.get().getLastName());
    }

    @Test
    void AccountService_updateUser_UserNotFound() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () ->
                accountService.updateUser(1, new User())
        );

        Assertions.assertEquals("User not found", exception.getMessage());
    }

    @Test
    void AccountService_updateUser_UpdateRestrictedFields() {
        User existingUser = createUser();
        User request = new User();
        request.setPassword("newPassword");

        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () ->
                accountService.updateUser(1, request)
        );

        Assertions.assertEquals("Updating password, pinCode, or localHash is not allowed due to security reasons.", exception.getMessage());
    }

    @Test
    void AccountService_verifySecurityQuestionAndAnswer_verified() {
        User user = createUser();
        when(accountRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        String result = accountService.verifySecurityQuestionAndAnswer("What is your favorite color?", "Blue", "test@example.com");

        Assertions.assertEquals("verified", result);
    }

    @Test
    void AccountService_verifySecurityQuestionAndAnswer_InvalidAccount() {
        when(accountRepository.findByEmail("invalid@example.com")).thenReturn(Optional.empty());

        String result = accountService.verifySecurityQuestionAndAnswer("What is your favorite color?", "Blue", "invalid@example.com");

        Assertions.assertEquals("Invalid account", result);
    }

    @Test
    void AccountService_verifySecurityQuestionAndAnswer_NoSecurityQuestion() {
        User user = createUser();
        user.setSecurityQuestion(null);
        when(accountRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () ->
                accountService.verifySecurityQuestionAndAnswer("What is your favorite color?", "Blue", "test@example.com")
        );
        Assertions.assertEquals("This accountId: 1 does not have a security question", exception.getMessage());
    }

    @Test
    void AccountService_verifySecurityQuestionAndAnswer_NoSecurityAnswer() {
        User user = createUser();
        user.setSecurityAnswer(null);
        when(accountRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () ->
                accountService.verifySecurityQuestionAndAnswer("What is your favorite color?", "Blue", "test@example.com")
        );
        Assertions.assertEquals("This accountId: 1 does not have a security Answer", exception.getMessage());
    }

    @Test
    void AccountService_verifySecurityQuestionAndAnswer_SecurityQuestionIncorrect() {
        User user = createUser();
        when(accountRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        String result = accountService.verifySecurityQuestionAndAnswer("What is your pet's name?", "Blue", "test@example.com");

        Assertions.assertEquals("Security Question is incorrect", result);
    }

    @Test
    void AccountService_verifySecurityQuestionAndAnswer_SecurityAnswerIncorrect() {
        User user = createUser();
        when(accountRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        String result = accountService.verifySecurityQuestionAndAnswer("What is your favorite color?", "Red", "test@example.com");

        Assertions.assertEquals("Security Answer is incorrect", result);
    }

    @Test
    public void AccountService_completedToRevokedContractRatio_returnRatio_WhenValidInput() {
        double ratio = accountService.completedToRevokedContractRatio(10, 2);
        Assertions.assertEquals(4.0, ratio);
    }

    @Test
    public void AccountService_completedToRevokedContractRatio_returnZero_WhenNoRevokedContracts() {
        double ratio = accountService.completedToRevokedContractRatio(10, 0);
        Assertions.assertEquals(0.0, ratio);
    }

    @Test
    public void AccountService_completedToRevokedContractRatio_returnZero_WhenAllContractsRevoked() {
        double ratio = accountService.completedToRevokedContractRatio(10, 10);
        Assertions.assertEquals(0.0, ratio);
    }

    @Test
    public void AccountService_completedToRevokedContractRatio_returnZero_WhenNoContractsExist() {
        double ratio = accountService.completedToRevokedContractRatio(0, 0);
        Assertions.assertEquals(0.0, ratio);
    }

    @Test
    public void AccountService_completedToRevokedContractRatio_returnRoundedRatio_WhenDecimalResult() {
        double ratio = accountService.completedToRevokedContractRatio(7, 3);
        Assertions.assertEquals(1.33, ratio);
    }
}