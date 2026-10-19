package com.example.smart_hospital_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class SmartHospitalBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
