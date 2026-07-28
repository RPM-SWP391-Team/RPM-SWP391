import time
import logging
import re
from typing import List, Dict, Any

logger = logging.getLogger(__name__)

PDF_TITLE_MAP = {
    "dc26s001.pdf": "ADA 2026: Cải thiện Chăm sóc & Sức khỏe Cộng đồng (Sec. 1)",
    "dc26s002.pdf": "ADA 2026: Chẩn đoán & Phân loại Đái tháo đường (Sec. 2)",
    "dc26s003.pdf": "ADA 2026: Dự phòng & Trì hoãn Đái tháo đường Týp 2 (Sec. 3)",
    "dc24s004.pdf": "ADA 2024: Đánh giá Y khoa Toàn diện & Bệnh kèm theo (Sec. 4)",
    "dc26s005.pdf": "ADA 2026: Hành vi Sức khỏe, Dinh dưỡng & Tập luyện (Sec. 5)",
    "dc26s006.pdf": "ADA 2026: Mục tiêu Kiểm soát Đường huyết & Hạ đường huyết (Sec. 6)",
    "dc26s007.pdf": "ADA 2026: Công nghệ Y tế & Đo đường huyết liên tục CGM (Sec. 7)",
    "dc26s008.pdf": "ADA 2026: Quản lý Béo phì & Cân nặng bệnh nhân T2D (Sec. 8)",
    "dc26s009.pdf": "ADA 2026: Phác đồ Thuốc hạ Đường huyết Metformin & Insulin (Sec. 9)",
    "dc26s010.pdf": "ADA 2026: Bệnh Tim mạch & Quản lý Nguy cơ Tim mạch (Sec. 10)",
    "dc26s011.pdf": "ADA 2026: Bệnh Thận Mạn tính ở Bệnh nhân Đái tháo đường (Sec. 11)",
    "dc26s012.pdf": "ADA 2026: Biến chứng Vọng mạc, Thần kinh & Bàn chân (Sec. 12)",
    "dc26s013.pdf": "ADA 2026: Quản lý Đái tháo đường ở Người cao tuổi (Sec. 13)",
    "dc26s014.pdf": "ADA 2026: Đái tháo đường ở Trẻ em & Thanh thiếu niên (Sec. 14)",
    "dc26s015.pdf": "ADA 2026: Quản lý Đái tháo đường ở Phụ nữ Mang thai (Sec. 15)",
    "dc26s016.pdf": "ADA 2026: Chăm sóc Đái tháo đường Nội trú tại Bệnh viện (Sec. 16)",
    "dc26s017.pdf": "ADA 2026: Tiêu chuẩn Chăm sóc & Quyền lợi Bệnh nhân (Sec. 17)",
    "dc26sint.pdf": "ADA 2026: Giới thiệu & Tổng quan Hướng dẫn Lâm sàng",
    "dc26srev.pdf": "ADA 2026: Tóm tắt Các Điểm Cập nhật Mới",
    "ehae178.pdf": "ESC 2023: Hướng dẫn Quản lý Bệnh Tim mạch ở Bệnh nhân Đái tháo đường"
}

def clean_corrupted_pdf_text(text: str) -> str:
    """
    Sửa các ký tự font PDF bị lỗi mã hóa font Type3/Ligature thành văn bản chuẩn dễ đọc.
    """
    if not text:
        return ""
    
    replacements = {
        'ø': 'o',
        'ï': 'i',
        'æ': 'ae',
        'ñ': 'd',
        'ö': 'o',
        'á': 'a',
        'ó': 'o',
        'ú': 'u',
        'é': 'e',
        'í': 'i',
        'ý': 'y',
        '=t': ' t',
        '=r': ' r',
        '=a': ' a',
        '=o': ' o',
        '=i': ' i',
        '=e': ' e',
        'Nlowchørt': 'Flowchart',
        'diøñetes': 'diabetes',
        'ødults': 'adults',
        'öhite': 'white',
        'uropeøn': 'european',
        'populøtions': 'populations'
    }
    for old, new in replacements.items():
        text = text.replace(old, new)
    
    text = re.sub(r'={2,}', ' ', text)
    text = re.sub(r'\s{2,}', ' ', text)
    return text.strip()

def is_medical_query(query: str, engine: Any = None, api_key: str = None) -> bool:
    """
    Deprecated keyword checker kept for backward compatibility.
    Now delegates to QueryUnderstandingService if engine is provided.
    """
    if engine and hasattr(engine, "query_understanding_service"):
        plan = engine.query_understanding_service.analyze(query, api_key=api_key)
        return plan.is_medical and plan.need_retrieval
    
    q = query.strip().lower()
    return len(q) >= 3 and not any(g in q for g in ["chào", "hi", "hello", "thời tiết"])

