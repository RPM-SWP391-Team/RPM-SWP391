-- ============================================================
-- CHANGELOG — Các điểm đã SỬA so với bản gốc - Tuần 7 - 06/27-10:46
-- ============================================================
-- 1.  audit_trails: bỏ actor_type 'SYSTEM' (chỉ còn DOCTOR/PATIENT/HOSPITAL_ADMIN).
-- 2.  Bỏ bảng ai_health_summaries (các bảng liên quan AI sẽ tạo ở giai đoạn sau).
-- 3.  Bỏ bảng menus (không còn AI gợi ý nguyên món ăn cụ thể).
-- 4.  Mô hình dinh dưỡng mới: bác sĩ/AI chỉ đưa CHỈ TIÊU dưỡng chất (nutrition_rules
--     giữ nguyên vai trò này). Bệnh nhân tự CHỌN thực phẩm có sẵn trong danh mục
--     foods_dictionary (thêm mới) và hệ thống ghi nhận vào diet_logs (thêm mới).
-- 5.  Ngưỡng cảnh báo viết lại:
--       - Đường huyết: CHỈ DÙNG mmol/L (bỏ hoàn toàn mg/dL).
--       - Huyết áp & đường huyết áp dụng đúng 4 mức màu GREEN/YELLOW/ORANGE/RED
--         theo tài liệu nghiệp vụ mới (KHÔNG còn mức CRITICAL).
-- 6.  Thêm bảng emergency_protocols: lưu Dấu hiệu nhận biết + Hướng dẫn xử lý
--     khẩn cấp khi Tăng huyết áp đột ngột / Hạ đường huyết đột ngột /
--     Tăng đường huyết đột ngột (khác với emergency_guides — bảng này là hướng dẫn
--     do hệ thống cảnh báo theo ngưỡng, còn emergency_protocols là cẩm nang
--     dấu hiệu nhận biết tình trạng cấp cứu nói chung).
-- 7.  Bỏ mức màu CRITICAL khỏi toàn bộ enum alert_level/alert_color liên quan
--     (daily_health_logs, alerts, emergency_guides).
-- 8.  appointments: thêm cột "location" (địa điểm khám) — bị thiếu ở bản gốc.
-- 9.  Thêm bảng diet_logs (nhật ký ăn uống, tham chiếu foods_dictionary) và
--     exercise_logs (nhật ký vận động) — bị thiếu ở bản gốc.
-- 10. Thêm bảng notifications: đóng vai trò trạm trung chuyển & lưu trữ toàn bộ
--     tin nhắn/thông báo hệ thống gửi tới Bệnh nhân/Bác sĩ/Bệnh viện.
-- 11. Thêm bảng foods_dictionary theo đúng cấu trúc được cung cấp + index tìm kiếm.
-- 12. clinical_records.fasting_glucose, treatment_plans.target_fasting_glucose
--     đổi đơn vị sang mmol/L (DECIMAL(4,2)) để đồng bộ với daily_health_logs.
-- 13. Đồng bộ lại các Stored Procedure, Index liên quan đến những thay đổi trên
--     (sp_record_daily_health_log, sp_get_doctor_dashboard, sp_get_hospital_overview).
-- ============================================================
 
-- ============================================================
-- DATABASE: RemotePatientMonitoring (Hệ thống theo dõi bệnh nhân từ xa)
-- MÔ TẢ: Kịch bản cơ sở dữ liệu đã đồng bộ 100% với Tài liệu nghiệp vụ.
-- Đơn vị đường huyết: mmol/L | Hệ thống cảnh báo: 4 Mức (GREEN, YELLOW, ORANGE, RED)
-- ============================================================

USE master;
GO

IF EXISTS (SELECT name FROM sys.databases WHERE name = N'RemotePatientMonitoringVer2')
BEGIN
    ALTER DATABASE RemotePatientMonitoringVer2 SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE RemotePatientMonitoringVer2;
END
GO

CREATE DATABASE RemotePatientMonitoringVer2 COLLATE Vietnamese_CI_AS;
GO
USE RemotePatientMonitoringVer2;
GO

-- ============================================================
-- BƯỚC 2: CÁC BẢNG DỮ LIỆU CHÍNH
-- ============================================================

-- ------------------------------------------------------------
-- BẢNG 1.1: accounts (Tài khoản người dùng chung)
-- ÁP DỤNG NGHIỆP VỤ: Mục 1.1 (Bệnh viện tạo tài khoản bác sĩ mới) & Mục 3.1 (Bệnh nhân đăng ký trực tuyến)
-- ------------------------------------------------------------
CREATE TABLE accounts (
    id                  INT             NOT NULL IDENTITY(1,1), -- Khóa chính định danh tài khoản tự tăng
    email               VARCHAR(255)    NOT NULL,               -- Email dùng đăng nhập và nhận mật khẩu/OTP ban đầu (Mục 1.1 & 3.1)
    password_hash       VARCHAR(255)    NOT NULL,               -- Chuỗi mật khẩu đã được mã hóa một chiều
    role                VARCHAR(20)     NOT NULL,               -- Phân quyền hệ thống: PATIENT, DOCTOR, HOSPITAL_ADMIN
    is_email_verified   BIT             NOT NULL CONSTRAINT DF_accounts_verified DEFAULT 0, -- 0 = Chưa xác thực, 1 = Bệnh nhân đã xác thực OTP để kích hoạt (Mục 3.1)
    is_active           BIT             NOT NULL CONSTRAINT DF_accounts_active DEFAULT 1,   -- 1 = Hoạt động, 0 = Bị khóa/Vô hiệu hóa tài khoản (Mục 1.1)
    created_at          DATETIME2       NOT NULL CONSTRAINT DF_accounts_created_at DEFAULT SYSDATETIME(), -- Thời điểm tạo tài khoản
    updated_at          DATETIME2       NOT NULL CONSTRAINT DF_accounts_updated_at DEFAULT SYSDATETIME(), -- Thời điểm cập nhật tài khoản gần nhất

    CONSTRAINT PK_accounts PRIMARY KEY (id),
    CONSTRAINT UQ_accounts_email UNIQUE (email),
    CONSTRAINT CHK_accounts_role CHECK (role IN ('PATIENT', 'DOCTOR', 'HOSPITAL_ADMIN'))
);
GO

-- ------------------------------------------------------------
-- BẢNG 1.2: otp_codes (Quản lý mã xác thực)
-- ÁP DỤNG NGHIỆP VỤ: Mục 3.1 (Bệnh nhân tự tạo tài khoản và xác thực OTP để kích hoạt)
-- ------------------------------------------------------------
CREATE TABLE otp_codes (
    id                  INT             NOT NULL IDENTITY(1,1), -- Khóa chính bảng mã OTP
    email               VARCHAR(255)    NOT NULL,               -- Email nhận mã OTP để đối chiếu xác thực
    otp_code            VARCHAR(10)     NOT NULL,               -- Mã số xác thực hệ thống sinh ra
    otp_type            VARCHAR(20)     NOT NULL,               -- Loại mã: REGISTRATION (Đăng ký), PASSWORD_RESET (Quên mật khẩu)
    expires_at          DATETIME2       NOT NULL,               -- Thời điểm mã OTP hết hiệu lực
    is_used             BIT             NOT NULL CONSTRAINT DF_otp_codes_is_used DEFAULT 0, -- 0 = Chưa dùng, 1 = Đã nhập xác thực kích hoạt thành công
    created_at          DATETIME2       NOT NULL CONSTRAINT DF_otp_codes_created_at DEFAULT SYSDATETIME(), -- Thời điểm tạo mã

    CONSTRAINT PK_otp_codes PRIMARY KEY (id),
    CONSTRAINT CHK_otp_codes_type CHECK (otp_type IN ('REGISTRATION', 'PASSWORD_RESET'))
);
GO

-- ------------------------------------------------------------
-- BẢNG 1: hospitals (Bệnh viện / Đơn vị quản lý cấp cao nhất)
-- ÁP DỤNG NGHIỆP VỤ: Phần 1 (Bệnh viện là đơn vị quản lý hệ thống ở cấp cao nhất)
-- ------------------------------------------------------------
CREATE TABLE hospitals (
    id                  INT             NOT NULL IDENTITY(1,1), -- Khóa chính của bệnh viện
    account_id          INT             NOT NULL,               -- Trỏ tới bảng accounts (role: HOSPITAL_ADMIN, do đội kỹ thuật tạo thủ công)
    hospital_code       VARCHAR(50)     NOT NULL,               -- Mã định danh bệnh viện nội bộ
    full_name           NVARCHAR(255)   NOT NULL,               -- Tên hiển thị đầy đủ của bệnh viện
    address             NVARCHAR(500)   NULL,                   -- Địa chỉ trụ sở bệnh viện
    phone               VARCHAR(20)     NULL,                   -- Số điện thoại đường dây nóng
    is_active           BIT             NOT NULL CONSTRAINT DF_hospitals_is_active DEFAULT 1, -- Trạng thái hoạt động của bệnh viện
    created_at          DATETIME2       NOT NULL CONSTRAINT DF_hospitals_created_at DEFAULT SYSDATETIME(), -- Ngày thiết lập trên hệ thống
    updated_at          DATETIME2       NOT NULL CONSTRAINT DF_hospitals_updated_at DEFAULT SYSDATETIME(), -- Ngày cập nhật thông tin

    CONSTRAINT PK_hospitals PRIMARY KEY (id),
    CONSTRAINT UQ_hospitals_account_id UNIQUE (account_id),
    CONSTRAINT FK_hospitals_account_id FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT UQ_hospitals_hospital_code UNIQUE (hospital_code)
);
GO

-- ------------------------------------------------------------
-- BẢNG 2: doctors (Bác sĩ)
-- ÁP DỤNG NGHIỆP VỤ: Mục 1.1 (Theo dõi nhân sự, thiết lập hạn ngạch, vô hiệu hóa)
-- ------------------------------------------------------------
CREATE TABLE doctors (
    id                          INT             NOT NULL IDENTITY(1,1), -- Khóa chính của bác sĩ
    account_id                  INT             NOT NULL,               -- Trỏ tới bảng accounts để đăng nhập (role: DOCTOR)
    hospital_id                 INT             NOT NULL,               -- Bác sĩ này thuộc sự quản lý của bệnh viện nào
    doctor_code                 VARCHAR(50)     NOT NULL,               -- Mã định danh bác sĩ (Mục 1.1)
    full_name                   NVARCHAR(255)   NOT NULL,               -- Họ tên đầy đủ của bác sĩ (Mục 1.1)
    gender                      VARCHAR(10)     NULL,                   -- Giới tính bác sĩ (MALE, FEMALE, OTHER)
    phone                       VARCHAR(20)     NOT NULL,               -- Số điện thoại liên lạc (Mục 1.1)
    specialty                   NVARCHAR(255)   NULL,                   -- Chuyên khoa (Ví dụ: Nội tiết, Tim mạch)
    capacity_limit              INT             NOT NULL CONSTRAINT DF_doctors_capacity_limit DEFAULT 50, -- Hạn ngạch tối đa (Mục 1.1)
    current_patient_count       INT             NOT NULL CONSTRAINT DF_doctors_current_patient_count DEFAULT 0, -- Số bệnh nhân đang quản lý thực tế
    is_active                   BIT             NOT NULL CONSTRAINT DF_doctors_is_active DEFAULT 1, -- Trạng thái hoạt động (Mục 1.1)
    created_at                  DATETIME2       NOT NULL CONSTRAINT DF_doctors_created_at DEFAULT SYSDATETIME(),
    updated_at                  DATETIME2       NOT NULL CONSTRAINT DF_doctors_updated_at DEFAULT SYSDATETIME(),

    CONSTRAINT PK_doctors PRIMARY KEY (id),
    CONSTRAINT UQ_doctors_account_id UNIQUE (account_id),
    CONSTRAINT UQ_doctors_doctor_code UNIQUE (doctor_code),
    CONSTRAINT UQ_doctors_phone UNIQUE (phone),
    CONSTRAINT FK_doctors_account_id FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT FK_doctors_hospital_id FOREIGN KEY (hospital_id) REFERENCES hospitals(id),
    CONSTRAINT CHK_doctors_gender CHECK (gender IN ('MALE', 'FEMALE', 'OTHER') OR gender IS NULL), -- Ràng buộc giới tính
    CONSTRAINT CHK_doctors_capacity_limit CHECK (capacity_limit >= 1 AND capacity_limit <= 500),
    CONSTRAINT CHK_doctors_current_patient_count CHECK (current_patient_count >= 0),
    CONSTRAINT CHK_doctors_count_not_exceed_limit CHECK (current_patient_count <= capacity_limit)
);
GO

