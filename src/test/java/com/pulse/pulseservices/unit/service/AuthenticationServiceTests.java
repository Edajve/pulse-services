package com.pulse.pulseservices.unit.service;

import com.pulse.pulseservices.config.auth.JwtService;
import com.pulse.pulseservices.entity.User;
import com.pulse.pulseservices.enums.AuthMethod;
import com.pulse.pulseservices.enums.Country;
import com.pulse.pulseservices.enums.Role;
import com.pulse.pulseservices.enums.Sex;
import com.pulse.pulseservices.model.auth.AuthenticationResponse;
import com.pulse.pulseservices.model.auth.RegisterRequest;
import com.pulse.pulseservices.repositories.AccountRepository;
import com.pulse.pulseservices.repositories.UserRepository;
import com.pulse.pulseservices.service.AccountService;
import com.pulse.pulseservices.service.AuthenticationService;
import com.pulse.pulseservices.service.QrService;
import com.pulse.pulseservices.utils.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTests {

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private QrService qrService;

    @Mock
    private AccountService accountService;

    @Mock
    private AccountRepository accountRepository;

    private RegisterRequest registerRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("johndoe@example.com")
                .password("password123")
                .securityAnswer("TestAnswer")
                .securityQuestion("TestQuestion")
                .accountCreatedDate(LocalDateTime.now())
                .sex(Sex.GENDER_QUEER)
                .dateOfBirth("10-21-2000")
                .countryRegion(Country.united_states)
                .build();

        mockUser = User.builder()
                .id(1L)
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .email(registerRequest.getEmail())
                .password("encodedPassword")
                .role(Role.USER)
                .securityAnswer(registerRequest.getSecurityAnswer())
                .securityQuestion(registerRequest.getSecurityQuestion())
                .accountCreatedDate(registerRequest.getAccountCreatedDate())
                .sex(registerRequest.getSex())
                .dateOfBirth(registerRequest.getDateOfBirth())
                .countryRegion(registerRequest.getCountryRegion())
                .localHash(Util.generateHash())
                .authMethod(AuthMethod.BASIC.toString())
                .hasUserBeenAskedAuthMethod(false)
                .build();
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        // Arrange
        when(userRepository.isUserAlreadyRegistered(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("mocked_jwt_token");
        UUID mockUUID = UUID.randomUUID();
        when(qrService.generateUUID()).thenReturn(mockUUID);

        // Act
        AuthenticationResponse response = authenticationService.registerUser(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("mocked_jwt_token", response.getToken());
        assertNotNull(response.getLocalHash());

        // Verify interactions
        verify(userRepository, times(1)).save(any(User.class));
        verify(qrService, times(1)).generateUUID();
        verify(qrService, times(1)).saveQrToDatabaseAndAssignToUser(eq(mockUUID), any(User.class));
    }

    @Test
    void shouldNotRegisterUserWhenUserAlreadyExists() {
        // Arrange
        when(userRepository.isUserAlreadyRegistered(registerRequest.getEmail())).thenReturn(Optional.of(mockUser));

        // Act
        AuthenticationResponse response = authenticationService.registerUser(registerRequest);

        // Assert
        assertNull(response);

        // Verify that save is never called
        verify(userRepository, never()).save(any(User.class));
    }
}