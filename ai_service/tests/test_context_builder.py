import pytest
import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from src.retriever import RetrievedChunk
from src.reranker import RerankedChunk
from src.chunking import ChunkNode
from src.chunk_store import DictChunkStore
from src.context_builder import ContextBuilder
from src.tokenizer import WhitespaceTokenizer

@pytest.fixture
def mock_store():
    store = DictChunkStore()
    
    # Tạo một hệ thống graph để test expansion
    # node_retrieved -> có parent, prev, next, sibling
    # Sibling lại trỏ ngược về node_retrieved (loop)
    
    chunks = [
        ChunkNode(node_id="parent_1", text="Parent text "*10, title_path=[], chunk_type="text"),
        ChunkNode(node_id="prev_1", text="Prev text "*10, title_path=[], chunk_type="text"),
        ChunkNode(node_id="next_1", text="Next text "*10, title_path=[], chunk_type="text"),
        
        # retrieved_1 có liên kết parent, prev, next
        ChunkNode(
            node_id="retrieved_1", 
            text="Retrieved text "*10, 
            title_path=[], chunk_type="text",
            parent_node_id="parent_1",
            previous_sibling_node="prev_1",
            next_sibling_node="next_1"
        ),
        
        # Tạo Loop: prev_1 trỏ ngược về retrieved_1 thông qua next_sibling_node
        ChunkNode(
            node_id="prev_1_loop", 
            text="Prev Loop "*10, 
            title_path=[], chunk_type="text",
            next_sibling_node="retrieved_1"
        )
    ]
    
    store.load_from_list(chunks)
    return store

@pytest.fixture
def dummy_candidates():
    rc = RetrievedChunk(node_id="retrieved_1", text="Retrieved text "*10, score=0.9, retriever_type="bm25", metadata={})
    return [RerankedChunk(chunk=rc, rerank_score=0.9)]


def test_expansion_order_is_locked(mock_store, dummy_candidates):
    builder = ContextBuilder(chunk_store=mock_store, tokenizer=WhitespaceTokenizer(), token_budget=1000)
    
    expanded = builder.expand(dummy_candidates)
    
    # Rút node_ids theo thứ tự
    ids = [c.node_id for c in expanded.chunks]
    
    # Bắt buộc phải là: retrieved -> parent -> prev -> next
    expected_order = ["retrieved_1", "parent_1", "prev_1", "next_1"]
    assert ids == expected_order, f"Sai thứ tự Expansion! Kỳ vọng {expected_order}, thực tế {ids}"


def test_context_builder_determinism(mock_store, dummy_candidates):
    builder = ContextBuilder(chunk_store=mock_store, tokenizer=WhitespaceTokenizer(), token_budget=1000)
    
    # Chạy 100 lần, lưu output thứ tự ID
    first_output = [c.node_id for c in builder.expand(dummy_candidates).chunks]
    
    for _ in range(100):
        current_output = [c.node_id for c in builder.expand(dummy_candidates).chunks]
        assert current_output == first_output, "Context Expansion không deterministic, kết quả khác nhau qua các lần chạy!"


def test_context_builder_deduplication_and_loops(mock_store):
    # prev_1_loop trỏ ngược lại retrieved_1.
    builder = ContextBuilder(chunk_store=mock_store, tokenizer=WhitespaceTokenizer(), token_budget=1000)
    
    dummy = [RerankedChunk(
        chunk=RetrievedChunk(node_id="prev_1_loop", text="", score=0, retriever_type=""), 
        rerank_score=0
    )]
    
    expanded = builder.expand(dummy)
    
    # Sibling_1 sẽ gọi retrieved_1. Nhưng output k được có duplicate
    ids = [c.node_id for c in expanded.chunks]
    assert len(ids) == len(set(ids)), "Deduplication thất bại, có node bị lặp lại!"
    assert "retrieved_1" in ids


def test_context_builder_token_budget(mock_store, dummy_candidates):
    # Mỗi text có 20 tokens (chữ "text" * 10 và chữ "Retrieved/Parent..." * 10)
    # 5 nodes = 100 tokens.
    # Set budget = 50 tokens => Chỉ chứa được 2.5 nodes -> Chỉ add được retrieved (20), parent (20), đến prev là fail.
    
    builder = ContextBuilder(chunk_store=mock_store, tokenizer=WhitespaceTokenizer(), token_budget=45)
    
    expanded = builder.expand(dummy_candidates)
    
    ids = [c.node_id for c in expanded.chunks]
    
    assert "retrieved_1" in ids
    assert "parent_1" in ids
    assert "prev_1" not in ids, "Token Budget bị vượt qua! Lẽ ra không được thêm Prev"
    assert "next_1" not in ids
