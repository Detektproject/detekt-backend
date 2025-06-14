alter table organization drop column subscription_plan_id;

drop table subscription_plan_detail;
drop table subscription_plan;

-- Create subscription_plans table in PostgreSQL
CREATE TABLE subscription_plans (
    id SERIAL PRIMARY KEY, -- Auto-incrementing primary key
    name VARCHAR(255) NOT NULL, -- Plan name
    price NUMERIC(10, 2) NOT NULL, -- Plan price with 2 decimal places
    features JSONB NOT NULL, -- JSONB column for storing features
    billing_cycle VARCHAR(50) NOT NULL, -- Billing cycle (e.g., Monthly, Yearly)
    is_default CHAR(1) NOT NULL DEFAULT 'N' -- Default plan indicator ('Y' or 'N')
);

CREATE TABLE organization_subscriptions (
    id SERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    FOREIGN KEY (organization_id) REFERENCES organization(id),
    FOREIGN KEY (plan_id) REFERENCES subscription_plans(id)
);


-- Insert the Free plan with is_default set to 'Y'
INSERT INTO subscription_plans (name, price, features, billing_cycle, is_default)
VALUES (
    'Free',
    0.00,
    '{
        "requests_per_month": 100,
        "schemas": 1,
        "ai_builds": 1,
        "endpoints": 1,
        "rules_per_schema": 1,
        "actions_per_rule": 1
    }'::jsonb,
    'Monthly',
    'Y'
);

-- Insert the Basic plan with is_default set to 'N'
INSERT INTO subscription_plans (name, price, features, billing_cycle, is_default)
VALUES (
    'Basic',
    9.99,
    '{
        "requests_per_month": 1000,
        "schemas": 5,
        "ai_builds": 5,
        "endpoints": 5,
        "rules_per_schema": 10,
        "actions_per_rule": 5
    }'::jsonb,
    'Monthly',
    'N'
);

-- Insert the Pro plan with is_default set to 'N'
INSERT INTO subscription_plans (name, price, features, billing_cycle, is_default)
VALUES (
    'Pro',
    29.99,
    '{
        "requests_per_month": 10000,
        "schemas": 20,
        "ai_builds": 10,
        "endpoints": 10,
        "rules_per_schema": 20,
        "actions_per_rule": 10
    }'::jsonb,
    'Monthly',
    'N'
);

-- Insert the Enterprise plan with is_default set to 'N'
INSERT INTO subscription_plans (name, price, features, billing_cycle, is_default)
VALUES (
    'Enterprise',
    99.99,
    '{
        "requests_per_month": 100000,
        "schemas": 100,
        "ai_builds": 50,
        "endpoints": 50,
        "rules_per_schema": 100,
        "actions_per_rule": 50
    }'::jsonb,
    'Monthly',
    'N'
);