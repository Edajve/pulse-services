package com.pulse.pulseservices.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull(message = "Username is required")
    @Size(min = 1, message = "Username cannot be empty")
    private String username;

    @NotNull(message = "Password is required")
    @Size(min = 1, message = "Password cannot be empty")
    private String password;

    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "First name is required")
    @Size(min = 1, message = "First name cannot be empty")
    private String firstName;

    @NotNull(message = "Last name is required")
    @Size(min = 1, message = "Last name cannot be empty")
    private String lastName;

    @NotNull(message = "Enabled status is required")
    private boolean enabled;

    @NotNull(message = "Account expiration status is required")
    private boolean accountNonExpired;

    @NotNull(message = "Account expiration status is required")
    private String role;

    @NotNull(message = "Account lock status is required")
    private boolean accountNonLocked;

    @NotNull(message = "Credentials expiration status is required")
    private boolean credentialsNonExpired;

    @NotNull(message = "Created date is required")
    private LocalDateTime createdDate;

    private LocalDateTime lastLoginDate;
    // Add more fields as needed based on your application requirements
}