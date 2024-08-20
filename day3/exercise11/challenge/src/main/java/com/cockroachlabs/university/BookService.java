package com.cockroachlabs.university;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class BookService {

	private static final Logger LOG = LoggerFactory.getLogger(BookService.class);

	private final JdbcClient jdbcClient;

	BookService(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	@Transactional
	void putBooksOnSale() {
		jdbcClient //
				.sql("""
						UPDATE Book
						SET price = price - :sale * price
						WHERE format = :format
						""") //
				.param("sale", 0.1f) //
				.param("format", "ebook") //
				.update();
	}

	List<UUID> booksOnSale() {
		return jdbcClient //
				.sql("""
						SELECT book.book_id
						FROM book
						JOIN books_msrp
						ON book.book_id = books_msrp.book_id
						AND book.price != books_msrp.price
						""") //
				.query(UUID.class) //
				.list();
	}

	void cancelSaleInChunks(int chunkSize) {
		List<UUID> bookIdsOnSale = booksOnSale();

		LOG.debug("*** There are [" + bookIdsOnSale.size() + "] books on sale. Preparing to cancel!");

		// TODO: Using com.google.common.collect.Lists, partition bookIdsOnSale into chunkSize bites

		// TODO: Using Java 8's forEach() operation, process each "chunk" of UUIDs using jdbcClient.
		// TODO: With each "chunk" write a SQL UPDATE operation that uses a CTE and restores the original price of those
		// books.
		// NOTE: Be sure to supply that chunk's list of UUIDs as a parameter to the operation!

		LOG.debug("*** There are [" + booksOnSale().size() + "] books still on sale.");
	}
}
