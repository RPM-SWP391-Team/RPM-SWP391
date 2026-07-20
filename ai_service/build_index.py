import os
import json
import logging

from src.chunking import ChunkNode
from src.tokenizer import WhitespaceTokenizer
from src.embedder import ModelLoader, EmbeddingCache
from src.index_builder import IndexBuilder

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

def main(chunks_jsonl_path: str = "data/chunks.jsonl"):
    if not os.path.exists(chunks_jsonl_path):
        raise FileNotFoundError(f"Không tìm thấy file input {chunks_jsonl_path}")

    # 1. Đọc chunks
    chunks = []
    with open(chunks_jsonl_path, "r", encoding="utf-8") as f:
        for line in f:
            if not line.strip():
                continue
            data = json.loads(line)
            # Rào chắn không import module patient_context
            # Kiểm tra dữ liệu Patient qua metadata (Zero-dependency check)
            if "patient_id" in data or data.get("metadata", {}).get("is_patient_data", False):
                raise PermissionError("Lỗi Bảo mật: Không được phép index dữ liệu Patient vào VectorDB/BM25.")
            chunks.append(ChunkNode.from_dict(data))
            
    logger.info(f"Đã đọc {len(chunks)} chunks từ {chunks_jsonl_path}")

    # 2. Composition Root: Tiêm các thành phần khởi tạo
    tokenizer = WhitespaceTokenizer()
    embedder = ModelLoader()
    cache = EmbeddingCache()
    
    # 3. Chạy Index Builder
    builder = IndexBuilder(tokenizer=tokenizer, embedder=embedder, cache=cache)
    
    manifest = builder.build_all(chunks)
    logger.info(f"Quá trình build index hoàn tất. Metadata: {manifest}")

if __name__ == "__main__":
    os.makedirs("data", exist_ok=True)
    main()
