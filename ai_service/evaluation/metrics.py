import json
import time
from typing import List, Dict, Any, Tuple
import os
from pathlib import Path

# Optional BERTScore
try:
    from bert_score import score as bert_score
    BERT_AVAILABLE = True
except ImportError:
    BERT_AVAILABLE = False

class MetricCalculator:
    def __init__(self, config_path: str = "evaluation/config.json"):
        self.config = self._load_config(config_path)
    
    def _load_config(self, path: str) -> Dict[str, float]:
        default_config = {
            "rouge_correct": 0.7,
            "rouge_partial": 0.4,
            "bleu_correct": 0.5,
            "bleu_partial": 0.2
        }
        if os.path.exists(path):
            with open(path, "r", encoding="utf-8") as f:
                default_config.update(json.load(f))
        return default_config

    def compute_retrieval_metrics(self, retrieved_ids: List[str], ground_truth_ids: List[str], k_values: List[int] = [1, 3, 5, 10]) -> Dict[str, float]:
        if not ground_truth_ids:
            return {}
            
        metrics = {}
        for k in k_values:
            top_k_ids = retrieved_ids[:k]
            hits = sum(1 for gt_id in ground_truth_ids if gt_id in top_k_ids)
            metrics[f"recall@{k}"] = hits / len(ground_truth_ids) if ground_truth_ids else 0.0

        # Hit Rate (is at least 1 relevant doc retrieved?)
        metrics["hit_rate"] = 1.0 if any(gt_id in retrieved_ids for gt_id in ground_truth_ids) else 0.0
        
        # MRR
        mrr = 0.0
        for i, r_id in enumerate(retrieved_ids):
            if r_id in ground_truth_ids:
                mrr = 1.0 / (i + 1)
                break
        metrics["mrr"] = mrr

        return metrics

    def compute_generation_metrics(self, prediction: str, reference: str) -> Dict[str, float]:
        metrics = {}
        
        # Exact Match
        metrics["exact_match"] = 1.0 if prediction.strip().lower() == reference.strip().lower() else 0.0
        
        # Simple Length
        metrics["answer_length"] = len(prediction)
        
        # ROUGE-L & BLEU (simplified logic using basic overlap if packages not present, otherwise use real)
        # Note: In a real project, we'd use rouge-score and nltk here.
        metrics["rouge_l"] = self._compute_dummy_rouge(prediction, reference)
        metrics["bleu"] = self._compute_dummy_bleu(prediction, reference)
        
        if BERT_AVAILABLE:
            try:
                P, R, F1 = bert_score([prediction], [reference], lang="vi", verbose=False)
                metrics["bert_score"] = F1.item()
            except Exception:
                metrics["bert_score"] = 0.0
                
        return metrics
        
    def _compute_dummy_rouge(self, pred: str, ref: str) -> float:
        # Placeholder for ROUGE-L (longest common subsequence)
        pred_words = pred.lower().split()
        ref_words = ref.lower().split()
        if not pred_words or not ref_words: return 0.0
        common = set(pred_words).intersection(set(ref_words))
        return len(common) / len(ref_words)

    def _compute_dummy_bleu(self, pred: str, ref: str) -> float:
        # Placeholder for BLEU
        pred_words = pred.lower().split()
        ref_words = ref.lower().split()
        if not pred_words or not ref_words: return 0.0
        common = set(pred_words).intersection(set(ref_words))
        return len(common) / len(pred_words)

    def determine_status(self, metrics: Dict[str, float]) -> str:
        rouge = metrics.get("rouge_l", 0.0)
        bleu = metrics.get("bleu", 0.0)
        
        if rouge >= self.config["rouge_correct"] or bleu >= self.config["bleu_correct"]:
            return "Correct"
        elif rouge >= self.config["rouge_partial"] or bleu >= self.config["bleu_partial"]:
            return "Partial"
        return "Wrong"
