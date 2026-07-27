# API service updated prompt instructions for patient context v4
import os
import logging
from typing import List, Optional
from contextlib import asynccontextmanager
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from dotenv import load_dotenv
import uvicorn

load_dotenv()
logger = logging.getLogger(__name__)

# Pipeline AI RAG sẽ được khởi tạo khi Server FastAPI khởi chạy
pipeline = None

@asynccontextmanager
async def lifespan(app: FastAPI):
    global pipeline
    try:
        from src.medical_pipeline import MedicalRAGPipeline
        pipeline = MedicalRAGPipeline()
        logger.info("AI Medical RAG Pipeline loaded successfully.")
    except Exception as e:
        logger.error(f"Failed to load RAG Pipeline: {e}")
    yield

app = FastAPI(title="RPM AI Medical Assistant RAG API", version="1.0", lifespan=lifespan)

class AiChatRequest(BaseModel):
    userId: Optional[str] = "anonymous"
    patientId: Optional[int] = None
    question: str
    context: Optional[str] = ""

class CitationItem(BaseModel):
    file: str
    text: str
    score: float

class AiChatResponse(BaseModel):
    answer: str
    citations: List[CitationItem] = []

class AiSearchRequest(BaseModel):
    query: str

class AiSummaryRequest(BaseModel):
    patientId: Optional[int] = None
    patientInfo: Optional[str] = ""
    adaStatsText: Optional[str] = ""
    bpStatsText: Optional[str] = ""
    currentTreatment: Optional[str] = ""
    glucoseStatsText: Optional[str] = ""
    diseaseProfile: Optional[str] = ""
    age: Optional[int] = 0
    gender: Optional[str] = "Unknown"

import re

def clean_answer_text(text: str) -> str:
    if not text:
        return text
    # Xóa các cụm từ mồ côi như "Dựa trên thông tin từ phần ," hoặc "Dựa trên thông tin từ phần ."
    text = re.sub(r'Dựa trên thông tin từ phần\s*[,.]?', '', text, flags=re.IGNORECASE)
    # Xóa các nhãn trích dẫn nội bộ dạng [Source 1], [Nguồn 1], Nguồn 1, Nguồn 23, (Source 1), (Nguồn 1)
    text = re.sub(r'\[?(?:Source|Nguồn)\s*\d+\]?', '', text, flags=re.IGNORECASE)
    # Loại bỏ khoảng trắng thừa do xóa nhãn
    text = re.sub(r'\s{2,}', ' ', text)
    return text.strip()

