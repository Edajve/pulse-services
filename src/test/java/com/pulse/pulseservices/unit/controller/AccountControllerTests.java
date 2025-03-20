package com.pulse.pulseservices.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulse.pulseservices.controller.AccountController;
import com.pulse.pulseservices.entity.User;
import com.pulse.pulseservices.enums.Country;
import com.pulse.pulseservices.enums.Role;
import com.pulse.pulseservices.enums.Sex;
import com.pulse.pulseservices.model.AccountStats;
import com.pulse.pulseservices.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AccountControllerTests {

    private MockMvc mockMvc;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    private User mockUser;
    private AccountStats mockStats;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();

        mockUser = User.builder()
                .id(1)
                .firstName("John")
                .lastName("Doe")
                .email("johndoe@example.com")
                .password("hashedpassword")
                .role(Role.USER)
                .securityQuestion("What is your pet's name?")
                .securityAnswer("Fluffy")
                .accountCreatedDate(LocalDateTime.now())
                .sex(Sex.MALE)
                .dateOfBirth("1990-01-01")
                .countryRegion(Country.UNITED_STATES)
                .pinCode("1234")
                .build();

        mockStats = AccountStats.builder()
                .totalContracts(50)
                .totalContractsRevoked(10)
                .successfulToRevokedRatio(5.0)
                .mostConsentedPartner("Partner A")
                .mostRevokedPartner("Partner B")
                .build();
    }

    @Test
    void shouldReturnUserWhenGetAccountByIdIsCalled() throws Exception {
        // Given
        Long accountId = 1L;
        when(accountService.getAccountById(accountId)).thenReturn(mockUser);

        // When & Then
        mockMvc.perform(get("/api/v1/account/{accountId}", accountId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mockUser.getId()))
                .andExpect(jsonPath("$.firstName").value(mockUser.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(mockUser.getLastName()))
                .andExpect(jsonPath("$.email").value(mockUser.getEmail()))
                .andExpect(jsonPath("$.role").value(mockUser.getRole().name()));

        verify(accountService, times(1)).getAccountById(accountId);
    }

    @Test
    void shouldReturnAccountStatsWhenGetAccountStatsByIdIsCalled() throws Exception {
        // Given
        Long accountId = 1L;
        when(accountService.getAccountById(accountId)).thenReturn(mockUser);
        when(accountService.getStats(accountId)).thenReturn(mockStats);

        // When & Then
        mockMvc.perform(get("/api/v1/account/stats/{accountId}", accountId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalContracts").value(50))
                .andExpect(jsonPath("$.totalContractsRevoked").value(10))
                .andExpect(jsonPath("$.successfulToRevokedRatio").value(5.0))
                .andExpect(jsonPath("$.mostConsentedPartner").value("Partner A"))
                .andExpect(jsonPath("$.mostRevokedPartner").value("Partner B"));

        verify(accountService, times(1)).getAccountById(accountId);
        verify(accountService, times(1)).getStats(accountId);
    }

    @Test
    void testUpdateUserById_Success() throws Exception {
        User user = new User();
        user.setId(1);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(Role.USER);

        when(accountService.updateUser(eq(1), any(User.class))).thenReturn(Optional.of(user));

        mockMvc.perform(put("/api/v1/account/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void testUpdateUserById_UserNotFound() throws Exception {
        User user = new User();
        user.setId(1);
        user.setFirstName("John");
        user.setLastName("Doe");

        when(accountService.updateUser(eq(1), any(User.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/account/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isNotFound());
    }
}