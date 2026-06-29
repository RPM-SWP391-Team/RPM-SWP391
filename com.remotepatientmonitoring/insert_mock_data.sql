-- =========================================================
-- SCRIPT CHÈN DỮ LIỆU MOCK ĐỂ TEST (SQL SERVER)
-- =========================================================

-- Lấy Bác sĩ đầu tiên
DECLARE @doctorId INT;
SELECT TOP 1 @doctorId = id FROM doctors;

-- Lấy Bệnh nhân đầu tiên
DECLARE @patientId INT;
SELECT TOP 1 @patientId = id FROM patients;

IF @patientId IS NOT NULL AND @doctorId IS NOT NULL
BEGIN
    -- Gán bệnh nhân này cho bác sĩ và đặt trạng thái hoạt động (phải dùng TREATING theo ràng buộc)
    UPDATE patients SET doctor_id = @doctorId, status = 'TREATING' WHERE id = @patientId;

    -- Xóa data cũ của bệnh nhân này để tránh trùng lặp nếu chạy nhiều lần
    DELETE FROM daily_health_logs WHERE patient_id = @patientId;
    DELETE FROM exercise_logs WHERE patient_id = @patientId;
    DELETE FROM water_logs WHERE patient_id = @patientId;
    DELETE FROM medication_logs WHERE patient_medication_id IN (SELECT id FROM patient_medications WHERE patient_id = @patientId);
    DELETE FROM patient_medications WHERE patient_id = @patientId;
    DELETE FROM alerts WHERE patient_id = @patientId;

    -- Vòng lặp chèn 7 ngày dữ liệu sinh tồn, nước, vận động
    DECLARE @day INT = 7;
    DECLARE @currentDate DATE;
    DECLARE @currentTime DATETIME;

    WHILE @day >= 1
    BEGIN
        SET @currentDate = CAST(GETDATE() - @day AS DATE);
        SET @currentTime = DATEADD(hour, 8, CAST(@currentDate AS DATETIME));

        -- 1. Chèn Daily Health Logs (Huyết áp, đường huyết)
        INSERT INTO daily_health_logs 
        (patient_id, log_date, log_time, log_type, systolic_bp, diastolic_bp, heart_rate, glucose_level, input_method, is_ocr_validated, is_alert_processed, created_at)
        VALUES 
        (@patientId, @currentDate, @currentTime, 'MORNING', 120 + (@day % 10), 80 + (@day % 5), 75, 5.5 + (@day % 3), 'MANUAL', 0, 1, GETDATE());

        -- 2. Chèn Exercise Logs
        INSERT INTO exercise_logs (patient_id, log_date, exercise_type, duration_minutes, steps_count, logged_at)
        VALUES (@patientId, @currentDate, N'Đi bộ', 30 + (@day * 5), 3000 + (@day * 500), GETDATE());

        -- 3. Chèn Water Logs
        INSERT INTO water_logs (patient_id, log_date, amount_ml)
        VALUES (@patientId, @currentDate, 1500 + (@day * 100));

        SET @day = @day - 1;
    END

    -- 4. Chèn Patient Medication
    INSERT INTO patient_medications (patient_id, medicine_name, dosage, scheduled_time, is_active, created_at, updated_at)
    VALUES (@patientId, N'Metformin 500mg', N'1 viên', '08:00', 1, GETDATE(), GETDATE());
    
    DECLARE @pMedId INT;
    SET @pMedId = SCOPE_IDENTITY();

    -- 5. Chèn Medication Logs (Lịch sử uống thuốc)
    SET @day = 7;
    WHILE @day >= 1
    BEGIN
        SET @currentDate = CAST(GETDATE() - @day AS DATE);
        SET @currentTime = DATEADD(hour, 8, CAST(@currentDate AS DATETIME));

        -- Giả lập bệnh nhân thi thoảng quên thuốc (vd ngày lẻ thì quên)
        DECLARE @isTaken BIT = CASE WHEN @day % 3 = 0 THEN 0 ELSE 1 END;
        
        INSERT INTO medication_logs (patient_medication_id, log_date, is_taken, taken_at)
        VALUES (@pMedId, @currentDate, @isTaken, CASE WHEN @isTaken = 1 THEN @currentTime ELSE NULL END);

        SET @day = @day - 1;
    END

    -- 6. Chèn Alerts (Cảnh báo thông minh)
    INSERT INTO alerts (patient_id, doctor_id, alert_level, alert_color, metric_type, metric_value, threshold_violated, alert_message, is_resolved, triggered_at, created_at)
    VALUES 
    (@patientId, @doctorId, 3, 'RED', 'BLOOD_PRESSURE', '160/100', '> 140/90', N'Huyết áp tăng quá cao (160/100)', 0, GETDATE(), GETDATE()),
    (@patientId, @doctorId, 2, 'ORANGE', 'GLUCOSE', '10.5', '> 7.0', N'Đường huyết ở mức nguy hiểm (10.5)', 0, GETDATE(), GETDATE());

    -- 7. Chèn Notifications (Thông báo chuông)
    INSERT INTO notifications (doctor_id, patient_id, recipient_type, recipient_id, notification_type, channel, status, title, content, is_read, created_at)
    VALUES 
    (@doctorId, @patientId, 'DOCTOR', @doctorId, 'SYSTEM', 'IN_APP', 'SENT', N'Cảnh báo sức khỏe bệnh nhân', N'Bệnh nhân có sự kiện bất thường cần kiểm tra ngay lập tức.', 0, GETDATE());

    PRINT N'Đã chèn dữ liệu Mock thành công vào Database!';
END
ELSE
BEGIN
    PRINT N'Chưa có bác sĩ hoặc bệnh nhân nào trong DB, vui lòng tạo tài khoản trước.';
END
