package com.pulse.pulseservices.model.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This JSON is for returning on the authenticate method,
 * it will return the users token and their id once logging into the system
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse {
    private Long id;
    private AuthenticationResponse token;
    private String localHash;
}
