from typing import Dict, Any

class PatientContextFormatter:
    """
    Format tĩnh Patient Data JSON thành chuỗi văn bản cho Prompt.
    Trách nhiệm đơn lẻ: Xử lý null, chuẩn hóa bool/đơn vị, sinh text.
    Không dính dáng tới LLM hay Retrieval.
    """
    
    @staticmethod
    def format(patient_data: Dict[str, Any]) -> str:
        if not patient_data:
            return "Không có thông tin bệnh nhân."
            
        lines = []
        
        # Age
        age = patient_data.get("age")
        if age is not None:
            lines.append(f"- Age: {age}")
            
        # Gender
        gender = patient_data.get("gender")
        if gender:
            lines.append(f"- Gender: {gender}")
            
        # Medical History
        history = patient_data.get("medical_history")
        if history:
            lines.append("- Medical History: " + ", ".join(history))
            
        # Vitals
        vitals = patient_data.get("vitals", {})
        for k, v in vitals.items():
            if v is not None:
                lines.append(f"- {k.capitalize()}: {v}")
                
        # Lab Results (với đơn vị nếu có)
        labs = patient_data.get("lab_results", {})
        if labs:
            lines.append("- Lab Results:")
            for k, v in labs.items():
                if v is not None:
                    # Giả định chuẩn hóa đơn vị nếu dict có cấu trúc {value, unit}
                    if isinstance(v, dict):
                        val = v.get("value", "N/A")
                        unit = v.get("unit", "")
                        lines.append(f"  + {k}: {val} {unit}".strip())
                    else:
                        lines.append(f"  + {k}: {v}")
                        
        if not lines:
            return "Thông tin bệnh nhân trống."
            
        return "\n".join(lines)
