"""
patient_context.py — Patient Context Injection

RESEARCH_FOUNDATION.md mục 8 — quyết định kiến trúc riêng, có cơ sở an
toàn rõ ràng:

Lý do: nếu patient data được index vào vector corpus, retrieval có thể
BỎ SÓT thông tin quan trọng (VD: eGFR = 45) nếu câu hỏi không match ngữ
nghĩa rõ ràng với con số đó — trong khi thông tin này bắt buộc phải có để
trả lời an toàn (VD: chống chỉ định Metformin khi eGFR thấp).

Đã khóa: patient JSON LUÔN LÀ context injection trực tiếp mỗi session,
KHÔNG index vào corpus — bất kể việc này "tốn token" ra sao. Đây là
nguyên tắc an toàn, KHÔNG PHẢI chỗ tối ưu chi phí.

CẤM TUYỆT ĐỐI (xem AGENTS.md mục 2): không cần hỏi lại, agent phải tự
từ chối bất kỳ yêu cầu nào muốn "cache" hoặc "index" patient data vào
vector store để tiết kiệm token.
"""

from __future__ import annotations

from dataclasses import dataclass

from . import config


@dataclass(frozen=True)
class PatientContext:
    patient_id: str
    raw_json: dict


class PatientContextInjector:
    def build_injection_block(self, patient: PatientContext) -> str:
        """
        Trả về khối text patient context để prepend/append trực tiếp vào
        prompt LLM mỗi session — KHÔNG bao giờ đi qua retriever/vector
        search.
        """
        assert config.SAFETY.patient_context_indexed is False, (
            "VI PHẠM mục 8: patient_context_indexed phải luôn là False. "
            "Không index patient data vào corpus."
        )
        # TODO: format dict → text có cấu trúc rõ ràng (labs, medications,
        # allergies...) để LLM dễ đọc và trích dẫn khi diễn giải.
        raise NotImplementedError("TODO: implement format hiển thị patient context.")

    @staticmethod
    def assert_not_indexed(source: str) -> None:
        """Gọi hàm này ở bất kỳ đâu trong pipeline có khả năng vô tình
        đẩy patient data vào indexing job, để fail sớm và rõ ràng thay vì
        âm thầm vi phạm mục 8."""
        if source == "vector_index" or source == "bm25_index":
            raise RuntimeError(
                "VI PHẠM RESEARCH_FOUNDATION.md mục 8: patient data không "
                "được index. Đây là nguyên tắc an toàn, không phải chỗ "
                "tối ưu chi phí."
            )