-- ------------------------------------------------------------
-- BẢNG 3: disease_profiles (Phân loại gói bệnh lý)
-- ÁP DỤNG NGHIỆP VỤ: Mục 2.1 (Phân quyền danh mục bệnh lý, điều khiển ẩn/hiện giao diện App bệnh nhân)
-- Xác định nhóm bệnh lý của bệnh nhân.
-- Lựa chọn này trực tiếp điều khiển giao diện nhập liệu trong app.
-- HYPERTENSION  : Chỉ nhập huyết áp và nhịp tim
-- DIABETES       : Chỉ nhập glucose
-- BOTH           : Bắt buộc nhập đầy đủ cả hai nhóm chỉ số
-- ------------------------------------------------------------
CREATE TABLE disease_profiles (
    id                          INT             NOT NULL IDENTITY(1,1), -- Khóa chính danh mục
    profile_code                VARCHAR(20)     NOT NULL,               -- Mã định danh bệnh: HYPERTENSION, DIABETES, BOTH
    profile_name                NVARCHAR(100)   NOT NULL,               -- Tên gói hiển thị (Ví dụ: Tăng huyết áp, Tiểu đường Type 2)
    description                 NVARCHAR(500)   NULL,                   -- Mô tả chi tiết về gói chỉ định này
    requires_bp_input           BIT             NOT NULL CONSTRAINT DF_disease_profiles_requires_bp DEFAULT 0, -- 1 = Bắt buộc nhập Huyết áp, 0 = Ẩn phần nhập huyết áp (Mục 2.1)
    requires_glucose_input      BIT             NOT NULL CONSTRAINT DF_disease_profiles_requires_glucose DEFAULT 0, -- 1 = Bắt buộc nhập Đường huyết, 0 = Ẩn phần nhập tiểu đường (Mục 2.1)
    is_active                   BIT             NOT NULL CONSTRAINT DF_disease_profiles_is_active DEFAULT 1, -- Trạng thái sử dụng của gói

    CONSTRAINT PK_disease_profiles PRIMARY KEY (id),
    CONSTRAINT UQ_disease_profiles_profile_code UNIQUE (profile_code),
    CONSTRAINT CHK_disease_profiles_profile_code CHECK (profile_code IN ('HYPERTENSION', 'DIABETES', 'BOTH'))
);
GO

INSERT INTO disease_profiles (profile_code, profile_name, description, requires_bp_input, requires_glucose_input)
VALUES
    ('HYPERTENSION', N'Tăng huyết áp',         N'Bệnh nhân chỉ theo dõi huyết áp và nhịp tim. Màn hình ẩn phần nhập tiểu đường.', 1, 0),
    ('DIABETES',     N'Tiểu đường Type 2',     N'Bệnh nhân chỉ theo dõi glucose. Màn hình ẩn phần nhập huyết áp.', 0, 1),
    ('BOTH',         N'Đồng mắc (Cả hai bệnh)', N'Bệnh nhân bắt buộc phải nhập đầy đủ cả hai nhóm chỉ số huyết áp và glucose.', 1, 1);
GO

-- ------------------------------------------------------------
-- BẢNG 4: patients (Hồ sơ Bệnh nhân)
-- ÁP DỤNG NGHIỆP VỤ: Mục 3.1 (Đăng ký tài khoản, Đặt lịch khám) & Mục 2.1 (Tạo/Cập nhật hồ sơ bệnh nhân, Phân loại bệnh lý)
-- Lưu thông tin cá nhân và trạng thái theo dõi của bệnh nhân.
-- status:
--   NEW      = Mới đăng ký, chỉ được đặt lịch khám
--   TREATING = Đang điều trị, mở đầy đủ tính năng
--   INACTIVE = Ngừng theo dõi
-- ------------------------------------------------------------
CREATE TABLE patients (
    id                          INT             NOT NULL IDENTITY(1,1), -- Khóa chính của bệnh nhân
    account_id                  INT             NOT NULL,               -- Trỏ tới bảng accounts để đăng nhập (role: PATIENT)
    hospital_id                 INT             NOT NULL,               -- Bệnh viện mà bệnh nhân này đăng ký theo dõi
    doctor_id                   INT             NULL,                   -- Bác sĩ trực tiếp phụ trách. Ràng buộc quyền xem Mục 2.7 (Chỉ xem bệnh nhân do mình phụ trách)
    disease_profile_id          INT             NULL,                   -- Gói phân loại bệnh lý do bác sĩ tích chọn (Mục 2.1)
    patient_code                VARCHAR(50)     NULL,                   -- Mã bệnh án nội bộ
    full_name                   NVARCHAR(255)   NOT NULL,               -- Họ tên đầy đủ của bệnh nhân
    date_of_birth               DATE            NULL,                   -- Ngày tháng năm sinh
    gender                      VARCHAR(10)     NULL,                   -- Giới tính bệnh nhân
    phone                       VARCHAR(20)     NOT NULL,               -- Số điện thoại dùng tạo tài khoản online (Mục 3.1)
    address                     NVARCHAR(500)   NULL,                   -- Địa chỉ liên hệ
    emergency_contact_name      NVARCHAR(255)   NULL,                   -- Tên người thân liên hệ khẩn cấp
    emergency_contact_phone     VARCHAR(20)     NULL,                   -- Số điện thoại người thân để hệ thống yêu cầu gọi (Mục 2.3)
    status                      VARCHAR(20)     NOT NULL CONSTRAINT DF_patients_status DEFAULT 'NEW', -- NEW (Mới đăng ký, chỉ mở đặt lịch), TREATING (Đang điều trị, mở đầy đủ tính năng) (Mục 3.1)
    registration_source         VARCHAR(20)     NOT NULL CONSTRAINT DF_patients_registration_source DEFAULT 'ONLINE', -- ONLINE (Đăng ký trực tuyến), CLINIC (Tạo tại viện)
    is_active                   BIT             NOT NULL CONSTRAINT DF_patients_is_active DEFAULT 1, -- Trạng thái theo dõi
    onboarded_at                DATETIME2       NULL,                   -- Thời điểm bác sĩ khám lâm sàng xong và bấm "Liên kết bệnh nhân vào danh sách theo dõi" (Mục 3.1)
    created_at                  DATETIME2       NOT NULL CONSTRAINT DF_patients_created_at DEFAULT SYSDATETIME(), -- Giờ tạo hồ sơ
    updated_at                  DATETIME2       NOT NULL CONSTRAINT DF_patients_updated_at DEFAULT SYSDATETIME(), -- Giờ sửa hồ sơ

    CONSTRAINT PK_patients PRIMARY KEY (id),
    CONSTRAINT UQ_patients_account_id UNIQUE (account_id),
    CONSTRAINT UQ_patients_phone UNIQUE (phone),
    CONSTRAINT FK_patients_account_id FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT FK_patients_hospital_id FOREIGN KEY (hospital_id) REFERENCES hospitals(id),
    CONSTRAINT FK_patients_doctor_id FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    CONSTRAINT FK_patients_disease_profile_id FOREIGN KEY (disease_profile_id) REFERENCES disease_profiles(id),
    CONSTRAINT CHK_patients_status CHECK (status IN ('NEW', 'TREATING', 'INACTIVE')),
    CONSTRAINT CHK_patients_gender CHECK (gender IN ('MALE', 'FEMALE', 'OTHER') OR gender IS NULL),
    CONSTRAINT CHK_patients_registration_source CHECK (registration_source IN ('ONLINE', 'CLINIC'))
);
GO

-- ------------------------------------------------------------
-- BẢNG 5: alert_thresholds (Cấu hình ngưỡng cảnh báo)
-- ÁP DỤNG NGHIỆP VỤ: Mục 1.2 (Bệnh viện thiết lập mặc định, Bác sĩ điều chỉnh cá nhân hóa) & Mục 2.3 (Khung chỉ số quy định cứng)
-- GREEN (an toàn) / YELLOW (chú ý) / ORANGE (nguy hiểm) / RED (cấp cứu).
-- Bệnh viện thiết lập khung ngưỡng toàn cục.
-- Bác sĩ được điều chỉnh ngưỡng riêng cho từng bệnh nhân
-- nhưng không được vượt ngoài khung tối thiểu/tối đa của bệnh viện.
-- scope: HOSPITAL = ngưỡng toàn viện, PATIENT = ngưỡng riêng cho bệnh nhân 
------------------------------------------------------------
CREATE TABLE alert_thresholds (
    id                              INT             NOT NULL IDENTITY(1,1), -- Khóa chính
    hospital_id                     INT             NOT NULL,               -- Bệnh viện thiết lập ngưỡng cảnh báo (Mục 1.2)
    patient_id                      INT             NULL,                   -- NULL = Mặc định toàn viện. Có ID = Bác sĩ điều chỉnh cá nhân hóa (Mục 1.2)
    scope                           VARCHAR(10)     NOT NULL CONSTRAINT DF_alert_thresholds_scope DEFAULT 'HOSPITAL', -- Phạm vi: HOSPITAL hoặc PATIENT
    metric_type                     VARCHAR(50)     NOT NULL,               -- Loại chỉ số áp dụng: GLUCOSE, BLOOD_PRESSURE, COMBINED
    
    -- Quy định cứng Mục 1.2 & 2.3: Ngưỡng Tiểu đường Type 2 (mmol/L)
    glucose_hypo_threshold          DECIMAL(4,2)    NULL,                   -- Level 1 (Hạ - RED): Dưới mức này (< 4.4 mmol/L)
    glucose_normal_max              DECIMAL(4,2)    NULL,                   -- Level 2 (Bình thường - GREEN): Từ hạ đến mức này (4.4 - 10.0)
    glucose_high_max                DECIMAL(4,2)    NULL,                   -- Level 3 (Cao - ORANGE): Từ bình thường đến mức này (10.0 - 16.0). Level 4 (Khẩn cấp - RED): > 16.0
    
    -- Quy định cứng Mục 1.2 & 2.3: Ngưỡng Tăng huyết áp tâm thu (mmHg)
    systolic_normal_max             INT             NULL,                   -- XANH LÁ (An toàn - Đạt mục tiêu): < 120
    systolic_warning_min            INT             NULL,                   -- VÀNG (Chú ý - Level 1): Từ 130
    systolic_warning_max            INT             NULL,                   -- VÀNG (Chú ý - Level 1): Đến 139
    systolic_danger_min             INT             NULL,                   -- CAM (Nguy hiểm - Level 2): Từ 140
    systolic_danger_max             INT             NULL,                   -- CAM (Nguy hiểm - Level 2): Đến 179
    systolic_emergency_threshold    INT             NULL,                   -- ĐỎ (Cấp cứu - Level 3): >= 180
    
    -- Quy định cứng Mục 1.2 & 2.3: Ngưỡng Tăng huyết áp tâm trương (mmHg)
    diastolic_normal_max            INT             NULL,                   -- XANH LÁ (An toàn - Đạt mục tiêu): < 80
    diastolic_warning_min           INT             NULL,                   -- VÀNG (Chú ý - Level 1): Từ 85
    diastolic_warning_max           INT             NULL,                   -- VÀNG (Chú ý - Level 1): Đến 89
    diastolic_danger_min            INT             NULL,                   -- CAM (Nguy hiểm - Level 2): Từ 90
    diastolic_danger_max            INT             NULL,                   -- CAM (Nguy hiểm - Level 2): Đến 109
    diastolic_emergency_threshold   INT             NULL,                   -- ĐỎ (Cấp cứu - Level 3): >= 110
    
    created_by_doctor_id            INT             NULL,                   -- Bác sĩ thiết lập ngưỡng cá nhân hóa
    created_at                      DATETIME2       NOT NULL CONSTRAINT DF_alert_thresholds_created_at DEFAULT SYSDATETIME(), -- Giờ thiết lập
    updated_at                      DATETIME2       NOT NULL CONSTRAINT DF_alert_thresholds_updated_at DEFAULT SYSDATETIME(), -- Giờ sửa đổi

    CONSTRAINT PK_alert_thresholds PRIMARY KEY (id),
    CONSTRAINT FK_alert_thresholds_hospital_id FOREIGN KEY (hospital_id) REFERENCES hospitals(id),
    CONSTRAINT FK_alert_thresholds_patient_id FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT FK_alert_thresholds_doctor_id FOREIGN KEY (created_by_doctor_id) REFERENCES doctors(id),
    CONSTRAINT CHK_alert_thresholds_scope CHECK (scope IN ('HOSPITAL', 'PATIENT')),
    CONSTRAINT CHK_alert_thresholds_metric_type CHECK (metric_type IN ('GLUCOSE', 'BLOOD_PRESSURE', 'HEART_RATE', 'COMBINED'))
);
GO

