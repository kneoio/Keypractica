CREATE TABLE phrases
(
	id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
	reg_date TIMESTAMP with TIME zone not null,
	title VARCHAR(128),
	author INT not null,
	last_mod_date TIMESTAMP with TIME zone not null,
	last_mod_user INT not null,
	base VARCHAR(256),
	translation VARCHAR(256),
	base_pronunciation VARCHAR(512),
	translation_pronunciation VARCHAR(512),
	base_type VARCHAR(64)
);

CREATE INDEX idx_phrases_value ON phrases(base);

CREATE TABLE phrase_rls
(
     entity_id uuid NOT NULL,
     reader INT NOT NULL,
     reading_time TIMESTAMP WITH TIME ZONE,
     is_edit_allowed INT NOT NULL,
     PRIMARY KEY (entity_id, reader)
);


CREATE TABLE phrase_labels
(
     entity_id uuid NOT NULL,
     label_id uuid NOT NULL,
     PRIMARY KEY (entity_id, label_id)
);


CREATE TABLE languages
(
	  id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
	  reg_date TIMESTAMP WITH TIME ZONE NOT NULL,
	  title VARCHAR(128),
	  author INT NOT NULL,
	  last_mod_date TIMESTAMP WITH TIME ZONE NOT NULL,
	  last_mod_user INT NOT NULL,
	  rank INT NOT NULL DEFAULT 999,
	  is_active BOOLEAN NOT NULL DEFAULT TRUE,
	  name VARCHAR(512) UNIQUE,
	  localized_names jsonb,
	  code VARCHAR(3)
);

CREATE TABLE labels
(
	  id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
	  reg_date TIMESTAMP WITH TIME ZONE NOT NULL,
	  title VARCHAR(128),
	  author INT NOT NULL,
	  last_mod_date TIMESTAMP WITH TIME ZONE NOT NULL,
	  last_mod_user INT NOT NULL,
	  rank INT NOT NULL DEFAULT 999,
	  is_active BOOLEAN NOT NULL DEFAULT TRUE,
	  name VARCHAR(64) UNIQUE,
	  localized_names jsonb,
	  category VARCHAR(64),
	  color CHAR(7)
);


INSERT INTO public.labels (id, reg_date, title, author, last_mod_date, last_mod_user, "rank", is_active, "name", localized_names, category, color)
VALUES(uuid_generate_v4(), now(), 'important', 0, now(), 0, 999, true, 'important', '{}', 'basic', 'red');
