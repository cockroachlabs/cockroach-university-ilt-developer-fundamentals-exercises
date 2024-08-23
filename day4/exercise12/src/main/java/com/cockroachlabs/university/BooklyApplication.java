package com.cockroachlabs.university;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BooklyApplication {

	public static void main(String[] args) {
		SpringApplication.run(BooklyApplication.class);
	}

	@Bean
	CommandLineRunner run(BookService bookService) {
		return args -> {
			bookService.clearOutOldBooks();
			bookService.loadUpBooks(50000);
		};
	}
}
