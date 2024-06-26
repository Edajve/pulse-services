package com.pulse.pulseservices.model.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrUpdateContractRequest {
    private Integer contractNumber;
    private String usersPassword;
    private Long scannerUserId;
    private Long scannieUserId;
}
