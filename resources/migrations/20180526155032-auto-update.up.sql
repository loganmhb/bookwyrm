CREATE FUNCTION trigger_set_timestamp()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_author_timestamp
BEFORE UPDATE ON authors
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();
