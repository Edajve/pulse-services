package com.pulse.pulseservices.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountStats {
    private int totalContracts;
    private int totalContractsRevoked;
    private double successfulToRevokedRatio;
    private String mostConsentedPartner;
    private String mostRevokedPartner;
}
