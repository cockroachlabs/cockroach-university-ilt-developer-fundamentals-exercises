package com.cockroachlabs.university;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

interface BookRepository extends JpaRepository<Book, UUID> {

    @Modifying
    @Query("""
            UPDATE Book
            SET price = price - :sale * price
            WHERE format = :format
            """)
    void putBooksOnSale(String format, Float sale);

    @Modifying
    @Query(value = """
            WITH original_prices AS (
                SELECT book_id, price
                FROM books_msrp
                WHERE format = :format
            )
            UPDATE book
            SET price = original_prices.price
            FROM original_prices
            WHERE book.book_id = original_prices.book_id;
            """, nativeQuery = true)
    void cancelSale(String format);

    @Query(value = """
            SELECT book.*
            FROM book
            JOIN books_msrp
            ON book.book_id = books_msrp.book_id
            AND book.price != books_msrp.price
            """, nativeQuery = true)
    List<Book> booksOnSale();
}
