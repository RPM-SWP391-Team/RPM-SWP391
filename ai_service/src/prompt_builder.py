from typing import Dict, Any, Tuple
from dataclasses import dataclass
import logging

from .context_builder import ExpandedContext, ContextChunk
from .patient_formatter import PatientContextFormatter
from .tokenizer import Tokenizer

logger = logging.getLogger(__name__)

SYSTEM_INSTRUCTION = """<SystemRole>
Bạn là Trợ lý Y khoa Lâm sàng dành cho Bác sĩ. Nhiệm vụ của bạn là hỗ trợ Bác sĩ phân tích tình trạng bệnh nhân từ dữ liệu <PatientData> và tra cứu hướng dẫn y khoa từ <Evidence>.
</SystemRole>

<Instructions>
1. THÔNG TIN BỆNH NHÂN (<PatientData>):
   - Tất cả thông tin về bệnh nhân (Họ tên, Mã BN, Tuổi, Giới tính, Bệnh nền, Phác đồ thuốc) nằm trong khối <PatientData>.
   - Khi Bác sĩ hỏi về bệnh nhân (ví dụ: "bệnh nhân tình trạng như nào", "bệnh nhân này có thông tin gì"), bạn PHẢI trích xuất và trình bày rõ ràng toàn bộ thông tin có trong <PatientData>.
   - Nếu khối <PatientData> ghi rõ Bác sĩ đang ở Dashboard tổng quan (chưa mở bệnh nhân nào), hãy thông báo cho Bác sĩ biết để chọn bệnh nhân.

2. TRA CỨU PHÁC ĐỒ Y KHOA (<Evidence>):
   - Với các câu hỏi về hướng dẫn điều trị, liều dùng thuốc, quy chuẩn ADA/ESC, CHỈ sử dụng thông tin trong <Evidence>. KHÔNG tự suy diễn ngoài tài liệu.

3. TRÌNH BÀY:
   - Trả lời bằng tiếng Việt mạch lạc, chuyên nghiệp, súc tích.
   - TUYỆT ĐỐI KHÔNG tự chèn các nhãn [Source 1], [Nguồn 1] hoặc Nguồn 23 vào trong lời văn.
</Instructions>"""

@dataclass
class PromptResult:
    prompt_text: str
    citation_map: Dict[str, ContextChunk]
    token_count: int

class PromptBudgetExceededError(Exception):
    pass

from collections import defaultdict

class MetadataGroupingProcessor:
    def __init__(self, delimiter: str = "\n\n" + "="*40 + "\n\n"):
        self.delimiter = delimiter

    def process(self, chunks: list) -> Tuple[str, Dict[str, ContextChunk]]:
        if not chunks:
            return "Không tìm thấy tài liệu y khoa nào phù hợp.", {}

        # Cấu trúc: Dict[Source_Document, Dict[Title_Path, List[Tuple[int, ContextChunk]]]]
        grouped_data = defaultdict(lambda: defaultdict(list))
        citation_map = {}

        # 1. Quét qua mảng và gom nhóm
        for idx, chunk in enumerate(chunks):
            source = chunk.source if chunk.source else "Tài liệu chưa xác định (Unknown Source)"
            
            if chunk.title_path and len(chunk.title_path) > 0:
                clean_titles = [str(t).strip() for t in chunk.title_path if str(t).strip()]
                path_str = " > ".join(clean_titles) if clean_titles else "Nội dung chung"
            else:
                path_str = "Nội dung chung"
                
            grouped_data[source][path_str].append((idx + 1, chunk))

        # 2. Định dạng đầu ra thành Markdown String
        formatted_documents = []
        
        for source, sections in grouped_data.items():
            source_block = [f"### 📄 Nguồn tài liệu: {source}"]
            
            for section_path, chunk_tuples in sections.items():
                if section_path != "Nội dung chung":
                    source_block.append(f"**📌 Cấu trúc:** {section_path}")
                
                for original_idx, chunk in chunk_tuples:
                    source_tag = f"[Source {original_idx}]"
                    citation_map[source_tag] = chunk
                    source_block.append(f"{source_tag}:\n{chunk.text.strip()}\n")
                
            formatted_documents.append("\n".join(source_block))
        
        # 3. Apply Long Context Reorder on the formatted document blocks
        from .context_builder import LongContextReorderProcessor
        reorder = LongContextReorderProcessor()
        reordered_documents = reorder.process(formatted_documents)
        
        return self.delimiter.join(reordered_documents), citation_map

class PromptBuilder:
    """
    Trách nhiệm: Lắp ghép các khối text thành Prompt hoàn chỉnh.
    Không giao tiếp với Retrieval. Không giao tiếp LLM. Không tự gọt (truncate) context.
    """
    def __init__(self, tokenizer: Tokenizer, max_prompt_tokens: int = 1000000):
        self.tokenizer = tokenizer
        self.max_prompt_tokens = max_prompt_tokens
        self.grouping_processor = MetadataGroupingProcessor()

    def build(self, user_question: str, expanded_context: ExpandedContext, patient_data: Dict[str, Any]) -> PromptResult:
        
        # 1. Format Patient Block
        patient_block_text = PatientContextFormatter.format(patient_data)
        patient_block = f"<PatientData>\n{patient_block_text}\n</PatientData>"

        # 2. Build Retrieved Context & Citation Map using MetadataGroupingProcessor
        grouped_text, citation_map = self.grouping_processor.process(expanded_context.chunks)
        retrieved_block = f"<Evidence>\n{grouped_text}\n</Evidence>"

        # 3. Assemble Final Prompt
        final_prompt = (
            f"{SYSTEM_INSTRUCTION}\n\n"
            f"{patient_block}\n\n"
            f"{retrieved_block}\n\n"
            f"<UserQuestion>\nCâu hỏi của Bác sĩ: {user_question}\n</UserQuestion>\n\n"
            f"Answer:"
        )

        # 4. Token Budget Check
        tokens = self.tokenizer.tokenize(final_prompt)
        token_count = len(tokens)
        
        if token_count > self.max_prompt_tokens:
            raise PromptBudgetExceededError(
                f"Prompt quá dài ({token_count} tokens) so với mức trần ({self.max_prompt_tokens} tokens). "
                f"Cần điều chỉnh ContextBuilder để cắt giảm."
            )
            
        logger.info(f"[PromptBuilder] Đã tạo prompt thành công. Số tokens = {token_count}. Số references = {len(citation_map)}.")
        
        return PromptResult(
            prompt_text=final_prompt,
            citation_map=citation_map,
            token_count=token_count
        )
