package com.teknisio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TeknisioBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(TeknisioBackendApplication.class, args);
	}

}
