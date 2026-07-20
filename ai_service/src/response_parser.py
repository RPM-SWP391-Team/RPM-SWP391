import re
from typing import Dict, List
from dataclasses import dataclass

from .context_builder import ContextChunk

@dataclass
class FinalResponse:
    answer: str
    sources: List[ContextChunk]

class ResponseParser:
    """
    Parser đầu ra của LLM.
    Chỉ có 1 nhiệm vụ: Tìm và dịch các thẻ [Source X] thành danh sách trích dẫn đầy đủ.
    Không gọi API, không sửa Prompt.
    """
    @staticmethod
    def parse(llm_output: str, citation_map: Dict[str, ContextChunk]) -> FinalResponse:
        
        # Regex tìm tất cả các mẫu dạng [Source 1], [Source 2]
        matches = re.findall(r"(\[Source \d+\])", llm_output)
        
        # Lọc ra các source thực sự có trong map
        unique_tags = list(dict.fromkeys(matches)) # Xóa trùng lặp nhưng giữ thứ tự
        
        sources = []
        for tag in unique_tags:
            if tag in citation_map:
                sources.append(citation_map[tag])
                
        return FinalResponse(
            answer=llm_output,
            sources=sources
        )
