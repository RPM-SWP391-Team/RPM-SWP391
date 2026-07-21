import sys
from pathlib import Path
from batch_process import convert_one, chunk_one

pdf_path = Path("data/raw_documents/ehae178.pdf")
print("Converting...")
md_text, md_path = convert_one(pdf_path)
print(f"Converted to {md_path}")
out_json = pdf_path.parent / f"{pdf_path.stem}_chunks.json"
print("Chunking...")
chunks = chunk_one(md_path, out_json)
print(f"Chunked into {len(chunks)} chunks!")
