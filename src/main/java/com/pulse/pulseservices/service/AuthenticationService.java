package com.pulse.pulseservices.service;

import com.pulse.pulseservices.config.auth.JwtService;
import com.pulse.pulseservices.entity.User;
import com.pulse.pulseservices.enums.AuthMethod;
import com.pulse.pulseservices.enums.Role;
import com.pulse.pulseservices.exception.MultipleUsersFoundException;
import com.pulse.pulseservices.exception.UserHasNoLocalHashException;
import com.pulse.pulseservices.exception.UserNotFoundException;
import com.pulse.pulseservices.model.auth.AuthenticationRequest;
import com.pulse.pulseservices.model.auth.AuthenticationResponse;
import com.pulse.pulseservices.model.auth.RegisterRequest;
import com.pulse.pulseservices.model.auth.RegisterResponse;
import com.pulse.pulseservices.repositories.UserRepository;
import com.pulse.pulseservices.utils.Util;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final QrService qrService;

    public AuthenticationResponse register(RegisterRequest request) {

        boolean doesUserExist = userRepository.isUserAlreadyRegistered(request.getEmail()).isPresent();

        // If user already exists this will return true, therefore we return null in method
        if (doesUserExist) return null;

        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .securityAnswer(request.getSecurityAnswer())
                .securityQuestion(request.getSecurityQuestion())
                .accountCreatedDate(request.getAccountCreatedDate())
                .sex(request.getSex())
                .dateOfBirth(String.valueOf(request.getDateOfBirth()))
                .countryRegion(request.getCountryRegion())
                .localHash(Util.generateHash())
                .authMethod(String.valueOf(AuthMethod.BASIC)) // Default value
                .build();

        userRepository.save(user);

        // Generate UUID and insert it in qr table
        UUID uuid = qrService.generateUUID();
        qrService.saveQrToDatabaseAndAssignToUser(uuid, user);

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .localHash(user.getLocalHash())
                .build();
    }

    public User getAccountByEmail(String email) {
        Optional<User> accounts = userRepository.findByEmail(email);

        if (accounts.isEmpty())
            throw new UserNotFoundException("No user account found for email: " + email);

        if (accounts.stream().toList().size() > 1)
            throw new MultipleUsersFoundException("Multiple user accounts found for email: " + email);

        return accounts.get();
    }

    public RegisterResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail()
                        , request.getPassword()
                )
        );

        // If the code gets to this point it means the user is authenticated
        User user = getAccountByEmail(request.getEmail());

        if (StringUtils.isEmpty(user.getLocalHash())) {
            throw new UserHasNoLocalHashException("This user has no local hash in the database");
        }

        var jwtToken = jwtService.generateToken(user);

        var token = AuthenticationResponse.builder()
                .token(jwtToken)
                .build();

        return RegisterResponse.builder()
                .id(Long.valueOf(user.getId()))
                .token(token)
                .localHash(user.getLocalHash())
                .build();
    }
}
