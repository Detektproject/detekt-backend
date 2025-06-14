ALTER TABLE organization ADD COLUMN subscription_status VARCHAR(50); -- e.g., active, inactive, expired
ALTER TABLE organization ADD COLUMN subscription_start_date TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE organization ADD COLUMN subscription_end_date TIMESTAMP WITHOUT TIME ZONE;
