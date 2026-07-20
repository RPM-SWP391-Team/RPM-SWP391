import json
import os
from pathlib import Path
from typing import List, Dict, Any
from datetime import datetime

class ReportGenerator:
    def __init__(self, output_dir: str = "reports"):
        self.output_dir = Path(output_dir)
        self.output_dir.mkdir(parents=True, exist_ok=True)

    def generate_errors_report(self, results: List[Dict[str, Any]]):
        errors_path = self.output_dir / "errors.json"
        
        # Lọc ra các câu có status là Wrong hoặc Partial nếu muốn, 
        # hoặc lưu tất cả và sắp xếp theo status
        error_records = []
        for r in results:
            error_records.append({
                "question": r["question"],
                "reference": r["reference"],
                "prediction": r["prediction"],
                "bleu": r["metrics"].get("bleu", 0.0),
                "rouge": r["metrics"].get("rouge_l", 0.0),
                "generation_time": r.get("generation_time", 0.0),
                "retrieval_time": r.get("retrieval_time", 0.0),
                "status": r["status"],
                "retrieved_chunks": r.get("retrieved_chunks", []),
                "citation_ids": r.get("citation_ids", [])
            })
            
        with open(errors_path, "w", encoding="utf-8") as f:
            json.dump(error_records, f, ensure_ascii=False, indent=4)

    def generate_summary_report(self, results: List[Dict[str, Any]]):
        if not results:
            return
            
        # Tính Aggregate Metrics
        total = len(results)
        
        avg_metrics = {}
        metric_keys = results[0]["metrics"].keys()
        for k in metric_keys:
            avg_metrics[k] = sum(r["metrics"].get(k, 0.0) for r in results) / total
            
        avg_gen_time = sum(r.get("generation_time", 0.0) for r in results) / total
        avg_ret_time = sum(r.get("retrieval_time", 0.0) for r in results) / total
        avg_latency = avg_gen_time + avg_ret_time
        
        # Sort results for Top 10 correct/wrong based on ROUGE (or other metric)
        sorted_results = sorted(results, key=lambda x: x["metrics"].get("rouge_l", 0.0), reverse=True)
        top_10_correct = sorted_results[:10]
        top_10_wrong = sorted_results[-10:] if len(sorted_results) >= 10 else sorted_results[::-1]

        # 1. Write JSON Report (Regression/Latest)
        report_data = {
            "timestamp": datetime.now().isoformat(),
            "total_questions": total,
            "average_metrics": avg_metrics,
            "average_latency": avg_latency,
            "average_retrieval_time": avg_ret_time,
            "average_generation_time": avg_gen_time,
            "top_10_correct": [r["question"] for r in top_10_correct],
            "top_10_wrong": [r["question"] for r in top_10_wrong]
        }
        
        with open(self.output_dir / "latest.json", "w", encoding="utf-8") as f:
            json.dump(report_data, f, ensure_ascii=False, indent=4)
            
        # Tự động tạo benchmark_vX.json
        v_idx = 1
        while (self.output_dir / f"benchmark_v{v_idx}.json").exists():
            v_idx += 1
        with open(self.output_dir / f"benchmark_v{v_idx}.json", "w", encoding="utf-8") as f:
            json.dump(report_data, f, ensure_ascii=False, indent=4)

        # 2. Write Markdown Report
        md_content = f"# Evaluation Summary Report\n\n"
        md_content += f"**Total Questions:** {total}\n\n"
        md_content += "## Average Metrics\n"
        for k, v in avg_metrics.items():
            md_content += f"- **{k.capitalize()}**: {v:.4f}\n"
            
        md_content += f"- **Avg Retrieval Time**: {avg_ret_time:.4f}s\n"
        md_content += f"- **Avg Generation Time**: {avg_gen_time:.4f}s\n"
        md_content += f"- **Avg Latency**: {avg_latency:.4f}s\n\n"
        
        md_content += "## Top 10 Correct\n"
        for r in top_10_correct:
            md_content += f"1. {r['question']} (ROUGE: {r['metrics'].get('rouge_l', 0):.2f})\n"
            
        md_content += "\n## Top 10 Wrong\n"
        for r in top_10_wrong:
            md_content += f"1. {r['question']} (ROUGE: {r['metrics'].get('rouge_l', 0):.2f})\n"
            
        with open(self.output_dir / "evaluation_report.md", "w", encoding="utf-8") as f:
            f.write(md_content)
