ALTER TABLE organization add column deleted varchar(1);
ALTER TABLE user_organization add column deleted varchar(1);
ALTER TABLE action add column deleted varchar(1);
ALTER TABLE aggregate add column deleted varchar(1);
ALTER TABLE criteria add column deleted varchar(1);
ALTER TABLE operator add column deleted varchar(1);
ALTER TABLE rule add column deleted varchar(1);
