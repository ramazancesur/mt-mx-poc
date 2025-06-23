-- Database initialization script for MT-MX application
-- This script will run when PostgreSQL container starts

-- Create database if it doesn't exist
-- (This will be handled by POSTGRES_DB environment variable)

-- Connect to the database
\c mtmxdb;

-- Create the schema (copy from resources/schema.sql)
-- Swift Messages Table Schema
DROP TABLE IF EXISTS swift_messages CASCADE;
DROP SEQUENCE IF EXISTS swift_messages_id_seq CASCADE;

-- Create sequence first
CREATE SEQUENCE swift_messages_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE swift_messages (
    id BIGINT PRIMARY KEY DEFAULT nextval('swift_messages_id_seq'),
    message_type VARCHAR(10) NOT NULL CHECK (message_type IN ('MT102', 'MT103', 'MT202', 'MT202COV', 'MT203')),
    sender_bic VARCHAR(15),
    receiver_bic VARCHAR(15),
    amount DECIMAL(19,2),
    currency VARCHAR(3),
    value_date DATE,
    raw_mt_message TEXT NOT NULL,
    generated_mx_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Set sequence ownership
ALTER SEQUENCE swift_messages_id_seq OWNED BY swift_messages.id;

-- Create indexes
CREATE INDEX idx_swift_messages_message_type ON swift_messages (message_type);
CREATE INDEX idx_swift_messages_sender_bic ON swift_messages (sender_bic);
CREATE INDEX idx_swift_messages_receiver_bic ON swift_messages (receiver_bic);
CREATE INDEX idx_swift_messages_currency ON swift_messages (currency);
CREATE INDEX idx_swift_messages_created_at ON swift_messages (created_at);
CREATE INDEX idx_swift_messages_value_date ON swift_messages (value_date);
CREATE INDEX idx_swift_messages_amount ON swift_messages (amount);

-- Create trigger function for updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger
CREATE TRIGGER update_swift_messages_updated_at 
    BEFORE UPDATE ON swift_messages 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Grant permissions to user (will be created by POSTGRES_USER env var)
-- Note: The user 'user' is created automatically by the postgres image
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO "user";
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO "user";
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO "user";

-- Ensure future objects get proper permissions
ALTER DEFAULT PRIVILEGES IN SCHEMA public 
    GRANT ALL PRIVILEGES ON TABLES TO "user";
ALTER DEFAULT PRIVILEGES IN SCHEMA public 
    GRANT ALL PRIVILEGES ON SEQUENCES TO "user"; 