-- ------------------------------------------------------------
-- BẢNG 6: emergency_guides (Hướng dẫn xử lý khẩn cấp hệ thống)
-- ÁP DỤNG NGHIỆP VỤ: Mục 1.2 (Cấu hình xử lý khẩn cấp chung) & Mục 3.2 (Bệnh nhân truy cập nhanh để tự xử lý lập tức)
-- ------------------------------------------------------------
CREATE TABLE emergency_guides (
    id                          INT             NOT NULL IDENTITY(1,1), -- Khóa chính
    hospital_id                 INT             NOT NULL,               -- Hướng dẫn này do bệnh viện nào cấu hình (Mục 1.2)
    alert_level                 VARCHAR(10)     NOT NULL,               -- Kích hoạt khi chỉ số rơi vào vùng cảnh báo xấu: YELLOW, ORANGE, RED (Mục 3.2)
    metric_type                 VARCHAR(30)     NOT NULL,               -- Hướng dẫn áp dụng cho loại bệnh: GLUCOSE, BLOOD_PRESSURE, BOTH
    title                       NVARCHAR(255)   NOT NULL,               -- Tiêu đề cảnh báo (Mục 1.2)
    instruction_content         NVARCHAR(MAX)   NOT NULL,               -- Các bước sơ cứu, tư thế nằm, uống nước ấm, uống thuốc dự phòng (Mục 1.2)
    is_active                   BIT             NOT NULL CONSTRAINT DF_emergency_guides_is_active DEFAULT 1, -- Bật/tắt hiển thị
    created_at                  DATETIME2       NOT NULL CONSTRAINT DF_emergency_guides_created_at DEFAULT SYSDATETIME(), -- Giờ tạo
    updated_at                  DATETIME2       NOT NULL CONSTRAINT DF_emergency_guides_updated_at DEFAULT SYSDATETIME(), -- Giờ cập nhật

    CONSTRAINT PK_emergency_guides PRIMARY KEY (id),
    CONSTRAINT FK_emergency_guides_hospital_id FOREIGN KEY (hospital_id) REFERENCES hospitals(id),
    CONSTRAINT CHK_emergency_guides_alert_level CHECK (alert_level IN ('GREEN', 'YELLOW', 'ORANGE', 'RED')),
    CONSTRAINT CHK_emergency_guides_metric_type CHECK (metric_type IN ('GLUCOSE', 'BLOOD_PRESSURE', 'BOTH'))
);
GO

-- ------------------------------------------------------------
-- BẢNG MỚI: emergency_protocols (Cẩm nang Dấu hiệu nhận biết bệnh lý)
-- ÁP DỤNG NGHIỆP VỤ: Bổ sung cẩm nang giáo dục sức khỏe nhận biết tình trạng cấp cứu cho bệnh nhân.
-- ------------------------------------------------------------
CREATE TABLE emergency_protocols (
    id                          INT             NOT NULL IDENTITY(1,1), -- Khóa chính cẩm nang
    hospital_id                 INT             NOT NULL,               -- Do bệnh viện cấu hình
    condition_type              VARCHAR(30)     NOT NULL,               -- Nhóm bệnh lý: HYPERTENSIVE_CRISIS, HYPOGLYCEMIA, HYPERGLYCEMIA
    title                       NVARCHAR(255)   NOT NULL,               -- Tiêu đề cẩm nang
    warning_signs               NVARCHAR(MAX)   NOT NULL,               -- Dấu hiệu nhận biết triệu chứng xấu đột ngột
    instruction_content         NVARCHAR(MAX)   NOT NULL,               -- Hướng dẫn xử lý khẩn cấp tại chỗ
    is_active                   BIT             NOT NULL CONSTRAINT DF_emergency_protocols_is_active DEFAULT 1, -- Trạng thái hiển thị trên app
    created_at                  DATETIME2       NOT NULL CONSTRAINT DF_emergency_protocols_created_at DEFAULT SYSDATETIME(), -- Giờ tạo
    updated_at                  DATETIME2       NOT NULL CONSTRAINT DF_emergency_protocols_updated_at DEFAULT SYSDATETIME(), -- Giờ cập nhật

    CONSTRAINT PK_emergency_protocols PRIMARY KEY (id),
    CONSTRAINT FK_emergency_protocols_hospital_id FOREIGN KEY (hospital_id) REFERENCES hospitals(id),
    CONSTRAINT CHK_emergency_protocols_condition_type CHECK (condition_type IN ('HYPERTENSIVE_CRISIS', 'HYPOGLYCEMIA', 'HYPERGLYCEMIA'))
);
GO

-- ------------------------------------------------------------
-- BẢNG 7: clinical_records (Hồ sơ khám bệnh án điện tử tại viện)
-- ÁP DỤNG NGHIỆP VỤ: Mục 2.1 (Nhập kết quả khám tại viện, tạo hồ sơ bệnh án) & Mục 2.6 (Cập nhật hồ sơ sau tái khám)
-- ------------------------------------------------------------
CREATE TABLE clinical_records (
    id                          INT             NOT NULL IDENTITY(1,1), -- Khóa chính hồ sơ khám
    patient_id                  INT             NOT NULL,               -- Bệnh nhân được khám lâm sàng (Mục 3.1)
    doctor_id                   INT             NOT NULL,               -- Bác sĩ tiếp nhận bệnh nhân thực hiện khám lâm sàng (Mục 3.1)
    examination_date            DATETIME2       NOT NULL,               -- Ngày giờ bệnh nhân đến viện khám
    weight_kg                   DECIMAL(5,2)    NULL,                   -- Nhập Cân nặng kg (Mục 2.1)
    height_cm                   DECIMAL(5,2)    NULL,                   -- Nhập Chiều cao cm (Mục 2.1)
    bmi                         DECIMAL(5,2)    NULL,                   -- Chỉ số BMI tính toán (Mục 2.1)
    systolic_bp                 INT             NULL,                   -- Huyết áp tâm thu đo tại viện (Mục 2.1)
    diastolic_bp                INT             NULL,                   -- Huyết áp tâm trương đo tại viện (Mục 2.1)
    heart_rate                  INT             NULL,                   -- Nhịp tim đo tại viện (Mục 2.1)
    fasting_glucose             DECIMAL(4,2)    NULL,                   -- Đường huyết lúc đói xét nghiệm (đơn vị mmol/L) (Mục 2.1)
    hba1c                       DECIMAL(5,2)    NULL,                   -- Chỉ số HbA1c đánh giá đường 3 tháng (%) (Mục 2.1 & 2.6)
    diagnosis                   NVARCHAR(MAX)   NULL,                   -- Chẩn đoán bệnh nền và bệnh đi kèm (Mục 2.1 & 3.1)
    symptoms_noted              NVARCHAR(MAX)   NULL,                   -- Ghi nhận triệu chứng (Mục 3.1)
    doctor_notes                NVARCHAR(MAX)   NULL,                   -- Ghi chú nội bộ của bác sĩ
    is_initial_exam             BIT             NOT NULL CONSTRAINT DF_clinical_records_is_initial DEFAULT 0, -- Khởi tạo thông tin lâm sàng ban đầu (Mục 2.1) hay Tái khám (Mục 2.6)
    created_at                  DATETIME2       NOT NULL CONSTRAINT DF_clinical_records_created_at DEFAULT SYSDATETIME(), -- Giờ lưu hồ sơ
    updated_at                  DATETIME2       NOT NULL CONSTRAINT DF_clinical_records_updated_at DEFAULT SYSDATETIME(), -- Giờ cập nhật hồ sơ

    CONSTRAINT PK_clinical_records PRIMARY KEY (id),
    CONSTRAINT FK_clinical_records_patient_id FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT FK_clinical_records_doctor_id FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    CONSTRAINT CHK_clinical_records_weight CHECK (weight_kg IS NULL OR (weight_kg > 0 AND weight_kg < 500)),
    CONSTRAINT CHK_clinical_records_systolic_bp CHECK (systolic_bp IS NULL OR (systolic_bp >= 50 AND systolic_bp <= 300)),
    CONSTRAINT CHK_clinical_records_diastolic_bp CHECK (diastolic_bp IS NULL OR (diastolic_bp >= 30 AND diastolic_bp <= 200)),
    CONSTRAINT CHK_clinical_records_heart_rate CHECK (heart_rate IS NULL OR (heart_rate >= 20 AND heart_rate <= 300)),
    CONSTRAINT CHK_clinical_records_fasting_glucose CHECK (fasting_glucose IS NULL OR (fasting_glucose >= 1.0 AND fasting_glucose <= 33.3))
);
GO

-- ------------------------------------------------------------
-- BẢNG 8: nutrition_rules (Quy tắc dinh dưỡng)
-- ÁP DỤNG NGHIỆP VỤ: Mục 2.1 (Thiết lập Quy tắc Dinh dưỡng, khung giới hạn định lượng tùy thuộc bệnh lý)
-- món ăn cụ thể. Bệnh nhân tự chọn món từ foods_dictionary để khớp chỉ tiêu này.
-- ------------------------------------------------------------
CREATE TABLE nutrition_rules (
    id                          INT             NOT NULL IDENTITY(1,1), -- Khóa chính quy tắc
    patient_id                  INT             NOT NULL,               -- Bệnh nhân được áp dụng
    doctor_id                   INT             NOT NULL,               -- Bác sĩ thiết lập
    max_calories_per_day        INT             NOT NULL,               -- Khung giới hạn tổng calo/ngày (Mục 2.1)
    max_carbs_g                 DECIMAL(7,2)    NOT NULL,               -- Lượng tinh bột tối đa (Mục 2.1)
    max_salt_g                  DECIMAL(6,2)    NOT NULL,               -- Lượng muối giới hạn (Mục 2.1)
    min_fiber_g                 DECIMAL(6,2)    NOT NULL,               -- Tỷ lệ chất xơ (Mục 2.1)
    max_fat_g                   DECIMAL(7,2)    NULL,                   -- Giới hạn chất béo (nếu có)
    min_protein_g               DECIMAL(7,2)    NULL,                   -- Đạm tối thiểu (nếu có)
    daily_water_ml              INT             NULL CONSTRAINT DF_nutrition_rules_water DEFAULT 2000, -- Nước khuyến nghị
    additional_notes            NVARCHAR(MAX)   NULL,                   -- Những kiêng khem và nên làm do bác sĩ duyệt (Mục 2.1)
    is_current                  BIT             NOT NULL CONSTRAINT DF_nutrition_rules_is_current DEFAULT 1, -- 1 = Chế độ này bệnh nhân đang tuân thủ (Mục 2.1)
    effective_from              DATE            NOT NULL CONSTRAINT DF_nutrition_rules_effective_from DEFAULT CAST(SYSDATETIME() AS DATE), -- Ngày bắt đầu áp dụng
    created_at                  DATETIME2       NOT NULL CONSTRAINT DF_nutrition_rules_created_at DEFAULT SYSDATETIME(), -- Giờ tạo
    updated_at                  DATETIME2       NOT NULL CONSTRAINT DF_nutrition_rules_updated_at DEFAULT SYSDATETIME(), -- Giờ sửa

    CONSTRAINT PK_nutrition_rules PRIMARY KEY (id),
    CONSTRAINT FK_nutrition_rules_patient_id FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT FK_nutrition_rules_doctor_id FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    CONSTRAINT CHK_nutrition_rules_calories CHECK (max_calories_per_day >= 500 AND max_calories_per_day <= 5000),
    CONSTRAINT CHK_nutrition_rules_salt CHECK (max_salt_g >= 0 AND max_salt_g <= 20)
);
GO

