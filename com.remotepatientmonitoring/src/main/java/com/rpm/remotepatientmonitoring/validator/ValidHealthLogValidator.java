package com.rpm.remotepatientmonitoring.validator;

import com.rpm.remotepatientmonitoring.dto.patient.DailyHealthLogFormDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidHealthLogValidator implements ConstraintValidator<ValidHealthLog, DailyHealthLogFormDto> {

    @Override
    public boolean isValid(DailyHealthLogFormDto dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }

        // Check if both Huyết áp and Đường huyết are empty
        boolean hasBp = dto.getSystolicBp() != null || dto.getDiastolicBp() != null || dto.getHeartRate() != null;
        boolean hasGl = dto.getGlucoseLevel() != null;

        if (!hasBp && !hasGl) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Vui lòng nhập chỉ số Huyết áp hoặc Đường huyết.")
                   .addConstraintViolation();
            return false;
        }

        boolean isValid = true;

        if (hasBp) {
            // Conditional Required validations for Blood Pressure
            if (dto.getSystolicBp() == null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Chỉ số tâm thu không được để trống.")
                       .addPropertyNode("systolicBp")
                       .addConstraintViolation();
                isValid = false;
            }
            if (dto.getDiastolicBp() == null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Chỉ số tâm trương không được để trống.")
                       .addPropertyNode("diastolicBp")
                       .addConstraintViolation();
                isValid = false;
            }
        }

        if (hasGl) {
            // Conditional Required validation for Glucose
            if (dto.getGlucoseLevel() == null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Chỉ số Đường huyết không được để trống.")
                       .addPropertyNode("glucoseLevel")
                       .addConstraintViolation();
                isValid = false;
            }
        }

        return isValid;
    }
}
