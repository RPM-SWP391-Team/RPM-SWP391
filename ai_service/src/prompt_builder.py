from typing import Dict, Any, Tuple
from dataclasses import dataclass
import logging

from .context_builder import ExpandedContext, ContextChunk
from .patient_formatter import PatientContextFormatter
from .tokenizer import Tokenizer

logger = logging.getLogger(__name__)

SYSTEM_INSTRUCTION = """<SystemRole>
Bạn là một Trợ lý Y khoa Lâm sàng chuyên nghiệp. Nhiệm vụ của bạn là hỗ trợ Bác sĩ bằng cách đưa ra các khuyến nghị, tư vấn và phân tích y khoa tuyệt đối chính xác dựa trên dữ liệu được cung cấp.
</SystemRole>

<Instructions>
Bạn PHẢI tuân thủ nghiêm ngặt các quy tắc sinh tồn sau:
1. CHỐNG ẢO GIÁC (ZERO HALLUCINATION): Bạn CHỈ ĐƯỢC PHÉP sử dụng kiến thức có trong phần <Evidence>. TUYỆT ĐỐI KHÔNG sử dụng kiến thức nền (prior knowledge), trí nhớ hoặc tự suy diễn các phác đồ điều trị không có trong Evidence.
2. THIẾU THÔNG TIN: Nếu phần <Evidence> không chứa thông tin để trả lời câu hỏi của Bác sĩ, bạn phải lập tức trả lời: "Dựa trên các tài liệu hiện có, không có đủ dữ liệu để trả lời câu hỏi này." Không được cố gắng đoán mò.
3. BẮT BUỘC TRÍCH DẪN (CITATION): Bất kỳ một nhận định, liều lượng thuốc, hay hướng dẫn điều trị nào bạn đưa ra đều phải đi kèm trích dẫn nguồn ở cuối câu bằng định dạng [Source X].
4. CÁCH LÀM VIỆC VỚI DỮ LIỆU BỆNH NHÂN: Sử dụng <PatientData> để hiểu bối cảnh bệnh lý. Đối chiếu tình trạng của bệnh nhân với các tiêu chuẩn trong <Evidence> để đưa ra phân tích. Không tự chẩn đoán bệnh nếu <Evidence> không có hướng dẫn.
5. QUAN ĐIỂM ĐA CHIỀU: Nếu phần <Evidence> có nhiều nguồn tài liệu đưa ra các hướng dẫn khác nhau (xung đột), hãy liệt kê rành mạch quan điểm của từng nguồn. Không tự ý thiên vị nguồn nào.
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
