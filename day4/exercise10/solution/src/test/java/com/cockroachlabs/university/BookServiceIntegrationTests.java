package com.cockroachlabs.university;

import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class BookServiceIntegrationTests {

	@Autowired BookService bookService;

	@Autowired JdbcTemplate jdbcTemplate;

	private static final int NUMBER_OF_BOOKS = 1000;

	@BeforeEach
	void setUp() {
		jdbcTemplate //
				.update("""
						DELETE FROM book
						WHERE publish_date > NOW() - INTERVAL '1 DAY'
						""");
	}

	@Test
	void insertNewBooksWithJdbcBatchApi() throws SQLException {
		bookService.insertNewBooksWithJdbcBatchAPI(NUMBER_OF_BOOKS);
	}

	@Test
	void insertNewBooksWithUnnest() throws SQLException {
		bookService.insertNewBooksWithUnnest(NUMBER_OF_BOOKS);
	}
}
