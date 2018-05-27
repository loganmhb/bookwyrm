CREATE TABLE books(
       id serial primary key,
       title text,
       ol_key text unique,
       publication_date text,
       created_at timestamptz not null default current_timestamp,
       update_at timestamptz
);

CREATE TRIGGER set_book_timestamp
BEFORE UPDATE ON books
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();
