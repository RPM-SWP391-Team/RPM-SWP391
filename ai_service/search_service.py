import time
import logging
from typing import List, Dict, Any

logger = logging.getLogger(__name__)

def execute_semantic_search(query: str, engine: Any, api_key: str = None) -> Dict[str, Any]:
    """
    Thực hiện Semantic Search dựa trên động cơ MedicalRAGPipeline có sẵn.
    Bỏ qua bước gọi LLM sinh text, chỉ trả về các đoạn văn bản (chunks) được tìm thấy.
    """
    t0 = time.time()
    
    # 1. Query Expansion & Translation cho Retrieval
    translation_prompt = (
        f"You are a medical search query optimizer. Translate the user's Vietnamese query to English and expand it with relevant medical synonyms for a vector search engine.\n\n"
        f"CRITICAL RULES:\n"
        f"- DO NOT answer the question.\n"
        f"- DO NOT provide medical knowledge or advice.\n"
        f"- Output AT MOST 8 distinct keywords/phrases, comma-separated, on a single line.\n"
        f"- Do NOT repeat any phrase or synonym you have already used.\n"
        f"- Output ONLY the keyword list, nothing else — no preamble, no explanation, no numbering.\n\n"
        f"Query: {query}"
    )
    
    try:
        search_query = engine.translation_llm.generate(
            prompt=translation_prompt, 
            api_key=api_key,
            max_tokens=100,
            temperature=0.3,
            frequency_penalty=0.5
        ).strip()
        logger.info(f"[*] Translated query for retrieval: '{search_query}'")
    except Exception as e:
        logger.warning(f"[*] Translation failed, using original query: {e}")
        search_query = query

    # Combined query for max recall across BM25 and Vector search
    combined_query = f"{query} {search_query}" if search_query != query else query

    # 2. Retrieval Phase
    pipeline_result = engine.rag_pipeline.run(
        query=combined_query,
        node_lookup=engine.chunk_store._store,
        patient=None
    )
    
    # 3. Định dạng kết quả (JSON)
    results = []
    if pipeline_result and pipeline_result.expanded_context:
        for chunk in pipeline_result.expanded_context.chunks:
            meta = getattr(chunk, "metadata", {}) or {}
            
            # Trích xuất Tiêu đề Đường dẫn (Path) từ title_path hoặc Header trong text
            path_str = ""
            if hasattr(chunk, "title_path") and chunk.title_path and len(chunk.title_path) > 0:
                clean_titles = [str(t).strip() for t in chunk.title_path if str(t).strip()]
                path_str = " > ".join(clean_titles)
            
            if not path_str or path_str == "Nội dung chung":
                text_lines = [l.strip() for l in chunk.text.split('\n') if l.strip()]
                headers = [l.replace('#', '').strip() for l in text_lines if l.startswith('#')]
                path_str = " > ".join(headers[:2]) if headers else "Hướng dẫn Điều trị Lâm sàng"

            # Trích xuất Nguồn Tài liệu (Source Document Name)
            source_file = meta.get("source_file", meta.get("document_name", meta.get("source_document", "")))
            if not source_file or source_file == "Unknown":
                if "Diabetes Care" in chunk.text or "Standards of Care" in chunk.text or "ADA" in chunk.text:
                    source_file = "ADA Standards of Care in Diabetes (2024)"
                elif "ESC" in chunk.text or "European Society" in chunk.text:
                    source_file = "2024 ESC Guidelines (Elevated BP & Diabetes)"
                else:
                    source_file = "Tài liệu Y khoa Chuyên ngành (ESC / ADA 2024)"
            
            pdf_name = meta.get("source_file", "ehae178.pdf")
            if not str(pdf_name).endswith(".pdf"):
                pdf_name = "ehae178.pdf"

            results.append({
                "source": source_file,
                "path": path_str,
                "text": chunk.text,
                "citation": getattr(chunk, "citation", "") or source_file,
                "pdf_url": f"/docs/{pdf_name}"
            })
            
    latency = time.time() - t0
    
    return {
        "results": results,
        "total_found": len(results),
        "latency_seconds": round(latency, 3)
    }
