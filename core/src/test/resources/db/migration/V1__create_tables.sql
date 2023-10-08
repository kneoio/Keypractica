CREATE TABLE _users (
	id serial4 NOT NULL,
	default_lang int4 NULL,
	email varchar(64) NULL,
	i_su bool NULL,
	login varchar(64) NULL,
	pwd varchar(255) NULL,
	pwdhash varchar(255) NULL,
	reg_date timestamp NOT NULL,
	status int4 NULL,
	messagingtype int4 NULL,
	time_zone int4 NULL,
	confirmation_code int4 NULL,
	CONSTRAINT "_users_login_key" UNIQUE (login),
	CONSTRAINT "_users_pkey" PRIMARY KEY (id)
);

INSERT INTO _users (default_lang, email, i_su, login, pwd, pwdhash, reg_date, status, messagingtype, time_zone, confirmation_code) VALUES(0, 'test1@test.kz', false, 'test1', '123', '123', now(), 0, 0, 0, 0);
INSERT INTO _users (default_lang, email, i_su, login, pwd, pwdhash, reg_date, status, messagingtype, time_zone, confirmation_code) VALUES(0, 'test2@test.kz', false, 'test2', '123', '123', now(), 0, 0, 0, 0);
INSERT INTO _users (default_lang, email, i_su, login, pwd, pwdhash, reg_date, status, messagingtype, time_zone, confirmation_code) VALUES(0, 'test3@test.kz', false, 'test3', '123', '123', now(), 0, 0, 0, 0);