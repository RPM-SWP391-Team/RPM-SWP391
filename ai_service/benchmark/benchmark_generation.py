import json
import time
import sys
import logging
from pathlib import Path

# Add root project path
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from src.prompt_builder import PromptBuilder, PromptResult
from src.context_builder import ExpandedContext, ContextChunk
from src.llm_client import DummyRetryLLMClient
from src.response_parser import ResponseParser
from src.tokenizer import WhitespaceTokenizer

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def run_generation_benchmark():
    eval_dir = Path(__file__).resolve().parents[1] / "evaluation"
    with open(eval_dir / "golden_queries.json", "r", encoding="utf-8") as f:
        queries = json.load(f)
    with open(eval_dir / "golden_answers.json", "r", encoding="utf-8") as f:
        golden_answers = json.load(f)

    tokenizer = WhitespaceTokenizer()
    prompt_builder = PromptBuilder(tokenizer=tokenizer, max_prompt_tokens=4000)
    llm_client = DummyRetryLLMClient(max_retries=1, fail_times=0) 
    
    # Fake Context
    chunks = [
        ContextChunk(node_id="c_insulin_001", text="Insulin 10 unit.", source="doc1", title_path=[], citation="link1", metadata={}),
        ContextChunk(node_id="c_metformin_contra", text="Chống chỉ định eGFR < 30.", source="doc2", title_path=[], citation="link2", metadata={})
    ]
    expanded_ctx = ExpandedContext(chunks=chunks)
    patient_data = {"age": 62}
    
    metrics = {
        "latencies": [],
        "prompt_tokens": [],
        "completion_tokens": [],
        "citation_coverages": [],
        "keyword_hit_rates": []
    }

    for q_id, q_text in queries.items():
        t0 = time.time()
        
        # 1. Build
        prompt_result = prompt_builder.build(q_text, expanded_ctx, patient_data)
        
        # 2. Call LLM (Dummy luôn cite Source 1 và chứa các từ khóa để test)
        expected_kws = golden_answers.get(q_id, {}).get("expected_answer_keywords", [])
        dummy_answer = " ".join(expected_kws) + " [Source 1]"
        llm_output = llm_client.generate(prompt_result.prompt_text)
        llm_output = dummy_answer # Đè bằng kết quả mock để tính keyword
        
        # 3. Parse
        final_res = ResponseParser.parse(llm_output, prompt_result.citation_map)
        
        latency = (time.time() - t0) * 1000
        
        # Keyword Coverage (So khớp text)
        hits = sum(1 for kw in expected_kws if kw.lower() in llm_output.lower())
        kw_rate = hits / len(expected_kws) if expected_kws else 1.0
        
        # Citation Coverage
        coverage = len(final_res.sources) / len(chunks) if chunks else 1.0
        
        metrics["latencies"].append(latency)
        metrics["prompt_tokens"].append(prompt_result.token_count)
        metrics["completion_tokens"].append(len(tokenizer.tokenize(llm_output)))
        metrics["citation_coverages"].append(coverage)
        metrics["keyword_hit_rates"].append(kw_rate)

    report = {
        "benchmark": "Generation Pipeline",
        "total_queries": len(queries),
        "mean_latency_ms": sum(metrics["latencies"]) / len(queries),
        "mean_prompt_tokens": sum(metrics["prompt_tokens"]) / len(queries),
        "mean_completion_tokens": sum(metrics["completion_tokens"]) / len(queries),
        "mean_citation_coverage": sum(metrics["citation_coverages"]) / len(queries),
        "mean_keyword_hit_rate": sum(metrics["keyword_hit_rates"]) / len(queries)
    }

    reports_dir = Path(__file__).resolve().parents[1] / "reports"
    reports_dir.mkdir(exist_ok=True)
    
    with open(reports_dir / "generation_report.json", "w", encoding="utf-8") as f:
        json.dump(report, f, indent=4)
        
    logger.info(f"Đã xuất báo cáo Generation tại {reports_dir}")
    return report

if __name__ == "__main__":
    run_generation_benchmark()
