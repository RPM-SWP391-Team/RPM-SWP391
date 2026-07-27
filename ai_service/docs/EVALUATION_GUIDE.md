# Phase 7: Evaluation Framework Guide

This framework allows you to evaluate the performance of the Medical RAG system against an objective benchmark dataset independently of the core pipeline logic.

## Prerequisites
- A dataset in `.csv`, `.json`, or `.jsonl` format. 
- Required columns/keys: `question_id`, `question`, `reference_answer`.
- Optional metadata: `ground_truth_ids` (List of strings) to compute retrieval metrics.

## Running the Benchmark
To run the evaluation, use the `run_benchmark.py` script:

```bash
python benchmark/run_benchmark.py --dataset evaluation/ViMedAQA.csv --top-k 5
```

- `--dataset`: Path to the dataset file (mandatory).
- `--top-k`: Number of retrieved documents to evaluate (optional, default 5).

## Reports Generated
All reports are saved in the `reports/` folder:
- `errors.json`: Detailed, per-question analysis including status (Correct, Partial, Wrong) and metrics to help you debug.
- `evaluation_report.md`: Markdown summary highlighting average metrics and Top 10 correct/wrong answers.
- `latest.json`: The raw aggregated JSON metrics for the latest run.
- `benchmark_vX.json`: Versioned regression reports to compare progress over time.

## Modifying Evaluation Thresholds
Thresholds for what constitutes a "Correct", "Partial", or "Wrong" answer are configured via `evaluation/config.json`.
You can change `rouge_correct`, `bleu_correct`, etc., without editing code.

## Optional Packages
- If `bert-score` is installed in your environment, the evaluation will automatically compute BERTScore. Otherwise, it skips it smoothly and relies solely on ROUGE and BLEU.
