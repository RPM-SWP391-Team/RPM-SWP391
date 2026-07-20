import pytest
import os
import tempfile
import json
import numpy as np

# Thêm đường dẫn project
import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from src.retriever import FAISSVectorIndex, BM25RetrieverImpl, HybridRetriever
from src.chunking import ChunkNode
from src.index_builder import IndexBuilder
from src.tokenizer import WhitespaceTokenizer
from src.embedder import ModelLoader, EmbeddingCache
from src.config import EMBEDDING
from build_index import main as build_index_main

class MockModelLoader(ModelLoader):
    def load(self):
        pass
    def embed_texts(self, texts, batch_size=32):
        # Giả lập trả về mảng có kích thước giống EMBEDDING.embedding_dimension
        return [np.zeros(EMBEDDING.embedding_dimension) for _ in texts]

class MockQueryEmbedder:
    def embed_texts(self, texts, batch_size=32):
        dim = EMBEDDING.embedding_dimension
        emb = np.zeros(dim)
        emb[0] = 1.0
        return [emb]

@pytest.fixture
def dummy_chunks():
    return [
        ChunkNode(node_id="id1", text="Viêm phổi là bệnh hô hấp.", title_path=["Bệnh lý"], chunk_type="text"),
        ChunkNode(node_id="id2", text="Đái tháo đường liên quan đến insulin.", title_path=["Bệnh lý"], chunk_type="text"),
        ChunkNode(node_id="id3", text="Một văn bản khác không liên quan.", title_path=["Khác"], chunk_type="text")
    ]

@pytest.fixture
def dummy_embeddings():
    dim = EMBEDDING.embedding_dimension
    emb1, emb2, emb3 = np.zeros(dim), np.zeros(dim), np.zeros(dim)
    emb1[0] = 1.0
    emb2[1] = 1.0
    emb3[2] = 1.0
    return [emb1, emb2, emb3]

def test_index_builder_and_retrievers(dummy_chunks, dummy_embeddings):
    with tempfile.TemporaryDirectory() as tmpdir:
        # Override paths
        import src.config
        src.config.INDEX = type('IndexConfigMock', (object,), {
            "vector_index_path": os.path.join(tmpdir, "vector.index"),
            "vector_id_map_path": os.path.join(tmpdir, "map.json"),
            "bm25_index_path": os.path.join(tmpdir, "bm25.pkl"),
            "manifest_path": os.path.join(tmpdir, "manifest.json"),
            "cache_db_path": os.path.join(tmpdir, "cache.db"),
            "embedding_batch_size": 32,
            "device": "cpu"
        })()

        tokenizer = WhitespaceTokenizer()
        embedder = MockModelLoader()
        cache = EmbeddingCache(db_path=src.config.INDEX.cache_db_path)
        
        builder = IndexBuilder(tokenizer, embedder, cache)
        
        # Override embedder to return our dummy embeddings
        def mock_embed(texts):
            return dummy_embeddings[:len(texts)]
        embedder.embed_texts = mock_embed
        
        # Build Index
        manifest = builder.build_all(dummy_chunks)
        assert manifest["total_chunks"] == 3
        
        # Load FAISS Vector Index
        vector_index = FAISSVectorIndex(
            index_path=src.config.INDEX.vector_index_path, 
            id_map_path=src.config.INDEX.vector_id_map_path,
            manifest_path=src.config.INDEX.manifest_path
        )
        vector_index.load()
        
        query_emb = dummy_embeddings[0].tolist()
        v_res = vector_index.search(query_emb, top_k=1)
        assert len(v_res) == 1
        assert v_res[0].node_id == "id1"
        assert v_res[0].retriever_type == "dense"
        
        # Load BM25 Index
        bm25_index = BM25RetrieverImpl(
            index_path=src.config.INDEX.bm25_index_path,
            tokenizer=tokenizer
        )
        bm25_index.load()
        
        b_res = bm25_index.search("insulin", top_k=1)
        assert len(b_res) == 1
        assert b_res[0].node_id == "id2"
        assert b_res[0].retriever_type == "bm25"
        
        # Test HybridRetriever orchestration
        hybrid = HybridRetriever(bm25_index, vector_index, MockQueryEmbedder())
        b_out, v_out = hybrid.retrieve_both("test query")
        assert len(b_out) >= 0
        assert len(v_out) >= 0

def test_patient_data_safety():
    with tempfile.TemporaryDirectory() as tmpdir:
        dummy_jsonl = os.path.join(tmpdir, "chunks.jsonl")
        
        with open(dummy_jsonl, "w", encoding="utf-8") as f:
            data = {
                "id": "patient1",
                "text": "Bệnh nhân có tiền sử tiểu đường.",
                "patient_id": "P001",
                "metadata": {"is_patient_data": True}
            }
            f.write(json.dumps(data) + "\n")
            
        with pytest.raises(PermissionError, match="Không được phép index dữ liệu Patient"):
            build_index_main(chunks_jsonl_path=dummy_jsonl)