-- ------------------------------------------------------------
-- BẢNG 9: treatment_plans (Kế hoạch điều trị & Phác đồ)
-- ÁP DỤNG NGHIỆP VỤ: Mục 2.1 (Tạo kế hoạch điều trị) & Mục 3.1 (Gửi phác đồ điều trị) & Phần 4 (Chỉ số mục tiêu)
-- [SỬA] target_fasting_glucose đổi sang mmol/L: DECIMAL(4,2)
-- [SỬA] Bỏ liên kết tới menus (đã xoá bảng menus)
-- Lưu phác đồ thuốc, lịch uống thuốc và mục tiêu vận động.
-- Mỗi bệnh nhân có DUY NHẤT một kế hoạch điều trị đang áp dụng.
-- ------------------------------------------------------------
CREATE TABLE treatment_plans (
    id                          INT             NOT NULL IDENTITY(1,1), -- Khóa chính phác đồ
    patient_id                  INT             NOT NULL,               -- Bệnh nhân nhận phác đồ
    doctor_id                   INT             NOT NULL,               -- Bác sĩ lên phác đồ
    nutrition_rule_id           INT             NULL,                   -- Trỏ sang bảng Quy tắc dinh dưỡng/Chế độ ăn (Mục 3.1)
    target_systolic_bp          INT             NULL,                   -- Chỉ số huyết áp mục tiêu cần đạt (Mục 2.1 & Phần 4)
    target_diastolic_bp         INT             NULL,                   -- Chỉ số huyết áp mục tiêu cần đạt (Mục 2.1 & Phần 4)
    target_fasting_glucose      DECIMAL(4,2)    NULL,                   -- Chỉ số đường huyết mục tiêu cần đạt mmol/L (Mục 2.1 & Phần 4)
    target_hba1c                DECIMAL(5,2)    NULL,                   -- Mục tiêu HbA1c
    target_weight_kg            DECIMAL(5,2)    NULL,                   -- Mục tiêu cân nặng hoặc BMI (Phần 4)
    target_steps                INT             NULL,                   -- Mục tiêu số bước chân hoặc thời gian vận động (Phần 4)
    target_sleep_hours          DECIMAL(4,2)    NULL,                   -- Mục tiêu giấc ngủ (Phần 4)
    medical_order               NVARCHAR(MAX)   NOT NULL,               -- Phác đồ thuốc tổng quát kèm lưu ý (Mục 3.1)
    exercise_goal               NVARCHAR(500)   NULL,                   -- Mức vận động mục tiêu (Mục 2.1 & 3.2)
    additional_notes            NVARCHAR(MAX)   NULL,                   -- Ghi chú bổ sung khác của bác sĩ
    is_current                  BIT             NOT NULL CONSTRAINT DF_treatment_plans_is_current DEFAULT 1, -- 1 = Đang áp dụng, 0 = Đã cập nhật phác đồ mới (Mục 2.6)
    effective_from              DATE            NOT NULL CONSTRAINT DF_treatment_plans_effective_from DEFAULT CAST(SYSDATETIME() AS DATE), -- Ngày bắt đầu
    created_at                  DATETIME2       NOT NULL CONSTRAINT DF_treatment_plans_created_at DEFAULT SYSDATETIME(), -- Giờ kê phác đồ
    updated_at                  DATETIME2       NOT NULL CONSTRAINT DF_treatment_plans_updated_at DEFAULT SYSDATETIME(), -- Giờ cập nhật

    CONSTRAINT PK_treatment_plans PRIMARY KEY (id),
    CONSTRAINT FK_treatment_plans_patient_id FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT FK_treatment_plans_doctor_id FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    CONSTRAINT FK_treatment_plans_nutrition_rule_id FOREIGN KEY (nutrition_rule_id) REFERENCES nutrition_rules(id)
);
GO

-- ------------------------------------------------------------
-- BẢNG MỚI: foods_dictionary (Từ điển thực phẩm chuẩn)
-- ÁP DỤNG NGHIỆP VỤ: Mục 3.2 (Xem thực đơn, danh sách món ăn gợi ý tham khảo trích xuất từ CSDL)
-- ------------------------------------------------------------
CREATE TABLE foods_dictionary (
    id              INT             IDENTITY(1,1) PRIMARY KEY, -- Khóa chính món ăn
    food_code       VARCHAR(50)     NOT NULL,               -- Mã định danh thực phẩm
    food_name       NVARCHAR(255)   NOT NULL,               -- Tên thực phẩm hiển thị tiếng Việt để chọn từ danh sách thả xuống (Mục 3.3)
    english_name    NVARCHAR(255)   NULL,                   -- Tên thực phẩm tiếng Anh
    water_g         DECIMAL(5,2)    NULL,                   -- Các chỉ số dinh dưỡng hiển thị rõ ràng của món ăn để tuân thủ (Mục 3.2)
    energy_kcal     INT             NULL,                   -- Năng lượng/Calo
    protein_g       DECIMAL(5,2)    NULL,                   -- Đạm
    lipid_g         DECIMAL(5,2)    NULL,                   -- Béo
    glucid_g        DECIMAL(5,2)    NULL,                   -- Tinh bột
    celluloza_g     DECIMAL(5,2)    NULL,                   -- Xơ
    ash_g           DECIMAL(5,2)    NULL,                   -- Tro
    is_active       BIT             NOT NULL DEFAULT 1,     -- Trạng thái sử dụng
    created_at      DATETIME2       NOT NULL DEFAULT SYSDATETIME(), -- Ngày nạp dữ liệu

    CONSTRAINT UQ_foods_dictionary_code UNIQUE (food_code)
);
GO
CREATE INDEX IX_foods_dictionary_search ON foods_dictionary (food_name) INCLUDE (food_code, energy_kcal, protein_g, lipid_g, glucid_g) WHERE is_active = 1;
GO

-- ------------------------------------------------------------
-- BẢNG MỚI: diet_logs (Nhật ký ăn uống thực tế)
-- ÁP DỤNG NGHIỆP VỤ: Mục 3.3 (Ghi nhận bữa ăn: Chọn món ăn từ danh sách thả xuống)
-- ------------------------------------------------------------
CREATE TABLE diet_logs (
    id              INT             NOT NULL IDENTITY(1,1), -- Khóa chính nhật ký ăn
    patient_id      INT             NOT NULL,               -- Bệnh nhân nạp liệu
    food_id         INT             NOT NULL,               -- Liên kết món ăn từ từ điển (Mục 3.3)
    log_date        DATE            NOT NULL,               -- Ngày nhập liệu ăn uống
    meal_type       VARCHAR(10)     NOT NULL,               -- Phân loại bữa ăn: BREAKFAST, LUNCH, DINNER, SNACK
    quantity_g      DECIMAL(6,2)    NOT NULL,               -- Khối lượng thực tế để hệ thống nhân với chỉ số trong foods_dictionary
    logged_at       DATETIME2       NOT NULL CONSTRAINT DF_diet_logs_logged_at DEFAULT SYSDATETIME(), -- Giờ nhập liệu

    CONSTRAINT PK_diet_logs PRIMARY KEY (id),
    CONSTRAINT FK_diet_logs_patient_id FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT FK_diet_logs_food_id FOREIGN KEY (food_id) REFERENCES foods_dictionary(id),
    CONSTRAINT CHK_diet_logs_meal_type CHECK (meal_type IN ('BREAKFAST', 'LUNCH', 'DINNER', 'SNACK')),
    CONSTRAINT CHK_diet_logs_quantity CHECK (quantity_g > 0)
);
GO
CREATE INDEX IX_diet_logs_patient_date ON diet_logs (patient_id, log_date DESC) INCLUDE (food_id, meal_type, quantity_g);
GO

-- ------------------------------------------------------------
-- BẢNG MỚI: exercise_logs (Nhật ký vận động)
-- ÁP DỤNG NGHIỆP VỤ: Mục 3.3 (Ghi nhận vận động: Chọn loại hình vận động, số bước chân, thời gian tập)
-- ------------------------------------------------------------
CREATE TABLE exercise_logs (
    id                  INT             NOT NULL IDENTITY(1,1), -- Khóa chính nhật ký tập
    patient_id          INT             NOT NULL,               -- Bệnh nhân tập luyện
    log_date            DATE            NOT NULL,               -- Ngày tập
    exercise_type       NVARCHAR(100)   NOT NULL,               -- Loại hình vận động (Mục 3.3)
    duration_minutes    INT             NULL,                   -- Thời gian tập luyện phút (Mục 3.3)
    steps_count         INT             NULL,                   -- Số bước chân vận động (Mục 3.3)
    logged_at           DATETIME2       NOT NULL CONSTRAINT DF_exercise_logs_logged_at DEFAULT SYSDATETIME(), -- Giờ nhập liệu

    CONSTRAINT PK_exercise_logs PRIMARY KEY (id),
    CONSTRAINT FK_exercise_logs_patient_id FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT CHK_exercise_logs_duration CHECK (duration_minutes IS NULL OR duration_minutes > 0),
    CONSTRAINT CHK_exercise_logs_steps CHECK (steps_count IS NULL OR steps_count >= 0)
);
GO
CREATE INDEX IX_exercise_logs_patient_date ON exercise_logs (patient_id, log_date DESC) INCLUDE (exercise_type, duration_minutes, steps_count);
GO

-- ------------------------------------------------------------
-- BẢNG 11: daily_health_logs (Nhật ký chỉ số sinh tồn)
-- ÁP DỤNG NGHIỆP VỤ: Mục 3.3 (Quy định cứng mốc thời gian, phương thức nhập liệu thông minh) & Mục 2.2 (Bác sĩ theo dõi dữ liệu thô)
-- ------------------------------------------------------------
CREATE TABLE daily_health_logs (
    id                          INT             NOT NULL IDENTITY(1,1), -- Khóa chính bản ghi đo lường
    patient_id                  INT             NOT NULL,               -- Bệnh nhân tự theo dõi
    log_date                    DATE            NOT NULL,               -- Ngày đo
    log_time                    DATETIME2       NOT NULL,               -- Thời điểm ghi nhận thực tế
    log_type                    VARCHAR(10)     NOT NULL,               -- Các mốc thời gian bắt buộc: MORNING (Sáng), EVENING (Tối), RANDOM (Tùy chọn) (Mục 3.3)
    systolic_bp                 INT             NULL,                   -- Huyết áp tâm thu hiển thị nhập liệu theo quyền (Mục 3.3)
    diastolic_bp                INT             NULL,                   -- Huyết áp tâm trương (Mục 3.3)
    heart_rate                  INT             NULL,                   -- Nhịp tim
    glucose_level               DECIMAL(4,2)    NULL,                   -- Tiểu đường / Glucose đơn vị mmol/L (Mục 3.3 & 1.2)
    input_method                VARCHAR(10)     NOT NULL CONSTRAINT DF_daily_health_logs_input_method DEFAULT 'MANUAL', -- Nhập số thủ công/thanh lăn (MANUAL) hoặc Chụp ảnh (OCR) (Mục 3.3)
    image_url                   VARCHAR(500)    NULL,                   -- Ảnh upload màn hình máy đo qua camera (Mục 3.3)
    is_ocr_validated            BIT             NOT NULL CONSTRAINT DF_daily_health_logs_ocr_valid DEFAULT 0, -- Hệ thống hiển thị lại giá trị OCR để người dùng "Xác nhận" (Mục 3.3)
    alert_level                 VARCHAR(10)     NULL,                   -- Phân loại cảnh báo do hệ thống tính toán (GREEN, YELLOW, ORANGE, RED)
    patient_notes               NVARCHAR(500)   NULL,                   -- Ghi chú tùy chọn của bệnh nhân
    is_alert_processed          BIT             NOT NULL CONSTRAINT DF_daily_health_logs_is_alert_processed DEFAULT 0, -- Trạng thái xử lý dữ liệu thô (Mục 2.2)
    alert_processed_at          DATETIME2       NULL,                   -- Giờ bác sĩ xử lý log
    alert_processed_by          INT             NULL,                   -- ID bác sĩ xử lý
    created_at                  DATETIME2       NOT NULL CONSTRAINT DF_daily_health_logs_created_at DEFAULT SYSDATETIME(), -- Giờ insert dữ liệu

    CONSTRAINT PK_daily_health_logs PRIMARY KEY (id),
    CONSTRAINT FK_daily_health_logs_patient_id FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT FK_daily_health_logs_alert_processed_by FOREIGN KEY (alert_processed_by) REFERENCES doctors(id),
    CONSTRAINT CHK_daily_health_logs_log_type CHECK (log_type IN ('MORNING', 'EVENING', 'RANDOM')),
    CONSTRAINT CHK_daily_health_logs_alert_level CHECK (alert_level IN ('GREEN', 'YELLOW', 'ORANGE', 'RED') OR alert_level IS NULL),
    CONSTRAINT CHK_daily_health_logs_input_method CHECK (input_method IN ('MANUAL', 'OCR')),
    CONSTRAINT CHK_daily_health_logs_systolic_bp CHECK (systolic_bp IS NULL OR (systolic_bp >= 50 AND systolic_bp <= 300)),
    CONSTRAINT CHK_daily_health_logs_diastolic_bp CHECK (diastolic_bp IS NULL OR (diastolic_bp >= 30 AND diastolic_bp <= 200)),
    CONSTRAINT CHK_daily_health_logs_heart_rate CHECK (heart_rate IS NULL OR (heart_rate >= 20 AND heart_rate <= 300)),
    CONSTRAINT CHK_daily_health_logs_glucose CHECK (glucose_level IS NULL OR (glucose_level >= 1.0 AND glucose_level <= 33.3))
);
GO

-- ------------------------------------------------------------
-- BẢNG NHẬT KÝ TUÂN THỦ: patient_medications & medication_logs & water_logs
-- ÁP DỤNG NGHIỆP VỤ: Mục 3.5 (Tính năng tích chọn hoàn thành xác nhận "Đã uống thuốc", "Đã uống đủ nước") & Mục 2.1 (Lên lịch uống thuốc)
-- ------------------------------------------------------------
CREATE TABLE patient_medications (
    id              INT IDENTITY(1,1) PRIMARY KEY,  -- Khóa chính danh mục thuốc của cá nhân
    patient_id      INT NOT NULL,                   -- Bệnh nhân được kê
    medicine_name   NVARCHAR(255) NOT NULL,         -- Tên thuốc theo chỉ định (Mục 2.1)
    dosage          NVARCHAR(100) NOT NULL,         -- Liều lượng thuốc uống (Mục 2.1)
    scheduled_time  VARCHAR(10)   NOT NULL,         -- Giờ uống thuốc (Mục 2.1)
    is_active       BIT NOT NULL DEFAULT 1,         -- Tình trạng còn uống không
    created_at      DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    updated_at      DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_patient_medications_patients FOREIGN KEY (patient_id) REFERENCES patients(id)
);
GO

