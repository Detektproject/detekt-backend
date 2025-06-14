create table event (
    id VARCHAR(100) primary key,
    severity VARCHAR(100),
    type VARCHAR(1),
    content VARCHAR(400),
    is_deleted VARCHAR(1),
    created_on timestamp with time zone,
    created_by VARCHAR(100),
    updated_on timestamp with time zone,
    updated_by VARCHAR(100)
);

create table event_configuration (
    id serial primary key,
    name VARCHAR(100),
    type VARCHAR(1),
    is_deleted VARCHAR(1),
    description VARCHAR(400),
    created_on timestamp with time zone,
    created_by VARCHAR(100),
    updated_on timestamp with time zone,
    updated_by VARCHAR(100)
);

create table organization_configured_event (
    organization_id integer,
    event_configuration_id integer,
    is_deleted VARCHAR(1),
    created_on timestamp with time zone,
    created_by VARCHAR(100),
    updated_on timestamp with time zone,
    updated_by VARCHAR(100)
);