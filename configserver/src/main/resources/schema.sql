-- H2 Database Schema for Configuration Server

-- AppConfig table: stores application configuration properties
CREATE TABLE IF NOT EXISTS app_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_name VARCHAR(255) NOT NULL,
    domain VARCHAR(5) NOT NULL,
    property_key VARCHAR(255) NOT NULL,
    property_value TEXT NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    created_tm TIMESTAMP NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    updated_tm TIMESTAMP NOT NULL,
    UNIQUE KEY unique_app_config (application_name, domain, property_key)
);

-- ConfigSync table: tracks version numbers for configuration sync
CREATE TABLE IF NOT EXISTS config_sync (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_name VARCHAR(255) NOT NULL,
    domain VARCHAR(5) NOT NULL,
    version_number INT NOT NULL DEFAULT 1,
    updated_by VARCHAR(255) NOT NULL,
    updated_tm TIMESTAMP NOT NULL,
    UNIQUE KEY unique_config_sync (application_name, domain)
);

-- AppConfigAudit table: audit trail for configuration changes
CREATE TABLE IF NOT EXISTS app_config_audit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_name VARCHAR(255) NOT NULL,
    domain VARCHAR(5) NOT NULL,
    property_key VARCHAR(255) NOT NULL,
    old_property_value TEXT,
    new_property_value TEXT,
    operation VARCHAR(20) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    updated_tm TIMESTAMP NOT NULL,
    version_number INT NOT NULL,
    INDEX idx_app_config_audit (application_name, domain, version_number DESC)
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_app_config_app_domain ON app_config(application_name, domain);
CREATE INDEX IF NOT EXISTS idx_config_sync_app_domain ON config_sync(application_name, domain);

-- Sample Data for Testing
INSERT INTO app_config (application_name, domain, property_key, property_value, created_by, created_tm, updated_by, updated_tm)
VALUES
('UserService', 'prod', 'db.host', 'prod-db.example.com', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
('UserService', 'prod', 'db.port', '5432', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
('UserService', 'prod', 'api.timeout', '30000', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
('UserService', 'dev', 'db.host', 'dev-db.example.com', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
('UserService', 'dev', 'db.port', '5432', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
('OrderService', 'prod', 'db.host', 'prod-db.example.com', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
('OrderService', 'prod', 'db.pool.size', '20', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP),
('OrderService', 'dev', 'db.host', 'dev-db.example.com', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP);

INSERT INTO config_sync (application_name, domain, version_number, updated_by, updated_tm)
VALUES
('UserService', 'prod', 1, 'admin', CURRENT_TIMESTAMP),
('UserService', 'dev', 1, 'admin', CURRENT_TIMESTAMP),
('OrderService', 'prod', 1, 'admin', CURRENT_TIMESTAMP),
('OrderService', 'dev', 1, 'admin', CURRENT_TIMESTAMP);

