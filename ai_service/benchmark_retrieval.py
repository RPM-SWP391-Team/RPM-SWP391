import json
import time
import os
import logging
from collections import defaultdict
from src.retriever import FAISSVectorIndex, BM25RetrieverImpl, HybridRetriever
from src.embedder import ModelLoader
from src.tokenizer import WhitespaceTokenizer
from src.config import INDEX
from src.fusion import reciprocal_rank_fusion

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

def calculate_metrics(relevant_nodes, retrieved_nodes):
    retrieved_ids = [n.node_id for n in retrieved_nodes]
    relevant_set = set(relevant_nodes)
    
    hits = [1 if r_id in relevant_set else 0 for r_id in retrieved_ids]
    
    recall_at_1 = 1.0 if sum(hits[:1]) > 0 else 0.0
    recall_at_3 = 1.0 if sum(hits[:3]) > 0 else 0.0
    recall_at_5 = 1.0 if sum(hits[:5]) > 0 else 0.0
    recall_at_10 = 1.0 if sum(hits[:10]) > 0 else 0.0
    
    mrr = 0.0
    for i, is_hit in enumerate(hits):
        if is_hit:
            mrr = 1.0 / (i + 1)
            break
            
    hit_rate = 1.0 if sum(hits) > 0 else 0.0
    
    return {
        "recall@1": recall_at_1,
        "recall@3": recall_at_3,
        "recall@5": recall_at_5,
        "recall@10": recall_at_10,
        "mrr": mrr,
        "hit_rate": hit_rate
    }

def main(eval_set_path: str = "data/eval_set.json", report_path: str = "data/benchmark_report.json"):
    if not os.path.exists(eval_set_path):
        logger.warning(f"Bỏ qua benchmark vì không có {eval_set_path}")
        return

    # Load eval set
    with open(eval_set_path, "r", encoding="utf-8") as f:
        eval_data = json.load(f)
        
    if not eval_data:
        logger.warning("Eval set trống.")
        return
        
    # Đọc manifest
    manifest = {}
    if os.path.exists(INDEX.manifest_path):
        with open(INDEX.manifest_path, "r", encoding="utf-8") as f:
            manifest = json.load(f)

    logger.info("Đang khởi tạo các dependencies (Composition Root)...")
    tokenizer = WhitespaceTokenizer()
    embedder = ModelLoader()
    
    vector_index = FAISSVectorIndex()
    vector_index.load()
    
    bm25_index = BM25RetrieverImpl(tokenizer=tokenizer)
    bm25_index.load()
    
    hybrid = HybridRetriever(bm25_index, vector_index, query_embedder=embedder)
    
    results = defaultdict(list)
    
    time_bm25 = []
    time_dense = []
    time_rrf = []
    time_total = []

    logger.info(f"Bắt đầu chạy benchmark trên {len(eval_data)} queries...")
    for idx, item in enumerate(eval_data):
        query = item["query"]
        expected_ids = item["expected_node_ids"]
        
        t0 = time.time()
        
        # 1. BM25 Search
        t_bm25_0 = time.time()
        bm25_res = hybrid.bm25_search(query)
        t_bm25_1 = time.time()
        
        # 2. Dense Search
        t_dense_0 = time.time()
        dense_res = hybrid.vector_search(query)
        t_dense_1 = time.time()
        
        # 3. RRF
        t_rrf_0 = time.time()
        final_res = reciprocal_rank_fusion(bm25_res, dense_res)
        t_rrf_1 = time.time()
        
        t_total = time.time() - t0
        
        time_bm25.append(t_bm25_1 - t_bm25_0)
        time_dense.append(t_dense_1 - t_dense_0)
        time_rrf.append(t_rrf_1 - t_rrf_0)
        time_total.append(t_total)
        
        metrics = calculate_metrics(expected_ids, final_res)
        for k, v in metrics.items():
            results[k].append(v)

    # Aggregate
    final_report = {
        "metrics": {
            k: round(sum(v) / len(v), 4) for k, v in results.items()
        },
        "performance_ms": {
            "latency_avg": round((sum(time_total) / len(time_total)) * 1000, 2),
            "bm25_time_avg": round((sum(time_bm25) / len(time_bm25)) * 1000, 2),
            "dense_time_avg": round((sum(time_dense) / len(time_dense)) * 1000, 2),
            "rrf_time_avg": round((sum(time_rrf) / len(time_rrf)) * 1000, 2)
        },
        "build_stats": {
            "build_time_seconds": manifest.get("build_time", 0),
            "embedding_cache_hit_rate": manifest.get("embedding_cache_hit_rate", 0),
            "total_chunks": manifest.get("total_chunks", 0),
            "average_chunk_length": manifest.get("average_chunk_length", 0)
        },
        "query_count": len(eval_data)
    }

    with open(report_path, "w", encoding="utf-8") as f:
        json.dump(final_report, f, indent=4)
        
    logger.info(f"Benchmark hoàn tất. Kết quả lưu tại {report_path}")

if __name__ == "__main__":
    main()
