-- ============================================================
-- Restaurant Table Reservation System - Database Schema
-- Database: restaurant_db
-- Run with:  mysql -u root -p < restaurant_db.sql
-- NOTE: The Spring Boot app auto-creates the DB (createDatabaseIfNotExist=true)
-- and auto-creates/updates tables via JPA (ddl-auto=update), then seeds sample data.
-- This script is provided for manual / reference setup.
-- ============================================================

CREATE DATABASE IF NOT EXISTS restaurant_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE restaurant_db;

-- ---------- Users ----------
CREATE TABLE IF NOT EXISTS users (
  user_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
  name       VARCHAR(100) NOT NULL,
  email      VARCHAR(150) NOT NULL UNIQUE,
  password   VARCHAR(255) NOT NULL,
  phone      VARCHAR(20),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ---------- Tables ----------
-- zone: tables sharing a zone can be joined together for large parties
CREATE TABLE IF NOT EXISTS tables (
  table_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
  table_number VARCHAR(20)  NOT NULL UNIQUE,
  capacity     INT          NOT NULL,
  status       VARCHAR(20)  NOT NULL DEFAULT 'AVAILABLE',
  zone         VARCHAR(30)
);

-- ---------- Reservations ----------
-- occasion: NONE | BIRTHDAY | ANNIVERSARY | DATE_NIGHT | BUSINESS | FAMILY | OTHER
--           the setup buffer is derived from it (see backend Occasion enum)
-- table_id: anchor table of the booking (the biggest one for a combo booking)
CREATE TABLE IF NOT EXISTS reservations (
  reservation_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id           BIGINT NOT NULL,
  table_id          BIGINT NOT NULL,
  reservation_date  DATE   NOT NULL,
  reservation_time  VARCHAR(10) NOT NULL,
  number_of_people  INT    NOT NULL,
  status            VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  occasion          VARCHAR(20)  NOT NULL DEFAULT 'NONE',
  occasion_note     VARCHAR(200),
  created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_res_user  FOREIGN KEY (user_id) REFERENCES users (user_id),
  CONSTRAINT fk_res_table FOREIGN KEY (table_id) REFERENCES tables (table_id)
);

-- ---------- Reservation tables (combo bookings) ----------
-- one row per table of a reservation, so a combo booking keeps all its tables
CREATE TABLE IF NOT EXISTS reservation_tables (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  reservation_id BIGINT NOT NULL,
  table_id       BIGINT NOT NULL,
  CONSTRAINT uk_reservation_table UNIQUE (reservation_id, table_id),
  CONSTRAINT fk_rt_reservation FOREIGN KEY (reservation_id) REFERENCES reservations (reservation_id) ON DELETE CASCADE,
  CONSTRAINT fk_rt_table       FOREIGN KEY (table_id)       REFERENCES tables (table_id)
);

-- ---------- Admin ----------
CREATE TABLE IF NOT EXISTS admin (
  admin_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50)  NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL
);

-- ---------- Seed Data (sample) ----------
INSERT INTO tables (table_number, capacity, status, zone) VALUES
  ('1', 2, 'AVAILABLE', 'WINDOW'),
  ('2', 2, 'AVAILABLE', 'WINDOW'),
  ('3', 4, 'AVAILABLE', 'WINDOW'),
  ('4', 4, 'AVAILABLE', 'WINDOW'),
  ('5', 4, 'AVAILABLE', 'MAIN'),
  ('6', 6, 'AVAILABLE', 'MAIN'),
  ('7', 8, 'AVAILABLE', 'PRIVATE'),
  ('8', 10, 'AVAILABLE', 'PRIVATE');

INSERT INTO admin (username, password) VALUES ('sri', 'sri123');

-- Sample user + reservation (passwords are plain for demo simplicity)
INSERT INTO users (name, email, password, phone) VALUES
  ('John Doe', 'john@example.com', 'password', '555-0100');

INSERT INTO reservations (user_id, table_id, reservation_date, reservation_time, number_of_people, status, occasion, occasion_note)
VALUES (1, 5, CURDATE(), '19:00', 3, 'PENDING', 'BIRTHDAY', 'Cake with candles');

INSERT INTO reservation_tables (reservation_id, table_id) VALUES (1, 5);

-- ============================================================
-- Existing databases: JPA (ddl-auto=update) adds the new columns and the
-- reservation_tables table on the next backend start, so nothing else is needed.
-- ============================================================