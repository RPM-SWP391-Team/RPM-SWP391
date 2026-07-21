-- =========================================================================
-- SQL SCRIPT: CHÈN DỮ LIỆU SỨC KHỎE DÀI HẠN (14 NGÀY LIÊN TỤC) ĐỂ TEST AI
-- Bệnh nhân: ID = 1 (Nguyễn Văn An)
-- =========================================================================

USE [RemotePatientMonitoringVer2];
GO

-- 1. Xóa nhật ký đo cũ của Bệnh nhân ID = 1
DELETE FROM [dbo].[daily_health_logs] WHERE [patient_id] = 1;
GO

-- 2. Tự động chèn 14 ngày nhật ký sức khỏe (Mỗi ngày 2 lần đo: Sáng MORNING & Tối EVENING)
DECLARE @StartDate DATE = DATEADD(DAY, -14, CAST(GETDATE() AS DATE));
DECLARE @DayCount INT = 0;

WHILE @DayCount < 14
BEGIN
    DECLARE @LogDate DATE = DATEADD(DAY, @DayCount, @StartDate);
    
    -- Lần đo 1: Buổi sáng (MORNING)
    INSERT INTO [dbo].[daily_health_logs] (
        [patient_id], [log_date], [log_time], [log_type], 
        [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], 
        [input_method], [image_url], [is_ocr_validated], [alert_level], 
        [patient_notes], [is_alert_processed], [created_at]
    ) VALUES (
        1, 
        @LogDate, 
        CAST(CONCAT(FORMAT(@LogDate, 'yyyy-MM-dd'), ' 07:15:00') AS DATETIME2), 
        'MORNING', 
        128 + (@DayCount % 4) * 2,           -- Tâm thu sáng: 128 - 134 mmHg
        82 + (@DayCount % 3) * 2,            -- Tâm trương sáng: 82 - 86 mmHg
        72 + (@DayCount % 4),                -- Nhịp tim sáng: 72 - 75 bpm
        CAST(6.5 + (@DayCount % 4) * 0.4 AS DECIMAL(4,2)), -- Đường huyết đói sáng: 6.5 - 7.7 mmol/L
        'MANUAL', NULL, 0, 'GREEN', 
        N'Đo đường huyết đói và huyết áp sáng trước ăn', 0, GETDATE()
    );

    -- Lần đo 2: Buổi tối (EVENING)
    INSERT INTO [dbo].[daily_health_logs] (
        [patient_id], [log_date], [log_time], [log_type], 
        [systolic_bp], [diastolic_bp], [heart_rate], [glucose_level], 
        [input_method], [image_url], [is_ocr_validated], [alert_level], 
        [patient_notes], [is_alert_processed], [created_at]
    ) VALUES (
        1, 
        @LogDate, 
        CAST(CONCAT(FORMAT(@LogDate, 'yyyy-MM-dd'), ' 19:30:00') AS DATETIME2), 
        'EVENING', 
        142 + (@DayCount % 4) * 3,           -- Tâm thu tối: 142 - 151 mmHg (hơi cao)
        90 + (@DayCount % 3) * 2,            -- Tâm trương tối: 90 - 94 mmHg
        78 + (@DayCount % 5),                -- Nhịp tim tối: 78 - 82 bpm
        CAST(9.8 + (@DayCount % 5) * 0.7 AS DECIMAL(4,2)), -- Đường huyết tối sau ăn: 9.8 - 12.6 mmol/L
        'MANUAL', NULL, 0, 
        CASE WHEN (142 + (@DayCount % 4) * 3) >= 145 THEN 'YELLOW' ELSE 'GREEN' END, 
        N'Đo huyết áp và đường huyết 2h sau ăn tối', 0, GETDATE()
    );

    SET @DayCount = @DayCount + 1;
END;
GO

PRINT N'✅ Đã chèn thành công 28 bản ghi nhật ký đo (14 ngày x 2 lần/ngày) cho Bệnh nhân ID = 1!';
GO
