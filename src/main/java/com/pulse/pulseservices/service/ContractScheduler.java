package com.pulse.pulseservices.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ContractScheduler {

    @Autowired
    private  ContractService contractService;

    @Scheduled(fixedRate = 60000) // Every 60 seconds
    public void checkAndUpdateContracts() {
        System.out.println("Running Schedule for out of time contracts");

    }
}
