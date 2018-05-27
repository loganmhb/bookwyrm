DROP TRIGGER index_books_by_title ON books;

DROP INDEX books_by_title_gin;

DROP FUNCTION index_book_title();

ALTER TABLE books DROP title_vec;
