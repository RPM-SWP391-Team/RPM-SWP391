import logging
from dataclasses import dataclass, field
from typing import List, Set
from .chunk_store import ChunkStore
from .tokenizer import Tokenizer
from .chunking import ChunkNode
from .reranker import RerankedChunk

logger = logging.getLogger(__name__)

class DeduplicateProcessor:
    """
    Processor đầu tiên trong tầng Context Builder.
    Nhiệm vụ: Loại bỏ các chunks trùng lặp dựa trên chunk_hash hoặc node_id.
    """
    def __init__(self, primary_key: str = "chunk_hash", fallback_key: str = "chunk_id"):
        self.primary_key = primary_key
        self.fallback_key = fallback_key

    def process(self, candidates: List[RerankedChunk]) -> List[RerankedChunk]:
        if not candidates:
            return []

        unique_candidates: List[RerankedChunk] = []
        seen_identifiers: Set[str] = set()
        duplicate_count = 0

        for candidate in candidates:
            # metadata in RerankedChunk might be inside candidate.chunk.metadata
            meta = getattr(candidate.chunk, "metadata", {}) or {}
            
            # Sử dụng get để tránh fallback nhầm với chuỗi rỗng
            identifier = meta.get(self.primary_key)
            if identifier is None:
                identifier = getattr(candidate.chunk, "node_id", None)
            
            if not identifier:
                unique_candidates.append(candidate)
                continue

            try:
                if identifier in seen_identifiers:
                    duplicate_count += 1
                    logger.debug(f"[DeduplicateProcessor] Bỏ qua chunk trùng lặp: {identifier}")
                else:
                    seen_identifiers.add(identifier)
                    unique_candidates.append(candidate)
            except TypeError:
                # Bắt lỗi unhashable type (dict, list)
                logger.warning(f"[DeduplicateProcessor] Bỏ qua do identifier không hash được: {identifier}")
                unique_candidates.append(candidate)

        if duplicate_count > 0:
            logger.info(f"[DeduplicateProcessor] Đã lọc {duplicate_count} chunks trùng lặp, giữ lại {len(unique_candidates)} chunks.")
            
        return unique_candidates

from typing import TypeVar

T = TypeVar('T')

class LongContextReorderProcessor:
    """
    Tái sắp xếp ngữ cảnh để khắc phục hiện tượng 'Lost in the Middle' của LLM.
    Đưa các chunk quan trọng nhất ra rìa (đầu/cuối), dồn các chunk ít quan trọng vào giữa.
    """
    def process(self, items: List[T]) -> List[T]:
        if not items or len(items) < 3:
            return items

        items_copy = list(items)
        items_copy.reverse()
        
        reordered_items: List[T] = []
        
        for i, item in enumerate(items_copy):
            if i % 2 == 0:
                reordered_items.insert(0, item)
            else:
                reordered_items.append(item)
                
        logger.debug(f"[LongContextReorder] Đã đảo vị trí {len(reordered_items)} items.")
        return reordered_items

@dataclass(frozen=True)
class ContextChunk:
    """Một khối văn bản trong ExpandedContext"""
    node_id: str
    text: str
    source: str
    title_path: List[str]
    citation: str
    metadata: dict

@dataclass(frozen=True)
class ExpandedContext:
    """Đầu ra duy nhất của Phase 3. Bất biến."""
    chunks: List[ContextChunk] = field(default_factory=list)


