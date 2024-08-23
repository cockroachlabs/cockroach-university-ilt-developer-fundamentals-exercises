package com.cockroachlabs.university;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
class BookService {

	private final JdbcTemplate jdbcTemplate;

	BookService(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	void insertNewBooksWithJdbcBatchAPI(int numOfNewBooks) {

		List<Book> newBooks = generateNewBooks(numOfNewBooks);

		List<Object[]> listOfBooks = newBooks.stream() //
				.map(book -> new Object[] { //
						book.getTitle(), //
						book.getAuthor() }) //
				.toList();

		jdbcTemplate //
				.batchUpdate("""
						INSERT INTO book (title, author, price, format, publish_date)
						VALUES (?, ?, 9.99, 'ebook', now())
						""", listOfBooks);
	}

	void insertNewBooksWithUnnest(int numOfNewBooks) throws SQLException {

		List<Book> newBooks = generateNewBooks(numOfNewBooks);

		java.sql.Array titleArray = connection().createArrayOf( //
				"VARCHAR", //
				newBooks.stream() //
						.map(Book::getTitle) //
						.toArray());
		java.sql.Array authorArray = connection().createArrayOf( //
				"VARCHAR", //
				newBooks.stream() //
						.map(Book::getAuthor) //
						.toArray());

		jdbcTemplate //
				.update("""
						INSERT INTO book (title, author, price, format, publish_date)
						SELECT new_books.title, new_books.author, 9.99, 'ebook', now()
						FROM (SELECT unnest(?) as title, unnest(?) as author) as new_books
						""", titleArray, authorArray);
	}

	private Connection connection() {
		try {
			return jdbcTemplate.getDataSource().getConnection();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	//
	// Generate random collection of books and authors
	//

	private List<Book> generateNewBooks(int numOfNewBooks) {
		return Stream.generate(() -> new Book(createTitle(), createAuthor())) //
				.limit(numOfNewBooks) //
				.toList();
	}

	private static final Random randomGenerator = new Random();

	private static final List<String> OPENING = List.of( //
			"Understanding", //
			"Mastering", //
			"Getting Going With", //
			"Introduction to" //
	);

	private static final List<String> TECH = List.of(" Spring", " Jakarta EE", " Python", " Go", " C++", " Rust");

	private static final List<String> VERSION = List.of("", " 2.0", " 3.0", " 4.0", " 5.0", " 6.1", " 7.2", " 11.0.1");

	private static final List<String> EDITION = List.of("", "", "", " 2nd Edition", " 2nd Edition", " 3rd Edition",
			" 4th Edition");

	private String createTitle() {
		return OPENING.get(randomGenerator.nextInt(OPENING.size())) //
				+ TECH.get(randomGenerator.nextInt(TECH.size())) //
				+ VERSION.get(randomGenerator.nextInt(VERSION.size())) //
				+ EDITION.get(randomGenerator.nextInt(EDITION.size()));
	}

	List<String> FIRST_NAMES = List.of("James", "Olivia", "John", "Emma", "Robert", "Ava", "Michael", "Sophia", "William",
			"Isabella", "David", "Mia", "Richard", "Charlotte", "Joseph", "Amelia", "Charles", "Harper", "Thomas", "Evelyn",
			"Christopher", "Abigail", "Daniel", "Emily", "Matthew", "Ella", "Anthony", "Elizabeth", "Mark", "Sofia", "Donald",
			"Avery", "Paul", "Scarlett", "Steven", "Grace", "Andrew", "Chloe", "Joshua", "Lily", "Kenneth", "Victoria",
			"Kevin", "Aria", "Brian", "Madison", "George", "Eleanor", "Edward", "Hannah", "Ronald", "Nora", "Timothy",
			"Ellie", "Jason", "Luna", "Jeffrey", "Zoe", "Ryan", "Penelope", "Jacob", "Layla", "Gary", "Riley", "Nicholas",
			"Zoey", "Eric", "Stella", "Jonathan", "Hazel", "Larry", "Aurora", "Justin", "Natalie", "Brandon", "Addison",
			"Frank", "Brooklyn", "Scott", "Leah", "Gregory", "Lucy", "Raymond", "Savannah", "Samuel", "Skylar", "Patrick",
			"Anna", "Jack", "Paisley");

	List<String> LAST_NAMES = List.of("Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
			"Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas", "Taylor", "Moore",
			"Jackson", "Martin", "Lee", "Perez", "Thompson", "White", "Harris", "Sanchez", "Clark", "Ramirez", "Lewis",
			"Robinson", "Walker", "Young", "Allen", "King", "Wright", "Scott", "Torres", "Nguyen", "Hill", "Flores", "Green",
			"Adams", "Nelson", "Baker", "Hall", "Rivera", "Campbell", "Mitchell", "Carter", "Roberts");

	private String createAuthor() {
		return FIRST_NAMES.get(randomGenerator.nextInt(FIRST_NAMES.size())) //
				+ " " //
				+ LAST_NAMES.get(randomGenerator.nextInt(LAST_NAMES.size()));
	}
}
