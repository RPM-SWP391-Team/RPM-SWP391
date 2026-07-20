import sys
import json
import pytest
import os
from pathlib import Path

# Add root project path
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from benchmark.benchmark_retrieval import calculate_mrr, calculate_recall_at_k

def test_retrieval_metrics():
    """Kiểm tra độ chính xác của hàm tính MRR và Recall"""
    expected = ["chunk_1", "chunk_2"]
    
    # Test MRR
    assert calculate_mrr(expected, ["chunk_3", "chunk_1"]) == 0.5
    assert calculate_mrr(expected, ["chunk_1"]) == 1.0
    assert calculate_mrr(expected, ["chunk_99"]) == 0.0
    
    # Test Recall
    assert calculate_recall_at_k(expected, ["chunk_1", "chunk_3"], k=5) == 0.5
    assert calculate_recall_at_k(expected, ["chunk_1", "chunk_2", "chunk_3"], k=2) == 1.0

def test_determinism():
    """Cố tình chạy hàm DummyQueryEmbedder 2 lần xem kết quả có bị random không (Nếu dummy, ta cho phép test này cảnh báo hoặc tuỳ biến)"""
    from src.retriever import RetrievedChunk
    from src.reranker import CrossEncoderReranker, RerankedChunk
    
    class DeterministicMock:
        def predict(self, sentences, batch_size):
            return [1.0] * len(sentences)
            
    reranker = CrossEncoderReranker(model=DeterministicMock(), batch_size=32)
    candidates = [RetrievedChunk(node_id="n1", text="a", score=1, retriever_type="")]
    
    res1 = [c.chunk.node_id for c in reranker.rerank("q", candidates, 1)]
    res2 = [c.chunk.node_id for c in reranker.rerank("q", candidates, 1)]
    
    assert res1 == res2, "Lỗi Determinism! Pipeline trả kết quả ngẫu nhiên!"
    
def test_citation_integrity():
    from src.response_parser import ResponseParser, FinalResponse
    from src.context_builder import ContextChunk
    
    # Citation Coverage phải tính toán dựa trên map hợp lệ
    chunk = ContextChunk(node_id="n1", text="txt", source="src", title_path=[], citation="link", metadata={})
    cmap = {"[Source 1]": chunk}
    
    out = ResponseParser.parse("Test [Source 1]", cmap)
    assert len(out.sources) == 1
    assert out.sources[0].node_id == "n1"
    
    out2 = ResponseParser.parse("Test [Source 2]", cmap)
    assert len(out2.sources) == 0 # Orphan reference -> Sẽ làm giảm Citation Coverage
