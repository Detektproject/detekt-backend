create table user_organization (
    id serial primary key,
    organization_id integer,
    user_id VARCHAR(100),
    status VARCHAR(1),
    created_on timestamp with time zone,
    created_by VARCHAR(100),
    updated_on timestamp with time zone,
    updated_by VARCHAR(100)
);
