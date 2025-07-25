CREATE TABLE organization_parameter (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
   created_on TIMESTAMP WITHOUT TIME ZONE,
   created_by VARCHAR(255),
   updated_on TIMESTAMP WITHOUT TIME ZONE,
   updated_by VARCHAR(255),
   deleted VARCHAR(1),
   name VARCHAR(255) NOT NULL,
   description VARCHAR(255) NOT NULL,
   organization_id BIGINT NOT NULL,
   CONSTRAINT pk_organization_parameter PRIMARY KEY (id)
);