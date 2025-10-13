package com.example.health_care_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class HealthCareSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(HealthCareSystemApplication.class, args);
	}

	@GetMapping("/")
	public String apiRoot() {
		return "Welcome to Smart Healthcare System";
	}

}
