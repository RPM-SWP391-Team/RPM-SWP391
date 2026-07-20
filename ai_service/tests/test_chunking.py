import pytest
from pathlib import Path

# Add project root to path
import sys
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from src.pdf_parser import ParsedDocument, Page
from src.chunking import HierarchicalChunker, ChunkNode

def test_chunking_simple():
    chunker = HierarchicalChunker(min_chunk_chars=10)
    chunker.chunk_size_chars = 50  # Set small chunk size for testing
    
    pages = [
        Page(page_number=1, text="This is the first page of the document. " * 5),
        Page(page_number=2, text="This is the second page of the document. " * 5)
    ]
    doc = ParsedDocument(document_name="test.pdf", pages=pages)
    
    nodes = chunker.chunk_document(doc)
    
    # Kì vọng nhiều node do chunk_size_chars nhỏ
    assert len(nodes) > 1
    
    # Kiểm tra metadata
    for node in nodes:
        assert node.document_name == "test.pdf"
        assert node.source_file == "test.pdf"
        assert node.page_number in [1, 2]
        assert node.page_start in [1, 2]
        assert node.page_end in [1, 2]
        assert len(node.text) > 0

def test_hash_change():
    node = ChunkNode(
        node_id="test_id", 
        text="Hello", 
        page_number=1,
        source_file="test.pdf",
        document_name="test.pdf"
    )
    
    data = node.to_dict()
    assert data["id"] == "test_id"
    assert data["text"] == "Hello"
    assert data["metadata"]["page_number"] == 1
    
    node2 = ChunkNode.from_dict(data)
    assert node2.node_id == "test_id"
    assert node2.text == "Hello"
    assert node2.page_number == 1
