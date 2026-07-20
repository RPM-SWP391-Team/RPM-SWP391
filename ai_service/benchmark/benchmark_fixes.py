"""
benchmark_fixes.py — Đánh giá retrieval (Recall@K / MRR) + khung cho
Ragas-style evaluation sau này.

RESEARCH_FOUNDATION.md mục 10:
- Ragas: bộ 4 chỉ số Context Recall, Context Precision, Faithfulness,
  Answer Relevancy, dùng LLM-as-judge.
- Thứ tự tối ưu đúng: Recall trước (không bỏ sót thông tin) → Precision
  sau (giảm nhiễu) — vì generation quality phụ thuộc hoàn toàn vào độ
  bao phủ ngữ cảnh trước.
- TRẠNG THÁI: hiện chỉ có Recall@K/MRR (không tốn token). Faithfulness/
  Answer Relevancy CHƯA triển khai (cần LLM-as-judge) — đây là khoảng
  trống thật, ghi rõ trong RESEARCH_FOUNDATION.md, không phải "không có
  nghiên cứu".

Dùng file này để benchmark TRƯỚC KHI thay đổi bất kỳ dòng "Đã khóa" nào
có yêu cầu benchmark (RRF, hybrid retrieval, embedding model, reranker) —
xem AGENTS.md mục 1, bước 4.
"""

from __future__ import annotations

from dataclasses import dataclass


@dataclass
class QueryGroundTruth:
    query: str
    relevant_node_ids: set[str]


def recall_at_k(retrieved_node_ids: list[str], relevant_node_ids: set[str], k: int) -> float:
    if not relevant_node_ids:
        return 0.0
    top_k = set(retrieved_node_ids[:k])
    hit = len(top_k & relevant_node_ids)
    return hit / len(relevant_node_ids)


def mrr(retrieved_node_ids: list[str], relevant_node_ids: set[str]) -> float:
    for rank, node_id in enumerate(retrieved_node_ids, start=1):
        if node_id in relevant_node_ids:
            return 1.0 / rank
    return 0.0


def run_benchmark_suite(
    test_set: list[QueryGroundTruth],
    retrieve_fn,  # callable(query: str) -> list[str] (node_ids đã rank)
    k_values: tuple[int, ...] = (5, 10, 20),
) -> dict:
    """
    retrieve_fn nên trỏ vào pipeline thật (hoặc từng stage riêng: chỉ
    BM25, chỉ dense, RRF-only, RRF+rerank...) để so sánh trước/sau khi
    đổi 1 thành phần — đúng tinh thần "phải có số liệu mới được đổi".
    """
    results = {f"recall@{k}": [] for k in k_values}
    results["mrr"] = []

    for item in test_set:
        retrieved = retrieve_fn(item.query)
        for k in k_values:
            results[f"recall@{k}"].append(recall_at_k(retrieved, item.relevant_node_ids, k))
        results["mrr"].append(mrr(retrieved, item.relevant_node_ids))

    return {metric: sum(values) / len(values) if values else 0.0 for metric, values in results.items()}


# TODO (khoảng trống đã ghi nhận ở mục 10): thêm Faithfulness / Answer
# Relevancy dùng LLM-as-judge (Ragas hoặc tự viết prompt judge), theo
# thứ tự ưu tiên Recall trước rồi mới Precision.
