ALTER TABLE rule add column is_active varchar(1);
ALTER TABLE rule drop column status;
ALTER TABLE operation_schema add column is_active varchar(1);
