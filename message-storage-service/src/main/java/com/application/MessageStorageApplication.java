package com.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MessageStorageApplication {

	public static void main(String[] args) {
		SpringApplication.run(MessageStorageApplication.class, args);
		System.out.println("✅ Application successfully launched 🚀🚀");
	}
}
