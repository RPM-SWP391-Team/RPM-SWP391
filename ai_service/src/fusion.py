"""
fusion.py — Reciprocal Rank Fusion (RRF)

Nguồn: Cormack, Clarke, Büttcher, "Reciprocal Rank Fusion Outperforms
Condorcet and Individual Rank Learning Methods", SIGIR 2009.

Công thức (RESEARCH_FOUNDATION.md mục 3, "Đã khóa"):
    score(d) = Σ 1 / (k + rank(d))     với k = 60

Vì sao KHÔNG cộng bm25_score + vector_score trực tiếp: hai loại điểm có
thang đo khác nhau (BM25 không chặn trên, cosine similarity trong [-1,1]).
RRF chỉ dùng RANK, không cần normalize điểm.

CẤM: thay bằng weighted-sum hay learned fusion trừ khi có benchmark cụ thể
(xem AGENTS.md mục 1, bước 4).
"""

from __future__ import annotations

from collections import defaultdict

from . import config
from .retriever import RetrievedChunk


def reciprocal_rank_fusion(
    ranked_lists: list[list[RetrievedChunk]],
    k: int = config.RRF.k,
) -> list[RetrievedChunk]:
    """
    Nhận nhiều danh sách đã xếp hạng (VD: [bm25_results, dense_results]),
    trả về 1 danh sách hợp nhất theo điểm RRF, sắp giảm dần.

    KHÔNG được sửa công thức bên trong hàm này để cộng score gốc vào —
    đó chính là lỗi "cộng điểm trực tiếp" mà mục 3 đã cảnh báo là sai về
    mặt toán học.
    """
    rrf_scores: dict[str, float] = defaultdict(float)
    chunk_by_id: dict[str, RetrievedChunk] = {}

    for ranked_list in ranked_lists:
        for rank, chunk in enumerate(ranked_list, start=1):
            rrf_scores[chunk.node_id] += 1.0 / (k + rank)
            # Giữ lại 1 bản chunk đại diện (text/title_path giống nhau dù
            # đến từ nguồn nào — node_id là khóa duy nhất).
            chunk_by_id.setdefault(chunk.node_id, chunk)

    fused = [
        RetrievedChunk(
            node_id=node_id,
            text=chunk_by_id[node_id].text,
            score=score,
            title_path=chunk_by_id[node_id].title_path,
            source="rrf",
        )
        for node_id, score in rrf_scores.items()
    ]
    fused.sort(key=lambda c: c.score, reverse=True)
    return fused
