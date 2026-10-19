package com.example.smart_hospital_backend;

import org.springframework.boot.SpringApplication;

public class TestSmartHospitalBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(SmartHospitalBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
