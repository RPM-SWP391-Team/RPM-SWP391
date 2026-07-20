import pytest
import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from src.patient_formatter import PatientContextFormatter

def test_patient_context_format_empty():
    res = PatientContextFormatter.format({})
    assert res == "Không có thông tin bệnh nhân."
    
def test_patient_context_format_missing_fields():
    data = {"age": 62, "gender": None}
    res = PatientContextFormatter.format(data)
    assert "- Age: 62" in res
    assert "Gender" not in res # Bỏ qua null

def test_patient_context_format_full():
    data = {
        "age": 62,
        "gender": "Male",
        "medical_history": ["Type 2 Diabetes", "Hypertension"],
        "vitals": {"bmi": 28.5},
        "lab_results": {
            "hba1c": {"value": 8.5, "unit": "%"},
            "egfr": {"value": 42, "unit": "mL/min/1.73m²"}
        }
    }
    
    res = PatientContextFormatter.format(data)
    
    assert "- Age: 62" in res
    assert "- Gender: Male" in res
    assert "- Medical History: Type 2 Diabetes, Hypertension" in res
    assert "- Bmi: 28.5" in res
    assert "+ hba1c: 8.5 %" in res
    assert "+ egfr: 42 mL/min/1.73m²" in res