class ContextBuilder:
    """
    Chịu trách nhiệm mở rộng ngữ cảnh (Context Expansion).
    Tuyệt đối không đụng vào Reranker hay Logic LLM.
    """
    def __init__(self, chunk_store: ChunkStore, tokenizer: Tokenizer, token_budget: int = 2048):
        self.chunk_store = chunk_store
        self.tokenizer = tokenizer
        self.token_budget = token_budget

    def _estimate_tokens(self, text: str) -> int:
        return len(self.tokenizer.tokenize(text))

    def _create_context_chunk(self, node: ChunkNode, score: float = 0.0) -> ContextChunk:
        source_name = node.source_file or node.document_name or node.source_document or "Unknown"
        return ContextChunk(
            node_id=node.node_id,
            text=node.text,
            source=source_name,
            title_path=node.title_path or [],
            citation=node.source_url or "",
            metadata={
                "parent_node_id": node.parent_node_id,
                "previous_sibling_node": node.previous_sibling_node,
                "next_sibling_node": node.next_sibling_node,
                "source_file": source_name,
                "document_name": source_name,
                "score": score
            }
        )

    def _needs_prev_sibling(self, text: str) -> bool:
        """Heuristic xác định xem có cần câu trước không"""
        text = text.strip()
        if len(text) < 50: return True
        anaphora = ["this", "that", "these", "those", "they", "he", "she", "tuy nhiên", "do đó", "thuốc này"]
        first_words = text.lower().split()[:2]
        return any(word in anaphora for word in first_words)

    def _needs_next_sibling(self, text: str) -> bool:
        """Heuristic xác định xem có cần câu sau không"""
        text = text.strip()
        return text.endswith(":") or text.endswith(",") or "bao gồm:" in text.lower()

    def expand(self, reranked_candidates: List[RerankedChunk]) -> ExpandedContext:
        # Bước 1: Loại bỏ chunks trùng lặp trước khi mở rộng ngữ cảnh
        deduplicator = DeduplicateProcessor()
        unique_candidates = deduplicator.process(reranked_candidates)
        
        seen_node_ids: Set[str] = set()
        final_chunks: List[ContextChunk] = []
        current_tokens = 0
        
        # Hàm add_node tiện ích để DRY (Don't repeat yourself)
        def try_add_node(n_id: str, candidate_score: float = 0.0) -> bool:
            """Cố gắng thêm node_id vào kết quả. Trả về True nếu thêm thành công, False nếu fail/budget out"""
            nonlocal current_tokens
            if not n_id or n_id in seen_node_ids:
                return False
                
            node = self.chunk_store.get_node(n_id)
            if not node:
                # Missing node, graceful degrade
                return False
                
            node_tokens = self._estimate_tokens(node.text)
            if current_tokens + node_tokens > self.token_budget:
                return False
                
            seen_node_ids.add(n_id)
            final_chunks.append(self._create_context_chunk(node, score=candidate_score))
            current_tokens += node_tokens
            return True

        for candidate in unique_candidates:
            # Nếu ngân sách đã cạn, dừng hẳn
            if current_tokens >= self.token_budget:
                logger.info("[ContextBuilder] Token budget đã đầy, dừng expand.")
                break
                
            c_node_id = candidate.chunk.node_id
            c_score = getattr(candidate, "rerank_score", getattr(getattr(candidate, "chunk", None), "score", 0.0))
            # 1. Thêm Retrieved
            added = try_add_node(c_node_id, candidate_score=c_score)
            if not added:
                continue # Nếu ngay cả node gốc cũng k add được do budget, skip hẳn
                
            # Tra cứu ChunkNode gốc để đọc metadata quan hệ
            base_node = self.chunk_store.get_node(c_node_id)
            if not base_node:
                continue
                
            chunk_text = base_node.text

            # 2. Heuristic: Có nên lấy Parent không? 
            if len(chunk_text) < 100:
                parent_id = base_node.parent_node_id
                if parent_id:
                    try_add_node(parent_id)
                    
            # 3. Heuristic: Có nên lấy Previous Sibling không?
            if self._needs_prev_sibling(chunk_text):
                prev_id = base_node.previous_sibling_node
                if prev_id:
                    try_add_node(prev_id)
                    
            # 4. Heuristic: Có nên lấy Next Sibling không?
            if self._needs_next_sibling(chunk_text):
                next_id = base_node.next_sibling_node
                if next_id:
                    try_add_node(next_id)

        logger.info(f"[ContextBuilder] Đã expand xong. Tổng tokens ~ {current_tokens}/{self.token_budget}. Số chunks = {len(final_chunks)}")
        return ExpandedContext(chunks=final_chunks)
