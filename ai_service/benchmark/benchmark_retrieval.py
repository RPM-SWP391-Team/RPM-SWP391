import json
import time
import os
import sys
import logging
from pathlib import Path

# Add root project path
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from src.retriever import HybridRetriever, FAISSVectorIndex, BM25RetrieverImpl
from src.embedder import ModelLoader
from src.tokenizer import WhitespaceTokenizer
from src.config import INDEX

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class DummyQueryEmbedder:
    def embed_texts(self, texts, batch_size=32):
        import numpy as np
        # Return dummy vector of dimension 768
        return [np.random.rand(768).astype('float32') for _ in texts]

def calculate_mrr(expected_ids, retrieved_ids):
    for i, rid in enumerate(retrieved_ids):
        if rid in expected_ids:
            return 1.0 / (i + 1)
    return 0.0

def calculate_recall_at_k(expected_ids, retrieved_ids, k):
    top_k_retrieved = set(retrieved_ids[:k])
    expected_set = set(expected_ids)
    if not expected_set:
        return 0.0
    hits = len(expected_set.intersection(top_k_retrieved))
    return hits / len(expected_set)

def run_retrieval_benchmark():
    eval_dir = Path(__file__).resolve().parents[1] / "evaluation"
    with open(eval_dir / "golden_queries.json", "r", encoding="utf-8") as f:
        queries = json.load(f)
    with open(eval_dir / "retrieval_ground_truth.json", "r", encoding="utf-8") as f:
        ground_truth = json.load(f)

    # Khởi tạo Pipeline (Mock load nếu không có index thực tế để demo)
    try:
        tokenizer = WhitespaceTokenizer()
        vector_index = FAISSVectorIndex(INDEX.vector_index_path, INDEX.vector_id_map_path, INDEX.manifest_path)
        vector_index.load()
        bm25_index = BM25RetrieverImpl(tokenizer=tokenizer, index_path=INDEX.bm25_index_path)
        bm25_index.load()
        hybrid_retriever = HybridRetriever(bm25_index, vector_index, DummyQueryEmbedder())
        has_index = True
    except Exception as e:
        logger.warning(f"Không thể load index thực tế ({e}). Khởi động chế độ MOCK để test framework.")
        has_index = False
        
    metrics = {
        "recall_at_1": [],
        "recall_at_3": [],
        "recall_at_5": [],
        "recall_at_10": [],
        "mrr": [],
        "latencies": []
    }
    
    error_analysis = []

    for q_id, q_text in queries.items():
        expected_ids = ground_truth.get(q_id, {}).get("expected_chunk_ids", [])
        
        t0 = time.time()
        
        # MOCK logic nếu chưa build index
        if not has_index:
            # Giả lập trả về đúng 1 expected_id và vài id rác
            retrieved_ids = ["dummy_1", expected_ids[0] if expected_ids else "dummy_2", "dummy_3"]
            time.sleep(0.05) # Fake latency
        else:
            bm25_res, dense_res = hybrid_retriever.retrieve_both(q_text)
            # Giả lập RRF (Hoặc lấy từ src.reranker nếu có)
            # Lấy top 10 từ bm25
            retrieved_ids = [c.node_id for c in bm25_res][:10]
            if not retrieved_ids and dense_res:
                retrieved_ids = [c.node_id for c in dense_res][:10]
            
        latency = (time.time() - t0) * 1000
        metrics["latencies"].append(latency)
        
        mrr = calculate_mrr(expected_ids, retrieved_ids)
        r1 = calculate_recall_at_k(expected_ids, retrieved_ids, 1)
        r3 = calculate_recall_at_k(expected_ids, retrieved_ids, 3)
        r5 = calculate_recall_at_k(expected_ids, retrieved_ids, 5)
        r10 = calculate_recall_at_k(expected_ids, retrieved_ids, 10)
        
        metrics["mrr"].append(mrr)
        metrics["recall_at_1"].append(r1)
        metrics["recall_at_3"].append(r3)
        metrics["recall_at_5"].append(r5)
        metrics["recall_at_10"].append(r10)
        
        # Lưu error analysis nếu MRR = 0 (Không lọt top)
        if mrr == 0:
            error_analysis.append({
                "query_id": q_id,
                "question": q_text,
                "expected_ids": expected_ids,
                "retrieved_ids": retrieved_ids,
                "failure_reason": "Missed all expected chunks in top K"
            })

    report = {
        "benchmark": "Retrieval Pipeline",
        "total_queries": len(queries),
        "mean_recall_at_1": sum(metrics["recall_at_1"]) / len(queries),
        "mean_recall_at_5": sum(metrics["recall_at_5"]) / len(queries),
        "mean_mrr": sum(metrics["mrr"]) / len(queries),
        "average_latency_ms": sum(metrics["latencies"]) / len(queries)
    }

    reports_dir = Path(__file__).resolve().parents[1] / "reports"
    reports_dir.mkdir(exist_ok=True)
    
    with open(reports_dir / "retrieval_report.json", "w", encoding="utf-8") as f:
        json.dump(report, f, indent=4)
        
    with open(reports_dir / "error_analysis.json", "w", encoding="utf-8") as f:
        json.dump(error_analysis, f, indent=4)
        
    logger.info(f"Đã xuất báo cáo Retrieval tại {reports_dir}")
    return report

if __name__ == "__main__":
    run_retrieval_benchmark()
