-- Global, operator-editable application settings (single row, id = 1).
-- Currently: the boarding ("hospedagem") daily rates used as the default when a
-- reservation is created without an explicit dailyRate.

CREATE TABLE app_settings (
    id                  INT           NOT NULL,
    daily_rate_standard DECIMAL(10,2) NOT NULL,
    daily_rate_large    DECIMAL(10,2) NOT NULL,
    updated_at          DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT ck_app_settings_singleton CHECK (id = 1)
) ENGINE=InnoDB;

INSERT INTO app_settings (id, daily_rate_standard, daily_rate_large, updated_at)
VALUES (1, 50.00, 80.00, CURRENT_TIMESTAMP(6));
