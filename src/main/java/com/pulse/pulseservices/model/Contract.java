package com.pulse.pulseservices.model;

import com.pulse.pulseservices.entity.User;
import com.pulse.pulseservices.enums.ContractStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Contract {
    private Long id;
    private User participantOne;
    private User participantTwo;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ContractStatus status;
    private boolean didParticipantOneRevoke;
    private boolean didParticipantTwoRevoke;
    private String participantOneRevokeContractReason;
    private String participantTwoRevokeContractReason;
    private String contractCancelReason;
    private int durationMinutes;
}
