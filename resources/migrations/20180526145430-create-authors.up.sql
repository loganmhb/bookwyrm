CREATE TABLE authors(
       id serial primary key,
       ol_key text unique,
       name text,
       birth_date text,
       death_date text,
       created_at timestamp with time zone not null default current_timestamp,
       updated_at timestamp with time zone not null default current_timestamp
);
