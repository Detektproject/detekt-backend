create table endpoint (
    id serial primary key,
    name varchar(100),
    description text,
    address text,
    certificate text,
    direction varchar(2),
    organization_id integer not null,
    type varchar(20) not null,
    is_active varchar(1) not null,
    deleted varchar(1),
    created_on timestamp with time zone,
    created_by VARCHAR(100),
    updated_on timestamp with time zone,
    updated_by VARCHAR(100)
);
