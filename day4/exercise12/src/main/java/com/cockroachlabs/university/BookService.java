package com.cockroachlabs.university;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
class BookService {

	private static final Logger LOG = LoggerFactory.getLogger(BookService.class);

	private final JdbcTemplate jdbcTemplate;

	private byte[] cockroachdbCover;

	BookService(JdbcTemplate jdbcTemplate, ResourceLoader resourceLoader) throws IOException {
		this.jdbcTemplate = jdbcTemplate;
		this.cockroachdbCover = resourceLoader.getResource("classpath:cockroachdb-the-definitive-guide-cover.jpg")
				.getContentAsByteArray();
	}

	void clearOutOldBooks() {
		jdbcTemplate.update("TRUNCATE book");
	}

	void loadUpBooks(int numOfNewBooks) {
		List<Book> newBooks = generateNewBooks(numOfNewBooks);

		LOG.debug("*** Loading [" + newBooks.size() + "] new books into the book table...");

		List<Object[]> listOfBooks = newBooks.stream() //
				.map(book -> new Object[] { //
						book.getTitle(), //
						book.getAuthor(), //
						book.getFormat(), //
						book.getCover() //
				}) //
				.toList();

		jdbcTemplate //
				.batchUpdate("""
                        INSERT INTO book (title, author, price, format, publish_date, cover)
                        VALUES (?, ?, 9.99, ?, now(), ?)
                        """, listOfBooks);
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
		return Stream.generate(() -> new Book(createTitle(), createAuthor(), createForm(), cockroachdbCover)) //
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

	private List<String> FIRST_NAMES = List.of("James", "Olivia", "John", "Emma", "Robert", "Ava", "Michael", "Sophia",
			"William", "Isabella", "David", "Mia", "Richard", "Charlotte", "Joseph", "Amelia", "Charles", "Harper", "Thomas",
			"Evelyn", "Christopher", "Abigail", "Daniel", "Emily", "Matthew", "Ella", "Anthony", "Elizabeth", "Mark", "Sofia",
			"Donald", "Avery", "Paul", "Scarlett", "Steven", "Grace", "Andrew", "Chloe", "Joshua", "Lily", "Kenneth",
			"Victoria", "Kevin", "Aria", "Brian", "Madison", "George", "Eleanor", "Edward", "Hannah", "Ronald", "Nora",
			"Timothy", "Ellie", "Jason", "Luna", "Jeffrey", "Zoe", "Ryan", "Penelope", "Jacob", "Layla", "Gary", "Riley",
			"Nicholas", "Zoey", "Eric", "Stella", "Jonathan", "Hazel", "Larry", "Aurora", "Justin", "Natalie", "Brandon",
			"Addison", "Frank", "Brooklyn", "Scott", "Leah", "Gregory", "Lucy", "Raymond", "Savannah", "Samuel", "Skylar",
			"Patrick", "Anna", "Jack", "Paisley");

	private List<String> LAST_NAMES = List.of("Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller",
			"Davis", "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas", "Taylor",
			"Moore", "Jackson", "Martin", "Lee", "Perez", "Thompson", "White", "Harris", "Sanchez", "Clark", "Ramirez",
			"Lewis", "Robinson", "Walker", "Young", "Allen", "King", "Wright", "Scott", "Torres", "Nguyen", "Hill", "Flores",
			"Green", "Adams", "Nelson", "Baker", "Hall", "Rivera", "Campbell", "Mitchell", "Carter", "Roberts");

	private List<String> FORMS = List.of("ebook", "audiobook", "paperback", "hardcover");

	private String createAuthor() {
		return FIRST_NAMES.get(randomGenerator.nextInt(FIRST_NAMES.size())) //
				+ " " //
				+ LAST_NAMES.get(randomGenerator.nextInt(LAST_NAMES.size()));
	}

	private String createForm() {
		return FORMS.get(randomGenerator.nextInt(FORMS.size()));
	}

	// private String createReview() {
	// return """
	// Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent sit amet tristique purus. Interdum et malesuada
	// fames ac ante ipsum primis in faucibus. Suspendisse dignissim ipsum vitae mauris ornare, non rutrum dolor
	// tincidunt. Etiam nec est condimentum, semper turpis ut, porttitor metus. Donec ut est in dolor dapibus fringilla
	// non ut dolor. Ut ut pulvinar orci. Praesent nec lectus a lorem cursus sollicitudin a quis massa. Sed malesuada
	// porttitor nunc, eu euismod quam aliquet vitae. Quisque dictum lectus a ante ultricies, quis mollis mauris faucibus.
	// Aenean scelerisque purus quis purus aliquet, eu mattis ligula cursus. Etiam mauris quam, cursus a ante quis, cursus
	// sollicitudin magna. Sed sagittis laoreet ultricies. Aenean ac elit ac ante volutpat vestibulum.
	// Cras in justo vestibulum, placerat lorem ut, tincidunt enim. Mauris sit amet mattis quam. Morbi ullamcorper tellus
	// eget dolor bibendum, vitae ultrices tellus lacinia. Proin finibus, tortor non facilisis mattis, magna dolor commodo
	// neque, a iaculis neque augue nec mauris. Cras convallis rutrum augue nec maximus. Sed mollis dui ut est posuere
	// tincidunt ut egestas augue. Nulla sit amet laoreet felis, in pulvinar lectus. Vivamus id posuere libero. Praesent
	// volutpat, purus in vulputate aliquet, lacus eros finibus tellus, et iaculis nibh ex quis justo. Sed eu dignissim
	// nulla. Morbi leo leo, hendrerit sit amet sapien in, euismod faucibus nunc. Sed condimentum sagittis eleifend. Sed
	// nec mauris id quam tempor tempor. Sed maximus diam eget volutpat malesuada.
	// Curabitur odio risus, congue et posuere eget, hendrerit id felis. Pellentesque eu bibendum augue, vel condimentum
	// arcu. Vivamus tortor orci, lobortis non dolor gravida, auctor pharetra nisi. Duis euismod facilisis orci eu
	// rhoncus. Integer eu lobortis nisi, vel tempor eros. Vestibulum ante ipsum primis in faucibus orci luctus et
	// ultrices posuere cubilia curae; Proin et elit nec justo posuere bibendum. Suspendisse maximus enim ante, vitae
	// scelerisque sem rhoncus vel. Proin nec libero sapien.
	// Donec iaculis mattis ultrices. Nam ante arcu, congue non turpis nec, venenatis interdum nisl. Curabitur id luctus
	// magna. Etiam nisi nibh, fringilla sit amet turpis quis, convallis ullamcorper augue. Aliquam cursus odio quis nisi
	// pretium semper. Nam accumsan nisl velit, ullamcorper tristique justo luctus gravida. Maecenas eu massa at mi
	// tristique consequat eget nec est. Vestibulum consectetur risus sed mauris euismod, sed vehicula tellus suscipit.
	// Nulla nisl libero, venenatis vitae luctus hendrerit, venenatis tristique lorem. Morbi mollis, nunc et porta
	// placerat, diam tortor congue risus, vitae tempus ex nisi interdum lectus. Nunc a dapibus ligula, ac rhoncus sem.
	// Fusce sit amet scelerisque magna. Vestibulum id facilisis est. Quisque et dapibus quam, vel aliquam massa. Proin
	// bibendum ligula nunc, ultrices rhoncus purus ornare non.
	// Proin et leo velit. Aenean euismod mi et enim finibus porta. Duis ornare orci a risus faucibus, tempor tempor dolor
	// mollis. Nulla mattis orci sed leo ultricies, ac semper sem malesuada. Nunc laoreet turpis leo, vel molestie nunc
	// interdum commodo. Nulla at vulputate justo. Phasellus tincidunt viverra lobortis.
	// Duis ut dolor lorem. Phasellus interdum turpis eget tellus auctor mollis. Maecenas convallis justo vitae lectus
	// luctus, finibus venenatis metus bibendum. Quisque sit amet laoreet diam. Fusce turpis odio, tincidunt in accumsan
	// quis, vehicula et erat. Sed fringilla nisl id euismod vulputate. Mauris id nulla aliquam, accumsan velit in,
	// porttitor erat. Aliquam mollis ac justo ac semper. Aliquam sodales fringilla scelerisque. Vestibulum eu magna et
	// purus placerat aliquet ac congue nibh. Curabitur felis urna, aliquam aliquam metus id, maximus venenatis felis.
	// Aliquam eget arcu elit. Nulla semper nulla vitae faucibus pulvinar. Aliquam mattis convallis mauris, at interdum
	// lectus rutrum eu. Nulla facilisi.
	// Maecenas elementum nunc vitae massa pellentesque, sit amet dictum est venenatis. Suspendisse ac erat quis augue
	// malesuada ultrices et maximus eros. In fringilla felis sed odio auctor iaculis. Fusce ut nisl eu dui gravida
	// porttitor. Integer eget rutrum erat. Duis vitae eros maximus, laoreet nisi ut, accumsan libero. Nulla accumsan est
	// quis nunc tincidunt porta at vel turpis. Integer ac nisl augue. Etiam nec semper est, vitae tempus tortor.
	// Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent sit amet tristique purus. Interdum et malesuada
	// fames ac ante ipsum primis in faucibus. Suspendisse dignissim ipsum vitae mauris ornare, non rutrum dolor
	// tincidunt. Etiam nec est condimentum, semper turpis ut, porttitor metus. Donec ut est in dolor dapibus fringilla
	// non ut dolor. Ut ut pulvinar orci. Praesent nec lectus a lorem cursus sollicitudin a quis massa. Sed malesuada
	// porttitor nunc, eu euismod quam aliquet vitae. Quisque dictum lectus a ante ultricies, quis mollis mauris faucibus.
	// Aenean scelerisque purus quis purus aliquet, eu mattis ligula cursus. Etiam mauris quam, cursus a ante quis, cursus
	// sollicitudin magna. Sed sagittis laoreet ultricies. Aenean ac elit ac ante volutpat vestibulum.
	// Cras in justo vestibulum, placerat lorem ut, tincidunt enim. Mauris sit amet mattis quam. Morbi ullamcorper tellus
	// eget dolor bibendum, vitae ultrices tellus lacinia. Proin finibus, tortor non facilisis mattis, magna dolor commodo
	// neque, a iaculis neque augue nec mauris. Cras convallis rutrum augue nec maximus. Sed mollis dui ut est posuere
	// tincidunt ut egestas augue. Nulla sit amet laoreet felis, in pulvinar lectus. Vivamus id posuere libero. Praesent
	// volutpat, purus in vulputate aliquet, lacus eros finibus tellus, et iaculis nibh ex quis justo. Sed eu dignissim
	// nulla. Morbi leo leo, hendrerit sit amet sapien in, euismod faucibus nunc. Sed condimentum sagittis eleifend. Sed
	// nec mauris id quam tempor tempor. Sed maximus diam eget volutpat malesuada.
	// Curabitur odio risus, congue et posuere eget, hendrerit id felis. Pellentesque eu bibendum augue, vel condimentum
	// arcu. Vivamus tortor orci, lobortis non dolor gravida, auctor pharetra nisi. Duis euismod facilisis orci eu
	// rhoncus. Integer eu lobortis nisi, vel tempor eros. Vestibulum ante ipsum primis in faucibus orci luctus et
	// ultrices posuere cubilia curae; Proin et elit nec justo posuere bibendum. Suspendisse maximus enim ante, vitae
	// scelerisque sem rhoncus vel. Proin nec libero sapien.
	// Donec iaculis mattis ultrices. Nam ante arcu, congue non turpis nec, venenatis interdum nisl. Curabitur id luctus
	// magna. Etiam nisi nibh, fringilla sit amet turpis quis, convallis ullamcorper augue. Aliquam cursus odio quis nisi
	// pretium semper. Nam accumsan nisl velit, ullamcorper tristique justo luctus gravida. Maecenas eu massa at mi
	// tristique consequat eget nec est. Vestibulum consectetur risus sed mauris euismod, sed vehicula tellus suscipit.
	// Nulla nisl libero, venenatis vitae luctus hendrerit, venenatis tristique lorem. Morbi mollis, nunc et porta
	// placerat, diam tortor congue risus, vitae tempus ex nisi interdum lectus. Nunc a dapibus ligula, ac rhoncus sem.
	// Fusce sit amet scelerisque magna. Vestibulum id facilisis est. Quisque et dapibus quam, vel aliquam massa. Proin
	// bibendum ligula nunc, ultrices rhoncus purus ornare non.
	// Proin et leo velit. Aenean euismod mi et enim finibus porta. Duis ornare orci a risus faucibus, tempor tempor dolor
	// mollis. Nulla mattis orci sed leo ultricies, ac semper sem malesuada. Nunc laoreet turpis leo, vel molestie nunc
	// interdum commodo. Nulla at vulputate justo. Phasellus tincidunt viverra lobortis.
	// Duis ut dolor lorem. Phasellus interdum turpis eget tellus auctor mollis. Maecenas convallis justo vitae lectus
	// luctus, finibus venenatis metus bibendum. Quisque sit amet laoreet diam. Fusce turpis odio, tincidunt in accumsan
	// quis, vehicula et erat. Sed fringilla nisl id euismod vulputate. Mauris id nulla aliquam, accumsan velit in,
	// porttitor erat. Aliquam mollis ac justo ac semper. Aliquam sodales fringilla scelerisque. Vestibulum eu magna et
	// purus placerat aliquet ac congue nibh. Curabitur felis urna, aliquam aliquam metus id, maximus venenatis felis.
	// Aliquam eget arcu elit. Nulla semper nulla vitae faucibus pulvinar. Aliquam mattis convallis mauris, at interdum
	// lectus rutrum eu. Nulla facilisi.
	// Maecenas elementum nunc vitae massa pellentesque, sit amet dictum est venenatis. Suspendisse ac erat quis augue
	// malesuada ultrices et maximus eros. In fringilla felis sed odio auctor iaculis. Fusce ut nisl eu dui gravida
	// porttitor. Integer eget rutrum erat. Duis vitae eros maximus, laoreet nisi ut, accumsan libero. Nulla accumsan est
	// quis nunc tincidunt porta at vel turpis. Integer ac nisl augue. Etiam nec semper est, vitae tempus tortor.
	// Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent sit amet tristique purus. Interdum et malesuada
	// fames ac ante ipsum primis in faucibus. Suspendisse dignissim ipsum vitae mauris ornare, non rutrum dolor
	// tincidunt. Etiam nec est condimentum, semper turpis ut, porttitor metus. Donec ut est in dolor dapibus fringilla
	// non ut dolor. Ut ut pulvinar orci. Praesent nec lectus a lorem cursus sollicitudin a quis massa. Sed malesuada
	// porttitor nunc, eu euismod quam aliquet vitae. Quisque dictum lectus a ante ultricies, quis mollis mauris faucibus.
	// Aenean scelerisque purus quis purus aliquet, eu mattis ligula cursus. Etiam mauris quam, cursus a ante quis, cursus
	// sollicitudin magna. Sed sagittis laoreet ultricies. Aenean ac elit ac ante volutpat vestibulum.
	// Cras in justo vestibulum, placerat lorem ut, tincidunt enim. Mauris sit amet mattis quam. Morbi ullamcorper tellus
	// eget dolor bibendum, vitae ultrices tellus lacinia. Proin finibus, tortor non facilisis mattis, magna dolor commodo
	// neque, a iaculis neque augue nec mauris. Cras convallis rutrum augue nec maximus. Sed mollis dui ut est posuere
	// tincidunt ut egestas augue. Nulla sit amet laoreet felis, in pulvinar lectus. Vivamus id posuere libero. Praesent
	// volutpat, purus in vulputate aliquet, lacus eros finibus tellus, et iaculis nibh ex quis justo. Sed eu dignissim
	// nulla. Morbi leo leo, hendrerit sit amet sapien in, euismod faucibus nunc. Sed condimentum sagittis eleifend. Sed
	// nec mauris id quam tempor tempor. Sed maximus diam eget volutpat malesuada.
	// Curabitur odio risus, congue et posuere eget, hendrerit id felis. Pellentesque eu bibendum augue, vel condimentum
	// arcu. Vivamus tortor orci, lobortis non dolor gravida, auctor pharetra nisi. Duis euismod facilisis orci eu
	// rhoncus. Integer eu lobortis nisi, vel tempor eros. Vestibulum ante ipsum primis in faucibus orci luctus et
	// ultrices posuere cubilia curae; Proin et elit nec justo posuere bibendum. Suspendisse maximus enim ante, vitae
	// scelerisque sem rhoncus vel. Proin nec libero sapien.
	// Donec iaculis mattis ultrices. Nam ante arcu, congue non turpis nec, venenatis interdum nisl. Curabitur id luctus
	// magna. Etiam nisi nibh, fringilla sit amet turpis quis, convallis ullamcorper augue. Aliquam cursus odio quis nisi
	// pretium semper. Nam accumsan nisl velit, ullamcorper tristique justo luctus gravida. Maecenas eu massa at mi
	// tristique consequat eget nec est. Vestibulum consectetur risus sed mauris euismod, sed vehicula tellus suscipit.
	// Nulla nisl libero, venenatis vitae luctus hendrerit, venenatis tristique lorem. Morbi mollis, nunc et porta
	// placerat, diam tortor congue risus, vitae tempus ex nisi interdum lectus. Nunc a dapibus ligula, ac rhoncus sem.
	// Fusce sit amet scelerisque magna. Vestibulum id facilisis est. Quisque et dapibus quam, vel aliquam massa. Proin
	// bibendum ligula nunc, ultrices rhoncus purus ornare non.
	// Proin et leo velit. Aenean euismod mi et enim finibus porta. Duis ornare orci a risus faucibus, tempor tempor dolor
	// mollis. Nulla mattis orci sed leo ultricies, ac semper sem malesuada. Nunc laoreet turpis leo, vel molestie nunc
	// interdum commodo. Nulla at vulputate justo. Phasellus tincidunt viverra lobortis.
	// Duis ut dolor lorem. Phasellus interdum turpis eget tellus auctor mollis. Maecenas convallis justo vitae lectus
	// luctus, finibus venenatis metus bibendum. Quisque sit amet laoreet diam. Fusce turpis odio, tincidunt in accumsan
	// quis, vehicula et erat. Sed fringilla nisl id euismod vulputate. Mauris id nulla aliquam, accumsan velit in,
	// porttitor erat. Aliquam mollis ac justo ac semper. Aliquam sodales fringilla scelerisque. Vestibulum eu magna et
	// purus placerat aliquet ac congue nibh. Curabitur felis urna, aliquam aliquam metus id, maximus venenatis felis.
	// Aliquam eget arcu elit. Nulla semper nulla vitae faucibus pulvinar. Aliquam mattis convallis mauris, at interdum
	// lectus rutrum eu. Nulla facilisi.
	// Maecenas elementum nunc vitae massa pellentesque, sit amet dictum est venenatis. Suspendisse ac erat quis augue
	// malesuada ultrices et maximus eros. In fringilla felis sed odio auctor iaculis. Fusce ut nisl eu dui gravida
	// porttitor. Integer eget rutrum erat. Duis vitae eros maximus, laoreet nisi ut, accumsan libero. Nulla accumsan est
	// quis nunc tincidunt porta at vel turpis. Integer ac nisl augue. Etiam nec semper est, vitae tempus tortor.
	// """;
	// }
}
