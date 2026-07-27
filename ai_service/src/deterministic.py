"""
deterministic.py — Deterministic tính toán, LLM chỉ diễn giải

RESEARCH_FOUNDATION.md mục 9 — quyết định kiến trúc riêng, phù hợp
khuyến nghị chung trong hệ thống y tế/tài chính an toàn: mọi phép tính có
công thức rõ ràng (BMR, trend statistics, kiểm tra chống chỉ định) PHẢI
dùng code, KHÔNG BAO GIỜ để LLM tự tính hoặc tự suy luận số học — LLM yếu
ở tính toán chính xác và không deterministic giữa các lần gọi.

Đã khóa: không để LLM tự tính bất kỳ con số y khoa nào (liều lượng, chỉ
số, ngưỡng cảnh báo) — chỉ được diễn giải kết quả code đã tính sẵn.

Cách dùng đúng trong pipeline: gọi các hàm ở đây → format kết quả number
thành text → đưa vào prompt kèm chú thích "đây là số đã tính sẵn, chỉ
diễn giải, không tính lại". Prompt template nên nhắc lại rule này.
"""

from __future__ import annotations

from dataclasses import dataclass


@dataclass(frozen=True)
class ContraindicationResult:
    drug: str
    is_contraindicated: bool
    reason: str | None


def calculate_egfr_ckd_epi(
    creatinine_mg_dl: float,
    age: int,
    is_female: bool,
) -> float:
    """CKD-EPI 2021 (race-free). TODO: xác nhận version công thức khớp
    với KDIGO 2024 guideline đang dùng trong corpus trước khi dùng
    production — đây là chỗ cần double-check với ADA/KDIGO 2024, không
    phải chỗ để AI agent tự chọn công thức khác mà không hỏi.
    """
    raise NotImplementedError("TODO: implement công thức CKD-EPI 2021 chuẩn.")


def check_metformin_contraindication(egfr: float) -> ContraindicationResult:
    """
    Ngưỡng tham khảo phổ biến (KDIGO/ADA — CẦN xác nhận số chính xác từ
    corpus ADA 2025 / KDIGO 2024 trước khi dùng thật, không hardcode theo
    trí nhớ mô hình):
        eGFR < 30  → chống chỉ định
        eGFR 30-45 → giảm liều, thận trọng
        eGFR > 45  → an toàn thường quy
    """
    raise NotImplementedError(
        "TODO: implement dựa trên ngưỡng chính xác trích từ retrieved "
        "context (ADA 2025 / KDIGO 2024), không hardcode số từ memory "
        "của LLM/agent."
    )


def calculate_bmr_mifflin_st_jeor(
    weight_kg: float,
    height_cm: float,
    age: int,
    is_female: bool,
) -> float:
    if is_female:
        return 10 * weight_kg + 6.25 * height_cm - 5 * age - 161
    return 10 * weight_kg + 6.25 * height_cm - 5 * age + 5
