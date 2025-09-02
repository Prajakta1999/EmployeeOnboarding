package com.elearning.projects.elearn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Elearning {

	public static void main(String[] args) {
		SpringApplication.run(Elearning.class, args);
	}

}
