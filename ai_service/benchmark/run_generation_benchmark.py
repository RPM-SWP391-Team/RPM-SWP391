import argparse
import sys
import time
import random
from pathlib import Path

# Add root directory to python path
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from evaluation.dataset_loader import DatasetLoader
from evaluation.metrics import MetricCalculator
from evaluation.report_generator import ReportGenerator
from src.medical_pipeline import MedicalRAGPipeline

def parse_args():
    parser = argparse.ArgumentParser(description="Real LLM Medical RAG Generation Benchmark")
    parser.add_argument("--dataset", type=str, default="evaluation/merged_real_medical_benchmark.csv", help="Path to dataset")
    parser.add_argument("--sample-size", type=int, default=10, help="Number of random questions to sample")
    parser.add_argument("--seed", type=int, default=42, help="Random seed for sampling")
    return parser.parse_args()

def main():
    if hasattr(sys.stdout, 'reconfigure'):
        sys.stdout.reconfigure(encoding='utf-8', errors='replace')
        
    args = parse_args()
    
    loader = DatasetLoader(args.dataset)
    samples = list(loader.load())
    
    if not samples:
        print("Dataset is empty. Exiting.")
        return

    # Sample random questions
    random.seed(args.seed)
    if len(samples) > args.sample_size:
        sampled_samples = random.sample(samples, args.sample_size)
    else:
        sampled_samples = samples

    print(f"=== Starting REAL LLM Generation Test on {len(sampled_samples)} questions ===")
    print("Initializing Medical RAG Pipeline with Real Llama-3.3-70b LLM...")
    
    pipeline = MedicalRAGPipeline()
    calculator = MetricCalculator()
    
    results = []
    total = len(sampled_samples)
    
    for idx, sample in enumerate(sampled_samples, 1):
        print(f"\n==================================================")
        print(f"[{idx}/{total}] Processing Question ID: {sample.question_id}")
        print(f"❓ Question: {sample.question}")
        
        t0 = time.time()
        try:
            response = pipeline.run(question=sample.question, patient_context=None)
            prediction = response.answer
        except Exception as e:
            print(f"❌ Error generating answer: {e}")
            prediction = f"Error: {e}"
            
        latency = time.time() - t0
        
        print(f"\n🤖 [Real Llama-3.3-70b Generated Answer]:\n{prediction}")
        print(f"\n📚 [Reference Expected Answer]:\n{sample.reference_answer}")
        
        gen_metrics = calculator.compute_generation_metrics(
            prediction=prediction, 
            reference=sample.reference_answer
        )
        
        print(f"\n📊 Latency: {latency:.2f}s | Answer Length: {len(prediction)} chars | ROUGE-L: {gen_metrics.get('rouge_l', 0):.2f}")
        
        results.append({
            "question_id": sample.question_id,
            "question": sample.question,
            "reference": sample.reference_answer,
            "prediction": prediction,
            "latency": latency,
            "metrics": gen_metrics
        })

    # Write summary report
    report_path = Path("reports/generation_real_llm_report.md")
    report_path.parent.mkdir(parents=True, exist_ok=True)
    
    with open(report_path, "w", encoding="utf-8") as f:
        f.writelines([
            "# Real LLM Generation Test Report\n\n",
            f"**Total Questions Tested:** {len(results)}\n",
            f"**Model Used:** Groq Llama-3.3-70b-versatile\n\n",
            "## Question-by-Question Results\n\n"
        ])
        for item in results:
            f.write(f"### Question {item['question_id']}: {item['question']}\n")
            f.write(f"- **Real LLM Answer:** {item['prediction']}\n")
            f.write(f"- **Reference Answer:** {item['reference']}\n")
            f.write(f"- **Latency:** {item['latency']:.2f}s\n\n")
            
    print(f"\n✅ All {len(results)} questions completed! Full report saved to {report_path}")

if __name__ == "__main__":
    main()
