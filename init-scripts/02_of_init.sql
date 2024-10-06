\connect kneox_db;

CREATE TABLE __org_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author BIGINT NOT NULL,
    reg_date TIMESTAMP NOT NULL DEFAULT now(),
    last_mod_user BIGINT NOT NULL,
    last_mod_date TIMESTAMP NOT NULL DEFAULT now(),
    identifier VARCHAR(255) NOT NULL,
    loc_name JSONB
);

CREATE TABLE __organizations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author BIGINT NOT NULL,
    reg_date TIMESTAMP NOT NULL DEFAULT now(),
    last_mod_user BIGINT NOT NULL,
    last_mod_date TIMESTAMP NOT NULL DEFAULT now(),
    identifier VARCHAR(255) NOT NULL,
    org_category_id UUID NOT NULL,
    biz_id VARCHAR(255),
    rank INT,
    loc_name JSONB,
    FOREIGN KEY (org_category_id) REFERENCES __org_categories(id) ON DELETE CASCADE
);

CREATE TABLE __departments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author BIGINT NOT NULL,
    reg_date TIMESTAMP NOT NULL DEFAULT now(),
    last_mod_user BIGINT NOT NULL,
    last_mod_date TIMESTAMP NOT NULL DEFAULT now(),
    identifier VARCHAR(255) NOT NULL,
    type_id UUID NOT NULL,
    organization_id UUID NOT NULL,
    lead_department_id UUID,
    rank INT,
    loc_name JSONB,
    FOREIGN KEY (organization_id) REFERENCES __organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (lead_department_id) REFERENCES __departments(id) ON DELETE SET NULL
);

CREATE TABLE __positions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author BIGINT NOT NULL,
    reg_date TIMESTAMP NOT NULL DEFAULT now(),
    last_mod_user BIGINT NOT NULL,
    last_mod_date TIMESTAMP NOT NULL DEFAULT now(),
    identifier VARCHAR(255) NOT NULL,
    rank INT NOT null DEFAULT 99,
    loc_name JSONB
);

CREATE TABLE __employees (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author BIGINT NOT NULL,
    reg_date TIMESTAMP NOT NULL DEFAULT now(),
    last_mod_user BIGINT NOT NULL,
    last_mod_date TIMESTAMP NOT NULL DEFAULT now(),
    status INT NOT NULL,
    birth_date DATE,
    department_id UUID NOT NULL,
    organization_id UUID NOT NULL,
    position_id UUID NOT NULL,
    user_id BIGINT NOT NULL,
    rank INT,
    loc_name JSONB,
    phone VARCHAR(20),
    FOREIGN KEY (department_id) REFERENCES __departments(id) ON DELETE CASCADE,
    FOREIGN KEY (organization_id) REFERENCES __organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (position_id) REFERENCES __positions(id) ON DELETE SET NULL
);

CREATE TABLE __labels (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author BIGINT NOT NULL,
    reg_date TIMESTAMP NOT NULL DEFAULT now(),
    last_mod_user BIGINT NOT NULL,
    last_mod_date TIMESTAMP NOT NULL DEFAULT now(),
    identifier VARCHAR(255) NOT NULL,
    color VARCHAR(50),
    font_color VARCHAR(50),
    category VARCHAR(100),
    parent UUID,
    hidden BOOLEAN DEFAULT FALSE,
    loc_name JSONB,
    FOREIGN KEY (parent) REFERENCES __labels(id) ON DELETE SET NULL
);

CREATE TABLE __task_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author BIGINT NOT NULL,
    reg_date TIMESTAMP NOT NULL DEFAULT now(),
    last_mod_user BIGINT NOT NULL,
    last_mod_date TIMESTAMP NOT NULL DEFAULT now(),
    identifier VARCHAR(255) NOT NULL,
    loc_name JSONB
);