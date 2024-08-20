package com.cockroachlabs.university;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

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

		Lists //
				.partition(bookIdsOnSale, chunkSize) //
				.forEach(uuids -> {
					LOG.debug("*** About to cancel [" + uuids.size() + "] books right now!");
					jdbcClient //
							.sql("""
									WITH original_prices AS (
										SELECT book_id, price
										FROM books_msrp
									)
									UPDATE book
									SET price = original_prices.price
									FROM original_prices
									WHERE book.book_id = original_prices.book_id
									AND book.book_id IN (:books_on_sale)
									""") //
							.param("books_on_sale", uuids) //
							.update();
				});

		LOG.debug("*** There are [" + booksOnSale().size() + "] books still on sale.");
	}
}