CREATE TABLE medication_logs (
    id                      INT IDENTITY(1,1) PRIMARY KEY, -- Khóa chính log xác nhận thuốc
    patient_medication_id   INT NOT NULL,                  -- Trỏ sang đơn thuốc
    log_date                DATE NOT NULL,                 -- Ngày tích chọn
    is_taken                BIT NOT NULL DEFAULT 0,        -- Nút bấm tích chọn xác nhận Đã uống thuốc (Mục 3.5)
    taken_at                DATETIME2 NULL,                -- Giờ thực tế người dùng bấm hoàn thành
    CONSTRAINT FK_medication_logs_patient_medications FOREIGN KEY (patient_medication_id) REFERENCES patient_medications(id)
);
GO

CREATE TABLE water_logs (
    id          INT IDENTITY(1,1) PRIMARY KEY, -- Khóa chính log uống nước
    patient_id  INT NOT NULL,                  -- Bệnh nhân uống
    log_date    DATE NOT NULL,                 -- Ngày tích
    amount_ml   INT NOT NULL DEFAULT 0,        -- Cộng dồn nước để xác nhận Đã uống đủ nước (Mục 3.5)
    CONSTRAINT FK_water_logs_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT UQ_patient_water_date UNIQUE (patient_id, log_date)
);
GO

CREATE NONCLUSTERED INDEX IX_medication_logs_med_date ON medication_logs (patient_medication_id, log_date) INCLUDE (is_taken, taken_at);
GO
CREATE NONCLUSTERED INDEX IX_patient_medications_patient_active ON patient_medications (patient_id, is_active) INCLUDE (medicine_name, dosage, scheduled_time);
GO

-- ------------------------------------------------------------
-- BẢNG 12: alerts (Trung tâm Xử lý Cảnh báo hệ thống)
-- ÁP DỤNG NGHIỆP VỤ: Mục 2.3 (Hệ thống tự động sinh bản ghi nếu đạt Mức 3-Cam hoặc Mức 4-Đỏ để báo Bác sĩ)
--XANH LÁ – An toàn (Đạt mục tiêu level 1 )
--VÀNG – Chú ý (Level 2)
--CAM – Nguy hiểm (Level 3)
--ĐỎ – Cấp cứu / Nguy kịch (Level 4)
-- ------------------------------------------------------------
CREATE TABLE alerts (
    id                          INT             NOT NULL IDENTITY(1,1), -- Khóa chính sự kiện
    patient_id                  INT             NOT NULL,               -- Bệnh nhân có chỉ số vi phạm
    doctor_id                   INT             NOT NULL,               -- Bác sĩ trực tiếp nhận xử lý (Mục 2.3)
    health_log_id               INT             NULL,                   -- Trỏ sang nhật ký sinh tồn chứa chỉ số vi phạm
    alert_level                 INT             NOT NULL,               -- Phân cấp nghiêm trọng theo Mục 2.3: 1, 2, 3 (Nguy hiểm), 4 (Khẩn cấp)
    alert_color                 VARCHAR(10)     NOT NULL,               -- Cảnh báo đổi màu: GREEN, YELLOW, ORANGE, RED (Mục 2.3)
    metric_type                 VARCHAR(30)     NOT NULL,               -- Cảnh báo dành cho: GLUCOSE, BLOOD_PRESSURE, BOTH
    metric_value                NVARCHAR(200)   NOT NULL,               -- Giá trị nguyên bản lúc báo động
    threshold_violated          NVARCHAR(200)   NOT NULL,               -- Vi phạm ngưỡng nào
    alert_message               NVARCHAR(500)   NOT NULL,               -- Lời nhắn cho bác sĩ
    is_resolved                 BIT             NOT NULL CONSTRAINT DF_alerts_is_resolved DEFAULT 0, -- 0 = Chưa xem, 1 = Bác sĩ kiểm tra lại dữ liệu và điều chỉnh (Mục 2.3)
    resolved_at                 DATETIME2       NULL,                   -- Giờ bác sĩ can thiệp
    resolved_by_doctor_id       INT             NULL,                   -- ID bác sĩ giải quyết
    resolution_notes            NVARCHAR(500)   NULL,                   -- Ghi chú can thiệp (VD: Hẹn tái khám) (Mục 2.3)
    triggered_at                DATETIME2       NOT NULL CONSTRAINT DF_alerts_triggered_at DEFAULT SYSDATETIME(), -- Giờ kích hoạt
    created_at                  DATETIME2       NOT NULL CONSTRAINT DF_alerts_created_at DEFAULT SYSDATETIME(),   -- Giờ lưu hệ thống

    CONSTRAINT PK_alerts PRIMARY KEY (id),
    CONSTRAINT FK_alerts_patient_id FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT FK_alerts_doctor_id FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    CONSTRAINT FK_alerts_health_log_id FOREIGN KEY (health_log_id) REFERENCES daily_health_logs(id),
    CONSTRAINT FK_alerts_resolved_by_doctor_id FOREIGN KEY (resolved_by_doctor_id) REFERENCES doctors(id),
    CONSTRAINT CHK_alerts_alert_level CHECK (alert_level IN (1, 2, 3, 4)),
    CONSTRAINT CHK_alerts_alert_color CHECK (alert_color IN ('GREEN', 'YELLOW', 'ORANGE', 'RED')),
    CONSTRAINT CHK_alerts_metric_type CHECK (metric_type IN ('GLUCOSE', 'BLOOD_PRESSURE', 'HEART_RATE', 'BOTH'))
);
GO

-- ------------------------------------------------------------
-- BẢNG 13: appointments (Quản lý Lịch hẹn khám)
-- ÁP DỤNG NGHIỆP VỤ: Mục 3.1 (Đặt lịch, Chọn lịch có sẵn), Mục 2.6 (Hủy/thay đổi lịch, Duyệt yêu cầu) & Mục 3.2 (Địa điểm hẹn)
-- [SỬA] Thêm cột "location" (địa điểm khám) — bị thiếu ở bản gốc.
-- status: PENDING, ACCEPTED, REJECTED, COMPLETED, CANCELLED
-- ------------------------------------------------------------
CREATE TABLE appointments (
    id                          INT             NOT NULL IDENTITY(1,1), -- Khóa chính lịch hẹn
    patient_id                  INT             NOT NULL,               -- Bệnh nhân khám
    doctor_id                   INT             NOT NULL,               -- Bác sĩ tiếp nhận (Mục 3.1)
    appointment_time            DATETIME2       NOT NULL,               -- Lịch hẹn khám theo thời gian (Mục 3.1)
    location                    NVARCHAR(255)   NULL,                   -- Xem rõ địa điểm hẹn gặp (Mục 3.2)
    status                      VARCHAR(15)     NOT NULL CONSTRAINT DF_appointments_status DEFAULT 'PENDING', -- Trạng thái lịch: PENDING, ACCEPTED, REJECTED, COMPLETED, CANCELLED
    appointment_type            VARCHAR(20)     NOT NULL CONSTRAINT DF_appointments_type DEFAULT 'CHECKUP',   -- Phân loại khám
    created_by                  VARCHAR(10)     NOT NULL CONSTRAINT DF_appointments_created_by DEFAULT 'PATIENT', -- PATIENT (Bệnh nhân chọn), DOCTOR (Bác sĩ lên lịch tái khám) (Mục 2.6)
    patient_requested_time      DATETIME2       NULL,                   -- Giờ mong muốn khác nếu xin đổi (Mục 3.6)
    patient_request_reason      NVARCHAR(MAX)   NULL,                   -- Lý do gửi yêu cầu đổi lịch (Mục 3.6)
    doctor_note                 NVARCHAR(MAX)   NULL,                   -- Ghi chú của bác sĩ cho buổi khám
    rejection_reason            NVARCHAR(500)   NULL,                   -- Lý do Bệnh viện hủy/thay đổi lịch (Mục 2.6)
    reminder_sent_2days         BIT             NOT NULL CONSTRAINT DF_appointments_reminder_2days DEFAULT 0, -- Tự động nhắc nhở trước 02 ngày (Mục 3.4)
    completed_at                DATETIME2       NULL,                   -- Giờ hoàn thành khám
    created_at                  DATETIME2       NOT NULL CONSTRAINT DF_appointments_created_at DEFAULT SYSDATETIME(), -- Giờ tạo lịch
    updated_at                  DATETIME2       NOT NULL CONSTRAINT DF_appointments_updated_at DEFAULT SYSDATETIME(), -- Giờ cập nhật

    CONSTRAINT PK_appointments PRIMARY KEY (id),
    CONSTRAINT FK_appointments_patient_id FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT FK_appointments_doctor_id FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    CONSTRAINT CHK_appointments_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT CHK_appointments_type CHECK (appointment_type IN ('CHECKUP', 'INITIAL', 'FOLLOWUP')),
    CONSTRAINT CHK_appointments_created_by CHECK (created_by IN ('PATIENT', 'DOCTOR'))
);
GO

-- ------------------------------------------------------------
-- BẢNG 14: change_requests (Yêu cầu thay đổi từ Bệnh nhân)
-- ÁP DỤNG NGHIỆP VỤ: Mục 3.6 (Bệnh nhân gửi yêu cầu) & Mục 2.5 (Bác sĩ nhận quy tắc tiếp nhận trực tiếp)
-- request_type: TREATMENT_PLAN = đổi phác đồ, SCHEDULE = đổi lịch sinh hoạt
-- ------------------------------------------------------------
CREATE TABLE change_requests (
    id                          INT             NOT NULL IDENTITY(1,1), -- Khóa chính đơn
    patient_id                  INT             NOT NULL,               -- Bệnh nhân gửi yêu cầu (Mục 3.6)
    doctor_id                   INT             NOT NULL,               -- Gửi trực tiếp tới giao diện duyệt của Bác sĩ (Mục 3.6)
    request_type                VARCHAR(20)     NOT NULL,               -- Phân loại: TREATMENT_PLAN, SCHEDULE, DIET
    patient_reason              NVARCHAR(MAX)   NOT NULL,               -- Nguyên do chi tiết bệnh nhân viết/nói mong muốn (Mục 3.6)
    status                      VARCHAR(15)     NOT NULL CONSTRAINT DF_change_requests_status DEFAULT 'PENDING', -- Trạng thái duyệt của bác sĩ
    doctor_response             NVARCHAR(MAX)   NULL,                   -- Phản hồi hoặc nội dung bác sĩ đã tiến hành chỉnh sửa cập nhật lại (Mục 2.5)
    processed_at                DATETIME2       NULL,                   -- Giờ bác sĩ duyệt
    created_at                  DATETIME2       NOT NULL CONSTRAINT DF_change_requests_created_at DEFAULT SYSDATETIME(), -- Giờ gửi
    updated_at                  DATETIME2       NOT NULL CONSTRAINT DF_change_requests_updated_at DEFAULT SYSDATETIME(), -- Giờ update

    CONSTRAINT PK_change_requests PRIMARY KEY (id),
    CONSTRAINT FK_change_requests_patient_id FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT FK_change_requests_doctor_id FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    CONSTRAINT CHK_change_requests_request_type CHECK (request_type IN ('TREATMENT_PLAN', 'SCHEDULE', 'DIET', 'APPOINTMENT')),
    CONSTRAINT CHK_change_requests_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'IN_REVIEW'))
);
GO

