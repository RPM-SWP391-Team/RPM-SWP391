import argparse
import sys
import time
from pathlib import Path

# Add root directory to python path
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from evaluation.dataset_loader import DatasetLoader
from evaluation.metrics import MetricCalculator
from evaluation.report_generator import ReportGenerator

from src.medical_pipeline import MedicalRAGPipeline

def parse_args():
    parser = argparse.ArgumentParser(description="Medical RAG Evaluation Benchmark")
    parser.add_argument("--dataset", type=str, required=True, help="Path to evaluation dataset (.csv, .json, .jsonl)")
    parser.add_argument("--top-k", type=int, default=5, help="Top-K for retrieval metrics")
    return parser.parse_args()

def main():
    args = parse_args()
    
    loader = DatasetLoader(args.dataset)
    samples = list(loader.load())
    total_questions = len(samples)
    
    if total_questions == 0:
        print("Dataset is empty. Exiting.")
        return

    calculator = MetricCalculator()
    reporter = ReportGenerator()
    pipeline = MedicalRAGPipeline()
    
    # Mock LLM Client to bypass Gemini API deprecation/404 errors 
    # since we only care about evaluating Retrieval Metrics right now.
    class MockLLM:
        def generate(self, prompt: str) -> str:
            return "Mock LLM Response for Benchmark."
    pipeline.llm_client = MockLLM()
    
    results = []
    
    print(f"Starting Evaluation on {args.dataset} ({total_questions} questions) with top-k={args.top_k}")
    
    running_latency = 0.0
    
    for idx, sample in enumerate(samples, 1):
        start_time = time.time()
        
        # 1. Ask pipeline
        try:
            response = pipeline.run(question=sample.question, patient_context=None)
        except Exception as e:
            print(f"Error on question {idx}: {e}")
            continue
            
        latency = time.time() - start_time
        running_latency += latency
        avg_latency = running_latency / idx
        
        prediction = response.answer
        retrieved_ids = [c.node_id for c in response.retrieved_chunks]
        retrieval_time = response.retrieval_time
        generation_time = response.generation_time
        citation_ids = response.citation_ids


        ground_truth_ids = sample.metadata.get("ground_truth_ids", [])
        
        # 2. Compute Metrics
        retrieval_metrics = calculator.compute_retrieval_metrics(
            retrieved_ids=retrieved_ids, 
            ground_truth_ids=ground_truth_ids, 
            k_values=[1, 3, args.top_k, 10]
        )
        
        generation_metrics = calculator.compute_generation_metrics(
            prediction=prediction, 
            reference=sample.reference_answer
        )
        
        all_metrics = {**retrieval_metrics, **generation_metrics}
        status = calculator.determine_status(all_metrics)
        
        # 3. Store result
        results.append({
            "question_id": sample.question_id,
            "question": sample.question,
            "reference": sample.reference_answer,
            "prediction": prediction,
            "metrics": all_metrics,
            "retrieval_time": retrieval_time,
            "generation_time": generation_time,
            "status": status,
            "retrieved_chunks": retrieved_ids,
            "citation_ids": citation_ids
        })
        
        # 4. Print Progress
        print(f"[{idx}/{total_questions}] Current Question | Latency: {latency:.2f}s | Running Avg: {avg_latency:.2f}s")

    # 5. Generate Reports
    print("\nEvaluation Completed. Generating reports...")
    reporter.generate_errors_report(results)
    reporter.generate_summary_report(results)
    print("Report Saved to reports/ folder.")

if __name__ == "__main__":
    main()
