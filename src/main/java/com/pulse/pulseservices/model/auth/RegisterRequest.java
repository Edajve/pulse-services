package com.pulse.pulseservices.model.auth;

import com.pulse.pulseservices.enums.Country;
import com.pulse.pulseservices.enums.Role;
import com.pulse.pulseservices.enums.Sex;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;
    private LocalDateTime accountCreatedDate;
    @Enumerated(EnumType.STRING)
    private Sex sex;
    private String dateOfBirth;
    private Country countryRegion;
}
