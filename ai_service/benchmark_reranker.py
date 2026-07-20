import time
import json
import logging
import numpy as np

from src.reranker import CrossEncoderReranker, RetrievedChunk
from src.embedder import ModelLoader

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class DummyCrossEncoder(ModelLoader):
    # Dùng tạm mảng ngẫu nhiên để test latency framework
    def predict(self, sentences, batch_size):
        # Fake delay
        time.sleep(0.01 * len(sentences))
        return np.random.rand(len(sentences)).tolist()

def main():
    logger.info("Chạy Benchmark cho Reranker...")
    
    # Tạo dummy data
    candidates = [
        RetrievedChunk(node_id=f"id_{i}", text=f"Document content {i} "*50, score=0.5, retriever_type="bm25")
        for i in range(100)
    ]
    
    model = DummyCrossEncoder()
    reranker = CrossEncoderReranker(model=model, batch_size=32)
    
    query = "đái tháo đường tuýp 2"
    
    latencies = []
    
    # Warmup
    reranker.rerank(query, candidates[:10], top_k=5)
    
    # Test Latency với Top 20 (chạy 50 lần)
    for _ in range(50):
        t0 = time.time()
        reranker.rerank(query, candidates[:20], top_k=5)
        latencies.append(time.time() - t0)
        
    avg_latency = sum(latencies) / len(latencies) * 1000
    
    report = {
        "benchmark": "CrossEncoder Reranker",
        "candidates_count": 20,
        "top_k_output": 5,
        "batch_size": 32,
        "average_latency_ms": round(avg_latency, 2)
    }
    
    with open("data/benchmark_reranker_report.json", "w", encoding="utf-8") as f:
        json.dump(report, f, indent=4)
        
    logger.info(f"Hoàn tất. Report: {json.dumps(report, indent=2)}")

if __name__ == "__main__":
    main()
