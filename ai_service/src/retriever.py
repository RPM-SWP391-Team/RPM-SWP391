"""
retriever.py — Tầng Retrieval Tìm Kiếm (Đã được Refactor với Strict Constraints)
Tuyệt đối chỉ đảm nhiệm load() và search(). Không build index ở đây.
Mọi phụ thuộc (tokenizer, model loader) phải được Inject vào constructor.
"""

from __future__ import annotations
from dataclasses import dataclass, field
from typing import Protocol, Any, Dict, List
import os
import json
import pickle
import numpy as np
import logging

from . import config
from .tokenizer import Tokenizer

logger = logging.getLogger(__name__)


@dataclass
class RetrievedChunk:
    node_id: str
    text: str
    score: float
    retriever_type: str = ""  # "bm25" hoặc "dense"
    metadata: Dict[str, Any] = field(default_factory=dict)
    
    # Backwards compatibility cho contract tests
    title_path: list[str] = field(default_factory=list)
    source: str = ""

    def __post_init__(self):
        if self.source and not self.retriever_type:
            self.retriever_type = self.source
        if self.retriever_type and not self.source:
            self.source = self.retriever_type
        if self.metadata and "title_path" in self.metadata and not self.title_path:
            self.title_path = self.metadata["title_path"]



class BM25Index(Protocol):
    def load(self) -> None: ...
    def search(self, query: str, top_k: int) -> List[RetrievedChunk]: ...


class VectorIndex(Protocol):
    def load(self) -> None: ...
    def search(self, query_embedding: List[float], top_k: int) -> List[RetrievedChunk]: ...


class QueryEmbedder(Protocol):
    """Protocol để Inject khả năng sinh embedding cho query vào HybridRetriever."""
    def embed_texts(self, texts: List[str], batch_size: int = ...) -> np.ndarray: ...


class FAISSVectorIndex:
    """
    Retriever sử dụng FAISS.
    Ràng buộc: Bắt buộc dùng IndexFlatIP và Normalization L2 (cosine similarity).
    """
    def __init__(self, index_path: str = config.INDEX.vector_index_path, id_map_path: str = config.INDEX.vector_id_map_path, manifest_path: str = config.INDEX.manifest_path):
        self.index_path = index_path
        self.id_map_path = id_map_path
        self.manifest_path = manifest_path
        self._index = None
        self._id_to_metadata = {}

    def _verify_manifest(self):
        if not os.path.exists(self.manifest_path):
            raise FileNotFoundError(f"Không tìm thấy manifest tại {self.manifest_path}")
        with open(self.manifest_path, 'r', encoding='utf-8') as f:
            manifest = json.load(f)
            
        expected_model = config.EMBEDDING.model_name
        expected_dim = config.EMBEDDING.embedding_dimension
        
        if manifest.get("embedding_model") != expected_model:
            raise RuntimeError(f"Index Manifest Mismatch: Model kỳ vọng '{expected_model}', thực tế '{manifest.get('embedding_model')}'")
        if manifest.get("embedding_dimension") != expected_dim:
            raise RuntimeError(f"Index Manifest Mismatch: Dimension kỳ vọng {expected_dim}, thực tế {manifest.get('embedding_dimension')}")
            
    def load(self) -> None:
        import faiss
        logger.info("Đang kiểm tra Manifest cho FAISS Index...")
        self._verify_manifest()
        
        if not os.path.exists(self.index_path):
            raise FileNotFoundError(f"Không tìm thấy FAISS Index tại {self.index_path}")
        if not os.path.exists(self.id_map_path):
            raise FileNotFoundError(f"Không tìm thấy ID Map tại {self.id_map_path}")
            
        logger.info(f"Đang load FAISS Index từ {self.index_path}...")
        self._index = faiss.read_index(self.index_path)
        
        with open(self.id_map_path, 'r', encoding='utf-8') as f:
            self._id_to_metadata = {int(k): v for k, v in json.load(f).items()}
        logger.info("Load FAISS Index thành công.")

    def search(self, query_embedding: List[float], top_k: int) -> List[RetrievedChunk]:
        """
        Khóa chặt API: Không nhận string, chỉ nhận vector.
        Không tự ý khởi tạo Model.
        """
        if self._index is None:
            raise RuntimeError("Chưa load FAISS index.")
            
        q = np.array([query_embedding], dtype='float32')
        import faiss
        faiss.normalize_L2(q)  # Chuẩn hóa để IndexFlatIP hoạt động như Cosine
        
        distances, indices = self._index.search(q, top_k)
        
        results = []
        for dist, idx in zip(distances[0], indices[0]):
            if idx == -1:
                continue
            chunk_data = self._id_to_metadata[idx]
            results.append(RetrievedChunk(
                node_id=chunk_data["id"],
                text=chunk_data["text"],
                score=float(dist),
                retriever_type="dense",
                metadata=chunk_data["metadata"]
            ))
        return results


class BM25RetrieverImpl:
    """
    Retriever sử dụng rank_bm25.
    Ràng buộc: Mọi tách từ phải qua Tokenizer interface.
    """
    def __init__(self, tokenizer: Tokenizer, index_path: str = config.INDEX.bm25_index_path):
        self.index_path = index_path
        self.tokenizer = tokenizer
        self._bm25 = None
        self._chunks_data = []

    def load(self) -> None:
        if not os.path.exists(self.index_path):
            raise FileNotFoundError(f"Không tìm thấy BM25 Index tại {self.index_path}")
            
        logger.info(f"Đang load BM25 Index từ {self.index_path}...")
        with open(self.index_path, 'rb') as f:
            data = pickle.load(f)
            self._bm25 = data['bm25']
            self._chunks_data = data['chunks']
        logger.info("Load BM25 Index thành công.")

    def search(self, query: str, top_k: int) -> List[RetrievedChunk]:
        if self._bm25 is None:
            raise RuntimeError("Chưa load BM25 index.")
            
        tokenized_query = self.tokenizer.tokenize(query)
        scores = self._bm25.get_scores(tokenized_query)
        
        top_k_indices = np.argsort(scores)[::-1][:top_k]
        
        results = []
        for idx in top_k_indices:
            score = scores[idx]
            if score <= 0:
                continue
            chunk_data = self._chunks_data[idx]
            results.append(RetrievedChunk(
                node_id=chunk_data["id"],
                text=chunk_data["text"],
                score=float(score),
                retriever_type="bm25",
                metadata=chunk_data["metadata"]
            ))
        return results


class HybridRetriever:
    """
    Điều phối BM25 và Dense độc lập, sau đó đưa lên Pipeline để gọi RRF.
    Dependencies (bm25, vector, embedder) được inject từ Composition Root.
    """
    def __init__(self, bm25_index: BM25Index, vector_index: VectorIndex, query_embedder: QueryEmbedder):
        self._bm25 = bm25_index
        self._vector = vector_index
        self._embedder = query_embedder

    def bm25_search(self, query: str, top_k: int = config.RETRIEVAL.bm25_top_k) -> List[RetrievedChunk]:
        return self._bm25.search(query, top_k)

    def vector_search(self, query: str, top_k: int = config.RETRIEVAL.dense_top_k) -> List[RetrievedChunk]:
        # Orchestration layer xử lý text -> embedding
        emb = self._embedder.embed_texts([query])[0]
        return self._vector.search(emb.tolist(), top_k)

    def retrieve_both(self, query: str) -> tuple[List[RetrievedChunk], List[RetrievedChunk]]:
        """Trả về tuple (bm25_results, dense_results) không can thiệp hay chuẩn hóa."""
        return self.bm25_search(query), self.vector_search(query)
