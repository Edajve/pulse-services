package com.pulse.pulseservices.services;

import com.pulse.pulseservices.models.Account;
import com.pulse.pulseservices.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountDetailService implements UserDetailsService {

    @Autowired
    private AccountRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Account> account = repository.findByUsername(username);

        if (account.isPresent()) {
            Account accountObject = account.get();
            return User.builder()
                    .username(accountObject.getUsername())
                    .password(accountObject.getPassword())
                    .roles(getRoles(accountObject))
                    .build();
        } else {
            throw new UsernameNotFoundException("Username: {" + username + "} is not found");
        }
    }

    private String[] getRoles(Account account) {
        if (account.getRole() == null) return new String[]{"USER"};
        return account.getRole().split(",");
    }
}