package com.pulse.pulseservices.model.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRequest {
    private String email;
    private String securityQuestion;
    private String securityAnswer;
    private String confirmPassword;
    private String newPassword;
}
