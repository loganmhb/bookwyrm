CREATE TABLE books(
       id serial primary key,
       title text,
       subtitle text,
       ol_key text unique,
       first_published text,
       created_at timestamptz not null default current_timestamp,
       updated_at timestamptz
);

CREATE TRIGGER set_book_timestamp
BEFORE UPDATE OR INSERT ON books
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();
