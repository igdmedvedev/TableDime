create function lowercase_column() returns trigger
    language plpgsql
as
$$
BEGIN
    IF NEW.category IS NOT NULL THEN
        NEW.category = LOWER(NEW.category);
    END IF;
    RETURN NEW;
END;
$$;


create sequence income_seq;
CREATE TABLE income (
    id integer DEFAULT nextval('income_seq'::regclass) PRIMARY KEY,
    amt numeric,
    date date,
    category character varying(200)
);
CREATE TRIGGER enforce_lowercase BEFORE INSERT OR UPDATE ON public.income FOR EACH ROW EXECUTE FUNCTION public.lowercase_column();

create sequence purchase_seq;
CREATE TABLE purchase (
    id integer DEFAULT nextval('purchase_seq'::regclass) PRIMARY KEY,
    amt numeric NOT NULL,
    date date NOT NULL,
    category character varying(200) NOT NULL,
    comments character varying
);
CREATE TRIGGER enforce_lowercase BEFORE INSERT OR UPDATE ON public.purchase FOR EACH ROW EXECUTE FUNCTION public.lowercase_column();