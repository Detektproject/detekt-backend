alter table operation_schema add column is_date_included VARCHAR(1);
alter table operation_schema add column date_path VARCHAR(100);
alter table operation_schema add column date_format VARCHAR(100);