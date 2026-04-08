-- =========================
-- DATABASE
-- =========================
CREATE DATABASE IF NOT EXISTS hotel_mgmt
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE hotel_mgmt;

-- =========================
-- DROP (để chạy lại cho sạch)
-- =========================
DROP TABLE IF EXISTS invoices;
DROP TABLE IF EXISTS service_usages;
DROP TABLE IF EXISTS services;
DROP TABLE IF EXISTS stays;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS room_types;
DROP TABLE IF EXISTS employees;

-- =========================
-- 1) EMPLOYEES
-- =========================
CREATE TABLE employees (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  full_name VARCHAR(100) NOT NULL,
  role ENUM('ADMIN','RECEPTIONIST') NOT NULL,
  status ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- 2) ROOM TYPES
-- =========================
CREATE TABLE room_types (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL UNIQUE,
  price_per_night DECIMAL(12,2) NOT NULL,
  capacity INT NOT NULL,
  description VARCHAR(255)
);

-- =========================
-- 3) ROOMS
-- =========================
CREATE TABLE rooms (
  id INT AUTO_INCREMENT PRIMARY KEY,
  room_number VARCHAR(10) NOT NULL UNIQUE,
  room_type_id INT NOT NULL,
  status ENUM('AVAILABLE','OCCUPIED','MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE',
  note VARCHAR(255),
  CONSTRAINT fk_rooms_room_type
    FOREIGN KEY (room_type_id) REFERENCES room_types(id)
);

-- =========================
-- 4) CUSTOMERS
-- =========================
CREATE TABLE customers (
  id INT AUTO_INCREMENT PRIMARY KEY,
  full_name VARCHAR(100) NOT NULL,
  phone VARCHAR(20),
  id_number VARCHAR(30),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- 5) STAYS (CHECKIN/CHECKOUT)
-- =========================
CREATE TABLE stays (
  id INT AUTO_INCREMENT PRIMARY KEY,
  customer_id INT NOT NULL,
  room_id INT NOT NULL,
  checkin_at DATETIME NOT NULL,
  checkout_at DATETIME NULL,
  status ENUM('CHECKED_IN','CHECKED_OUT','CANCELLED') NOT NULL DEFAULT 'CHECKED_IN',
  created_by INT NOT NULL,
  note VARCHAR(255),
  CONSTRAINT fk_stays_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
  CONSTRAINT fk_stays_room FOREIGN KEY (room_id) REFERENCES rooms(id),
  CONSTRAINT fk_stays_employee FOREIGN KEY (created_by) REFERENCES employees(id)
);

-- =========================
-- 6) SERVICES
-- =========================
CREATE TABLE services (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(80) NOT NULL UNIQUE,
  unit_price DECIMAL(12,2) NOT NULL,
  unit VARCHAR(20) NOT NULL DEFAULT 'lần',
  active BOOLEAN NOT NULL DEFAULT TRUE
);

-- =========================
-- 7) SERVICE USAGES
-- =========================
CREATE TABLE service_usages (
  id INT AUTO_INCREMENT PRIMARY KEY,
  stay_id INT NOT NULL,
  service_id INT NOT NULL,
  quantity INT NOT NULL,
  used_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  note VARCHAR(255),
  CONSTRAINT fk_usages_stay FOREIGN KEY (stay_id) REFERENCES stays(id),
  CONSTRAINT fk_usages_service FOREIGN KEY (service_id) REFERENCES services(id)
);

-- =========================
-- 8) INVOICES (paid_at dùng để báo cáo doanh thu)
-- =========================
CREATE TABLE invoices (
  id INT AUTO_INCREMENT PRIMARY KEY,
  stay_id INT NOT NULL UNIQUE,
  total_room DECIMAL(12,2) NOT NULL,
  total_service DECIMAL(12,2) NOT NULL,
  total DECIMAL(12,2) NOT NULL,
  paid_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  paid_by INT NOT NULL,
  CONSTRAINT fk_invoices_stay FOREIGN KEY (stay_id) REFERENCES stays(id),
  CONSTRAINT fk_invoices_employee FOREIGN KEY (paid_by) REFERENCES employees(id)
);

-- =========================
-- SEED DATA
-- (password_hash để demo; sau này bạn có thể hash)
-- =========================
INSERT INTO employees(username, password_hash, full_name, role) VALUES
('admin', 'admin', 'Admin', 'ADMIN'),
('recept', 'recept', 'Le Tan', 'RECEPTIONIST');

INSERT INTO room_types(name, price_per_night, capacity, description) VALUES
('Standard', 400000, 2, 'Phòng tiêu chuẩn'),
('Deluxe',   650000, 2, 'Phòng cao cấp'),
('Family',   900000, 4, 'Phòng gia đình');

INSERT INTO rooms(room_number, room_type_id, status, note) VALUES
('101', 1, 'AVAILABLE', ''),
('102', 1, 'AVAILABLE', ''),
('201', 2, 'MAINTENANCE', 'Sửa điều hòa'),
('301', 3, 'AVAILABLE', '');

INSERT INTO services(name, unit_price, unit) VALUES
('Gọi dậy buổi sáng', 20000, 'lần'),
('Ăn sáng',           50000, 'suất'),
('Giặt ủi',           30000, 'kg');

INSERT INTO customers(full_name, phone, id_number) VALUES
('Nguyen Van A', '0900000001', '0123456789'),
('Tran Thi B',   '0900000002', '9876543210');

USE hotel_mgmt;

-- 1) Sửa tên + mô tả room_types theo ID đã seed cũ (1=Standard,2=Deluxe,3=Family)
UPDATE room_types
SET name = 'Phòng thông thường',
    description = 'Phòng thông thường',
    price_per_night = 400000,
    capacity = 2
WHERE id = 1;

UPDATE room_types
SET name = 'Phòng cao cấp',
    description = 'Phòng cao cấp',
    price_per_night = 650000,
    capacity = 2
WHERE id = 2;

UPDATE room_types
SET name = 'Phòng bình dân',
    description = 'Phòng bình dân',
    price_per_night = 300000,
    capacity = 2
WHERE id = 3;

-- (Tuỳ chọn) Nếu bạn muốn các phòng mẫu khớp loại phòng mới:
-- 101,102 -> thông thường (id=1), 201 -> cao cấp (id=2), 301 -> bình dân (id=3)
UPDATE rooms SET room_type_id = 1 WHERE room_number IN ('101','102');
UPDATE rooms SET room_type_id = 2 WHERE room_number = '201';
UPDATE rooms SET room_type_id = 3 WHERE room_number = '301';

-- 2) Sửa giá dịch vụ theo tên dịch vụ cũ
UPDATE services SET unit_price = 0,     unit = 'lần'  WHERE name = 'Gọi dậy buổi sáng';
UPDATE services SET unit_price = 50000, unit = 'suất' WHERE name = 'Ăn sáng';
UPDATE services SET unit_price = 18000, unit = 'kg'   WHERE name = 'Giặt ủi';

-- Đổi tên dịch vụ "Ăn sáng" -> "Bữa ăn sáng đi kèm" và chỉnh giá 30000
UPDATE services
SET name = 'Bữa ăn sáng đi kèm',
    unit_price = 30000,
    unit = 'suất'
WHERE name = 'Ăn sáng';

-- 3) Kiểm tra lại
SELECT * FROM room_types ORDER BY id;
SELECT * FROM services ORDER BY id;
SELECT room_number, room_type_id, status FROM rooms ORDER BY room_number;