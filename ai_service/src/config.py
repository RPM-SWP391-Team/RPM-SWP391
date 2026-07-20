"""
config.py — Tất cả hằng số kiến trúc "đã khóa" nằm ở ĐÂY, một chỗ duy nhất.

Lý do gom vào 1 file: nếu một AI agent muốn "tối ưu" bằng cách đổi RRF k=60
thành số khác, hoặc đổi top_k_final, nó phải sửa ở đây — và mọi thay đổi ở
đây phải trích dẫn được vào RESEARCH_FOUNDATION.md. Không cho phép magic
number rải rác trong retriever.py / reranker.py.
"""

from dataclasses import dataclass


@dataclass(frozen=True)
class RRFConfig:
    # Nguồn: Cormack, Clarke, Büttcher, SIGIR 2009.
    # k=60 là hằng số khuyến nghị phổ biến trong paper gốc.
    # RESEARCH_FOUNDATION.md mục 3 — "Đã khóa".
    k: int = 60


@dataclass(frozen=True)
class RetrievalConfig:
    # RESEARCH_FOUNDATION.md mục 2 — "Đã khóa". Không xóa 1 trong 2 nhánh
    # (bm25 / dense) trừ khi có benchmark Recall@K chứng minh trên corpus
    # thật.
    bm25_top_k: int = 50
    dense_top_k: int = 50

    # Số ứng viên đưa từ RRF sang cross-encoder rerank.
    # RESEARCH_FOUNDATION.md mục 4 — "Đã khóa": rerank chỉ chạy trên top-N
    # từ RRF, KHÔNG chạy trên toàn bộ candidates.
    rrf_candidates_for_rerank: int = 20


@dataclass(frozen=True)
class RerankConfig:
    # RESEARCH_FOUNDATION.md mục 4.
    model_name: str = "BAAI/bge-reranker-v2-m3"
    top_k_final: int = 5
    # Bắt buộc: nếu model load lỗi, KHÔNG được crash — fallback về RRF-only.
    fail_open_to_rrf: bool = True


@dataclass(frozen=True)
class EmbeddingConfig:
    # RESEARCH_FOUNDATION.md mục 5 — "Đã khóa".
    # KHÔNG đổi sang OpenAI/Gemini embedding trừ khi có benchmark Recall@K
    # trên chính corpus ADA/KDIGO cho thấy cải thiện rõ rệt (đổi model kéo
    # theo re-index toàn bộ corpus).
    model_name: str = "BAAI/bge-small-en-v1.5"
    embedding_dimension: int = 384


@dataclass(frozen=True)
class IndexConfig:
    # Thông số cấu hình chung cho Phase 2 Indexing
    vector_index_path: str = "data/vector.index"
    vector_id_map_path: str = "data/vector_id_map.json"
    bm25_index_path: str = "data/bm25.pkl"
    manifest_path: str = "data/index_manifest.json"
    cache_db_path: str = "data/embedding_cache.db"
    embedding_batch_size: int = 32
    device: str = "cpu"  # Mặc định cpu, hệ thống sẽ ưu tiên cuda nếu có


@dataclass(frozen=True)
class ChunkingConfig:
    # RESEARCH_FOUNDATION.md mục 6 — "Đã khóa, đóng góp riêng".
    # Chunk theo heading thật (#, ##, **PHẦN N:** cho docx), KHÔNG dùng
    # RecursiveCharacterTextSplitter / SemanticChunker của LangChain.
    strategy: str = "hierarchical_heading"
    protect_atomic_blocks: bool = True  # bảng, công thức không bị cắt ngang


@dataclass(frozen=True)
class ContextExpansionConfig:
    # RESEARCH_FOUNDATION.md mục 7 — "Đã khóa, đóng góp riêng".
    # Thứ tự ưu tiên mở rộng — KHÔNG đổi trừ khi có lý do cụ thể.
    expansion_order: tuple = ("retrieved", "parent", "prev", "next", "sibling")
    token_budget: int = 1000000


@dataclass(frozen=True)
class SafetyConfig:
    # RESEARCH_FOUNDATION.md mục 8 — patient context KHÔNG BAO GIỜ được
    # index vào vector corpus, luôn injection trực tiếp mỗi session.
    patient_context_indexed: bool = False  # PHẢI luôn là False

    # RESEARCH_FOUNDATION.md mục 9 — LLM không bao giờ tự tính số liệu y
    # khoa. Mọi phép tính phải qua deterministic.py.
    llm_may_compute_numbers: bool = False  # PHẢI luôn là False


@dataclass(frozen=True)
class LLMConfig:
    model_name: str = "gemini-2.5-flash"
    temperature: float = 0.1
    max_output_tokens: int = 1024

RRF = RRFConfig()
RETRIEVAL = RetrievalConfig()
RERANK = RerankConfig()
EMBEDDING = EmbeddingConfig()
CHUNKING = ChunkingConfig()
CONTEXT_EXPANSION = ContextExpansionConfig()
SAFETY = SafetyConfig()
INDEX = IndexConfig()
LLM = LLMConfig()