-- ------------------------------------------------------------
-- BẢNG MỚI: notifications (Trung tâm Thông báo Đa kênh)
-- ÁP DỤNG NGHIỆP VỤ: Mục 3.4 (Nhắc nhở), Mục 2.6 (Thông báo hủy/đổi lịch) & Mục 2.3 (Thông báo cảnh báo)
-- Trạm trung chuyển & lưu trữ TOÀN BỘ thông báo/tin nhắn của hệ thống
-- (nhắc thuốc, nhắc nhập liệu, nhắc tái khám, cảnh báo, phản hồi yêu cầu...).
-- ------------------------------------------------------------
CREATE TABLE notifications (
    id                          BIGINT          NOT NULL IDENTITY(1,1), -- Khóa chính luồng thông báo
    recipient_type              VARCHAR(20)     NOT NULL,               -- Đích nhận: PATIENT, DOCTOR, HOSPITAL_ADMIN
    recipient_id                INT             NOT NULL,               -- ID người nhận
    notification_type           VARCHAR(50)     NOT NULL,               -- Loại: MEDICATION_REMINDER (Nhắc uống thuốc), LOG_REMINDER (Nhắc nhập liệu), APPOINTMENT_REMINDER (Nhắc khám) (Mục 3.4)
    title                       NVARCHAR(255)   NOT NULL,               -- Tiêu đề push notification
    content                     NVARCHAR(MAX)   NOT NULL,               -- Gửi thông báo kèm lý do (Mục 2.6) hoặc nội dung cảnh báo (Mục 2.3)
    channel                     VARCHAR(10)     NOT NULL CONSTRAINT DF_notifications_channel DEFAULT 'IN_APP', -- IN_APP, PUSH, EMAIL
    related_entity_type         VARCHAR(50)     NULL,                   -- Tham chiếu tính năng (Ví dụ: APPOINTMENT, ALERT)
    related_entity_id           INT             NULL,                   -- ID bản ghi tương ứng để chuyển hướng màn hình
    status                      VARCHAR(10)     NOT NULL CONSTRAINT DF_notifications_status DEFAULT 'PENDING', -- Trạng thái đẩy thông báo
    is_read                     BIT             NOT NULL CONSTRAINT DF_notifications_is_read DEFAULT 0, -- Quản lý dấu chấm chưa đọc
    sent_at                     DATETIME2       NULL,                   -- Thời gian gửi thành công
    read_at                     DATETIME2       NULL,                   -- Thời gian user mở thông báo
    created_at                  DATETIME2       NOT NULL CONSTRAINT DF_notifications_created_at DEFAULT SYSDATETIME(), -- Thời gian sinh luồng

    CONSTRAINT PK_notifications PRIMARY KEY (id),
    CONSTRAINT CHK_notifications_recipient_type CHECK (recipient_type IN ('PATIENT', 'DOCTOR', 'HOSPITAL_ADMIN')),
    CONSTRAINT CHK_notifications_channel CHECK (channel IN ('IN_APP', 'PUSH', 'EMAIL', 'SMS')),
    CONSTRAINT CHK_notifications_status CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'READ'))
);
GO
CREATE INDEX IX_notifications_recipient_unread ON notifications (recipient_type, recipient_id, is_read, created_at DESC) INCLUDE (notification_type, title);
GO

-- ------------------------------------------------------------
-- BẢNG 16: system_logs (Nhật ký lỗi Hệ thống)
-- ÁP DỤNG NGHIỆP VỤ: Phần 6 (Cơ chế dự phòng khi LLM API gặp sự cố gián đoạn)
-- ------------------------------------------------------------
CREATE TABLE system_logs (
    id                          BIGINT          NOT NULL IDENTITY(1,1), -- Khóa chính log sự cố
    log_level                   VARCHAR(10)     NOT NULL,               -- INFO, WARNING, ERROR, CRITICAL
    module_name                 VARCHAR(100)    NOT NULL,               -- Module phát sinh sự cố (Ví dụ: LLM API, Cloud Vision API) (Phần 6 & 3.3)
    event_code                  VARCHAR(100)    NULL,                   -- Mã lỗi
    message                     NVARCHAR(MAX)   NOT NULL,               -- Ghi nhận lỗi gián đoạn hoặc sập dịch vụ (Phần 6)
    related_patient_id          INT             NULL,                   -- Gắn kết với user
    related_doctor_id           INT             NULL,                   -- Gắn kết với user
    related_entity_type         VARCHAR(50)     NULL,                   -- Ngữ cảnh lỗi
    related_entity_id           INT             NULL,                   -- ID ngữ cảnh
    additional_data             NVARCHAR(MAX)   NULL,                   -- Trace lỗi Json
    created_at                  DATETIME2       NOT NULL CONSTRAINT DF_system_logs_created_at DEFAULT SYSDATETIME(), -- Giờ lỗi

    CONSTRAINT PK_system_logs PRIMARY KEY (id),
    CONSTRAINT CHK_system_logs_log_level CHECK (log_level IN ('INFO', 'WARNING', 'ERROR', 'CRITICAL'))
);
GO

-- ------------------------------------------------------------
-- BẢNG 17: audit_trails (Nhật ký kiểm toán Hệ thống)
-- ÁP DỤNG NGHIỆP VỤ: Quản lý minh bạch và lưu vết mọi thay đổi dữ liệu của người dùng y tế (Phục vụ thanh tra bệnh viện)
-- ------------------------------------------------------------
CREATE TABLE audit_trails (
    id                          BIGINT          NOT NULL IDENTITY(1,1), -- Khóa chính lịch sử
    actor_type                  VARCHAR(20)     NOT NULL,               -- Vai trò người dùng thao tác: DOCTOR, PATIENT, HOSPITAL_ADMIN
    actor_id                    INT             NOT NULL,               -- ID nhân sự thao tác
    action                      VARCHAR(100)    NOT NULL,               -- Thao tác thực hiện
    target_table                VARCHAR(100)    NOT NULL,               -- Sửa trên bảng nào
    target_record_id            INT             NOT NULL,               -- ID bản ghi bị sửa
    old_value                   NVARCHAR(MAX)   NULL,                   -- Chuỗi JSON dữ liệu trước khi sửa để làm bằng chứng
    new_value                   NVARCHAR(MAX)   NULL,                   -- Chuỗi JSON sau khi sửa
    ip_address                  VARCHAR(45)     NULL,                   -- Nguồn IP thao tác
    device_info                 NVARCHAR(255)   NULL,                   -- Thông tin máy móc thao tác
    notes                       NVARCHAR(500)   NULL,                   -- Giải trình lưu vết
    created_at                  DATETIME2       NOT NULL CONSTRAINT DF_audit_trails_created_at DEFAULT SYSDATETIME(), -- Giờ kiểm toán

    CONSTRAINT PK_audit_trails PRIMARY KEY (id),
    CONSTRAINT CHK_audit_trails_actor_type CHECK (actor_type IN ('DOCTOR', 'PATIENT', 'HOSPITAL_ADMIN'))
);
GO

-- ============================================================
-- BƯỚC 3: INDEX TỐI ƯU VÀ BƯỚC 4: STORED PROCEDURES (Bảo toàn)
-- ============================================================
CREATE INDEX IX_otp_codes_validation ON otp_codes (email, otp_type, is_used, expires_at) INCLUDE (otp_code);
GO
CREATE INDEX IX_doctors_available_capacity ON doctors (hospital_id, current_patient_count, capacity_limit) INCLUDE (doctor_code, full_name, specialty) WHERE is_active = 1;
GO
CREATE INDEX IX_doctors_workload_monitoring ON doctors (is_active, hospital_id) INCLUDE (doctor_code, full_name, current_patient_count, capacity_limit);
GO
CREATE INDEX IX_patients_doctor_status ON patients (doctor_id, status) INCLUDE (patient_code, full_name, phone, disease_profile_id, is_active) WHERE is_active = 1;
GO
CREATE INDEX IX_patients_account_login ON patients (account_id) INCLUDE (id, status, is_active);
GO
CREATE INDEX IX_patients_hospital_overview ON patients (hospital_id, status, is_active);
GO
CREATE INDEX IX_daily_health_logs_patient_date ON daily_health_logs (patient_id, log_date DESC) INCLUDE (log_type, systolic_bp, diastolic_bp, heart_rate, glucose_level, alert_level, is_alert_processed);
GO
CREATE INDEX IX_daily_health_logs_unprocessed_alerts ON daily_health_logs (patient_id, alert_level, is_alert_processed) INCLUDE (log_date, log_time, systolic_bp, diastolic_bp, glucose_level) WHERE is_alert_processed = 0 AND alert_level IS NOT NULL;
GO
CREATE INDEX IX_alerts_doctor_unresolved ON alerts (doctor_id, alert_level DESC, triggered_at DESC) INCLUDE (patient_id, alert_color, metric_type, alert_message) WHERE is_resolved = 0;
GO
CREATE INDEX IX_alerts_hospital_overview ON alerts (is_resolved, alert_level, triggered_at DESC);
GO
CREATE INDEX IX_appointments_patient_upcoming ON appointments (patient_id, appointment_time ASC) INCLUDE (doctor_id, status, appointment_type, location) WHERE status IN ('PENDING', 'ACCEPTED');
GO
CREATE INDEX IX_appointments_doctor_pending ON appointments (doctor_id, status, appointment_time ASC) INCLUDE (patient_id, appointment_type, created_by) WHERE status = 'PENDING';
GO
CREATE INDEX IX_system_logs_time_level ON system_logs (created_at DESC, log_level) INCLUDE (module_name, event_code, related_patient_id);
GO
CREATE INDEX IX_audit_trails_actor_action ON audit_trails (actor_type, actor_id, created_at DESC) INCLUDE (action, target_table, target_record_id);
GO
CREATE INDEX IX_audit_trails_target ON audit_trails (target_table, target_record_id, created_at DESC);
GO

-- ============================================================
-- BƯỚC 4: STORED PROCEDURES
-- ============================================================
 
-- ------------------------------------------------------------
-- SP 1: sp_assign_patient_to_doctor  (không đổi logic)
-- ------------------------------------------------------------
CREATE OR ALTER PROCEDURE sp_assign_patient_to_doctor
    @patient_id         INT,
    @doctor_id          INT,
    @actor_id           INT,
    @actor_type         VARCHAR(20),
    @result_message     NVARCHAR(500)   OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
 
    DECLARE @doctor_is_active       BIT;
    DECLARE @doctor_capacity_limit  INT;
    DECLARE @doctor_current_count   INT;
    DECLARE @doctor_full_name       NVARCHAR(255);
    DECLARE @patient_exists         INT;
    DECLARE @patient_current_doctor INT;
 
    BEGIN TRANSACTION;
    BEGIN TRY
 
        SELECT
            @doctor_is_active       = is_active,
            @doctor_capacity_limit  = capacity_limit,
            @doctor_current_count   = current_patient_count,
            @doctor_full_name       = full_name
        FROM doctors WITH (UPDLOCK)
        WHERE id = @doctor_id;
 
        IF @doctor_is_active IS NULL
        BEGIN
            ROLLBACK TRANSACTION;
            SET @result_message = N'Lỗi: Không tìm thấy bác sĩ.';
            RETURN -1;
        END
 
        IF @doctor_is_active = 0
        BEGIN
            ROLLBACK TRANSACTION;
            SET @result_message = N'Lỗi: Tài khoản bác sĩ đã bị vô hiệu hóa.';
            RETURN -2;
        END
 
        IF @doctor_current_count >= @doctor_capacity_limit
        BEGIN
            ROLLBACK TRANSACTION;
            SET @result_message = N'Lỗi: Bác sĩ đã đạt ngưỡng tải tối đa.';
            RETURN -3;
        END
 
        SELECT
            @patient_exists         = id,
            @patient_current_doctor = doctor_id
        FROM patients WITH (UPDLOCK)
        WHERE id = @patient_id;
 
        IF @patient_exists IS NULL
        BEGIN
            ROLLBACK TRANSACTION;
            SET @result_message = N'Lỗi: Không tìm thấy bệnh nhân.';
            RETURN -4;
        END
 
        IF @patient_current_doctor IS NOT NULL AND @patient_current_doctor <> @doctor_id
        BEGIN
            ROLLBACK TRANSACTION;
            SET @result_message = N'Lỗi: Bệnh nhân đã có bác sĩ phụ trách. Cần chuyển giao trước.';
            RETURN -5;
        END
 
        IF @patient_current_doctor = @doctor_id
        BEGIN
            ROLLBACK TRANSACTION;
            SET @result_message = N'Thông báo: Bệnh nhân đã được gán cho bác sĩ này rồi.';
            RETURN 0;
        END
 
        UPDATE patients
        SET doctor_id = @doctor_id, status = 'TREATING', onboarded_at = SYSDATETIME(), updated_at = SYSDATETIME()
        WHERE id = @patient_id;
 
        UPDATE doctors
        SET current_patient_count = current_patient_count + 1, updated_at = SYSDATETIME()
        WHERE id = @doctor_id;
 
        INSERT INTO audit_trails (actor_type, actor_id, action, target_table, target_record_id, new_value)
        VALUES (
            @actor_type, @actor_id, 'ASSIGN_PATIENT_TO_DOCTOR', 'patients', @patient_id,
            N'{"doctor_id":' + CAST(@doctor_id AS VARCHAR) + ', "status":"TREATING"}'
        );
 
        COMMIT TRANSACTION;
        SET @result_message = N'Thành công: Đã gán bệnh nhân cho bác sĩ [' + @doctor_full_name + '].';
        RETURN 0;
 
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        SET @result_message = N'Lỗi hệ thống khi gán bệnh nhân: ' + ERROR_MESSAGE();
        RETURN -99;
    END CATCH;
END;
GO
 
