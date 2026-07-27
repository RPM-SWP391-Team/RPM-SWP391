import logging
import os
import torch
from dataclasses import dataclass
from typing import Protocol, List
from .retriever import RetrievedChunk

logger = logging.getLogger(__name__)

os.environ["TOKENIZERS_PARALLELISM"] = "false"

@dataclass(frozen=True)
class RerankedChunk:
    """
    Class bất biến (Immutable) biểu diễn một chunk đã qua Reranking.
    Tuyệt đối không đụng vào `score` của RetrievedChunk gốc.
    """
    chunk: RetrievedChunk
    rerank_score: float

class Reranker(Protocol):
    """
    Protocol cho Reranker.
    Trách nhiệm duy nhất: Sắp xếp lại danh sách candidates.
    """
    def rerank(self, query: str, candidates: List[RetrievedChunk], top_k: int = 10) -> List[RerankedChunk]: ...


class CrossEncoderModelLoader(Protocol):
    """Protocol để Inject Model Load vào Reranker"""
    def predict(self, sentences: List[List[str]], batch_size: int) -> List[float]: ...

class CrossEncoderModelLoaderImpl:
    def __init__(self, model_name: str = "cross-encoder/ms-marco-MiniLM-L-6-v2", device: str = "cpu"):
        from sentence_transformers import CrossEncoder
        logger.info(f"Đang tải Reranker Model: {model_name} vào {device}...")
        self.model = CrossEncoder(model_name, device=device)
        logger.info("Tải Reranker model thành công.")
        
    def predict(self, sentences: List[List[str]], batch_size: int) -> List[float]:
        return self.model.predict(sentences, batch_size=batch_size, show_progress_bar=False).tolist()

class CrossEncoderReranker:
    """
    Reranker sử dụng mô hình Cross-Encoder.
    Tuân thủ nghiêm ngặt Fallback và Immutability.
    """
    def __init__(self, model: CrossEncoderModelLoader, batch_size: int = 32):
        self.model = model
        self.batch_size = batch_size

    def rerank(self, query: str, candidates: List[RetrievedChunk], top_k: int = 10) -> List[RerankedChunk]:
        if not candidates:
            return []
            
        try:
            # Format input cho cross-encoder: [[query, text], [query, text], ...]
            sentences = [[query, c.text] for c in candidates]
            
            # Batch inference
            scores = self.model.predict(sentences, batch_size=self.batch_size)
            
            # Gói lại thành RerankedChunk
            reranked_results = [
                RerankedChunk(chunk=c, rerank_score=float(s))
                for c, s in zip(candidates, scores)
            ]
            
            # Sort theo rerank_score giảm dần
            reranked_results.sort(key=lambda x: x.rerank_score, reverse=True)
            
            # Cắt lấy top_k
            final_results = reranked_results[:top_k]
            
            # Logging ID
            orig_ids = [c.node_id for c in candidates[:top_k]]
            new_ids = [r.chunk.node_id for r in final_results]
            logger.info(f"[Reranker] Original Top {len(orig_ids)}: {orig_ids}")
            logger.info(f"[Reranker] Reranked Top {len(new_ids)}: {new_ids}")
            
            return final_results
            
        except Exception as e:
            # Fallback: Trả về nguyên bản thứ tự ban đầu nếu có bất kỳ lỗi nào (OOM, Timeout, Model Lỗi)
            logger.warning(f"[Reranker Fallback] Lỗi khi chạy inference ({str(e)}). Trả về thứ tự gốc (RRF).")
            return [
                RerankedChunk(chunk=c, rerank_score=0.0)
                for c in candidates[:top_k]
            ]
