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

def is_medical_query(query: str) -> bool:
    """
    Kiểm tra từ khóa tra cứu có chứa ý định Y khoa / Lâm sàng hay không.
    """
    q = query.strip().lower()
    if len(q) < 3:
        return False
        
    non_medical_phrases = [
        "chào", "chào buổi sáng", "chào bác sĩ", "hi", "hello", "xin chào", 
        "good morning", "hôm nay thế nào", "bạn là ai", "thời tiết", "ăn gì", "chơi gì"
    ]
    if q in non_medical_phrases:
        return False
        
    # Danh sách từ khóa Y khoa chuyên môn phổ biến
    medical_keywords = [
        "đường huyết", "tiểu đường", "đái tháo đường", "huyết áp", "tim mạch", "thận", "mắt",
        "chế độ ăn", "tập luyện", "metformin", "insulin", "hba1c", "cgm", "ada", "esc", "bệnh",
        "triệu chứng", "thuốc", "liều", "chẩn đoán", "dự phòng", "biến chứng", "nội trú", "thai kỳ",
        "trẻ em", "người cao tuổi", "béo phì", "cân nặng", "glucose", "hypertension", "diabetes",
        "cardiovascular", "kidney", "nephropathy", "retinopathy", "neuropathy", "dose", "patient"
    ]
    for kw in medical_keywords:
        if kw in q:
            return True
            
    # Nếu từ khóa chứa từ tiếng Việt y khoa hoặc câu hỏi chuyên môn
    if re.search(r'\b(bệnh|thuốc|điều trị|liều|xử lý|khám|chỉ số|chẩn đoán)\b', q):
        return True
        
    return False

def execute_semantic_search(query: str, engine: Any, api_key: str = None) -> Dict[str, Any]:
    """
    Thực hiện Semantic Search dựa trên động cơ MedicalRAGPipeline có sẵn.
    Nếu từ khóa không liên quan đến y khoa, trả về không tìm thấy (total_found: 0).
    """
    t0 = time.time()
    
    # Kiểm tra từ khóa có liên quan đến Y khoa hay không
    if not is_medical_query(query):
        logger.info(f"[*] Query '{query}' identified as non-medical/irrelevant. Returning 0 results.")
        return {
            "results": [],
            "total_found": 0,
            "latency_seconds": round(time.time() - t0, 3)
        }
    
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

            if raw_file_str.endswith(".pdf"):
                pdf_name = raw_file_str
            elif raw_file_str and len(raw_file_str) > 2:
                pdf_name = f"{raw_file_str}.pdf"
            else:
                text_lower = cleaned_text.lower()
                if "diabetes" in text_lower or "glucose" in text_lower or "hba1c" in text_lower or "insulin" in text_lower or "ada" in text_lower:
                    pdf_name = "dc26s002.pdf"
                else:
                    pdf_name = "ehae178.pdf"

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
