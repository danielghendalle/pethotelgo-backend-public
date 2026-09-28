-- PetHotelGO — baseline schema (MySQL 8+).
--
-- Consolidates the former PostgreSQL migrations V1..V4 into one MySQL baseline
-- (there was no production data to preserve at cut-over). Column types mirror
-- what Hibernate's MySQLDialect expects so `ddl-auto=validate` passes:
--   String        -> VARCHAR(255)      (ids kept at 36 for the UUIDs)
--   Boolean       -> BIT
--   LocalDateTime -> DATETIME(6)
--   @Enumerated(STRING) -> native ENUM(...) with values in Hibernate's order
--   @Column(columnDefinition="text")     -> TEXT
--   @Column(columnDefinition="LONGTEXT") -> LONGTEXT   (base64 vaccination cards)

CREATE TABLE owners (
    id         VARCHAR(36)  NOT NULL,
    name       VARCHAR(255) NOT NULL,
    phone      VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    created_at DATETIME(6)  NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE users (
    id            VARCHAR(36)  NOT NULL,
    email         VARCHAR(255) NOT NULL,
    name          VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          ENUM('ADMIN','STAFF','USER') NOT NULL,
    is_active     BIT          NOT NULL,
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE pets (
    id                   VARCHAR(36)  NOT NULL,
    owner_id             VARCHAR(36)  NULL,
    name                 VARCHAR(255) NOT NULL,
    breed                VARCHAR(255) NOT NULL,
    size                 ENUM('grande','medio','pequeno') NOT NULL,
    needs_separate_space BIT          NOT NULL,
    sociability          ENUM('alta','baixa','media') NOT NULL,
    allergies            TEXT         NOT NULL,
    special_care         TEXT         NOT NULL,
    feeding_schedule     VARCHAR(255) NOT NULL,
    feeding_amount       VARCHAR(255) NOT NULL,
    vaccination_card_url LONGTEXT     NULL,
    created_at           DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_pets_owner FOREIGN KEY (owner_id) REFERENCES owners (id)
) ENGINE=InnoDB;

CREATE TABLE reservations (
    id                  VARCHAR(36) NOT NULL,
    pet_id              VARCHAR(36) NULL,
    owner_id            VARCHAR(36) NULL,
    check_in            DATETIME(6) NOT NULL,
    check_out           DATETIME(6) NOT NULL,
    status              ENUM('cancelled','completed','confirmed','pending') NOT NULL,
    notes               TEXT          NOT NULL,
    daily_rate          DECIMAL(10,2) NULL,
    discount_percentage DECIMAL(5,2)  NULL,
    created_at          DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_reservations_pet   FOREIGN KEY (pet_id)   REFERENCES pets (id),
    CONSTRAINT fk_reservations_owner FOREIGN KEY (owner_id) REFERENCES owners (id)
) ENGINE=InnoDB;

CREATE TABLE stay_histories (
    id             VARCHAR(36) NOT NULL,
    pet_id         VARCHAR(36) NULL,
    reservation_id VARCHAR(36) NULL,
    check_in       DATETIME(6) NOT NULL,
    check_out      DATETIME(6) NULL,
    behavior       TEXT        NOT NULL,
    notes          TEXT        NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_stay_histories_pet         FOREIGN KEY (pet_id)         REFERENCES pets (id),
    CONSTRAINT fk_stay_histories_reservation FOREIGN KEY (reservation_id) REFERENCES reservations (id)
) ENGINE=InnoDB;

CREATE TABLE refresh_tokens (
    id         VARCHAR(36)  NOT NULL,
    user_id    VARCHAR(36)  NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at DATETIME(6)  NOT NULL,
    created_at DATETIME(6)  NOT NULL,
    revoked_at DATETIME(6)  NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_refresh_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB;

CREATE INDEX idx_token_hash ON refresh_tokens (token_hash);

CREATE TABLE vaccination_cards (
    id          VARCHAR(36)  NOT NULL,
    pet_id      VARCHAR(36)  NOT NULL,
    file_name   VARCHAR(255) NOT NULL,
    file_type   VARCHAR(255) NULL,
    file_data   LONGTEXT     NOT NULL,
    file_size   BIGINT       NULL,
    uploaded_at DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE INDEX idx_vaccination_cards_pet_id ON vaccination_cards (pet_id);
