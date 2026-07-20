import pytest
import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from src.retriever import RetrievedChunk
from src.reranker import CrossEncoderReranker, RerankedChunk

class MockModelOOM:
    def predict(self, sentences, batch_size):
        raise RuntimeError("CUDA Out of Memory")

class MockModelNormal:
    def predict(self, sentences, batch_size):
        # Giả lập trả điểm ngẫu nhiên, ví dụ đảo ngược index
        return [float(len(sentences) - i) for i in range(len(sentences))]

@pytest.fixture
def dummy_candidates():
    return [
        RetrievedChunk(node_id="id1", text="text1", score=0.9, retriever_type="dense"),
        RetrievedChunk(node_id="id2", text="text2", score=0.8, retriever_type="bm25"),
        RetrievedChunk(node_id="id3", text="text3", score=0.7, retriever_type="dense")
    ]

def test_reranker_immutability(dummy_candidates):
    reranker = CrossEncoderReranker(model=MockModelNormal())
    
    # Copy metadata memory addresses
    orig_scores = [c.score for c in dummy_candidates]
    
    results = reranker.rerank("query", dummy_candidates, top_k=3)
    
    for i, c in enumerate(dummy_candidates):
        assert c.score == orig_scores[i], "Reranker đã vi phạm immutability: sửa đổi điểm số ban đầu!"
        
    for r in results:
        assert isinstance(r, RerankedChunk)

def test_reranker_reorder(dummy_candidates):
    reranker = CrossEncoderReranker(model=MockModelNormal())
    results = reranker.rerank("query", dummy_candidates, top_k=3)
    
    # MockModelNormal trả về score giảm dần từ cuối mảng (len - i)
    # sentences: id1(3.0), id2(2.0), id3(1.0)
    # id1 phải có score cao nhất (3.0)
    assert results[0].chunk.node_id == "id1"
    assert results[0].rerank_score == 3.0
    assert results[-1].chunk.node_id == "id3"
    assert results[-1].rerank_score == 1.0

def test_reranker_fallback_on_oom(dummy_candidates):
    reranker = CrossEncoderReranker(model=MockModelOOM())
    results = reranker.rerank("query", dummy_candidates, top_k=3)
    
    # Fallback phải trả về đúng thứ tự gốc
    assert results[0].chunk.node_id == "id1"
    assert results[1].chunk.node_id == "id2"
    assert results[2].chunk.node_id == "id3"
    
    # Và rerank_score phải bị gán về 0.0
    for r in results:
        assert r.rerank_score == 0.0
