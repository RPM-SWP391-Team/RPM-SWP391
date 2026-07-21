import logging
from typing import Dict, Any

logger = logging.getLogger(__name__)

def generate_patient_summary(patient_info: str, ada_stats_text: str, bp_stats_text: str, current_treatment: str, engine) -> Dict[str, Any]:
    """
    Sử dụng LLM của MedicalRAGPipeline để sinh báo cáo lâm sàng chuẩn ADA và AHA.
    """
    
    # Kiểm tra xem có dữ liệu nào không
    if "không đủ số lượng" in ada_stats_text.lower() and "không đủ số lượng" in bp_stats_text.lower():
        return {
            "clinicalSummary": "Bệnh nhân không có đủ dữ liệu Huyết áp và Đường huyết trong 14 ngày qua để phân tích lâm sàng."
        }

    system_prompt = """Bạn là một Bác sĩ Chuyên gia Nội khoa uy tín.
Bạn sẽ nhận được Thông tin bệnh nhân và Các chỉ số Huyết áp & Đường huyết 14 ngày qua (theo Khuyến cáo ESC / ADA / AHA).

Nhiệm vụ của bạn là viết một Báo cáo Phân tích Lâm sàng Tổng hợp kết hợp cả Huyết áp và Đường huyết theo Khuyến cáo ESC / ADA để tư vấn cho bác sĩ điều trị.
TUYỆT ĐỐI KHÔNG TÍNH TOÁN LẠI các con số (vì hệ thống đã tính chuẩn 100%).

Cấu trúc báo cáo bắt buộc (Sử dụng Markdown rõ ràng, trình bày mạch lạc):
### 1. Đánh giá Huyết áp (Chuẩn ESC / AHA)
- Phân tích mức trung bình (Avg SBP/DBP), sự chênh lệch Huyết áp Sáng vs Tối.
- Đánh giá nguy cơ nếu có số lần Huyết áp cao (>=140/90) hoặc Báo động (>=180/120).

### 2. Đánh giá Đường huyết (Chuẩn ESC / ADA)
- Phân tích các chỉ số quản lý đường huyết: TIR (Trong ngưỡng 3.9-10.0), TAR (Trên 10.0), TBR (Dưới 3.9), độ biến thiên CV% và GMI%.

### 3. Rủi ro Lâm sàng Tổng hợp (Huyết áp + Đường huyết - ESC Guidelines)
- Đánh giá sự tương tác giữa Tăng huyết áp và Đái tháo đường (nguy cơ biến chứng tim mạch, biến chứng thận, đột quỵ hoặc hạ đường huyết theo Khuyến cáo ESC 2024).

### 4. Đề xuất Hướng Xử trí & Phác đồ (Gợi ý cho Bác sĩ)
- Đưa ra 2-3 gợi ý cụ thể về điều chỉnh thuốc (huyết áp/đường huyết), theo dõi chỉ số và chế độ sinh hoạt.

Văn phong: Y khoa chuyên nghiệp, sắc bén, mạch lạc, có tính tổng hợp cao. Sử dụng tiếng Việt.
"""

    user_prompt = f"""
THÔNG TIN BỆNH NHÂN:
{patient_info}

CHỈ SỐ ĐƯỜNG HUYẾT (ADA):
{ada_stats_text}

CHỈ SỐ HUYẾT ÁP (AHA):
{bp_stats_text}

PHÁC ĐỒ HIỆN TẠI:
{current_treatment}

Hãy viết báo cáo theo đúng cấu trúc yêu cầu.
"""
    
    logger.info("Generating ADA patient summary using LLM...")
    try:
        response_text = engine.llm_client.generate(user_prompt, system_prompt=system_prompt)
        return {
            "clinicalSummary": response_text
        }
    except Exception as e:
        logger.error(f"Error generating summary: {e}")
        return {
            "clinicalSummary": f"Lỗi trong quá trình phân tích AI: {str(e)}"
        }
