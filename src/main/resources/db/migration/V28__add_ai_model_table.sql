create table machine_learning_metadata (
    id VARCHAR(100) primary key,
    operation_schema_id integer,
    last_build_time timestamp with time zone,
    accuracy decimal,
    data_size integer,
    is_active VARCHAR(1),
    deleted VARCHAR(1),
    created_on timestamp with time zone,
    created_by VARCHAR(100),
    updated_on timestamp with time zone,
    updated_by VARCHAR(100)
);