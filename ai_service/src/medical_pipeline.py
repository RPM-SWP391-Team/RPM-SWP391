import json
import logging
from dataclasses import dataclass
from typing import Optional, List, Dict, Any
import time

logger = logging.getLogger(__name__)

from .pipeline import RAGPipeline, PipelineResult
from .prompt_builder import PromptBuilder, PromptResult
from .llm_client import GeminiLLMClient, GroqLLMClient
from .retriever import BM25RetrieverImpl, FAISSVectorIndex, HybridRetriever
from .embedder import ModelLoader
from .reranker import CrossEncoderReranker
from .query_understanding import QueryUnderstandingService, QueryPlan
from .patient_formatter import PatientContextFormatter
from .context_builder import ContextBuilder
from .patient_context import PatientContextInjector
from .tokenizer import WhitespaceTokenizer
from .chunk_store import DictChunkStore

@dataclass
class MedicalPipelineResult:
    answer: str
    retrieved_chunks: List[Any]
    citation_ids: List[str]
    retrieval_time: float
    generation_time: float
    query_plan: Optional[QueryPlan] = None

class MedicalRAGPipeline:
    """
    Orchestrator kết nối RAGPipeline (Retrieval) với QueryUnderstandingService, PromptBuilder và LLM.
    """
    def __init__(self):
        # Initialize components
        self.tokenizer = WhitespaceTokenizer()
        self.chunk_store = DictChunkStore()
        # Ensure chunks.jsonl exists or it will fail here. For demo, it should be fine.
        try:
            self.chunk_store.load_from_jsonl(config.INDEX.chunks_jsonl_path)
        except Exception as e:
            logger.error(f"Lỗi khi load ChunkStore từ {config.INDEX.chunks_jsonl_path}: {e}")
            print(f"Warning: chunks.jsonl not found at {config.INDEX.chunks_jsonl_path}. Retrieval might be empty.")
            
        self.bm25_index = BM25RetrieverImpl(self.tokenizer)
        try:
            self.bm25_index.load()
        except Exception as e:
            print("Warning: BM25 index not found.")
            
        self.vector_index = FAISSVectorIndex()
        try:
            self.vector_index.load()
        except Exception as e:
            print("Warning: FAISS index not found.")
            
        self.embedder = ModelLoader()
        
        self.retriever = HybridRetriever(self.bm25_index, self.vector_index, self.embedder)
        from .reranker import CrossEncoderReranker, CrossEncoderModelLoaderImpl
        
        try:
            reranker_model = CrossEncoderModelLoaderImpl()
        except Exception as e:
            print(f"Warning: Failed to load CrossEncoder model. Reranking will fallback to RRF. Error: {e}")
            reranker_model = None
            
        self.reranker = CrossEncoderReranker(model=reranker_model)
        self.context_builder = ContextBuilder(self.chunk_store, self.tokenizer)
        self.patient_injector = PatientContextInjector()
        
        self.rag_pipeline = RAGPipeline(
            retriever=self.retriever,
            reranker=self.reranker,
            context_builder=self.context_builder,
            patient_injector=self.patient_injector
        )
        
        self.prompt_builder = PromptBuilder(self.tokenizer)
        import os
        self.llm_client = GroqLLMClient(api_key=os.environ.get("GROQ_API_KEY"), model_name="llama-3.3-70b-versatile")
        self.translation_llm = GroqLLMClient(api_key=os.environ.get("GROQ_TRANSLATION_API_KEY") or os.environ.get("GROQ_API_KEY"), model_name="llama-3.1-8b-instant")
        self.query_understanding_service = QueryUnderstandingService(llm_client=self.translation_llm)

    def run(self, question: str, patient_context: Optional[Dict[str, Any]] = None, api_key: str = None) -> MedicalPipelineResult:
        t0 = time.time()
        
        # 0. Query Understanding & Query Planning Phase
        query_plan: QueryPlan = self.query_understanding_service.analyze(
            question=question,
            patient_context=patient_context,
            api_key=api_key
        )
        print(f"[*] Query Understanding Intent: '{query_plan.intent}', Search Queries: {query_plan.search_queries}")

        # Routing logic based on query intent
        if query_plan.intent == "greeting":
            return MedicalPipelineResult(
                answer="Xin chào! Tôi là Trợ lý Y khoa AI. Tôi có thể hỗ trợ gì cho bạn về các hướng dẫn điều trị hoặc chỉ số sức khỏe?",
                retrieved_chunks=[],
                citation_ids=[],
                retrieval_time=time.time() - t0,
                generation_time=0.0,
                query_plan=query_plan
            )
        elif query_plan.intent == "off_topic" or not query_plan.is_medical:
            return MedicalPipelineResult(
                answer="Xin lỗi, tôi là Trợ lý Y khoa CDSS chuyên tư vấn các câu hỏi về y tế và quản lý bệnh lý (như Đái tháo đường, Tăng huyết áp). Tôi không thể hỗ trợ các câu hỏi ngoài lề này.",
                retrieved_chunks=[],
                citation_ids=[],
                retrieval_time=time.time() - t0,
                generation_time=0.0,
                query_plan=query_plan
            )

        # 1. Retrieval Phase using search_queries planned by LLM
        if query_plan.need_retrieval and query_plan.search_queries:
            search_query = " ".join(query_plan.search_queries)
        else:
            search_query = question

        pipeline_result = self.rag_pipeline.run(
            query=search_query,
            node_lookup=self.chunk_store._store,
            patient=None
        )
        retrieval_time = time.time() - t0
        
        # 2. Prompt Generation Phase
        t1 = time.time()
        prompt_result = self.prompt_builder.build(
            user_question=question,
            expanded_context=pipeline_result.expanded_context,
            patient_data=patient_context or {}
        )
        
        logger.info(f"Generated Prompt:\n{prompt_result.prompt_text[:500]}...\n[Prompt Patient Block]: {PatientContextFormatter.format(patient_context or {})}")
        
        # 3. LLM Generation Phase
        answer = self.llm_client.generate(prompt_result.prompt_text, api_key=api_key)
        generation_time = time.time() - t1
        
        citation_ids = [chunk.node_id for chunk in prompt_result.citation_map.values()]
        
        return MedicalPipelineResult(
            answer=answer,
            retrieved_chunks=pipeline_result.expanded_context.chunks,
            citation_ids=citation_ids,
            retrieval_time=retrieval_time,
            generation_time=generation_time,
            query_plan=query_plan
        )

