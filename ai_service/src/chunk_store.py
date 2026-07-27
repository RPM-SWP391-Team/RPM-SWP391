import json
import logging
from typing import Protocol, Optional, Dict, List
from .chunking import ChunkNode

logger = logging.getLogger(__name__)

class ChunkStore(Protocol):
    """
    Protocol tra cứu ChunkNode.
    Trách nhiệm duy nhất: Nhận node_id và trả về ChunkNode gốc.
    TUYỆT ĐỐI CẤM chứa business logic như BM25, FAISS hay Reranking ở đây.
    """
    def get_node(self, node_id: str) -> Optional[ChunkNode]: ...
    def get_nodes(self, node_ids: List[str]) -> List[ChunkNode]: ...

class DictChunkStore:
    """
    Implementation đơn giản của ChunkStore dựa trên Dictionary, load toàn bộ vào RAM.
    Được sử dụng khi parse chunks.jsonl vào memory.
    """
    def __init__(self):
        self._store: Dict[str, ChunkNode] = {}

    def load_from_jsonl(self, filepath: str) -> None:
        """Load từ file chunks.jsonl của Phase 1"""
        try:
            with open(filepath, 'r', encoding='utf-8') as f:
                for line in f:
                    if not line.strip():
                        continue
                    data = json.loads(line)
                    node = ChunkNode.from_dict(data)
                    self._store[node.node_id] = node
            logger.info(f"Đã load {len(self._store)} chunks vào ChunkStore.")
        except Exception as e:
            logger.error(f"Lỗi khi load ChunkStore từ {filepath}: {e}")
            raise

    def load_from_list(self, chunks: List[ChunkNode]) -> None:
        """Load trực tiếp từ mảng (dùng cho Unit Test)"""
        for chunk in chunks:
            self._store[chunk.node_id] = chunk

    def get_node(self, node_id: str) -> Optional[ChunkNode]:
        return self._store.get(node_id)
        
    def get_nodes(self, node_ids: List[str]) -> List[ChunkNode]:
        return [self._store[nid] for nid in node_ids if nid in self._store]
