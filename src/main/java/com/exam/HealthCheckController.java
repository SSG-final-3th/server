package com.exam;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HealthCheckController {

	@GetMapping
	public String healthCheck() {
		return "OK";
	}
	@GetMapping("/actuator/health")
	public String customHealthCheck() {
		return "UP";
	}
}
