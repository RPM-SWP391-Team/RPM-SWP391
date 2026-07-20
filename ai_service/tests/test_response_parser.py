import pytest
import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from src.response_parser import ResponseParser, FinalResponse
from src.context_builder import ContextChunk

def test_citation_mapping():
    llm_output = "Bệnh nhân cần tiêm Insulin [Source 1] và kết hợp ăn kiêng [Source 2]. Không dùng [Source 99]."
    
    chunk_1 = ContextChunk(node_id="n1", text="a", source="doc A", title_path=[], citation="linkA", metadata={})
    chunk_2 = ContextChunk(node_id="n2", text="b", source="doc B", title_path=[], citation="linkB", metadata={})
    
    citation_map = {
        "[Source 1]": chunk_1,
        "[Source 2]": chunk_2
    }
    
    res = ResponseParser.parse(llm_output, citation_map)
    
    # Source 99 không có trong map nên không lấy. [Source 1] và [Source 2] lấy ra thành công.
    assert len(res.sources) == 2
    assert res.sources[0].source == "doc A"
    assert res.sources[1].source == "doc B"
    assert res.answer == llm_output

def test_citation_mapping_duplicate_tags():
    llm_output = "Nhắc lại [Source 1] và [Source 1]"
    chunk_1 = ContextChunk(node_id="n1", text="a", source="doc A", title_path=[], citation="linkA", metadata={})
    
    citation_map = {
        "[Source 1]": chunk_1
    }
    
    res = ResponseParser.parse(llm_output, citation_map)
    
    # Mặc dù nhắc 2 lần nhưng source list chỉ trả về 1 instance (deduplicate)
    assert len(res.sources) == 1
