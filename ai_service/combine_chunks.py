import json
from pathlib import Path

def combine():
    pdf_dir = Path("data/raw_documents")
    out_file = Path("data/chunks.jsonl")
    
    with open(out_file, "w", encoding="utf-8") as out:
        for chunk_file in pdf_dir.glob("*_chunks.json"):
            print(f"Reading {chunk_file.name}...")
            with open(chunk_file, "r", encoding="utf-8") as f:
                data = json.load(f)
                for chunk in data:
                    out.write(json.dumps(chunk, ensure_ascii=False) + "\n")
                    
    print(f"Combined into {out_file}")

if __name__ == "__main__":
    combine()
