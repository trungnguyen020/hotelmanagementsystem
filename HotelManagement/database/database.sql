CREATE DATABASE hotel_mgmt_v2
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE hotel_mgmt_v2;

-- =========================
-- DROP TABLES (để chạy lại)
-- =========================
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS invoice_items;
DROP TABLE IF EXISTS invoices;
DROP TABLE IF EXISTS service_usages;
DROP TABLE IF EXISTS services;
DROP TABLE IF EXISTS stays;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS room_types;
DROP TABLE IF EXISTS employees;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS room_status;
DROP TABLE IF EXISTS employee_status;
DROP TABLE IF EXISTS stay_status;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================
-- LOOKUP TABLES
-- =========================
CREATE TABLE roles (
  id TINYINT PRIMARY KEY,
  code VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE employee_status (
  id TINYINT PRIMARY KEY,
  code VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE room_status (
  id TINYINT PRIMARY KEY,
  code VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE stay_status (
  id TINYINT PRIMARY KEY,
  code VARCHAR(20) NOT NULL UNIQUE
);

INSERT INTO roles(id, code) VALUES
(1,'ADMIN'),
(2,'STAFF');

INSERT INTO employee_status(id, code) VALUES
(1,'ACTIVE'),
(2,'INACTIVE');

INSERT INTO room_status(id, code) VALUES
(1,'AVAILABLE'),
(2,'OCCUPIED'),
(3,'MAINTENANCE');

INSERT INTO stay_status(id, code) VALUES
(1,'CHECKED_IN'),
(2,'CHECKED_OUT'),
(3,'CANCELLED');

-- =========================
-- CORE TABLES
-- =========================
CREATE TABLE employees (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  full_name VARCHAR(100) NOT NULL,
  role_id TINYINT NOT NULL,
  status_id TINYINT NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_emp_role FOREIGN KEY (role_id) REFERENCES roles(id),
  CONSTRAINT fk_emp_status FOREIGN KEY (status_id) REFERENCES employee_status(id)
);

CREATE TABLE room_types (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(60) NOT NULL UNIQUE,
  price_per_night DECIMAL(12,2) NOT NULL,
  capacity INT NOT NULL,
  description VARCHAR(255),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE rooms (
  id INT AUTO_INCREMENT PRIMARY KEY,
  room_number VARCHAR(10) NOT NULL UNIQUE,
  room_type_id INT NOT NULL,
  status_id TINYINT NOT NULL DEFAULT 1,
  note VARCHAR(255),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_room_type FOREIGN KEY (room_type_id) REFERENCES room_types(id),
  CONSTRAINT fk_room_status FOREIGN KEY (status_id) REFERENCES room_status(id)
);

CREATE INDEX idx_rooms_status ON rooms(status_id);

CREATE TABLE customers (
  id INT AUTO_INCREMENT PRIMARY KEY,
  full_name VARCHAR(100) NOT NULL,
  phone VARCHAR(20),
  id_number VARCHAR(30),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_customers_phone ON customers(phone);
CREATE INDEX idx_customers_idnum ON customers(id_number);

CREATE TABLE stays (
  id INT AUTO_INCREMENT PRIMARY KEY,
  customer_id INT NOT NULL,
  room_id INT NOT NULL,
  checkin_at DATETIME NOT NULL,
  checkout_at DATETIME NULL,
  status_id TINYINT NOT NULL DEFAULT 1,
  created_by INT NOT NULL,
  note VARCHAR(255),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_stay_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
  CONSTRAINT fk_stay_room FOREIGN KEY (room_id) REFERENCES rooms(id),
  CONSTRAINT fk_stay_status FOREIGN KEY (status_id) REFERENCES stay_status(id),
  CONSTRAINT fk_stay_employee FOREIGN KEY (created_by) REFERENCES employees(id)
);

CREATE INDEX idx_stays_room ON stays(room_id);
CREATE INDEX idx_stays_time ON stays(checkin_at, checkout_at);
CREATE INDEX idx_stays_status ON stays(status_id);

CREATE TABLE services (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(80) NOT NULL UNIQUE,
  unit_price DECIMAL(12,2) NOT NULL,
  unit VARCHAR(20) NOT NULL DEFAULT 'lần',
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE service_usages (
  id INT AUTO_INCREMENT PRIMARY KEY,
  stay_id INT NOT NULL,
  service_id INT NOT NULL,
  quantity INT NOT NULL,
  used_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  note VARCHAR(255),
  CONSTRAINT fk_usage_stay FOREIGN KEY (stay_id) REFERENCES stays(id),
  CONSTRAINT fk_usage_service FOREIGN KEY (service_id) REFERENCES services(id)
);

CREATE INDEX idx_usage_stay ON service_usages(stay_id);
CREATE INDEX idx_usage_time ON service_usages(used_at);

-- =========================
-- INVOICE (chi tiết từng dòng)
-- =========================
CREATE TABLE invoices (
  id INT AUTO_INCREMENT PRIMARY KEY,
  stay_id INT NOT NULL UNIQUE,
  paid_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  paid_by INT NOT NULL,
  total DECIMAL(12,2) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_inv_stay FOREIGN KEY (stay_id) REFERENCES stays(id),
  CONSTRAINT fk_inv_emp FOREIGN KEY (paid_by) REFERENCES employees(id)
);

CREATE INDEX idx_invoices_paidat ON invoices(paid_at);

CREATE TABLE invoice_items (
  id INT AUTO_INCREMENT PRIMARY KEY,
  invoice_id INT NOT NULL,
  item_type VARCHAR(20) NOT NULL,          -- 'ROOM' hoặc 'SERVICE'
  description VARCHAR(120) NOT NULL,
  qty DECIMAL(12,2) NOT NULL,
  unit_price DECIMAL(12,2) NOT NULL,
  amount DECIMAL(12,2) NOT NULL,
  ref_id INT NULL,                         -- service_id nếu là SERVICE, hoặc room_id/room_type_id nếu muốn
  CONSTRAINT fk_item_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id)
);

CREATE INDEX idx_items_invoice ON invoice_items(invoice_id);

-- =========================
-- SEED DATA (theo yêu cầu của bạn)
-- =========================
INSERT INTO employees(username, password_hash, full_name, role_id, status_id) VALUES
('admin', 'admin', 'Admin', 1, 1),
('staff', 'staff', 'Nhan vien', 2, 1);

INSERT INTO room_types(name, price_per_night, capacity, description) VALUES
('Phòng cao cấp',      650000, 2, 'Phòng cao cấp'),
('Phòng thông thường', 400000, 2, 'Phòng thông thường'),
('Phòng bình dân',     300000, 2, 'Phòng bình dân');

INSERT INTO rooms(room_number, room_type_id, status_id, note) VALUES
('101', 2, 1, ''),
('102', 2, 1, ''),
('201', 1, 3, 'Sửa chữa'),
('301', 3, 1, '');

INSERT INTO services(name, unit_price, unit, active) VALUES
('Gọi dậy buổi sáng', 0,     'lần', TRUE),
('Bữa ăn sáng đi kèm', 30000, 'suất', TRUE),
('Giặt ủi',           18000, 'kg',  TRUE);

INSERT INTO customers(full_name, phone, id_number) VALUES
('Nguyen Van A', '0900000001', '0123456789'),
('Tran Thi B',   '0900000002', '9876543210');

USE hotel_mgmt_v2;

ALTER TABLE stays
  ADD COLUMN expected_checkout_at DATETIME NULL AFTER checkout_at;

ALTER TABLE invoices
  ADD COLUMN subtotal DECIMAL(12,2) NOT NULL DEFAULT 0 AFTER paid_by,
  ADD COLUMN discount_percent DECIMAL(5,2) NOT NULL DEFAULT 0 AFTER subtotal,
  ADD COLUMN discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0 AFTER discount_percent;