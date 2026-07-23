import uvicorn
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import Optional, List, Dict, Any
import logging
from fastapi.middleware.cors import CORSMiddleware

from src.medical_pipeline import MedicalRAGPipeline
from search_service import execute_semantic_search
import summary_service

app = FastAPI(title="Medical RAG AI Service", version="1.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Setup logger
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("ai_api")

# Initialize pipeline globally
pipeline = None

@app.on_event("startup")
def startup_event():
    global pipeline
    logger.info("Initializing Medical RAG Pipeline... (This may take a minute to load models)")
    try:
        pipeline = MedicalRAGPipeline()
        app.state.engine = pipeline
        logger.info("Medical RAG Pipeline Initialized Successfully.")
    except Exception as e:
        logger.error(f"Failed to initialize pipeline: {e}")

@app.get("/health")
@app.get("/api/health")
def health_check():
    status = "healthy" if pipeline is not None else "initializing"
    return {"status": status, "service": "Medical RAG AI Service", "version": "1.0"}

class AiChatRequest(BaseModel):
    userId: str
    patientId: Optional[int] = None
    question: str
    context: str

class AiChatResponse(BaseModel):
    answer: str
    citations: Optional[List[Any]] = None
    summaryTakeaway: Optional[str] = None
    disclaimer: Optional[str] = None
    confidenceLevel: Optional[str] = None

class SearchRequest(BaseModel):
    query: str

class SearchResponse(BaseModel):
    results: List[Dict[str, Any]]
    total_found: int
    latency_seconds: float

class SummaryRequest(BaseModel):
    patientInfo: Optional[str] = ""
    adaStatsText: Optional[str] = ""
    bpStatsText: Optional[str] = ""
    currentTreatment: Optional[str] = ""

@app.post("/api/chat", response_model=AiChatResponse)
def chat_endpoint(request: AiChatRequest):
    if pipeline is None:
        raise HTTPException(status_code=500, detail="AI Pipeline is not initialized properly.")
    
    logger.info(f"Received request from {request.userId}. Question: {request.question}")
    
    try:
        patient_context_dict = {"context": request.context} if request.context else None
        
        result = pipeline.run(
            question=request.question, 
            patient_context=patient_context_dict
        )
        
        # Xây dựng danh sách trích dẫn có cấu trúc (Structured Citations)
        citations_list = []
        if result.retrieved_chunks and "không có đủ dữ liệu" not in result.answer.lower():
            seen_sources = set()
            for chunk in result.retrieved_chunks:
                meta = getattr(chunk, "metadata", {}) or {}
                
                # Tên tài liệu trích dẫn
                doc_name = meta.get("source_file", meta.get("document_name", meta.get("source_document", "")))
                if not doc_name or doc_name == "Unknown":
                    chunk_text = getattr(chunk, "text", "")
                    if "Diabetes Care" in chunk_text or "ADA" in chunk_text:
                        doc_name = "ADA Standards of Care in Diabetes (2024)"
                    elif "ESC" in chunk_text:
                        doc_name = "2024 ESC Guidelines (Elevated BP & Diabetes)"
                    else:
                        doc_name = "Tài liệu Y khoa Chuyên ngành (ESC / ADA 2024)"
                
                if doc_name not in seen_sources:
                    seen_sources.add(doc_name)
                    chunk_text = getattr(chunk, "text", "")
                    snippet = chunk_text[:300] + "..." if len(chunk_text) > 300 else chunk_text
                    score = float(getattr(chunk, "score", 0.90))
                    
                    citations_list.append({
                        "file": doc_name,
                        "text": snippet,
                        "score": score
                    })
            
        return AiChatResponse(
            answer=result.answer,
            citations=citations_list
        )
    except Exception as e:
        logger.error(f"Error during RAG generation: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/search")
async def semantic_search(request: SearchRequest):
    """
    Endpoint riêng cho tính năng Tra cứu Phác đồ (Semantic Search).
    Chỉ thực hiện retrieve, không generate text.
    """
    logger.info(f"Received search request: {request.query}")
    engine = app.state.engine
    if engine is None:
        raise HTTPException(status_code=503, detail="AI Engine is not initialized")
    
    return execute_semantic_search(request.query, engine)

@app.post("/api/summary")
async def generate_summary(request: SummaryRequest):
    """
    Endpoint sinh báo cáo phân tích lâm sàng dựa trên số liệu ADA từ Java truyền sang.
    """
    logger.info("Received summary request")
    engine = app.state.engine
    if engine is None:
        raise HTTPException(status_code=503, detail="AI Engine is not initialized")
        
    return summary_service.generate_patient_summary(
        request.patientInfo or "",
        request.adaStatsText or "",
        request.bpStatsText or "",
        request.currentTreatment or "",
        engine
    )

if __name__ == "__main__":
    uvicorn.run("api:app", host="0.0.0.0", port=8000, reload=True)
