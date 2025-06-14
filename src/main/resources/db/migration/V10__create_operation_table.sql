create table operation (
    id serial primary key,
    received_at timestamp with time zone not null,
    operation_schema_id integer not null,
    organization_id integer not null,
    request_content text,
    is_anomaly varchar(1),
    detected_rules varchar(100),
    deleted varchar(1),
    created_on timestamp with time zone,
    created_by VARCHAR(100),
    updated_on timestamp with time zone,
    updated_by VARCHAR(100)
);
