package com.pulse.pulseservices.exception;

public class MultipleUsersFoundException extends RuntimeException {
    public MultipleUsersFoundException(String message) {
        super(message);
    }
}
