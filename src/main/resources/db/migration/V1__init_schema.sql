create table organization (
    id serial primary key,
    name VARCHAR(100),
    status VARCHAR(1),
    external_id VARCHAR(100),
    created_on timestamp with time zone,
    created_by VARCHAR(100),
    updated_on timestamp with time zone,
    updated_by VARCHAR(100)
);

create table operation_schema (
    id serial primary key,
    name VARCHAR(100),
    value TEXT,
    schema_key VARCHAR(100),
    path VARCHAR(200),
    type VARCHAR(10),
    organisation_id integer,
    created_on timestamp with time zone,
    created_by VARCHAR(100),
    updated_on timestamp with time zone,
    updated_by VARCHAR(100)
);

create table rule (
    id serial primary key,
    description VARCHAR(200),
    status VARCHAR(1),
    interval_type VARCHAR(1),
    interval_value INT,
    start_date date,
    end_date date,
    operation_schema_id integer,
    created_on timestamp with time zone,
    created_by VARCHAR(100),
    updated_on timestamp with time zone,
    updated_by VARCHAR(100)
);

create table criteria (
    id serial primary key,
    name VARCHAR(100),
    value VARCHAR(200),
    rule_id integer,
    operation_schema_id integer,
    created_on timestamp with time zone,
    created_by VARCHAR(100),
    updated_on timestamp with time zone,
    updated_by VARCHAR(100)
);

create table operator (
    id serial primary key,
    name VARCHAR(100),
    operation VARCHAR(200),
    created_on timestamp with time zone,
    created_by VARCHAR(100),
    updated_on timestamp with time zone,
    updated_by VARCHAR(100)
);

create table aggregate (
    id serial primary key,
    name VARCHAR(100),
    operator_id VARCHAR(200),
    value integer,
    rule_id integer,
    created_on timestamp with time zone,
    created_by VARCHAR(100),
    updated_on timestamp with time zone,
    updated_by VARCHAR(100)
);

create table action (
    id serial primary key,
    name VARCHAR(100),
    type VARCHAR(10),
    value text,
    rule_id integer,
    created_on timestamp with time zone,
    created_by VARCHAR(100),
    updated_on timestamp with time zone,
    updated_by VARCHAR(100)
);