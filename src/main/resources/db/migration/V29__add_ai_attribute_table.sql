create table machine_learning_attributes (
    id serial primary key,
    operation_schema_id integer,
    attribute_name VARCHAR(100),
    attribute_value_path VARCHAR(200),
    attribute_type VARCHAR(20),
    is_active VARCHAR(1),
    deleted VARCHAR(1),
    created_on timestamp with time zone,
    created_by VARCHAR(100),
    updated_on timestamp with time zone,
    updated_by VARCHAR(100)
);