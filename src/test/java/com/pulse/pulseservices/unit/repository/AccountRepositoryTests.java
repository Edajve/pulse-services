package com.pulse.pulseservices.unit.repository;

import com.pulse.pulseservices.entity.User;
import com.pulse.pulseservices.enums.Country;
import com.pulse.pulseservices.enums.Role;
import com.pulse.pulseservices.enums.Sex;
import com.pulse.pulseservices.repositories.AccountRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class AccountRepositoryTests {

    @Autowired
    AccountRepository accountRepository;

    @Test
    public void AccountRepository_findByEmail_ReturnUser() {

        // Arrange
        User user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("johndoe@example.com")
                .password("securePassword123")
                .role(Role.USER)
                .securityQuestion("What is your favorite color?")
                .securityAnswer("Blue")
                .accountCreatedDate(LocalDateTime.now())
                .sex(Sex.MALE)
                .dateOfBirth("1990-05-15")
                .countryRegion(Country.united_states)
                .pinCode("1234")
                .localHash("hashedValue123")
                .authMethod("PIN")
                .hasUserBeenAskedAuthMethod(false)
                .build();

        accountRepository.save(user);

        // Act
        Optional<User> foundByEmail = accountRepository.findByEmail(user.getEmail());

        // Assert
        Assertions.assertThat(foundByEmail).isPresent();
        Assertions.assertThat(foundByEmail.get().getId()).isGreaterThan(0);
    }

    @Test
    public void AccountRepository_getUserByPinAndHash_ReturnUser() {
        String pinCode = "1234";
        String hash = "hashedValue123";

        // Arrange
        User user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("johndoe@example.com")
                .password("securePassword123")
                .role(Role.USER)
                .securityQuestion("What is your favorite color?")
                .securityAnswer("Blue")
                .accountCreatedDate(LocalDateTime.now())
                .sex(Sex.MALE)
                .dateOfBirth("1990-05-15")
                .countryRegion(Country.united_states)
                .pinCode(pinCode)
                .localHash(hash)
                .authMethod("PIN")
                .hasUserBeenAskedAuthMethod(false)
                .build();

        accountRepository.save(user);

        // Act
        Optional<User> foundByEmail = accountRepository.getUserByPinAndHash(pinCode, hash);

        // Assert
        Assertions.assertThat(foundByEmail).isPresent();
        Assertions.assertThat(foundByEmail.get().getId()).isGreaterThan(0);
    }

    @Test
    public void AccountRepository_getUserByHash_ReturnUser() {
        String hash = "hashedValue123";

        // Arrange
        User user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("johndoe@example.com")
                .password("securePassword123")
                .role(Role.USER)
                .securityQuestion("What is your favorite color?")
                .securityAnswer("Blue")
                .accountCreatedDate(LocalDateTime.now())
                .sex(Sex.MALE)
                .dateOfBirth("1990-05-15")
                .countryRegion(Country.united_states)
                .pinCode("1234")
                .localHash(hash)
                .authMethod("PIN")
                .hasUserBeenAskedAuthMethod(false)
                .build();

        accountRepository.save(user);

        // Act
        Optional<User> foundByEmail = accountRepository.getUserByHash(hash);

        // Assert
        Assertions.assertThat(foundByEmail).isPresent();
        Assertions.assertThat(foundByEmail.get().getId()).isGreaterThan(0);
    }

    @Test
    public void AccountRepository_getAllAccountsWithName_ReturnUser() {
        String name = "John";

        // Arrange
        User user = User.builder()
                .id(1L)
                .firstName(name)
                .lastName("Doe")
                .email("johndoe@example.com")
                .password("securePassword123")
                .role(Role.USER)
                .securityQuestion("What is your favorite color?")
                .securityAnswer("Blue")
                .accountCreatedDate(LocalDateTime.now())
                .sex(Sex.MALE)
                .dateOfBirth("1990-05-15")
                .countryRegion(Country.united_states)
                .pinCode("1234")
                .localHash("hashedValue123")
                .authMethod("PIN")
                .hasUserBeenAskedAuthMethod(false)
                .build();

        accountRepository.save(user);

        // Act
        List<Integer> userID = accountRepository.getAllAccountsWithName(name);

        // Assert
        Assertions.assertThat(userID.get(0)).isGreaterThan(0);
        Assertions.assertThat(userID.size()).isEqualTo(1);
    }

    @Test
    public void AccountRepository_getAllAccountsWithNameWithMultipleUsers_ReturnUser() {
        String name = "John";

        // Arrange
        User user = User.builder()
                .firstName(name)
                .lastName("Doe")
                .email("johndoe@example.com")
                .password("securePassword123")
                .role(Role.USER)
                .securityQuestion("What is your favorite color?")
                .securityAnswer("Blue")
                .accountCreatedDate(LocalDateTime.now())
                .sex(Sex.MALE)
                .dateOfBirth("1990-05-15")
                .countryRegion(Country.united_states)
                .pinCode("1234")
                .localHash("hashedValue123")
                .authMethod("PIN")
                .hasUserBeenAskedAuthMethod(false)
                .build();

        User anotherUser = User.builder()
                .firstName(name)
                .lastName("Doe")
                .email("johnhuhuhudoe@example.com")
                .password("securePasswhhuhord123")
                .role(Role.USER)
                .securityQuestion("What is your favorite color?")
                .securityAnswer("Bluhe")
                .accountCreatedDate(LocalDateTime.now())
                .sex(Sex.MALE)
                .dateOfBirth("1990-05-16")
                .countryRegion(Country.united_states)
                .pinCode("1236")
                .localHash("hashedValue126")
                .authMethod("BIOMETRIC")
                .hasUserBeenAskedAuthMethod(false)
                .build();

        accountRepository.save(user);
        accountRepository.save(anotherUser);

        // Act
        List<Integer> userID = accountRepository.getAllAccountsWithName(name);

        // Assert
        Assertions.assertThat(userID.size()).isEqualTo(2);
    }
}
