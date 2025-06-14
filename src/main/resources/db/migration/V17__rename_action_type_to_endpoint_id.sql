ALTER TABLE action add column is_active varchar(1);
ALTER TABLE action drop column type;
ALTER TABLE action add column endpoint_id integer;


