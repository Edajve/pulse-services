package com.pulse.pulseservices.controllers;

import com.pulse.pulseservices.models.Account;
import com.pulse.pulseservices.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("register/account")
    public ResponseEntity<String> createUser(@RequestBody Account user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        accountRepository.save(user);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
