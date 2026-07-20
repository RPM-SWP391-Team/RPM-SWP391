import sys
import json
import logging
from pathlib import Path

# Add root project path
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from benchmark.benchmark_retrieval import run_retrieval_benchmark
from benchmark.benchmark_generation import run_generation_benchmark

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def main():
    logger.info("=== BẮT ĐẦU CHẠY BENCHMARK PIPELINE ===")
    
    # 1. Run Benchmarks
    retrieval_report = run_retrieval_benchmark()
    generation_report = run_generation_benchmark()
    
    # 2. Đọc Config Thresholds
    eval_dir = Path(__file__).resolve().parents[1] / "evaluation"
    with open(eval_dir / "config.json", "r", encoding="utf-8") as f:
        thresholds = json.load(f)["thresholds"]
        
    # 3. Đánh giá (Validation)
    passed = True
    fail_reasons = []
    
    if retrieval_report["mean_recall_at_5"] < thresholds["recall_at_5"]:
        passed = False
        fail_reasons.append(f"Recall@5 ({retrieval_report['mean_recall_at_5']}) < Ngưỡng ({thresholds['recall_at_5']})")
        
    if retrieval_report["mean_mrr"] < thresholds["mrr"]:
        passed = False
        fail_reasons.append(f"MRR ({retrieval_report['mean_mrr']}) < Ngưỡng ({thresholds['mrr']})")
        
    if generation_report["mean_latency_ms"] > thresholds["average_latency_ms"]:
        passed = False
        fail_reasons.append(f"Latency ({generation_report['mean_latency_ms']}) > Ngưỡng ({thresholds['average_latency_ms']})")
        
    if generation_report["mean_citation_coverage"] < thresholds["citation_coverage"]:
        passed = False
        fail_reasons.append(f"Citation Coverage ({generation_report['mean_citation_coverage']}) < Ngưỡng ({thresholds['citation_coverage']})")
        
    # 4. Tạo Markdown Report
    reports_dir = Path(__file__).resolve().parents[1] / "reports"
    
    md_content = f"""# Báo Cáo Đánh Giá & Hồi Quy (Evaluation Report)

**KẾT QUẢ CUỐI CÙNG**: {'✅ **PASS**' if passed else '❌ **FAIL**'}

## 1. Retrieval Metrics
- **Mean Recall@1**: {retrieval_report['mean_recall_at_1']:.2f}
- **Mean Recall@5**: {retrieval_report['mean_recall_at_5']:.2f} (Ngưỡng: {thresholds['recall_at_5']})
- **Mean MRR**: {retrieval_report['mean_mrr']:.2f} (Ngưỡng: {thresholds['mrr']})
- **Average Latency (ms)**: {retrieval_report['average_latency_ms']:.2f}

## 2. Generation Metrics
- **Mean Latency (ms)**: {generation_report['mean_latency_ms']:.2f} (Ngưỡng: {thresholds['average_latency_ms']})
- **Mean Citation Coverage**: {generation_report['mean_citation_coverage']*100:.2f}% (Ngưỡng: {thresholds['citation_coverage']*100}%)
- **Mean Keyword Hit Rate**: {generation_report['mean_keyword_hit_rate']*100:.2f}%
- **Prompt Tokens**: {generation_report['mean_prompt_tokens']:.1f}
- **Completion Tokens**: {generation_report['mean_completion_tokens']:.1f}

## 3. Phân Tích Lỗi (Error Analysis)
"""
    if not passed:
        md_content += "Hệ thống đã **Thất Bại** (Regression) ở các tiêu chí sau:\n"
        for r in fail_reasons:
            md_content += f"- {r}\n"
    else:
        md_content += "Hệ thống đáp ứng toàn bộ các tiêu chuẩn Regression.\n"
        
    with open(reports_dir / "evaluation_report.md", "w", encoding="utf-8") as f:
        f.write(md_content)
        
    logger.info(f"Đã xuất báo cáo tại {reports_dir}/evaluation_report.md")
    
    if not passed:
        logger.error("PIPELINE FAIL: Không đạt ngưỡng chấp nhận (Thresholds). Xem evaluation_report.md")
        sys.exit(1)
    else:
        logger.info("PIPELINE PASS: Tất cả các tiêu chuẩn được đáp ứng.")
        sys.exit(0)

if __name__ == "__main__":
    main()
