package com.cockroachlabs.university;

import java.time.LocalDate;
import java.util.UUID;

class Book {

	private UUID bookId;

	private String title;

	private String author;

	private Float price;

	private String format;

	private LocalDate publishDate;

	private byte[] cover;

	Book(UUID bookId, String title, String author, Float price, String format, LocalDate publishDate, byte[] cover) {
		this.bookId = bookId;
		this.title = title;
		this.author = author;
		this.price = price;
		this.format = format;
		this.publishDate = publishDate;
		this.cover = cover;
	}

	Book(String title, String author, String format, byte[] cover) {
		this(null, title, author, null, format, null, cover);
	}

	public UUID getBookId() {
		return bookId;
	}

	public void setBookId(UUID bookId) {
		this.bookId = bookId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Float getPrice() {
		return price;
	}

	public void setPrice(Float price) {
		this.price = price;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public LocalDate getPublishDate() {
		return publishDate;
	}

	public void setPublishDate(LocalDate publishDate) {
		this.publishDate = publishDate;
	}

	public byte[] getCover() {
		return cover;
	}

	public void setCover(byte[] cover) {
		this.cover = cover;
	}

}
