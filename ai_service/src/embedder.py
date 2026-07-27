import os
import sqlite3
import numpy as np
import logging
from typing import List, Optional
from src.config import EMBEDDING, INDEX
import torch

logger = logging.getLogger(__name__)

class ModelLoader:
    """
    Cơ chế khởi tạo và load model.
    Không lưu state singleton ngầm định để đảm bảo Dependency Injection.
    """
    def __init__(self, model_name: str = EMBEDDING.model_name, device: str = INDEX.device):
        self.model_name = model_name
        self.device = device
        self._model = None
        os.environ["TOKENIZERS_PARALLELISM"] = "false"
        os.environ["OMP_NUM_THREADS"] = "1"
        os.environ["MKL_NUM_THREADS"] = "1"
        try:
            torch.set_num_threads(1)
        except Exception:
            pass

    def load(self):
        if self._model is None:
            try:
                from sentence_transformers import SentenceTransformer
            except ImportError:
                raise ImportError("Vui lòng cài đặt sentence-transformers: pip install sentence-transformers")
                
            logger.info(f"Đang tải Embedding Model: {self.model_name} vào {self.device}...")
            try:
                self._model = SentenceTransformer(self.model_name, device=self.device)
                logger.info("Tải model thành công.")
            except Exception as e:
                logger.error(f"Lỗi khi tải model {self.model_name}: {str(e)}")
                raise RuntimeError(f"Không thể khởi tạo Embedding Model {self.model_name}. Lỗi: {str(e)}")

    def embed_texts(self, texts: List[str], batch_size: int = INDEX.embedding_batch_size) -> np.ndarray:
        if not texts:
            return np.array([])
        self.load()
        embeddings = self._model.encode(
            texts,
            batch_size=batch_size,
            show_progress_bar=False,
            convert_to_numpy=True,
            normalize_embeddings=True
        )
        return embeddings


class EmbeddingCache:
    """
    Disk-based cache for embeddings using SQLite.
    Khóa đa hình: embedding_model + "_" + chunk_hash.
    Đảm bảo embedding_dimension khớp.
    """
    def __init__(self, db_path: str = INDEX.cache_db_path):
        self.db_path = db_path
        os.makedirs(os.path.dirname(os.path.abspath(self.db_path)), exist_ok=True)
        self._init_db()

    def _init_db(self):
        with sqlite3.connect(self.db_path) as conn:
            conn.execute('''
                CREATE TABLE IF NOT EXISTS embeddings (
                    cache_key TEXT PRIMARY KEY,
                    dimension INTEGER NOT NULL,
                    vector BLOB NOT NULL
                )
            ''')
            conn.commit()

    def _make_key(self, model_name: str, chunk_hash: str) -> str:
        return f"{model_name}_{chunk_hash}"

    def get(self, model_name: str, chunk_hash: str, expected_dimension: int) -> Optional[np.ndarray]:
        key = self._make_key(model_name, chunk_hash)
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT dimension, vector FROM embeddings WHERE cache_key = ?", (key,))
            row = cursor.fetchone()
            if row:
                dim, vector_blob = row
                if dim != expected_dimension:
                    # Dimension không khớp (model có thể update), xóa entry
                    logger.warning(f"Cache dimension mismatch cho key {key}. Kỳ vọng {expected_dimension}, nhận {dim}. Đang xóa entry.")
                    cursor.execute("DELETE FROM embeddings WHERE cache_key = ?", (key,))
                    conn.commit()
                    return None
                return np.frombuffer(vector_blob, dtype=np.float32)
        return None

    def set(self, model_name: str, chunk_hash: str, vector: np.ndarray):
        key = self._make_key(model_name, chunk_hash)
        dim = vector.shape[0]
        with sqlite3.connect(self.db_path) as conn:
            conn.execute(
                "INSERT OR REPLACE INTO embeddings (cache_key, dimension, vector) VALUES (?, ?, ?)",
                (key, dim, vector.astype(np.float32).tobytes())
            )
            conn.commit()

    def set_many(self, model_name: str, hash_vector_pairs: List[tuple[str, np.ndarray]]):
        with sqlite3.connect(self.db_path) as conn:
            data = []
            for h, v in hash_vector_pairs:
                key = self._make_key(model_name, h)
                dim = v.shape[0]
                data.append((key, dim, v.astype(np.float32).tobytes()))
            
            conn.executemany(
                "INSERT OR REPLACE INTO embeddings (cache_key, dimension, vector) VALUES (?, ?, ?)",
                data
            )
            conn.commit()
