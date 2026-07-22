import pandas as pd
from pathlib import Path

eval_dir = Path("evaluation")
batch1_path = eval_dir / "real_medical_benchmark.csv"
batch2_path = eval_dir / "real_medical_benchmark_batch2.csv"
batch3_path = eval_dir / "real_medical_benchmark_batch3.csv"

dfs = []

# Load Batch 1 if exists
if batch1_path.exists():
    df1 = pd.read_csv(batch1_path)
    if "ground_truth_ids" in df1.columns and "chunk_id" not in df1.columns:
        df1["chunk_id"] = df1["ground_truth_ids"]
    if "reference_answer" in df1.columns and "expected_answer" not in df1.columns:
        df1["expected_answer"] = df1["reference_answer"]
    dfs.append(df1)

# Load Batch 2
if batch2_path.exists():
    df2 = pd.read_csv(batch2_path)
    dfs.append(df2)

# Load Batch 3
if batch3_path.exists():
    df3 = pd.read_csv(batch3_path)
    dfs.append(df3)

if dfs:
    merged_df = pd.concat(dfs, ignore_index=True)
    # Ensure chunk_id and expected_answer are present
    if "ground_truth_ids" not in merged_df.columns:
        merged_df["ground_truth_ids"] = merged_df["chunk_id"]
    if "reference_answer" not in merged_df.columns:
        merged_df["reference_answer"] = merged_df["expected_answer"]
        
    output_path = eval_dir / "merged_real_medical_benchmark.csv"
    merged_df.to_csv(output_path, index=False)
    print(f"Successfully merged {len(merged_df)} questions into {output_path}")
