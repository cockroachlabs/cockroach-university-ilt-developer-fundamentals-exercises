package com.cockroachlabs.university;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BookServiceIntegrationTests {

	private static final int CHUNK_SIZE = 1000;

	@Autowired BookService bookService;

	@BeforeEach
	void setUp() {
		bookService.putBooksOnSale();
	}

	@Test
	void cancelSaleUsingGoogleCollections() {
		assertThat(bookService.booksOnSale().size()).isGreaterThan(24000);

		bookService.cancelSaleInChunks(CHUNK_SIZE);

		assertThat(bookService.booksOnSale().size()).isEqualTo(0);
	}
}
