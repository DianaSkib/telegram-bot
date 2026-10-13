-- liquibase formatted sql

-- changeset DianaSkib:1
CREATE TABLE notification_task
(
    id        BIGSERIAL PRIMARY KEY,
    chat_id   BIGINT    NOT NULL,
    message   TEXT      NOT NULL,
    date_time TIMESTAMP NOT NULL
);