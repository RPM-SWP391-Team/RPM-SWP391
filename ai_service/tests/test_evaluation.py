import pytest
import pandas as pd
import json
import os
from pathlib import Path
import tempfile

import sys
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from evaluation.dataset_loader import DatasetLoader
from evaluation.metrics import MetricCalculator
from evaluation.report_generator import ReportGenerator

def test_dataset_loader_csv():
    with tempfile.NamedTemporaryFile(mode="w", suffix=".csv", delete=False) as f:
        f.write("question_id,question,reference_answer,ground_truth_ids\n")
        f.write("1,What is diabetes?,It is a disease.,['doc1']\n")
        f_name = f.name
        
    try:
        loader = DatasetLoader(f_name)
        samples = list(loader.load())
        assert len(samples) == 1
        assert samples[0].question_id == "1"
        assert samples[0].question == "What is diabetes?"
        assert samples[0].reference_answer == "It is a disease."
    finally:
        os.remove(f_name)

def test_metric_calculator():
    calc = MetricCalculator(config_path="dummy.json") # Should fallback to default
    
    # Test generation metrics
    metrics = calc.compute_generation_metrics("Hello world", "Hello world")
    assert metrics["exact_match"] == 1.0
    assert metrics["rouge_l"] == 1.0
    assert metrics["bleu"] == 1.0
    
    # Test retrieval metrics
    ret_metrics = calc.compute_retrieval_metrics(
        retrieved_ids=["doc1", "doc2", "doc3"],
        ground_truth_ids=["doc2"],
        k_values=[1, 3]
    )
    assert ret_metrics["recall@1"] == 0.0
    assert ret_metrics["recall@3"] == 1.0
    assert ret_metrics["hit_rate"] == 1.0
    assert ret_metrics["mrr"] == 0.5

def test_report_generator():
    with tempfile.TemporaryDirectory() as tmpdir:
        generator = ReportGenerator(output_dir=tmpdir)
        
        dummy_results = [
            {
                "question_id": "1",
                "question": "Q1",
                "reference": "A1",
                "prediction": "A1",
                "metrics": {"rouge_l": 0.8, "bleu": 0.6},
                "retrieval_time": 0.1,
                "generation_time": 0.2,
                "status": "Correct"
            }
        ]
        
        generator.generate_errors_report(dummy_results)
        generator.generate_summary_report(dummy_results)
        
        assert (Path(tmpdir) / "errors.json").exists()
        assert (Path(tmpdir) / "evaluation_report.md").exists()
        assert (Path(tmpdir) / "latest.json").exists()
        assert (Path(tmpdir) / "benchmark_v1.json").exists()
