package com.pulse.pulseservices.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrUpdateContractRequest {
    private int contractNumber;
    private String usersPassword;
    private Long scannerUserId;
    private Long scannieUserId;
}
