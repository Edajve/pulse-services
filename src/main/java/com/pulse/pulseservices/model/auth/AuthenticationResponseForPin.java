package com.pulse.pulseservices.model.auth;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthenticationResponseForPin {
    private String token;
    private Long id;
    private String localHash;
}