def execute_semantic_search(query: str, engine: Any, api_key: str = None) -> Dict[str, Any]:
    """
    Thực hiện Semantic Search dựa trên động cơ MedicalRAGPipeline sử dụng QueryUnderstandingService.
    Nếu từ khóa không liên quan đến y khoa hoặc không cần retrieval, trả về 0 kết quả.
    """
    t0 = time.time()
    
    # 1. Query Understanding & Query Planning Phase
    if hasattr(engine, "query_understanding_service"):
        query_plan = engine.query_understanding_service.analyze(question=query, api_key=api_key)
    else:
        from src.query_understanding import QueryUnderstandingService
        service = QueryUnderstandingService(llm_client=getattr(engine, "translation_llm", None))
        query_plan = service.analyze(question=query, api_key=api_key)
        
    if not query_plan.is_medical or not query_plan.need_retrieval:
        logger.info(f"[*] Query '{query}' identified as non-medical/no-retrieval (Intent: {query_plan.intent}). Returning 0 results.")
        return {
            "results": [],
            "total_found": 0,
            "latency_seconds": round(time.time() - t0, 3)
        }

    search_query = " ".join(query_plan.search_queries) if query_plan.search_queries else query
    logger.info(f"[*] Planned search query for retrieval: '{search_query}'")

    # 2. Retrieval Phase
    pipeline_result = engine.rag_pipeline.run(
        query=search_query,
        node_lookup=engine.chunk_store._store,
        patient=None
    )
    
    # 3. Định dạng kết quả (JSON)
    results = []
    if pipeline_result and pipeline_result.expanded_context:
        for chunk in pipeline_result.expanded_context.chunks:
            meta = getattr(chunk, "metadata", None)
            if not meta and hasattr(chunk, "node"):
                meta = getattr(chunk.node, "metadata", {})
            if not meta:
                meta = {}
            
            # Làm sạch font chữ lỗi mã hóa PDF
            cleaned_text = clean_corrupted_pdf_text(chunk.text)
            
            # Trích xuất Tiêu đề Đường dẫn (Path) từ title_path hoặc Header trong text
            path_str = ""
            if hasattr(chunk, "title_path") and chunk.title_path and len(chunk.title_path) > 0:
                clean_titles = [str(t).strip() for t in chunk.title_path if str(t).strip()]
                path_str = " > ".join(clean_titles)
            
            if not path_str or path_str == "Nội dung chung":
                text_lines = [l.strip() for l in cleaned_text.split('\n') if l.strip()]
                headers = [clean_corrupted_pdf_text(l.replace('#', '').strip()) for l in text_lines if l.startswith('#')]
                path_str = " > ".join(headers[:2]) if headers else "Hướng dẫn Điều trị Lâm sàng"

            # Trích xuất Nguồn Tài liệu (Source Document Name & PDF File Name)
            raw_file = meta.get("source_file", meta.get("file_name", meta.get("document_name", meta.get("source_document", meta.get("file", "")))))
            raw_file_str = str(raw_file).strip() if raw_file else ""

            if not raw_file_str or "unknown" in raw_file_str.lower() or raw_file_str.lower() in ["none", "null"]:
                text_lower = cleaned_text.lower()
                if any(term in text_lower for term in ["recommendation 3.", "recommendations 3.", "prevention or delay", "prevent type 2"]):
                    pdf_name = "dc26s003.pdf"
                elif any(term in text_lower for term in ["recommendation 9.", "recommendations 9.", "metformin", "insulin", "pharmacologic"]):
                    pdf_name = "dc26s009.pdf"
                elif any(term in text_lower for term in ["recommendation 4.", "recommendations 4.", "comprehensive medical"]):
                    pdf_name = "dc24s004.pdf"
                elif any(term in text_lower for term in ["recommendation 10.", "recommendations 10.", "cardiovascular"]):
                    pdf_name = "dc26s010.pdf"
                elif any(term in text_lower for term in ["recommendation 2.", "recommendations 2.", "diagnosis"]):
                    pdf_name = "dc26s002.pdf"
                elif any(term in text_lower for term in ["esc", "hypertension", "huyết áp"]):
                    pdf_name = "ehae178.pdf"
                elif any(term in text_lower for term in ["diabetes", "glucose", "hba1c", "ada"]):
                    pdf_name = "dc26s002.pdf"
                else:
                    pdf_name = "ehae178.pdf"
            elif raw_file_str.endswith(".pdf"):
                pdf_name = raw_file_str
            elif len(raw_file_str) > 2:
                pdf_name = f"{raw_file_str}.pdf"
            else:
                pdf_name = "dc26s002.pdf"

            # Tra cứu tiêu đề tiếng Việt chuẩn của tài liệu PDF
            source_title = PDF_TITLE_MAP.get(pdf_name, f"Tài liệu Y khoa Chuyên ngành ({pdf_name})")

            results.append({
                "source": source_title,
                "path": path_str,
                "text": cleaned_text,
                "citation": getattr(chunk, "citation", "") or source_title,
                "pdf_file": pdf_name,
                "pdf_url": f"/docs/{pdf_name}"
            })
            
    latency = time.time() - t0
    
    return {
        "results": results,
        "total_found": len(results),
        "latency_seconds": round(latency, 3)
    }
