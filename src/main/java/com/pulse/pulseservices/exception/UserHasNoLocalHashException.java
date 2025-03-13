package com.pulse.pulseservices.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) // Sets the default response status
public class UserHasNoLocalHashException extends RuntimeException {
    public UserHasNoLocalHashException(String message) {
        super(message);
    }
}