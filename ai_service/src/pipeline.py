"""
pipeline.py — RAGPipeline: nối các module theo ĐÚNG thứ tự đã khóa trong
RESEARCH_FOUNDATION.md.

    query
      -> HybridRetriever.retrieve_both()           [mục 2]
      -> fusion.reciprocal_rank_fusion()             [mục 3]
      -> CrossEncoderReranker.rerank()                [mục 4]
      -> ContextBuilder.expand()                       [mục 7]
      -> PatientContextInjector.build_injection_block()  [mục 8, luôn chạy
         song song, KHÔNG đi qua retrieval]
      -> (nếu câu hỏi cần số liệu y khoa) deterministic.py TRƯỚC   [mục 9]
      -> LLM generation, chỉ diễn giải, không tự tính              [mục 9]

File này là nơi DUY NHẤT được phép quyết định thứ tự gọi các module.
Nếu một agent muốn thêm bước mới vào giữa pipeline (VD: query rewriting,
HyDE, multi-hop...) — đó là thay đổi kiến trúc, phải qua AGENTS.md mục 1
trước, KHÔNG tự chèn vào đây rồi báo cáo sau.
"""

from __future__ import annotations

from dataclasses import dataclass

from . import config
from .chunking import ChunkNode
from .context_builder import ContextBuilder, ExpandedContext
from .fusion import reciprocal_rank_fusion
from .patient_context import PatientContext, PatientContextInjector
from .reranker import CrossEncoderReranker
from .retriever import HybridRetriever


@dataclass
class PipelineResult:
    expanded_context: ExpandedContext
    patient_block: str | None
    # answer generation (gọi LLM thật) nằm NGOÀI phạm vi skeleton này —
    # đây chỉ là phần "grounding pipeline" theo mục 1.


class RAGPipeline:
    def __init__(
        self,
        retriever: HybridRetriever,
        reranker: CrossEncoderReranker,
        context_builder: ContextBuilder,
        patient_injector: PatientContextInjector,
    ):
        self._retriever = retriever
        self._reranker = reranker
        self._context_builder = context_builder
        self._patient_injector = patient_injector

    def run(
        self,
        query: str,
        node_lookup: dict,
        patient: PatientContext | None = None,
    ) -> PipelineResult:
        # 1. Hybrid retrieval — mục 2
        bm25_results, dense_results = self._retriever.retrieve_both(query)

        # 2. RRF fusion — mục 3 (KHÔNG cộng điểm trực tiếp)
        fused = reciprocal_rank_fusion([bm25_results, dense_results])
        candidates = fused[: config.RETRIEVAL.rrf_candidates_for_rerank]

        # 3. Cross-encoder rerank — mục 4 (fallback tự động nếu lỗi)
        top_chunks = self._reranker.rerank(query, candidates, top_k=config.RERANK.top_k_final)

        # 4. Context expansion — mục 7 (retrieved -> parent -> prev -> next -> sibling)
        expanded = self._context_builder.expand(top_chunks)

        # 5. Patient context — mục 8 (luôn injection trực tiếp, KHÔNG qua retrieval ở trên)
        patient_block = None
        if patient is not None:
            patient_block = self._patient_injector.build_injection_block(patient)

        return PipelineResult(expanded_context=expanded, patient_block=patient_block)
