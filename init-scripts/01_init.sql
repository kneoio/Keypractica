CREATE DATABASE kneox_db;

\connect kneox_db;

-- Table: _users
CREATE TABLE _users (
    id BIGSERIAL PRIMARY KEY,
    author BIGINT NOT NULL,
    reg_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_mod_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_mod_user BIGINT NOT NULL,
    login VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    default_lang INTEGER NOT NULL,
    status INTEGER NOT NULL,
    confirmation_code INTEGER,
    i_su BOOLEAN NOT NULL DEFAULT FALSE,
    ui_theme VARCHAR(50),
    time_zone VARCHAR(50)
);

-- Table: _modules
CREATE TABLE _modules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author BIGINT NOT NULL,
    is_on BOOLEAN NOT NULL DEFAULT TRUE,
    identifier VARCHAR(255) UNIQUE NOT NULL,
    reg_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_mod_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_mod_user BIGINT NOT NULL,
    loc_name JSONB,
    loc_descr JSONB
);

-- Table: _user_modules
CREATE TABLE _user_modules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id BIGINT NOT NULL REFERENCES _users(id) ON DELETE CASCADE,
    module_id UUID NOT NULL REFERENCES _modules(id) ON DELETE CASCADE,
    is_on BOOLEAN NOT NULL DEFAULT TRUE
);

-- Table: _roles
CREATE TABLE _roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author BIGINT NOT NULL,
    reg_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_mod_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_mod_user BIGINT NOT NULL,
    identifier VARCHAR(255) NOT NULL UNIQUE,
    loc_name JSONB,
    loc_descr JSONB
);

-- Table: _user_roles
CREATE TABLE _user_roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id BIGINT NOT NULL REFERENCES _users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES _roles(id) ON DELETE CASCADE,
    is_on BOOLEAN NOT NULL DEFAULT TRUE
);

-- Table: _langs
CREATE TABLE _langs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    reg_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_mod_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_mod_user BIGINT NOT NULL,
    position INTEGER NOT NULL,
    is_on BOOLEAN NOT NULL DEFAULT TRUE,
    loc_name JSONB
);
