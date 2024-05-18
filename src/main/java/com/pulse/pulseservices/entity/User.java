package com.pulse.pulseservices.entity;

import com.pulse.pulseservices.enums.Role;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "account")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

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
    @Enumerated(EnumType.STRING)
    private Role role;

    @NotNull(message = "Account lock status is required")
    private boolean accountNonLocked;

    @NotNull(message = "Credentials expiration status is required")
    private boolean credentialsNonExpired;

    @NotNull(message = "Created date is required")
    private LocalDateTime createdDate;

    private LocalDateTime lastLoginDate;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
