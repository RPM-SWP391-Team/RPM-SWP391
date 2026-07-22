import pandas as pd
from dataclasses import dataclass
from typing import Iterator, Dict, Any, Optional

@dataclass
class EvaluationSample:
    question_id: str
    question: str
    reference_answer: str
    metadata: Dict[str, Any]

class DatasetLoader:
    def __init__(self, file_path: str):
        self.file_path = file_path

    def load(self) -> Iterator[EvaluationSample]:
        if self.file_path.endswith(".csv"):
            df = pd.read_csv(self.file_path)
        elif self.file_path.endswith(".json"):
            df = pd.read_json(self.file_path)
        elif self.file_path.endswith(".jsonl"):
            df = pd.read_json(self.file_path, lines=True)
        else:
            raise ValueError("Unsupported file format. Use .csv, .json, or .jsonl")

        for idx, row in df.iterrows():
            question_id = str(row.get("question_id", idx))
            question = str(row.get("question", ""))
            reference_answer = str(row.get("reference_answer", row.get("answer", row.get("expected_answer", ""))))
            
            # Put remaining columns in metadata
            metadata = {k: v for k, v in row.items() if k not in ["question_id", "question", "reference_answer", "answer", "expected_answer"]}
            
            yield EvaluationSample(
                question_id=question_id,
                question=question,
                reference_answer=reference_answer,
                metadata=metadata
            )
