CREATE INDEX idx_config_app_env_name
    ON configurations (application, environment, name);