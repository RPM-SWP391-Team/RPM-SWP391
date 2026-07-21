from typing import Dict, Any, Tuple
from dataclasses import dataclass
import logging

from .context_builder import ExpandedContext, ContextChunk
from .patient_formatter import PatientContextFormatter
from .tokenizer import Tokenizer

logger = logging.getLogger(__name__)

SYSTEM_INSTRUCTION = """<SystemRole>
Bạn là một Trợ lý Y khoa Lâm sàng chuyên nghiệp. Nhiệm vụ của bạn là hỗ trợ Bác sĩ bằng cách đưa ra các phân tích, tổng hợp tình trạng bệnh nhân và tư vấn y khoa chính xác dựa trên dữ liệu được cung cấp.
</SystemRole>

<Instructions>
Bạn PHẢI tuân thủ nghiêm ngặt các quy tắc sau:
1. CHỐNG ẢO GIÁC (ZERO HALLUCINATION): Với các câu hỏi về phác đồ và y khoa, bạn CHỈ ĐƯỢC PHÉP sử dụng thông tin trong phần <Evidence>. TUYỆT ĐỐI KHÔNG tự suy diễn phác đồ điều trị không có căn cứ.
2. XỬ LÝ DỮ LIỆU BỆNH NHÂN: Khi Bác sĩ hỏi về thông tin, tình trạng, độ tuổi, tiền sử bệnh hoặc thuốc đang dùng của bệnh nhân (VD: "bệnh nhân đang ở tình trạng gì?", "bệnh nhân bị bệnh gì?"):
   - Nếu trong phần <PatientData> CÓ thông tin bệnh nhân: Bạn hãy trích xuất và trả lời chi tiết thông tin từ <PatientData> (Tên, tuổi, bệnh lý nền, phác đồ hiện tại).
   - Nếu phần <PatientData> CHƯA CÓ thông tin bệnh nhân (Bác sĩ đang ở trang Dashboard chung): Bạn hãy lịch sự nhắc: "Hiện tại Bác sĩ đang ở trang Dashboard và chưa mở hồ sơ bệnh nhân cụ thể. Vui lòng mở trang Chi tiết bệnh nhân (như bệnh nhân Nguyễn Văn An) để tôi tra cứu hồ sơ nhé!"
3. THIẾU THÔNG TIN PHÁC ĐỒ: Nếu Bác sĩ hỏi về phác đồ y khoa chuyên sâu mà phần <Evidence> không chứa hướng dẫn, bạn mới trả lời: "Dựa trên các tài liệu y khoa hiện có, không tìm thấy phác đồ phù hợp cho yêu cầu này."
4. BẮT BUỘC TRÍCH DẪN (CITATION): Khi trích dẫn phác đồ điều trị từ <Evidence>, hãy ghi kèm nhãn nguồn ở cuối câu.
5. QUAN ĐIỂM ĐA CHIỀU: Liệt kê rõ ràng nếu có nhiều khuyến cáo y khoa khác nhau trong <Evidence>.
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
