import uvicorn
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import Optional, List
import logging

from src.medical_pipeline import MedicalRAGPipeline

app = FastAPI(title="Medical RAG AI Service", version="1.0")

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
        logger.info("Medical RAG Pipeline Initialized Successfully.")
    except Exception as e:
        logger.error(f"Failed to initialize pipeline: {e}")

class AiChatRequest(BaseModel):
    userId: str
    patientId: Optional[int] = None
    question: str
    context: str

class AiChatResponse(BaseModel):
    answer: str

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
        
        # Format the response similar to the Streamlit app format
        answer_with_citations = result.answer + "\n\n📚 Nguồn trích dẫn:\n"
        for i, chunk in enumerate(result.retrieved_chunks, 1):
            meta = getattr(chunk, "metadata", {}) or {}
            source = meta.get("source_document", meta.get("source_file", "Unknown"))
            answer_with_citations += f"[{i}] {source}\n"
            
        return AiChatResponse(answer=answer_with_citations)
    except Exception as e:
        logger.error(f"Error during RAG generation: {e}")
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    uvicorn.run("api:app", host="0.0.0.0", port=8000, reload=True)
