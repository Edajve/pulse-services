package com.pulse.pulseservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PulseServicesApplication {

	public static void main(String[] args) {
		SpringApplication.run(PulseServicesApplication.class, args);
	}

}