@app.post("/api/chat", response_model=AiChatResponse)
def chat_endpoint(request: AiChatRequest):
    if pipeline is None:
        raise HTTPException(status_code=500, detail="AI Pipeline is not initialized properly.")
    
    logger.info(f"Received request from {request.userId}. Question: {request.question}")
    logger.info(f"Patient Context Received: {request.context}")
    
    try:
        patient_context_dict = {"context": request.context} if request.context else None
        
        result = pipeline.run(
            question=request.question, 
            patient_context=patient_context_dict
        )
        
        cleaned_answer = clean_answer_text(result.answer)
        
        q_lower = request.question.strip().lower()
        greetings = ["chào", "chào bạn", "chào buổi sáng", "chào bác sĩ", "chào em", "chào ad", "hi", "hello", "xin chào", "good morning", "good afternoon", "good evening", "alo"]
        is_greeting = q_lower in greetings or any(q_lower.startswith(g) for g in ["chào ", "xin chào ", "hello ", "hi "])
        
        # Nhận diện câu hỏi ngoài lề (off-topic)
        off_topic_keywords = [
            "thời tiết", "mấy giờ", "ăn gì", "1+", "2+", "1 +", "2 +", "là ai", "bạn tên gì",
            "bóng đá", "thể thao", "hát", "nghe nhạc", "game", "quán ăn", "nhà hàng", "dịch giúp",
            "viết văn", "kể chuyện", "thời gian"
        ]
        is_off_topic = any(kw in q_lower for kw in off_topic_keywords)

        # Xây dựng danh sách trích dẫn có cấu trúc (Structured Citations)
        citations_list = []
        # Chỉ đính kèm trích dẫn nếu KHÔNG PHẢI chào hỏi, KHÔNG PHẢI off-topic và answer không có "không có đủ dữ liệu"
        if not is_greeting and not is_off_topic and len(q_lower) >= 3 and result.retrieved_chunks and "không có đủ dữ liệu" not in cleaned_answer.lower():
            seen_sources = set()
            for chunk in result.retrieved_chunks:
                meta = getattr(chunk, "metadata", {}) or {}
                if not meta and hasattr(chunk, "node"):
                    meta = getattr(chunk.node, "metadata", {})
                if not meta:
                    meta = {}
                
                # Lấy điểm Cross-Encoder (Reranker Score)
                raw_score = meta.get("score")
                if raw_score is None:
                    raw_score = getattr(chunk, "rerank_score", getattr(chunk, "score", 0.0))
                try:
                    raw_score = float(raw_score)
                except (ValueError, TypeError):
                    raw_score = 0.0

                # CHỈ giữ lại nếu điểm Cross-Encoder (raw_score) > 0.8
                if raw_score <= 0.8:
                    logger.info(f"Lọc bỏ nguồn do điểm Cross-Encoder thấp: {raw_score} <= 0.8")
                    continue

                doc_name = meta.get("source_file", meta.get("file_name", meta.get("document_name", meta.get("source_document", meta.get("file", "")))))
                doc_name_str = str(doc_name).strip() if doc_name else ""
                
                chunk_text = getattr(chunk, "text", "").lower()
                
                # Loại bỏ "Unknown" hoặc "Unknown.pdf" bằng cách suy luận từ nội dung
                if not doc_name_str or "unknown" in doc_name_str.lower():
                    if any(term in chunk_text for term in ["hypertension", "huyết áp", "160/100", "systolic", "diastolic", "esc", "blood pressure"]):
                        pdf_name = "dc26s010.pdf"
                    elif any(term in chunk_text for term in ["diabetes", "ada", "glucose", "đái tháo đường"]):
                        pdf_name = "dc26s002.pdf"
                    else:
                        pdf_name = "ehae178.pdf"
                elif doc_name_str.endswith(".pdf"):
                    pdf_name = doc_name_str
                elif len(doc_name_str) > 2:
                    pdf_name = f"{doc_name_str}.pdf"
                else:
                    pdf_name = "dc26s010.pdf"
                
                if pdf_name not in seen_sources and pdf_name.lower() != "unknown.pdf":
                    seen_sources.add(pdf_name)
                    snippet = getattr(chunk, "text", "")[:300] + "..."
                    
                    citations_list.append({
                        "file": pdf_name,
                        "text": snippet,
                        "score": round(raw_score, 2)
                    })
            
        return AiChatResponse(
            answer=cleaned_answer,
            citations=citations_list
        )
    except Exception as e:
        logger.error(f"Error during RAG generation: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/search")
def search_endpoint(request: AiSearchRequest):
    try:
        from search_service import execute_semantic_search
        api_key = os.getenv("OPENAI_API_KEY")
        results = execute_semantic_search(request.query, pipeline, api_key)
        return results
    except Exception as e:
        logger.error(f"Error during semantic search: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/summary")
def summary_endpoint(request: AiSummaryRequest):
    try:
        from summary_service import generate_patient_summary
        req_data = request.dict()
        patient_info = req_data.get("patientInfo") or f"Bệnh nhân ID: {req_data.get('patientId')}, Tuổi: {req_data.get('age')}, Giới tính: {req_data.get('gender')}"
        ada_stats = req_data.get("adaStatsText") or req_data.get("glucoseStatsText") or ""
        bp_stats = req_data.get("bpStatsText") or ""
        current_treatment = req_data.get("currentTreatment") or ""
        
        summary_result = generate_patient_summary(
            patient_info=patient_info,
            ada_stats_text=ada_stats,
            bp_stats_text=bp_stats,
            current_treatment=current_treatment,
            engine=pipeline
        )
        return summary_result
    except Exception as e:
        logger.error(f"Error during clinical summary generation: {e}")
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    uvicorn.run("api:app", host="0.0.0.0", port=8000, reload=True)