-- ------------------------------------------------------------
-- SP 2: sp_unassign_patient_from_doctor (không đổi logic)
-- ------------------------------------------------------------
CREATE OR ALTER PROCEDURE sp_unassign_patient_from_doctor
    @patient_id         INT,
    @unassigned_by      INT,
    @actor_type         VARCHAR(20),
    @reason             NVARCHAR(500),
    @result_message     NVARCHAR(500)   OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
 
    DECLARE @current_doctor_id      INT;
    DECLARE @doctor_current_count   INT;
 
    SELECT @current_doctor_id = doctor_id
    FROM patients
    WHERE id = @patient_id;
 
    IF @current_doctor_id IS NULL
    BEGIN
        SET @result_message = N'Thông báo: Bệnh nhân chưa được gán cho bác sĩ nào.';
        RETURN 0;
    END
 
    SELECT @doctor_current_count = current_patient_count
    FROM doctors
    WHERE id = @current_doctor_id;
 
    BEGIN TRANSACTION;
    BEGIN TRY
 
        UPDATE patients
        SET
            doctor_id   = NULL,
            status      = 'INACTIVE',
            updated_at  = SYSDATETIME()
        WHERE id = @patient_id;
 
        UPDATE doctors
        SET
            current_patient_count   = CASE WHEN current_patient_count > 0
                                           THEN current_patient_count - 1
                                           ELSE 0 END,
            updated_at              = SYSDATETIME()
        WHERE id = @current_doctor_id;
 
        INSERT INTO audit_trails (actor_type, actor_id, action, target_table, target_record_id, old_value, notes)
        VALUES (
            @actor_type,
            @unassigned_by,
            'UNASSIGN_PATIENT_FROM_DOCTOR',
            'patients',
            @patient_id,
            N'{"doctor_id":' + CAST(@current_doctor_id AS VARCHAR) + '}',
            @reason
        );
 
        COMMIT TRANSACTION;
 
        SET @result_message = N'Thành công: Đã gỡ bệnh nhân (ID = ' + CAST(@patient_id AS VARCHAR)
                             + N') khỏi bác sĩ (ID = ' + CAST(@current_doctor_id AS VARCHAR)
                             + N'). Dữ liệu y tế được giữ nguyên.';
        RETURN 0;
 
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        SET @result_message = N'Lỗi hệ thống khi gỡ gán bệnh nhân: ' + ERROR_MESSAGE();
        RETURN -99;
    END CATCH;
 
END;
GO
 
-- ------------------------------------------------------------
-- SP 3: sp_get_available_doctors (không đổi logic)
-- ------------------------------------------------------------
CREATE OR ALTER PROCEDURE sp_get_available_doctors
    @hospital_id    INT             = NULL,
    @specialty      NVARCHAR(255)   = NULL
AS
BEGIN
    SET NOCOUNT ON;
 
    SELECT
        d.id                    AS doctor_id,
        d.doctor_code,
        d.full_name             AS doctor_name,
        d.specialty,
        d.phone,
        d.capacity_limit,
        d.current_patient_count,
        d.capacity_limit - d.current_patient_count  AS available_slots,
        CAST(
            (CAST(d.current_patient_count AS FLOAT) / CAST(d.capacity_limit AS FLOAT)) * 100
        AS DECIMAL(5,2))                            AS workload_percentage,
        h.full_name             AS hospital_name
    FROM doctors d
    INNER JOIN hospitals h ON d.hospital_id = h.id
    WHERE
        d.is_active = 1
        AND d.current_patient_count < d.capacity_limit
        AND h.is_active = 1
        AND (@hospital_id IS NULL OR d.hospital_id = @hospital_id)
        AND (@specialty IS NULL OR d.specialty = @specialty)
    ORDER BY
        workload_percentage ASC,
        d.full_name ASC;
 
END;
GO
 
-- ------------------------------------------------------------
-- SP 4: sp_record_daily_health_log
-- [SỬA] Viết lại toàn bộ logic tính cảnh báo theo:
--   - Đường huyết mmol/L: <4.4 RED(hạ) | 4.4-10 GREEN | 10-16 ORANGE | >16 RED(tăng)
--   - Huyết áp: <120/<80 GREEN | 130-139 hoặc 85-89 YELLOW |
--               140-179 hoặc 90-109 ORANGE | >=180 hoặc >=110 RED
--   - Bỏ hoàn toàn mức CRITICAL — mức cao nhất là RED.
--   - Lấy mức cảnh báo CAO NHẤT giữa huyết áp và đường huyết làm kết quả cuối.
-- ------------------------------------------------------------
CREATE OR ALTER PROCEDURE sp_record_daily_health_log
    @patient_id         INT,
    @log_type           VARCHAR(10),
    @input_method       VARCHAR(10),
    @systolic_bp        INT             = NULL,
    @diastolic_bp       INT             = NULL,
    @heart_rate         INT             = NULL,
    @glucose_level       DECIMAL(4,2)    = NULL,     -- mmol/L
    @image_url          VARCHAR(500)    = NULL,
    @patient_notes       NVARCHAR(500)   = NULL,
    @new_log_id          INT             OUTPUT,
    @alert_level_result   VARCHAR(10)     OUTPUT,
    @result_message     NVARCHAR(500)   OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
 
    DECLARE @bp_alert               VARCHAR(10) = 'GREEN';
    DECLARE @glucose_alert          VARCHAR(10) = 'GREEN';
    DECLARE @calculated_alert_level VARCHAR(10) = 'GREEN';
    DECLARE @hospital_id            INT;
    DECLARE @is_ocr_validated       BIT;
 
    SET @is_ocr_validated = CASE WHEN @input_method = 'OCR' THEN 0 ELSE 1 END;
 
    SELECT @hospital_id = hospital_id
    FROM patients
    WHERE id = @patient_id AND is_active = 1;
 
    IF @hospital_id IS NULL
    BEGIN
        SET @result_message = N'Lỗi: Bệnh nhân không tồn tại hoặc đã bị vô hiệu hóa.';
        RETURN -1;
    END
 
    -- ---- Tính mức cảnh báo huyết áp ----
    IF @systolic_bp IS NOT NULL OR @diastolic_bp IS NOT NULL
    BEGIN
        IF (@systolic_bp >= 180 OR @diastolic_bp >= 110)
            SET @bp_alert = 'RED';
        ELSE IF (@systolic_bp BETWEEN 140 AND 179) OR (@diastolic_bp BETWEEN 90 AND 109)
            SET @bp_alert = 'ORANGE';
        ELSE IF (@systolic_bp BETWEEN 130 AND 139) OR (@diastolic_bp BETWEEN 85 AND 89)
            SET @bp_alert = 'YELLOW';
        ELSE IF (ISNULL(@systolic_bp, 0) < 120) AND (ISNULL(@diastolic_bp, 0) < 80)
            SET @bp_alert = 'GREEN';
        ELSE
            -- Vùng trung gian chưa quy định rõ (VD: systolic 120-129 / diastolic 80-84)
            -- tạm xếp mức YELLOW để bệnh nhân lưu ý theo dõi thêm.
            SET @bp_alert = 'YELLOW';
    END
 
    -- ---- Tính mức cảnh báo đường huyết (mmol/L) ----
    IF @glucose_level IS NOT NULL
    BEGIN
        IF @glucose_level < 4.4
            SET @glucose_alert = 'RED';        -- Hạ đường huyết đột ngột
        ELSE IF @glucose_level > 16
            SET @glucose_alert = 'RED';        -- Tăng đường huyết khẩn cấp
        ELSE IF @glucose_level > 10
            SET @glucose_alert = 'ORANGE';     -- Cao
        ELSE
            SET @glucose_alert = 'GREEN';      -- Bình thường (4.4 - 10)
    END
 
    -- ---- Lấy mức cao nhất giữa hai chỉ số ----
    SET @calculated_alert_level = CASE
        WHEN 'RED'    IN (@bp_alert, @glucose_alert) THEN 'RED'
        WHEN 'ORANGE' IN (@bp_alert, @glucose_alert) THEN 'ORANGE'
        WHEN 'YELLOW' IN (@bp_alert, @glucose_alert) THEN 'YELLOW'
        ELSE 'GREEN'
    END;
 
    INSERT INTO daily_health_logs (
        patient_id, log_date, log_time, log_type,
        systolic_bp, diastolic_bp, heart_rate, glucose_level,
        input_method, image_url, is_ocr_validated,
        alert_level, patient_notes
    )
    VALUES (
        @patient_id,
        CAST(SYSDATETIME() AS DATE),
        SYSDATETIME(),
        @log_type,
        @systolic_bp, @diastolic_bp, @heart_rate, @glucose_level,
        @input_method, @image_url,
        @is_ocr_validated,
        @calculated_alert_level,
        @patient_notes
    );
 
    SET @new_log_id = SCOPE_IDENTITY();
 
    -- Theo nghiệp vụ 2.3: chỉ tạo bản ghi alerts (và báo bác sĩ) khi mức ORANGE hoặc RED
    IF @calculated_alert_level IN ('ORANGE', 'RED')
    BEGIN
        DECLARE @doctor_id       INT;
        DECLARE @alert_level_num INT;
 
        SELECT @doctor_id = doctor_id FROM patients WHERE id = @patient_id;
 
        SET @alert_level_num = CASE @calculated_alert_level
            WHEN 'ORANGE' THEN 3
            WHEN 'RED'    THEN 4
        END;
 
        IF @doctor_id IS NOT NULL
        BEGIN
            INSERT INTO alerts (
                patient_id, doctor_id, health_log_id, alert_level, alert_color,
                metric_type, metric_value, threshold_violated, alert_message
            )
            VALUES (
                @patient_id,
                @doctor_id,
                @new_log_id,
                @alert_level_num,
                @calculated_alert_level,
                CASE
                    WHEN @systolic_bp IS NOT NULL AND @glucose_level IS NOT NULL THEN 'BOTH'
                    WHEN @systolic_bp IS NOT NULL THEN 'BLOOD_PRESSURE'
                    ELSE 'GLUCOSE'
                END,
                N'HA: ' + ISNULL(CAST(@systolic_bp AS VARCHAR), 'N/A') + '/' + ISNULL(CAST(@diastolic_bp AS VARCHAR), 'N/A')
                + N' mmHg | Glucose: ' + ISNULL(CAST(@glucose_level AS VARCHAR), 'N/A') + N' mmol/L',
                N'Vượt ngưỡng cảnh báo mức ' + @calculated_alert_level,
                N'Bệnh nhân ID ' + CAST(@patient_id AS VARCHAR)
                + N' có chỉ số vượt ngưỡng: Mức ' + @calculated_alert_level
                + N'. Cần kiểm tra ngay.'
            );
 
            -- Đồng thời ghi vào notifications để gửi thông báo tới bác sĩ
            INSERT INTO notifications (recipient_type, recipient_id, notification_type, title, content, related_entity_type, related_entity_id)
            VALUES (
                'DOCTOR', @doctor_id, 'ALERT',
                N'Cảnh báo sức khỏe mức ' + @calculated_alert_level,
                N'Bệnh nhân ID ' + CAST(@patient_id AS VARCHAR) + N' có chỉ số vượt ngưỡng mức ' + @calculated_alert_level + N'.',
                'DAILY_HEALTH_LOG', @new_log_id
            );
        END
    END
 
    SET @alert_level_result = @calculated_alert_level;
    SET @result_message = N'Thành công: Đã ghi nhận nhật ký sức khỏe. Mức cảnh báo: ' + @calculated_alert_level;
    RETURN 0;
 
END;
GO
 
-- ------------------------------------------------------------
-- SP 5: sp_get_doctor_dashboard
-- [SỬA] Bỏ mức CRITICAL trong phân loại màu dashboard.
-- ------------------------------------------------------------
CREATE OR ALTER PROCEDURE sp_get_doctor_dashboard
    @doctor_id  INT,
    @target_date DATE = NULL
