package com.rpm.remotepatientmonitoring.patient;

import com.rpm.remotepatientmonitoring.dto.patient.DailyHealthLogFormDto;
import com.rpm.remotepatientmonitoring.validator.ValidHealthLogValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ValidHealthLogValidatorTest {

    private ValidHealthLogValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;

    @BeforeEach
    void setUp() {
        validator = new ValidHealthLogValidator();
        lenient().when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        lenient().when(violationBuilder.addPropertyNode(anyString())).thenReturn(nodeBuilder);
        lenient().when(nodeBuilder.addConstraintViolation()).thenReturn(context);
        lenient().when(violationBuilder.addConstraintViolation()).thenReturn(context);
    }

    @Test
    void isValid_UT01() {
        String logType = "MORNING";
        Integer systolicBp = 120;
        Integer diastolicBp = 80;
        Integer heartRate = 75;
        BigDecimal glucoseLevel = BigDecimal.valueOf(5.5);

        DailyHealthLogFormDto dto = new DailyHealthLogFormDto();
        dto.setLogType(logType);
        dto.setSystolicBp(systolicBp);
        dto.setDiastolicBp(diastolicBp);
        dto.setHeartRate(heartRate);
        dto.setGlucoseLevel(glucoseLevel);

        boolean expected = true;
        boolean actual = validator.isValid(dto, context);
        assertEquals(expected, actual);
    }

    @Test
    void isValid_UT02() {
        String logType = "EVENING";
        BigDecimal glucoseLevel = BigDecimal.valueOf(5.5);

        DailyHealthLogFormDto dto = new DailyHealthLogFormDto();
        dto.setLogType(logType);
        dto.setGlucoseLevel(glucoseLevel);

        boolean expected = true;
        boolean actual = validator.isValid(dto, context);
        assertEquals(expected, actual);
    }

    @Test
    void isValid_UT03() {
        String logType = "RANDOM";
        Integer systolicBp = null;
        Integer diastolicBp = null;
        BigDecimal glucoseLevel = null;

        DailyHealthLogFormDto dto = new DailyHealthLogFormDto();
        dto.setLogType(logType);
        dto.setSystolicBp(systolicBp);
        dto.setDiastolicBp(diastolicBp);
        dto.setGlucoseLevel(glucoseLevel);

        boolean expected = false;
        boolean actual = validator.isValid(dto, context);
        assertEquals(expected, actual);
    }

    @Test
    void isValid_UT04() {
        String logType = "MORNING";
        Integer systolicBp = 120;
        Integer diastolicBp = null;

        DailyHealthLogFormDto dto = new DailyHealthLogFormDto();
        dto.setLogType(logType);
        dto.setSystolicBp(systolicBp);
        dto.setDiastolicBp(diastolicBp);

        boolean expected = false;
        boolean actual = validator.isValid(dto, context);
        assertEquals(expected, actual);
    }

    @Test
    void isValid_UT05() {
        String logType = "MORNING";
        Integer systolicBp = null;
        Integer diastolicBp = 80;

        DailyHealthLogFormDto dto = new DailyHealthLogFormDto();
        dto.setLogType(logType);
        dto.setSystolicBp(systolicBp);
        dto.setDiastolicBp(diastolicBp);

        boolean expected = false;
        boolean actual = validator.isValid(dto, context);
        assertEquals(expected, actual);
    }

    @Test
    void isValid_UT06() {
        DailyHealthLogFormDto dto = null;

        boolean expected = true;
        boolean actual = validator.isValid(dto, context);
        assertEquals(expected, actual);
    }
}
