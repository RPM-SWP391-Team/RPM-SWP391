-- =========================================================================
-- MOCK DATA SCRIPT FOR REMOTE PATIENT MONITORING (SQL SERVER)
-- =========================================================================

-- 1. DELETE OLD DATA (To prevent duplicate keys)
-- Xóa theo thứ tự từ bảng con đến bảng cha để không vi phạm FK
DELETE FROM water_logs;
DELETE FROM exercise_logs;
DELETE FROM diet_logs;
DELETE FROM patient_medications;
DELETE FROM daily_health_logs;
DELETE FROM alerts;
DELETE FROM treatment_plans;
DELETE FROM patients;
DELETE FROM doctor_ratings;
DELETE FROM doctors;
DELETE FROM alert_thresholds;
DELETE FROM disease_profiles;
DELETE FROM hospitals;
DELETE FROM accounts;

-- =========================================================================
-- 2. INSERT HOSPITALS & DISEASE PROFILES
-- =========================================================================

SET IDENTITY_INSERT hospitals ON;
INSERT INTO hospitals (id, account_id, hospital_code, full_name, address, phone, is_active, created_at, updated_at) VALUES 
(1, 1, 'BV-001', N'Bệnh viện Đại học Y Dược TP.HCM', N'215 Hồng Bàng, Quận 5, TP.HCM', '19001234', 1, GETDATE(), GETDATE()),
(2, 1, 'BV-002', N'Bệnh viện Chợ Rẫy', N'201B Nguyễn Chí Thanh, Quận 5, TP.HCM', '19005678', 1, GETDATE(), GETDATE()),
(3, 1, 'BV-003', N'Bệnh viện Tim Tâm Đức', N'04 Nguyễn Lương Bằng, Quận 7, TP.HCM', '19009999', 1, GETDATE(), GETDATE());
SET IDENTITY_INSERT hospitals OFF;


SET IDENTITY_INSERT disease_profiles ON;
INSERT INTO disease_profiles (id, profile_name, description, created_at, updated_at) VALUES 
(1, N'Bệnh Tiểu Đường (Type 2)', N'Đái tháo đường type 2 cần theo dõi đường huyết thường xuyên.', GETDATE(), GETDATE()),
(2, N'Tăng Huyết Áp', N'Cao huyết áp mạn tính cần kiểm soát huyết áp và nhịp tim.', GETDATE(), GETDATE()),
(3, N'Suy Tim Mạn Tính', N'Bệnh nhân suy tim cần đo SpO2 và nhịp tim hàng ngày.', GETDATE(), GETDATE());
SET IDENTITY_INSERT disease_profiles OFF;

-- Ngưỡng cảnh báo (Alert Thresholds)
SET IDENTITY_INSERT alert_thresholds ON;
-- Tiểu đường
INSERT INTO alert_thresholds (id, disease_profile_id, metric_type, min_safe_value, max_safe_value, min_warning_value, max_warning_value, created_at, updated_at) VALUES 
(1, 1, 'BLOOD_SUGAR', 70, 130, 50, 180, GETDATE(), GETDATE());
-- Huyết áp
INSERT INTO alert_thresholds (id, disease_profile_id, metric_type, min_safe_value, max_safe_value, min_warning_value, max_warning_value, created_at, updated_at) VALUES 
(2, 2, 'SYSTOLIC_BP', 90, 120, 80, 140, GETDATE(), GETDATE()),
(3, 2, 'DIASTOLIC_BP', 60, 80, 50, 90, GETDATE(), GETDATE());
-- Suy tim
INSERT INTO alert_thresholds (id, disease_profile_id, metric_type, min_safe_value, max_safe_value, min_warning_value, max_warning_value, created_at, updated_at) VALUES 
(4, 3, 'HEART_RATE', 60, 100, 50, 120, GETDATE(), GETDATE()),
(5, 3, 'SPO2', 95, 100, 90, 100, GETDATE(), GETDATE());
SET IDENTITY_INSERT alert_thresholds OFF;