AS
BEGIN
    SET NOCOUNT ON;
 
    IF @target_date IS NULL SET @target_date = CAST(SYSDATETIME() AS DATE);
 
    SELECT
        p.id                    AS patient_id,
        p.patient_code,
        p.full_name             AS patient_name,
        p.phone,
        dp.profile_code         AS disease_profile,
        latest_log.systolic_bp          AS today_systolic_bp,
        latest_log.diastolic_bp         AS today_diastolic_bp,
        latest_log.glucose_level        AS today_glucose,
        latest_log.alert_level          AS today_alert_level,
        latest_log.log_time             AS last_log_time,
        dashboard_color = ISNULL(latest_log.alert_level, 'GREY'),   -- GREEN/YELLOW/ORANGE/RED hoặc GREY (chưa nhập liệu)
        unresolved_alerts.alert_count   AS unresolved_alert_count,
        morning_log.id                  AS has_morning_log,
        evening_log.id                  AS has_evening_log
    FROM patients p
    LEFT JOIN disease_profiles dp ON p.disease_profile_id = dp.id
    LEFT JOIN (
        SELECT DISTINCT
            dhl.patient_id,
            FIRST_VALUE(dhl.systolic_bp)    OVER (PARTITION BY dhl.patient_id ORDER BY dhl.log_time DESC) AS systolic_bp,
            FIRST_VALUE(dhl.diastolic_bp)   OVER (PARTITION BY dhl.patient_id ORDER BY dhl.log_time DESC) AS diastolic_bp,
            FIRST_VALUE(dhl.glucose_level)  OVER (PARTITION BY dhl.patient_id ORDER BY dhl.log_time DESC) AS glucose_level,
            FIRST_VALUE(dhl.alert_level)    OVER (PARTITION BY dhl.patient_id ORDER BY dhl.log_time DESC) AS alert_level,
            FIRST_VALUE(dhl.log_time)       OVER (PARTITION BY dhl.patient_id ORDER BY dhl.log_time DESC) AS log_time
        FROM daily_health_logs dhl
        WHERE dhl.log_date = @target_date
    ) latest_log ON p.id = latest_log.patient_id
    LEFT JOIN (
        SELECT patient_id, COUNT(*) AS alert_count
        FROM alerts
        WHERE is_resolved = 0
        GROUP BY patient_id
    ) unresolved_alerts ON p.id = unresolved_alerts.patient_id
    LEFT JOIN daily_health_logs morning_log
        ON p.id = morning_log.patient_id
        AND morning_log.log_date = @target_date
        AND morning_log.log_type = 'MORNING'
    LEFT JOIN daily_health_logs evening_log
        ON p.id = evening_log.patient_id
        AND evening_log.log_date = @target_date
        AND evening_log.log_type = 'EVENING'
    WHERE
        p.doctor_id     = @doctor_id
        AND p.status    = 'TREATING'
        AND p.is_active = 1
    ORDER BY
        -- Ưu tiên hiển thị: Đỏ -> Cam -> Vàng -> Xám (chưa nhập) -> Xanh
        CASE
            WHEN latest_log.alert_level = 'RED'    THEN 1
            WHEN latest_log.alert_level = 'ORANGE' THEN 2
            WHEN latest_log.alert_level = 'YELLOW' THEN 3
            WHEN latest_log.alert_level IS NULL    THEN 4
            ELSE 5
        END,
        unresolved_alerts.alert_count DESC;
 
END;
GO
 
-- ------------------------------------------------------------
-- SP 6: sp_get_hospital_overview
-- [SỬA] Đếm "cảnh báo nghiêm trọng" theo alert_color = 'RED' (không còn CRITICAL).
-- ------------------------------------------------------------
CREATE OR ALTER PROCEDURE sp_get_hospital_overview
    @hospital_id    INT
AS
BEGIN
    SET NOCOUNT ON;
 
    SELECT
        h.full_name             AS hospital_name,
        (SELECT COUNT(*) FROM patients p
         WHERE p.hospital_id = @hospital_id AND p.status = 'TREATING' AND p.is_active = 1)
                                AS total_treating_patients,
        (SELECT COUNT(*) FROM patients p
         WHERE p.hospital_id = @hospital_id AND p.status = 'NEW' AND p.is_active = 1)
                                AS total_new_patients,
        -- Số cảnh báo mức RED (cấp cứu) chưa xử lý toàn viện
        (SELECT COUNT(*) FROM alerts a
         INNER JOIN patients p ON a.patient_id = p.id
         WHERE p.hospital_id = @hospital_id AND a.is_resolved = 0 AND a.alert_color = 'RED')
                                AS red_unresolved_alerts,
        (SELECT COUNT(*) FROM doctors d
         WHERE d.hospital_id = @hospital_id AND d.is_active = 1)
                                AS active_doctors_count
    FROM hospitals h
    WHERE h.id = @hospital_id;
 
    SELECT
        d.id                    AS doctor_id,
        d.doctor_code,
        d.full_name             AS doctor_name,
        d.specialty,
        d.current_patient_count,
        d.capacity_limit,
        CAST(
            (CAST(d.current_patient_count AS FLOAT) / CAST(d.capacity_limit AS FLOAT)) * 100
        AS DECIMAL(5,2))        AS workload_percentage,
        workload_status = CASE
            WHEN (CAST(d.current_patient_count AS FLOAT) / d.capacity_limit) >= 1.0   THEN 'FULL'
            WHEN (CAST(d.current_patient_count AS FLOAT) / d.capacity_limit) >= 0.9   THEN 'CRITICAL'
            WHEN (CAST(d.current_patient_count AS FLOAT) / d.capacity_limit) >= 0.7   THEN 'HIGH'
            WHEN (CAST(d.current_patient_count AS FLOAT) / d.capacity_limit) >= 0.5   THEN 'MEDIUM'
            ELSE 'LOW'
        END,
        d.is_active
    FROM doctors d
    WHERE d.hospital_id = @hospital_id
    ORDER BY workload_percentage DESC;
 
END;
GO
 
 
-- ============================================================
-- BƯỚC 5: DỮ LIỆU MẪU (SEED DATA)
-- ============================================================
 
INSERT INTO accounts (email, password_hash, role, is_email_verified)
VALUES
('admin@bvdktrunguong-mau.vn', 'hashed_pwd_admin', 'HOSPITAL_ADMIN', 1),-- id = 1
('bs.an@hospital.vn', 'hashed_pwd_123', 'DOCTOR', 1),        -- id = 2
('bs.binh@hospital.vn', 'hashed_pwd_456', 'DOCTOR', 1),      -- id = 3
('benh.nhan.1@gmail.com', 'hashed_pwd_789', 'PATIENT', 1),   -- id = 4
('benh.nhan.2@gmail.com', 'hashed_pwd_000', 'PATIENT', 1),   -- id = 5
('benh.nhan.3@gmail.com', 'hashed_pwd_111', 'PATIENT', 1),   -- id = 6
('benh.nhan.4@gmail.com', 'hashed_pwd_222', 'PATIENT', 1);   -- id = 7
GO
 
INSERT INTO hospitals (account_id, hospital_code, full_name, address, phone)
VALUES (
    1,
    'BV001',
    N'Bệnh viện Đa khoa Trung ương Mẫu',
    N'123 Đường Giải Phóng, Hai Bà Trưng, Hà Nội',
    '024-3869-3731'
);
GO
 
-- Ngưỡng cảnh báo mặc định của bệnh viện (mmol/L cho đường huyết, 4 mức màu)
INSERT INTO alert_thresholds (
    hospital_id, scope, metric_type,
    glucose_hypo_threshold, glucose_normal_max, glucose_high_max,
    systolic_normal_max, systolic_warning_min, systolic_warning_max,
    systolic_danger_min, systolic_danger_max, systolic_emergency_threshold,
    diastolic_normal_max, diastolic_warning_min, diastolic_warning_max,
    diastolic_danger_min, diastolic_danger_max, diastolic_emergency_threshold
)
VALUES (
    1, 'HOSPITAL', 'COMBINED',
    4.4, 10.0, 16.0,           -- Đường huyết (mmol/L): Hạ <4.4 | Bình thường 4.4-10 | Cao 10-16 | Khẩn cấp >16
    120, 130, 139,             -- HA tâm thu: GREEN <120 | YELLOW 130-139
    140, 179, 180,             -- HA tâm thu: ORANGE 140-179 | RED >=180
    80, 85, 89,                -- HA tâm trương: GREEN <80 | YELLOW 85-89
    90, 109, 110                -- HA tâm trương: ORANGE 90-109 | RED >=110
);
GO
 
-- Hướng dẫn xử lý khẩn cấp do hệ thống kích hoạt theo ngưỡng (emergency_guides)
INSERT INTO emergency_guides (hospital_id, alert_level, metric_type, title, instruction_content)
VALUES
(1, 'RED', 'BLOOD_PRESSURE',
 N'Huyết áp tăng cao — Hướng dẫn xử lý',
 N'1. Ngừng ngay mọi hoạt động thể chất, nằm xuống nghỉ ngơi ở tư thế thoải mái.
2. Thở sâu và chậm, hít vào 4 giây, giữ 2 giây, thở ra 6 giây. Lặp lại 5-10 lần.
3. Tránh hoàn toàn cafein, muối và các chất kích thích.
4. Uống thuốc huyết áp theo đúng chỉ định của bác sĩ nếu đã đến giờ uống.
5. Đo lại sau 15-20 phút. Nếu không giảm, liên hệ ngay người thân hoặc gọi cấp cứu 115.'),
(1, 'RED', 'GLUCOSE',
 N'Đường huyết vượt ngưỡng — Hướng dẫn xử lý',
 N'1. Không ăn thêm bất kỳ thực phẩm có đường hoặc tinh bột nào.
2. Uống nước lọc (không đường) từ từ, khoảng 200-300ml.
3. Nghỉ ngơi hoàn toàn, tránh vận động mạnh ngay lúc này.
4. Kiểm tra lại đã uống thuốc tiểu đường đúng giờ chưa.
5. Đo lại đường huyết sau 1 tiếng. Nếu vẫn cao, liên hệ bác sĩ ngay.');
GO
 
-- Cẩm nang Dấu hiệu nhận biết + Hướng dẫn xử lý khẩn cấp (emergency_protocols)
INSERT INTO emergency_protocols (hospital_id, condition_type, title, warning_signs, instruction_content)
VALUES
(1, 'HYPERTENSIVE_CRISIS',
 N'Tăng huyết áp đột ngột (Cơn tăng huyết áp)',
 N'Đau đầu dữ dội, chóng mặt, mờ mắt, đau ngực, khó thở, tê yếu tay chân, lú lẫn, chảy máu mũi bất thường.',
 N'1. Cho người bệnh ngồi/nằm nghỉ ở tư thế đầu cao 30-45 độ, giữ bình tĩnh.
2. Đo lại huyết áp sau 5 phút để xác nhận.
3. Nếu huyết áp tâm thu >=180 hoặc tâm trương >=110 mmHg: gọi 115 hoặc đưa đến cơ sở y tế gần nhất ngay.
4. Không tự ý dùng thêm thuốc hạ áp ngoài chỉ định của bác sĩ.'),
(1, 'HYPOGLYCEMIA',
 N'Hạ đường huyết đột ngột',
 N'Vã mồ hôi lạnh, run tay chân, hoa mắt, chóng mặt, đói cồn cào, tim đập nhanh, lú lẫn, có thể ngất xỉu.',
 N'1. Cho người bệnh ăn/uống ngay 15g đường nhanh (nửa cốc nước ngọt, vài viên kẹo, hoặc 3 thìa đường pha nước).
2. Nghỉ ngơi, đo lại đường huyết sau 15 phút.
3. Nếu vẫn dưới 4.4 mmol/L hoặc người bệnh lơ mơ/mất ý thức: gọi 115 ngay, không cố ép ăn uống khi đã mất ý thức.'),
(1, 'HYPERGLYCEMIA',
 N'Tăng đường huyết đột ngột',
 N'Khát nước nhiều, đi tiểu nhiều, mệt mỏi, mờ mắt, hơi thở có mùi trái cây (dấu hiệu nhiễm toan ceton), buồn nôn.',
 N'1. Uống nhiều nước lọc, không ăn thêm tinh bột/đường.
2. Kiểm tra lại đã dùng thuốc/insulin đúng giờ chưa.
3. Đo lại đường huyết sau 1 giờ.
4. Nếu đường huyết > 16 mmol/L kèm mệt nhiều, nôn ói, thở nhanh: liên hệ bác sĩ hoặc đến cơ sở y tế ngay.');
GO
 
-- Dữ liệu mẫu cho danh mục thực phẩm (foods_dictionary)
INSERT INTO foods_dictionary (food_code, food_name, english_name, water_g, energy_kcal, protein_g, lipid_g, glucid_g, celluloza_g, ash_g)
VALUES
('V01', N'Cơm trắng', 'White rice (cooked)', 68.7, 130, 2.7, 0.3, 28.2, 0.4, 0.4),
('V02', N'Rau muống luộc', 'Boiled water spinach', 91.0, 23, 2.6, 0.3, 2.5, 1.0, 1.0),
('A01', N'Thịt gà luộc (bỏ da)', 'Boiled chicken breast (skinless)', 65.0, 165, 31.0, 3.6, 0.0, 0.0, 1.1),
('A02', N'Cá hồi áp chảo', 'Pan-seared salmon', 64.0, 208, 20.4, 13.4, 0.0, 0.0, 1.2);
GO
 
 
-- ============================================================
-- KẾT THÚC SCRIPT — TÓM TẮT SAU SỬA ĐỔI
--   - 23 bảng dữ liệu (đã bỏ menus, ai_health_summaries; thêm
--     foods_dictionary, diet_logs, exercise_logs, emergency_protocols,
--     notifications, patient_medications, medication_logs, water_logs).
--   - Đường huyết dùng thống nhất đơn vị mmol/L trong toàn hệ thống.
--   - alert_level/alert_color chỉ còn 4 mức: GREEN/YELLOW/ORANGE/RED.
--   - audit_trails.actor_type chỉ còn DOCTOR/PATIENT/HOSPITAL_ADMIN.
--   - appointments có thêm cột location.
--   - Các bảng/chức năng liên quan AI (ai_health_summaries, AI gợi ý
--     thực đơn cụ thể...) được loại bỏ khỏi MVP này, để triển khai sau.
-- ============================================================