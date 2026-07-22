import json
from dataclasses import dataclass
from typing import Optional, List, Dict, Any
import time

from .pipeline import RAGPipeline, PipelineResult
from .prompt_builder import PromptBuilder, PromptResult
from .llm_client import GeminiLLMClient, GroqLLMClient
from .retriever import BM25RetrieverImpl, FAISSVectorIndex, HybridRetriever
from .embedder import ModelLoader
from .reranker import CrossEncoderReranker
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

class MedicalRAGPipeline:
    """
    Orchestrator kết nối RAGPipeline (Retrieval) với PromptBuilder và LLM.
    """
    def __init__(self):
        # Initialize components
        self.tokenizer = WhitespaceTokenizer()
        self.chunk_store = DictChunkStore()
        # Ensure chunks.jsonl exists or it will fail here. For demo, it should be fine.
        try:
            self.chunk_store.load_from_jsonl("data/chunks.jsonl")
        except Exception as e:
            print("Warning: chunks.jsonl not found or failed to load. Retrieval might be empty.")
            
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

    def run(self, question: str, patient_context: Optional[Dict[str, Any]] = None, api_key: str = None) -> MedicalPipelineResult:
        t0 = time.time()
        
        # 0. Query Expansion & Translation (Vietnamese -> English) for Retrieval
        translation_prompt = (
            f"You are a medical search query optimizer. Translate the user's Vietnamese query to English and expand it with relevant medical synonyms for a vector search engine.\n\n"
            f"CRITICAL RULES:\n"
            f"- DO NOT answer the question.\n"
            f"- DO NOT provide medical knowledge or advice.\n"
            f"- Output AT MOST 8 distinct keywords/phrases, comma-separated, on a single line.\n"
            f"- Do NOT repeat any phrase or synonym you have already used.\n"
            f"- Output ONLY the keyword list, nothing else — no preamble, no explanation, no numbering.\n\n"
            f"Query: {question}"
        )
        try:
            search_query = self.translation_llm.generate(
                prompt=translation_prompt, 
                api_key=api_key,
                max_tokens=100,
                temperature=0.3,
                frequency_penalty=0.5
            ).strip()
            print(f"[*] Translated query for retrieval: '{search_query}'")
        except Exception as e:
            print(f"[*] Translation failed, using original query: {e}")
            search_query = question

        # 1. Retrieval Phase
        pipeline_result = self.rag_pipeline.run(
            query=search_query,
            node_lookup=self.chunk_store._store,
            patient=None # Simplification for now, or adapt to PatientContext obj
        )
        retrieval_time = time.time() - t0
        
        # 2. Prompt Generation Phase
        t1 = time.time()
        prompt_result = self.prompt_builder.build(
            user_question=question,
            expanded_context=pipeline_result.expanded_context,
            patient_data=patient_context or {}
        )
        
        # 3. LLM Generation Phase
        answer = self.llm_client.generate(prompt_result.prompt_text, api_key=api_key)
        generation_time = time.time() - t1
        
        # Extract citation IDs for metrics
        citation_ids = [chunk.node_id for chunk in prompt_result.citation_map.values()]
        
        return MedicalPipelineResult(
            answer=answer,
            retrieved_chunks=pipeline_result.expanded_context.chunks,
            citation_ids=citation_ids,
            retrieval_time=retrieval_time,
            generation_time=generation_time
        )