-- =========================================================================
-- 3. INSERT ACCOUNTS (Password Hash: $2a$10$PiWrEm9MyCp2dm./FhHuEOc0yknpyxwioozHYlIBFavsoxLcCJ2ee)
-- =========================================================================
SET IDENTITY_INSERT accounts ON;
-- Admin
INSERT INTO accounts (id, email, password_hash, role, is_email_verified, is_active, created_at, updated_at) VALUES 
(1, 'admin@rpm.com', '$2a$10$PiWrEm9MyCp2dm./FhHuEOc0yknpyxwioozHYlIBFavsoxLcCJ2ee', 'ROLE_ADMIN', 1, 1, GETDATE(), GETDATE());

-- 3 Doctors
INSERT INTO accounts (id, email, password_hash, role, is_email_verified, is_active, created_at, updated_at) VALUES 
(2, 'dr.tuan@rpm.com', '$2a$10$PiWrEm9MyCp2dm./FhHuEOc0yknpyxwioozHYlIBFavsoxLcCJ2ee', 'ROLE_DOCTOR', 1, 1, GETDATE(), GETDATE()),
(3, 'dr.lan@rpm.com', '$2a$10$PiWrEm9MyCp2dm./FhHuEOc0yknpyxwioozHYlIBFavsoxLcCJ2ee', 'ROLE_DOCTOR', 1, 1, GETDATE(), GETDATE()),
(4, 'dr.hung@rpm.com', '$2a$10$PiWrEm9MyCp2dm./FhHuEOc0yknpyxwioozHYlIBFavsoxLcCJ2ee', 'ROLE_DOCTOR', 1, 1, GETDATE(), GETDATE());

-- 5 Patients
INSERT INTO accounts (id, email, password_hash, role, is_email_verified, is_active, created_at, updated_at) VALUES 
(5, 'bn.an@rpm.com', '$2a$10$PiWrEm9MyCp2dm./FhHuEOc0yknpyxwioozHYlIBFavsoxLcCJ2ee', 'ROLE_PATIENT', 1, 1, GETDATE(), GETDATE()),
(6, 'bn.binh@rpm.com', '$2a$10$PiWrEm9MyCp2dm./FhHuEOc0yknpyxwioozHYlIBFavsoxLcCJ2ee', 'ROLE_PATIENT', 1, 1, GETDATE(), GETDATE()),
(7, 'bn.cuc@rpm.com', '$2a$10$PiWrEm9MyCp2dm./FhHuEOc0yknpyxwioozHYlIBFavsoxLcCJ2ee', 'ROLE_PATIENT', 1, 1, GETDATE(), GETDATE()),
(8, 'bn.dung@rpm.com', '$2a$10$PiWrEm9MyCp2dm./FhHuEOc0yknpyxwioozHYlIBFavsoxLcCJ2ee', 'ROLE_PATIENT', 1, 1, GETDATE(), GETDATE()),
(9, 'bn.hoa@rpm.com', '$2a$10$PiWrEm9MyCp2dm./FhHuEOc0yknpyxwioozHYlIBFavsoxLcCJ2ee', 'ROLE_PATIENT', 1, 1, GETDATE(), GETDATE());
SET IDENTITY_INSERT accounts OFF;

-- =========================================================================
-- 4. INSERT DOCTORS & PATIENTS
-- =========================================================================
SET IDENTITY_INSERT doctors ON;
INSERT INTO doctors (id, account_id, hospital_id, doctor_code, full_name, phone, specialty, gender, capacity_limit, current_patient_count, is_active, created_at, updated_at) VALUES 
(1, 2, 1, 'BS-001', N'BS. Nguyễn Minh Tuấn', '0901000001', N'Nội tiết - Tiểu đường', 'Male', 50, 2, 1, GETDATE(), GETDATE()),
(2, 3, 2, 'BS-002', N'BS. Lê Hoàng Lan', '0901000002', N'Tim mạch', 'Female', 50, 2, 1, GETDATE(), GETDATE()),
(3, 4, 3, 'BS-003', N'BS. Trần Quốc Hùng', '0901000003', N'Nội tổng hợp', 'Male', 50, 1, 1, GETDATE(), GETDATE());
SET IDENTITY_INSERT doctors OFF;

