ALTER TABLE books ADD title_vec tsvector;

CREATE INDEX books_by_title_gin ON books USING gin(title_vec);

CREATE FUNCTION index_book_title()
RETURNS TRIGGER AS $$
BEGIN
  NEW.title_vec = to_tsvector(NEW.title);
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER index_books_by_title
BEFORE UPDATE OR INSERT ON books
FOR EACH ROW
EXECUTE PROCEDURE index_book_title();
