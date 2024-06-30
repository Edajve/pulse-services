package com.pulse.pulseservices.service;

import com.pulse.pulseservices.entity.Contract;
import com.pulse.pulseservices.entity.User;
import com.pulse.pulseservices.enums.ContractStatus;
import com.pulse.pulseservices.enums.Country;
import com.pulse.pulseservices.enums.Role;
import com.pulse.pulseservices.enums.Sex;
import com.pulse.pulseservices.model.AccountStats;
import com.pulse.pulseservices.repositories.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTests {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ContractService contractService;

    @InjectMocks
    private AccountService accountService;

    @Test
    public void testGetStats() {
        // Arrange
        Long userId = 252L;
        List<Contract> contracts = createContractData();

        when(contractService.getTotalContractsCount(userId)).thenReturn(10);
        when(contractService.getTotalContractsRevokedCount(userId)).thenReturn(3);
        when(contractService.getAllContracts(userId)).thenReturn(contracts);
        when(contractService.getTotalRevokedContracts(userId)).thenReturn(contracts.subList(2, 10));

        User mostConsentedPartner = createUser(302, "android", "pixel");
        User mostRevokedPartner = createUser(302, "android", "pixel");

        when(accountRepository.findById(302)).thenReturn(java.util.Optional.of(mostConsentedPartner));
//        when(accountRepository.findById(352)).thenReturn(java.util.Optional.of(mostRevokedPartner));

        // Act
        AccountStats accountStats = accountService.getStats(userId);

        // Assert
        assertEquals("Expected total is not correct", 10, accountStats.getTotalContracts());
        assertEquals("Expected total is not correct", 3, accountStats.getTotalContractsRevoked());
        assertTrue("Completed to Revoked Ratio is not correct", String.valueOf(2.33).equals(String.valueOf(accountStats.getSuccessfulToRevokedRatio())));
        assertEquals("Most contract partner name is not correct", "android pixel", accountStats.getMostConsentedPartner());
        assertEquals("Most revoked partner name is not correct", "android pixel", accountStats.getMostRevokedPartner());
    }

    public static List<Contract> createContractData() {
        User userTwoFiftyTwo = createUser(252, "Ronald", "Riles");
        User userThreeZeroTwo = createUser(302, "android", "pixel");
        User userThreeFiftyThree = createUser(352, "Ashley", "Tisdale");

        List<Contract> contracts = new ArrayList<>();

        contracts.add(createContract(12L, userTwoFiftyTwo, userThreeZeroTwo, ContractStatus.COMPLETED, false, false, 30));
        contracts.add(createContract(16L, userThreeFiftyThree, userTwoFiftyTwo, ContractStatus.COMPLETED, false, false, 0));
        contracts.add(createContract(17L, userTwoFiftyTwo, userThreeZeroTwo, ContractStatus.CANCELLED, true, true, 5, "aofjafo...", "aofjisadfoijafoijaosjff..."));
        contracts.add(createContract(18L, userThreeFiftyThree, userTwoFiftyTwo, ContractStatus.COMPLETED, false, false, 60));
        contracts.add(createContract(19L, userThreeFiftyThree, userTwoFiftyTwo, ContractStatus.COMPLETED, false, false, 60));
        contracts.add(createContract(20L, userThreeFiftyThree, userTwoFiftyTwo, ContractStatus.COMPLETED, false, false, 400));
        contracts.add(createContract(26L, userThreeZeroTwo, userTwoFiftyTwo, ContractStatus.COMPLETED, false, false, 60));
        contracts.add(createContract(28L, userTwoFiftyTwo, userThreeZeroTwo, ContractStatus.COMPLETED, false, false, 60));
        contracts.add(createContract(31L, userThreeZeroTwo, userTwoFiftyTwo, ContractStatus.CANCELLED, true, true, 60, "test other revoke", "Here we goooo"));
        contracts.add(createContract(32L, userTwoFiftyTwo, userThreeZeroTwo, ContractStatus.CANCELLED, true, true, 60, "test revoke", "Get me out of hereeee"));

        return contracts;
    }

    private static User createUser(int id, String firstName, String lastName) {
        return User.builder()
                .id(id)
                .accountCreatedDate(LocalDateTime.parse("2024-05-22T04:49:07.081"))
                .countryRegion(Country.united_states)
                .dateOfBirth("12-12-1997")
                .email("ad")
                .firstName(firstName)
                .lastName(lastName)
                .password("fakePass")
                .role(Role.USER)
                .sex(Sex.female)
                .build();
    }

    private static Contract createContract(Long id, User participantOne, User participantTwo, ContractStatus status, boolean participantOneRevoked, boolean participantTwoRevoked, int durationMinutes) {
        return Contract.builder()
                .id(id)
                .participantOne(participantOne)
                .participantTwo(participantTwo)
                .startTime(LocalDateTime.parse("2024-05-25T22:54:24.199341"))
                .endTime(LocalDateTime.parse("2024-05-28T00:29:09.076679"))
                .status(status)
                .didParticipantOneRevoke(participantOneRevoked)
                .didParticipantTwoRevoke(participantTwoRevoked)
                .participantOneRevokeContractReason(null)
                .participantTwoRevokeContractReason(null)
                .contractCancelReason(null)
                .durationMinutes(durationMinutes)
                .build();
    }

    private static Contract createContract(Long id, User participantOne, User participantTwo, ContractStatus status, boolean participantOneRevoked, boolean participantTwoRevoked, int durationMinutes, String participantOneRevokeReason, String participantTwoRevokeReason) {
        return Contract.builder()
                .id(id)
                .participantOne(participantOne)
                .participantTwo(participantTwo)
                .startTime(LocalDateTime.parse("2024-05-25T22:54:24.199341"))
                .endTime(LocalDateTime.parse("2024-05-28T00:29:09.076679"))
                .status(status)
                .didParticipantOneRevoke(participantOneRevoked)
                .didParticipantTwoRevoke(participantTwoRevoked)
                .participantOneRevokeContractReason(participantOneRevokeReason)
                .participantTwoRevokeContractReason(participantTwoRevokeReason)
                .contractCancelReason("Both Parties Revoked")
                .durationMinutes(durationMinutes)
                .build();
    }
}
