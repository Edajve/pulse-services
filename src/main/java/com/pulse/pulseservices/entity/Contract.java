package com.pulse.pulseservices.entity;

import com.pulse.pulseservices.enums.ContractStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "contract")
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull // Has to be at least one user when creating a contract
    @ManyToOne
    private User participantOne;

    @ManyToOne
    private User participantTwo;

    @NonNull
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @NonNull
    private ContractStatus status;

    @Column(name = "did_participant_one_revoke")
    private boolean didParticipantOneRevoke;

    @Column(name = "did_participant_two_revoke")
    private boolean didParticipantTwoRevoke;

    @Column(name = "participant_one_revoke_reason")
    private String participantOneRevokeContractReason;

    @Column(name = "participant_two_revoke_reason")
    private String participantTwoRevokeContractReason;

    private String contractCancelReason;

    @Column(name = "duration_minutes")
    private int durationMinutes;
}
