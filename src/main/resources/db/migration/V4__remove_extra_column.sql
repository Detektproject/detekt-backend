ALTER TABLE criteria drop column operation_schema_id;
alter table criteria add column operator_id INT not null;
alter table aggregate drop COLUMN operator_id;
alter table aggregate add column operator_id INT not null;
