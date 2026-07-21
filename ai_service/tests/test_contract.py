"""
tests/test_contract.py — Bộ test BẤT BIẾN KIẾN TRÚC.

Đây không phải unit test thông thường cho logic nghiệp vụ — đây là
"tripwire" để bắt các vi phạm RESEARCH_FOUNDATION.md ngay cả khi agent
không đọc AGENTS.md hoặc cố tình lách qua.

Chạy trước mỗi lần merge: `pytest tests/test_contract.py -v`

Nếu 1 test ở đây fail, KHÔNG được sửa test cho pass — phải hỏi lại người
dùng xem thay đổi kiến trúc có được duyệt không (xem AGENTS.md mục 1).
Sửa test để né fail = chính là hành vi "tự ý thêm" mà tài liệu gốc cấm.
"""

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from src import config
from src.fusion import reciprocal_rank_fusion
from src.retriever import RetrievedChunk


# ---------------------------------------------------------------------
# Mục 8 — Patient context KHÔNG BAO GIỜ được index
# ---------------------------------------------------------------------
def test_patient_context_never_indexed():
    assert config.SAFETY.patient_context_indexed is False, (
        "VI PHẠM mục 8: patient_context_indexed đã bị đổi thành True. "
        "Đây là nguyên tắc an toàn tuyệt đối, không phải chỗ tối ưu."
    )


# ---------------------------------------------------------------------
# Mục 9 — LLM không bao giờ tự tính số liệu y khoa
# ---------------------------------------------------------------------
def test_llm_never_computes_numbers():
    assert config.SAFETY.llm_may_compute_numbers is False, (
        "VI PHẠM mục 9: llm_may_compute_numbers đã bị đổi thành True. "
        "Mọi phép tính y khoa phải qua src/deterministic.py."
    )


# ---------------------------------------------------------------------
# Mục 3 — RRF phải dùng đúng công thức 1/(k+rank), KHÔNG cộng điểm gốc
# ---------------------------------------------------------------------
def test_rrf_uses_rank_not_raw_score():
    """
    Kiểm chứng: nếu đổi 2 danh sách sao cho điểm gốc (score) khác biệt
    cực lớn nhưng RANK giữ nguyên, kết quả RRF phải KHÔNG đổi.
    Nếu ai đó lỡ sửa fusion.py để cộng score gốc vào, test này sẽ fail
    vì kết quả sẽ khác nhau giữa 2 trường hợp.
    """
    list_a = [
        RetrievedChunk(node_id="n1", text="", score=100.0, title_path=[], source="bm25"),
        RetrievedChunk(node_id="n2", text="", score=1.0, title_path=[], source="bm25"),
    ]
    list_b = [
        RetrievedChunk(node_id="n1", text="", score=0.0001, title_path=[], source="bm25"),
        RetrievedChunk(node_id="n2", text="", score=0.0000999, title_path=[], source="bm25"),
    ]

    result_a = reciprocal_rank_fusion([list_a])
    result_b = reciprocal_rank_fusion([list_b])

    assert [c.node_id for c in result_a] == [c.node_id for c in result_b], (
        "VI PHẠM mục 3: RRF score bị ảnh hưởng bởi giá trị score gốc thay "
        "vì chỉ rank — có thể fusion.py đã bị sửa để cộng điểm trực tiếp."
    )


def test_rrf_formula_matches_paper():
    """score(d) = Σ 1/(k+rank(d)), k=60 mặc định — Cormack et al. 2009."""
    single_item = [RetrievedChunk(node_id="only", text="", score=0, title_path=[], source="bm25")]
    result = reciprocal_rank_fusion([single_item], k=60)
    expected = 1.0 / (60 + 1)
    assert abs(result[0].score - expected) < 1e-9, (
        f"VI PHẠM mục 3: công thức RRF sai. Kỳ vọng {expected}, "
        f"nhận {result[0].score}."
    )


# ---------------------------------------------------------------------
# Mục 4 — rerank chỉ chạy trên top-N từ RRF, có fallback bắt buộc
# ---------------------------------------------------------------------
def test_reranker_fails_open_not_closed():
    from src.reranker import CrossEncoderReranker

    reranker = CrossEncoderReranker(model=None)  # mô phỏng load lỗi
    candidates = [
        RetrievedChunk(node_id=f"n{i}", text="x", score=1.0 / i, title_path=[], source="rrf")
        for i in range(1, 6)
    ]
    # KHÔNG được raise exception — phải fallback về RRF-only
    result = reranker.rerank("query bất kỳ", candidates, top_k=3)
    assert len(result) == 3, (
        "VI PHẠM mục 4: reranker phải fallback về RRF-only khi model lỗi, "
        "không được crash hệ thống."
    )


# ---------------------------------------------------------------------
# Mục 7 — thứ tự context expansion cố định
# ---------------------------------------------------------------------
def test_context_expansion_order_locked():
    assert config.CONTEXT_EXPANSION.expansion_order == (
        "retrieved", "parent", "prev", "next", "sibling",
    ), (
        "VI PHẠM mục 7: thứ tự ưu tiên context expansion đã bị đổi. "
        "Thứ tự retrieved → parent → prev → next → sibling là thiết kế "
        "đã khóa, không tự ý sắp lại."
    )


# ---------------------------------------------------------------------
# Mục 5 — embedding model đã khóa
# ---------------------------------------------------------------------
def test_embedding_model_locked():
    assert config.EMBEDDING.model_name == "BAAI/bge-small-en-v1.5", (
        "VI PHẠM mục 5: embedding model đã bị đổi. Đổi embedding kéo theo "
        "re-index toàn bộ corpus — phải có benchmark Recall@K trước, xem "
        "AGENTS.md mục 1 bước 4."
    )


# ---------------------------------------------------------------------
# Mục 2 — cả BM25 và dense đều bắt buộc tồn tại (không bị xóa 1 nhánh)
# ---------------------------------------------------------------------
def test_hybrid_retrieval_has_both_branches():
    from src.retriever import HybridRetriever
    import inspect
    
    source = inspect.getsource(HybridRetriever.retrieve_both)
    assert "bm25_search" in source, "HybridRetriever phải gọi nhánh bm25"
    assert "vector_search" in source, "HybridRetriever phải gọi nhánh vector"

def test_phase4_prompt_builder_no_retriever_imports():
    """PromptBuilder tuyệt đối không được gọi vào logic Retrieval hoặc DB"""
    import inspect
    from src import prompt_builder
    
    source = inspect.getsource(prompt_builder)
    assert "retriever" not in source, "Lỗi ranh giới: PromptBuilder import retriever!"
    assert "ChunkStore" not in source, "Lỗi ranh giới: PromptBuilder gọi ChunkStore!"
    assert "BM25" not in source, "Lỗi ranh giới: PromptBuilder dính tới BM25!"
    assert "FAISS" not in source, "Lỗi ranh giới: PromptBuilder dính tới FAISS!"

def test_phase4_no_patient_json_leak():
    """Kiểm tra không component nào của Phase 1, 2, 3 chứa từ khóa Patient"""
    import inspect
    import src.retriever
    import src.index_builder
    import src.chunk_store
    import src.context_builder
    
    for module in [src.retriever, src.index_builder, src.chunk_store, src.context_builder]:
        source = inspect.getsource(module)
        assert "patient" not in source.lower(), f"Lỗi ranh giới: {module.__name__} bị leak dữ liệu Patient JSON!"
