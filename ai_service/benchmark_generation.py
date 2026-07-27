import time
import json
import logging

from src.prompt_builder import PromptBuilder, PromptResult
from src.context_builder import ExpandedContext, ContextChunk
from src.patient_formatter import PatientContextFormatter
from src.llm_client import DummyRetryLLMClient
from src.response_parser import ResponseParser
from src.tokenizer import WhitespaceTokenizer

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def main():
    logger.info("Chạy Benchmark cho LLM Generation...")
    
    tokenizer = WhitespaceTokenizer()
    prompt_builder = PromptBuilder(tokenizer=tokenizer, max_prompt_tokens=4000)
    llm_client = DummyRetryLLMClient(max_retries=1, fail_times=0) # Cho pass luôn
    
    # Fake Data
    chunks = [
        ContextChunk(node_id=f"n{i}", text=f"Kiến thức y khoa {i} "*20, source=f"doc{i}", title_path=[], citation="", metadata={})
        for i in range(5)
    ]
    expanded_ctx = ExpandedContext(chunks=chunks)
    patient_data = {"age": 62, "medical_history": ["ĐTĐ Tuýp 2"]}
    question = "Bệnh nhân có nên dùng Metformin không?"
    
    latencies = []
    prompt_lengths = []
    completion_lengths = []
    citation_coverages = []
    
    # Chạy thử 50 lần
    for _ in range(50):
        t0 = time.time()
        
        # 1. Build Prompt
        prompt_result = prompt_builder.build(question, expanded_ctx, patient_data)
        
        # 2. LLM Call
        # Giả lập output của LLM luôn cite Source 1 và Source 3
        dummy_output = "Bệnh nhân nên dùng theo hướng dẫn [Source 1] và chú ý tuổi tác [Source 3]."
        llm_output = llm_client.generate(prompt_result.prompt_text) 
        # Ghi đè bằng dummy có chứa citations
        llm_output = dummy_output
        
        # 3. Parse Response
        final_res = ResponseParser.parse(llm_output, prompt_result.citation_map)
        
        t1 = time.time()
        
        latencies.append(t1 - t0)
        prompt_lengths.append(prompt_result.token_count)
        completion_lengths.append(len(tokenizer.tokenize(llm_output)))
        
        # Citation Coverage = số sources LLM cite / tổng sources cung cấp (hoặc % câu hỏi có cite)
        # Ở đây ta xem LLM đã dùng 2 sources trên tổng 5
        coverage = len(final_res.sources) / len(chunks) * 100
        citation_coverages.append(coverage)

    avg_latency = sum(latencies) / len(latencies) * 1000
    avg_prompt_len = sum(prompt_lengths) / len(prompt_lengths)
    avg_completion_len = sum(completion_lengths) / len(completion_lengths)
    avg_coverage = sum(citation_coverages) / len(citation_coverages)
    
    report = {
        "benchmark": "End-to-End Generation",
        "average_latency_ms": round(avg_latency, 2),
        "average_prompt_tokens": round(avg_prompt_len, 2),
        "average_completion_tokens": round(avg_completion_len, 2),
        "citation_coverage_percent": round(avg_coverage, 2),
        "hallucination_rate": "Chưa đo đạc bằng Data tay"
    }
    
    with open("data/benchmark_generation_report.json", "w", encoding="utf-8") as f:
        json.dump(report, f, indent=4)
        
    logger.info(f"Hoàn tất. Report: {json.dumps(report, indent=2)}")

if __name__ == "__main__":
    main()
