package com.pulse.pulseservices.enums;

public enum ContractStatus {
    PROGRESS // In the process of creating the contract
    , ACTIVE // Contract is active within the set duration
    , COMPLETED // Contract has been active the set duration, then closed successfully
    , CANCELLED // One or both users have revoked the contract during the set duration
}
