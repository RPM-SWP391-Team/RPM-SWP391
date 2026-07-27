import os
import json
import time
import datetime
import logging
import pickle
import numpy as np
from typing import List

from . import config
from .chunking import ChunkNode
from .tokenizer import Tokenizer
from .embedder import ModelLoader, EmbeddingCache

logger = logging.getLogger(__name__)

class IndexBuilder:
    """
    Nơi duy nhất chịu trách nhiệm Build Index.
    Tuyệt đối không lưu các biến state như _model, _bm25 trong các class phụ trợ.
    """
    def __init__(self, tokenizer: Tokenizer, embedder: ModelLoader, cache: EmbeddingCache):
        self.tokenizer = tokenizer
        self.embedder = embedder
        self.cache = cache
        
    def _validate_data(self, chunks: List[ChunkNode], embeddings: List[np.ndarray], dimension: int):
        logger.info("Đang kiểm tra tính toàn vẹn dữ liệu (Validation)...")
        # 1. Trùng lặp node_id
        node_ids = [c.node_id for c in chunks]
        if len(node_ids) != len(set(node_ids)):
            raise ValueError("Lỗi Validation: Có node_id bị trùng lặp.")
            
        # 2. Khớp độ dài
        if len(chunks) != len(embeddings):
            raise ValueError("Lỗi Validation: Số lượng chunks và embeddings không khớp.")
            
        # 3. Empty chunks
        for c in chunks:
            if not c.text or len(c.text.strip()) == 0:
                raise ValueError(f"Lỗi Validation: Chunk {c.node_id} có nội dung rỗng.")
                
        # 4. Empty / Zero embeddings và Dimension mismatch
        for i, emb in enumerate(embeddings):
            if emb.shape[0] != dimension:
                raise ValueError(f"Lỗi Validation: Embedding của chunk {chunks[i].node_id} sai kích thước (Kỳ vọng {dimension}, thực tế {emb.shape[0]}).")
            if not np.any(emb):
                raise ValueError(f"Lỗi Validation: Embedding của chunk {chunks[i].node_id} trống (toàn số 0).")

    def _build_faiss_and_save(self, chunks: List[ChunkNode], embeddings: List[np.ndarray], dimension: int):
        import faiss
        logger.info("Đang khởi tạo và build FAISS Index (IndexFlatIP)...")
        index = faiss.IndexFlatIP(dimension)
        
        vectors_np = np.stack(embeddings).astype('float32')
        faiss.normalize_L2(vectors_np)
        index.add(vectors_np)
        
        os.makedirs(os.path.dirname(os.path.abspath(config.INDEX.vector_index_path)), exist_ok=True)
        faiss.write_index(index, config.INDEX.vector_index_path)
        
        chunk_dicts = {i: c.to_dict() for i, c in enumerate(chunks)}
        with open(config.INDEX.vector_id_map_path, 'w', encoding='utf-8') as f:
            json.dump(chunk_dicts, f, ensure_ascii=False)
        logger.info("Đã lưu FAISS Index và ID Map.")

    def _build_bm25_and_save(self, chunks: List[ChunkNode]):
        from rank_bm25 import BM25Okapi
        logger.info("Đang build BM25 Index...")
        tokenized_corpus = [self.tokenizer.tokenize(chunk.text) for chunk in chunks]
        bm25_index = BM25Okapi(tokenized_corpus)
        
        os.makedirs(os.path.dirname(os.path.abspath(config.INDEX.bm25_index_path)), exist_ok=True)
        with open(config.INDEX.bm25_index_path, 'wb') as f:
            pickle.dump({
                'bm25': bm25_index,
                'chunks': [c.to_dict() for c in chunks]
            }, f)
        logger.info("Đã lưu BM25 Index.")

    def build_all(self, chunks: List[ChunkNode]) -> dict:
        start_time = time.time()
        
        expected_dim = config.EMBEDDING.embedding_dimension
        model_name = config.EMBEDDING.model_name
        
        embeddings = []
        uncached_indices = []
        uncached_texts = []
        
        cache_hits = 0
        
        # 1. Trích xuất từ Cache
        for i, chunk in enumerate(chunks):
            if not chunk.chunk_hash:
                chunk.update_hash()
                
            cached_vector = self.cache.get(model_name, chunk.chunk_hash, expected_dim)
            if cached_vector is not None:
                embeddings.append(cached_vector)
                cache_hits += 1
            else:
                embeddings.append(None)
                uncached_indices.append(i)
                uncached_texts.append(chunk.text)
                
        # 2. Sinh embedding cho mục chưa hit cache
        if uncached_texts:
            logger.info(f"Cache miss {len(uncached_texts)} chunks. Đang tiến hành chạy model embedding...")
            new_vectors = self.embedder.embed_texts(uncached_texts)
            
            cache_updates = []
            for j, idx in enumerate(uncached_indices):
                vector = new_vectors[j]
                embeddings[idx] = vector
                cache_updates.append((chunks[idx].chunk_hash, vector))
                
            self.cache.set_many(model_name, cache_updates)
        else:
            logger.info("Cache hit 100%. Không cần chạy model embedding.")
            
        # 3. Validation
        self._validate_data(chunks, embeddings, expected_dim)
        
        # 4. Ghi FAISS & BM25
        self._build_faiss_and_save(chunks, embeddings, expected_dim)
        self._build_bm25_and_save(chunks)
        
        build_duration = time.time() - start_time
        
        # 5. Sinh Manifest
        unique_docs = len(set(c.source_document for c in chunks if c.source_document))
        chunk_lengths = [len(c.text) for c in chunks]
        
        manifest = {
            "parser_version": "1.0.0",
            "schema_version": "2.0.0",
            "chunking_version": "1.0.0",
            "retriever_version": "2.0.0",
            "embedding_model": model_name,
            "embedding_dimension": expected_dim,
            "total_documents": unique_docs,
            "total_chunks": len(chunks),
            "average_chunk_length": round(sum(chunk_lengths)/len(chunk_lengths), 2) if chunks else 0,
            "max_chunk_length": max(chunk_lengths) if chunks else 0,
            "build_time": round(build_duration, 2),
            "embedding_cache_hit_rate": round(cache_hits / len(chunks) * 100, 2) if chunks else 0,
            "created_at": datetime.datetime.now(datetime.timezone.utc).isoformat()
        }
        
        with open(config.INDEX.manifest_path, "w", encoding="utf-8") as f:
            json.dump(manifest, f, indent=4)
            
        logger.info(f"Manifest đã lưu tại {config.INDEX.manifest_path}")
        return manifest
