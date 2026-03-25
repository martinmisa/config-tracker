package com.ohpen.configtracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class ConfigTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigTrackerApplication.class, args);
	}

}
