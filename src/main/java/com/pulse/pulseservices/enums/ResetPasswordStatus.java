package com.pulse.pulseservices.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResetPasswordStatus {
    INCORRECT_QUESTION("Security Question is incorrect"),
    INCORRECT_ANSWER("Security Answer is incorrect"),
    VERIFIED("verified");

    private final String message;
}