SET IDENTITY_INSERT patients ON;
INSERT INTO patients (id, account_id, hospital_id, doctor_id, disease_profile_id, patient_code, full_name, date_of_birth, gender, phone, address, status, registration_source, is_active, created_at, updated_at) VALUES 
(1, 5, 1, 1, 1, 'BN-001', N'Nguyễn Văn An', '1965-05-12', 'Male', '0911000001', N'Quận 1, TP.HCM', 'MONITORING', 'APP', 1, GETDATE(), GETDATE()),
(2, 6, 1, 1, 1, 'BN-002', N'Trần Thái Bình', '1958-10-22', 'Male', '0911000002', N'Quận 3, TP.HCM', 'MONITORING', 'APP', 1, GETDATE(), GETDATE()),
(3, 7, 2, 2, 2, 'BN-003', N'Lê Thị Cúc', '1970-02-15', 'Female', '0911000003', N'Quận 5, TP.HCM', 'MONITORING', 'APP', 1, GETDATE(), GETDATE()),
(4, 8, 2, 2, 2, 'BN-004', N'Phạm Quang Dũng', '1960-08-30', 'Male', '0911000004', N'Bình Thạnh, TP.HCM', 'MONITORING', 'APP', 1, GETDATE(), GETDATE()),
(5, 9, 3, 3, 3, 'BN-005', N'Ngô Mai Hoa', '1955-12-05', 'Female', '0911000005', N'Gò Vấp, TP.HCM', 'MONITORING', 'APP', 1, GETDATE(), GETDATE());
SET IDENTITY_INSERT patients OFF;


-- =========================================================================
-- 5. INSERT TREATMENT PLANS & HEALTH LOGS
-- =========================================================================
SET IDENTITY_INSERT treatment_plans ON;
INSERT INTO treatment_plans (id, patient_id, doctor_id, start_date, end_date, medical_order, exercise_goal, notes, is_current, created_at, updated_at) VALUES 
(1, 1, 1, GETDATE(), DATEADD(month, 3, GETDATE()), N'Uống Metformin 500mg, 1 viên sau ăn sáng.', N'Đi bộ 30 phút mỗi ngày.', N'Theo dõi đường huyết đói', 1, GETDATE(), GETDATE()),
(2, 3, 2, GETDATE(), DATEADD(month, 3, GETDATE()), N'Uống Amlodipine 5mg, 1 viên sáng.', N'Tập yoga 20 phút nhẹ nhàng.', N'Theo dõi huyết áp 2 lần/ngày', 1, GETDATE(), GETDATE()),
(3, 5, 3, GETDATE(), DATEADD(month, 3, GETDATE()), N'Bisoprolol 2.5mg 1 viên sáng.', N'Hạn chế vận động mạnh.', N'Đo SpO2 thường xuyên', 1, GETDATE(), GETDATE());
SET IDENTITY_INSERT treatment_plans OFF;

SET IDENTITY_INSERT daily_health_logs ON;
-- Bệnh nhân 1 (Tiểu đường) - Đường huyết hơi cao
INSERT INTO daily_health_logs (id, patient_id, log_date, blood_sugar, notes, created_at, updated_at) VALUES 
(1, 1, GETDATE(), 160.5, N'Hôm qua có ăn chè', GETDATE(), GETDATE());

-- Bệnh nhân 3 (Huyết áp) - Huyết áp bình thường
INSERT INTO daily_health_logs (id, patient_id, log_date, systolic_bp, diastolic_bp, heart_rate, created_at, updated_at) VALUES 
(2, 3, GETDATE(), 115, 75, 72, GETDATE(), GETDATE());

-- Bệnh nhân 5 (Suy tim) - SpO2 giảm
INSERT INTO daily_health_logs (id, patient_id, log_date, spo2, heart_rate, created_at, updated_at) VALUES 
(3, 5, GETDATE(), 94, 95, GETDATE(), GETDATE());
SET IDENTITY_INSERT daily_health_logs OFF;


-- =========================================================================
-- 6. INSERT ALERTS (Cảnh báo)
-- =========================================================================
SET IDENTITY_INSERT alerts ON;
INSERT INTO alerts (id, patient_id, health_log_id, alert_type, severity_level, alert_message, is_read, is_resolved, created_at) VALUES 
(1, 1, 1, 'BLOOD_SUGAR', 'WARNING', N'Đường huyết (160.5) vượt ngưỡng an toàn', 0, 0, GETDATE()),
(2, 5, 3, 'SPO2', 'CRITICAL', N'SpO2 (94%) dưới ngưỡng an toàn 95%', 0, 0, GETDATE());
SET IDENTITY_INSERT alerts OFF;


PRINT N'✅ Đã chèn dữ liệu Mock Y tế thành công vào Database!';
