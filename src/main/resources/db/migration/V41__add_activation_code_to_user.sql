ALTER TABLE user_organization ADD COLUMN activation_code VARCHAR(50);
ALTER TABLE user_organization ADD COLUMN activation_date timestamp with time zone;
ALTER TABLE user_organization ADD COLUMN activation_send_date timestamp with time zone;
