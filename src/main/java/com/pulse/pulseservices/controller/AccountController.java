package com.pulse.pulseservices.controller;

import com.pulse.pulseservices.entity.User;
import com.pulse.pulseservices.model.AccountStats;
import com.pulse.pulseservices.model.auth.ResetPasswordRequest;
import com.pulse.pulseservices.model.contract.UpdateContractRequest;
import com.pulse.pulseservices.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/{accountId}")
    public ResponseEntity<User> getAccountById(@PathVariable Long accountId) {
        return ResponseEntity.ok(accountService.getAccountById(accountId));
    }

    @GetMapping("stats/{accountId}")
    public ResponseEntity<AccountStats> getAccountStatsById(@PathVariable Long accountId) {
        // make sure the user exists
        User account = accountService.getAccountById(accountId);
        AccountStats accountStats = accountService.getStats(Long.valueOf(account.getId()));
        return ResponseEntity.ok(accountStats);
    }

    @PutMapping("/password/reset/{accountId}")
    public ResponseEntity resetUserPassword(
            @PathVariable Long accountId
            , @RequestBody ResetPasswordRequest resetPasswordRequest
    ) {
        // make sure the user exists
        try {
            User account = accountService.getAccountById(accountId);
            accountService.resetPassword(account.getId(), resetPasswordRequest.getPassword());
            return ResponseEntity.ok("Successfully reset password");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
