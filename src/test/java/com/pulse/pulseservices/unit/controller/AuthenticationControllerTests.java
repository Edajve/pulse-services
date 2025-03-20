package com.pulse.pulseservices.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulse.pulseservices.config.auth.JwtService;
import com.pulse.pulseservices.controller.AuthenticationController;
import com.pulse.pulseservices.entity.User;
import com.pulse.pulseservices.enums.Country;
import com.pulse.pulseservices.enums.ResetPasswordStatus;
import com.pulse.pulseservices.enums.Role;
import com.pulse.pulseservices.enums.Sex;
import com.pulse.pulseservices.model.auth.AuthenticationRequest;
import com.pulse.pulseservices.model.auth.AuthenticationResponse;
import com.pulse.pulseservices.model.auth.AuthenticateWithPinRequest;
import com.pulse.pulseservices.model.auth.AuthenticationResponseForPin;
import com.pulse.pulseservices.model.auth.RegisterRequest;
import com.pulse.pulseservices.model.auth.RegisterResponse;
import com.pulse.pulseservices.model.auth.ResetPasswordRequest;
import com.pulse.pulseservices.repositories.UserRepository;
import com.pulse.pulseservices.service.AccountService;
import com.pulse.pulseservices.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.pulse.pulseservices.utils.Constants.V1_BASE_URL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
@ExtendWith(MockitoExtension.class)
@WithMockUser
public class AuthenticationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private AccountService accountService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private final static String BASE_URL = V1_BASE_URL + "/auth";

    @Test
    void AuthenticationController_Register_User() throws Exception {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .id(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("SecurePassword123")
                .role(Role.USER)
                .securityAnswer("MyPetName")
                .securityQuestion("What is your pet’s name?")
                .accountCreatedDate(LocalDateTime.now())
                .sex(Sex.MALE)
                .dateOfBirth("1990-01-01")
                .countryRegion(Country.US)
                .imageBytes(new byte[]{1, 2, 3})
                .build();

        AuthenticationResponse authenticationResponse = AuthenticationResponse.builder()
                .token("token")
                .localHash("generatedLocalHash")
                .build();

        // Mock repository to simulate "user does not exist" check
        when(userRepository.isUserAlreadyRegistered(request.getEmail())).thenReturn(Optional.empty());

        // Mock service to return the expected response
        when(authenticationService.registerUser(any(RegisterRequest.class)))
                .thenReturn(authenticationResponse);

        // Act & Assert
        mockMvc.perform(post(BASE_URL + "/register")
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(authenticationResponse.getToken()))
                .andExpect(jsonPath("$.localHash").value(authenticationResponse.getLocalHash()));
    }

    @Test
    void AuthenticationController_authenticate_Success() throws Exception {
        // Arrange
        AuthenticationRequest authenticationRequest = AuthenticationRequest.builder()
                .email("test@test.com")
                .password("pass")
                .build();

        AuthenticationResponse authenticationResponse = AuthenticationResponse.builder()
                .token("token")
                .localHash("generatedLocalHash")
                .build();

        RegisterResponse registerResponse = RegisterResponse.builder()
                .id(1L)
                .token(authenticationResponse)
                .localHash("generatedLocalHash")
                .build();

        when(authenticationService.authenticate(authenticationRequest)).thenReturn(registerResponse);

        // Act & Assert
        mockMvc.perform(post(BASE_URL + "/authenticate")
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(authenticationRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token.token").value(authenticationResponse.getToken()))
                .andExpect(jsonPath("$.token.localHash").value(authenticationResponse.getLocalHash()));
    }

    @Test
    void AuthenticationController_authenticateWithPin_Success() throws Exception {
        // Arrange
        AuthenticateWithPinRequest pinRequest = AuthenticateWithPinRequest.builder()
                .localHash("sampleLocalHash")
                .pin("1234")
                .build();

        AuthenticationResponseForPin authenticationResponseForPin = AuthenticationResponseForPin.builder()
                .token("sampleToken")
                .localHash("sampleLocalHash")
                .build();

        when(authenticationService.authenticateUserWithPin(any(AuthenticateWithPinRequest.class)))
                .thenReturn(Optional.of(authenticationResponseForPin));

        // Act & Assert
        mockMvc.perform(post(BASE_URL + "/authenticate/pin")
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(pinRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(authenticationResponseForPin.getToken()))
                .andExpect(jsonPath("$.localHash").value(authenticationResponseForPin.getLocalHash()));
    }

    @Test
    void AuthenticationController_authenticateWithPin_Failure() throws Exception {
        // Arrange
        AuthenticateWithPinRequest pinRequest = AuthenticateWithPinRequest.builder()
                .localHash("sampleLocalHash")
                .pin("1234")
                .build();

        when(authenticationService.authenticateUserWithPin(any(AuthenticateWithPinRequest.class)))
                .thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post(BASE_URL + "/authenticate/pin")
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(pinRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void AuthenticationController_resetPassword_Success() throws Exception {
        // Arrange
        ResetPasswordRequest resetPasswordRequest = ResetPasswordRequest.builder()
                .email("test@test.com")
                .securityQuestion("What is your pet’s name?")
                .securityAnswer("Fluffy")
                .newPassword("NewSecurePassword123")
                .build();

        when(accountService.isEmailInDataBase(resetPasswordRequest.getEmail())).thenReturn(true);
        when(accountService.verifySecurityQuestionAndAnswer(
                resetPasswordRequest.getSecurityQuestion(),
                resetPasswordRequest.getSecurityAnswer(),
                resetPasswordRequest.getEmail()
        )).thenReturn(ResetPasswordStatus.VERIFIED.getMessage());

        User mockUser = new User();
        mockUser.setId(1L);
        when(accountService.getUserByEmail(resetPasswordRequest.getEmail())).thenReturn(Optional.of(mockUser));

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/password/reset")
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(resetPasswordRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Successfully reset password"));
    }

    @Test
    void AuthenticationController_resetPassword_InvalidEmail() throws Exception {
        // Arrange
        ResetPasswordRequest resetPasswordRequest = ResetPasswordRequest.builder()
                .email("invalid@test.com")
                .securityQuestion("What is your pet’s name?")
                .securityAnswer("Fluffy")
                .newPassword("NewSecurePassword123")
                .build();

        when(accountService.isEmailInDataBase(resetPasswordRequest.getEmail())).thenReturn(false);

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/password/reset")
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(resetPasswordRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("This email is not in the database"));
    }

    @Test
    void AuthenticationController_resetPassword_IncorrectSecurityAnswer() throws Exception {
        // Arrange
        ResetPasswordRequest resetPasswordRequest = ResetPasswordRequest.builder()
                .email("test@test.com")
                .securityQuestion("What is your pet’s name?")
                .securityAnswer("WrongAnswer")
                .newPassword("NewSecurePassword123")
                .build();

        when(accountService.isEmailInDataBase(resetPasswordRequest.getEmail())).thenReturn(true);
        when(accountService.verifySecurityQuestionAndAnswer(
                resetPasswordRequest.getSecurityQuestion(),
                resetPasswordRequest.getSecurityAnswer(),
                resetPasswordRequest.getEmail()
        )).thenReturn(ResetPasswordStatus.INCORRECT_ANSWER.getMessage());

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/password/reset")
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(resetPasswordRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(ResetPasswordStatus.INCORRECT_ANSWER.getMessage()));
    }

    @Test
    void AuthenticationController_getAuthMethodByLocalHash_Success() throws Exception {
        // Arrange
        String localHash = "sampleLocalHash";
        String expectedAuthMethod = "PIN";

        when(accountService.getAuthMethodByLocalHash(localHash)).thenReturn(expectedAuthMethod);

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/authMethod")
                        .param("localHash", localHash)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedAuthMethod));
    }

    @Test
    void AuthenticationController_getAuthMethodByLocalHash_NotFound() throws Exception {
        // Arrange
        String localHash = "invalidLocalHash";

        when(accountService.getAuthMethodByLocalHash(localHash)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/authMethod")
                        .param("localHash", localHash)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No user found for this local hash"));
    }

    @Test
    void AuthenticationController_registerWithPin_Success() throws Exception {
        // Arrange
        String localHash = "validLocalHash";

        AuthenticationResponseForPin authenticationResponseForPin = AuthenticationResponseForPin.builder()
                .token("sampleToken")
                .localHash(localHash)
                .build();

        when(authenticationService.authenticateUserWithHash(localHash))
                .thenReturn(Optional.of(authenticationResponseForPin));

        // Act & Assert
        mockMvc.perform(post(BASE_URL + "/authenticate/hash/{localHash}", localHash)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(authenticationResponseForPin.getToken()))
                .andExpect(jsonPath("$.localHash").value(authenticationResponseForPin.getLocalHash()));
    }

    @Test
    void AuthenticationController_registerWithPin_Failure() throws Exception {
        // Arrange
        String localHash = "invalidLocalHash";

        when(authenticationService.authenticateUserWithHash(localHash)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post(BASE_URL + "/authenticate/hash/{localHash}", localHash)
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));
    }
}
