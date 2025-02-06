package org.service.userservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@SpringBootApplication
public class UserServicesApplication {

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder
				.setConnectTimeout(Duration.ofSeconds(5))  // Set connection timeout
				.setReadTimeout(Duration.ofSeconds(5))     // Set read timeout
				.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(UserServicesApplication.class, args);
	}

}
