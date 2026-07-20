from __future__ import annotations
import hashlib
import json
import logging
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any

from src.pdf_parser import ParsedDocument

logger = logging.getLogger(__name__)

def generate_node_id(source_document: str, chunk_index: int) -> str:
    """MD5(source_document_name + chunk_index)"""
    base_str = f"{source_document}_{chunk_index}"
    return hashlib.md5(base_str.encode("utf-8")).hexdigest()

@dataclass
class ChunkNode:
    node_id: str
    text: str
    page_number: int | None = None
    source_file: str | None = None
    document_name: str | None = None
    page_start: int | None = None
    page_end: int | None = None
    # Restored for backward compatibility with Phase 2-7
    title_path: list[str] = field(default_factory=list)
    chunk_type: str = "text"
    parent_node_id: str | None = None
    previous_sibling_node: str | None = None
    next_sibling_node: str | None = None
    covered_node_ids: list[str] = field(default_factory=list)
    source_document: str | None = None
    guideline_version: str | None = None
    organization: str | None = None
    language: str | None = None
    effective_date: str | None = None
    source_url: str | None = None
    chunk_hash: str | None = None

    def update_hash(self):
        """Update hash to satisfy older indexing logic which explicitly calls it."""
        import json
        important_meta = {
            k: v for k, v in self.to_dict()["metadata"].items()
            if k in ["guideline_version", "effective_date", "page_number", "organization"]
        }
        hash_str = self.text + json.dumps(important_meta, sort_keys=True)
        self.chunk_hash = hashlib.sha256(hash_str.encode("utf-8")).hexdigest()

    def to_dict(self) -> dict[str, Any]:
        meta = {
            "source_file": self.source_file,
            "document_name": self.document_name,
            "page_number": self.page_number,
            "title_path": self.title_path,
            "chunk_type": self.chunk_type,
            "parent_node_id": self.parent_node_id,
            "previous_sibling_node": self.previous_sibling_node,
            "next_sibling_node": self.next_sibling_node,
            "covered_node_ids": self.covered_node_ids,
            "source_document": self.source_document,
            "guideline_version": self.guideline_version,
            "organization": self.organization,
            "language": self.language,
            "effective_date": self.effective_date,
            "source_url": self.source_url,
            "chunk_hash": self.chunk_hash
        }
        if self.page_start is not None:
            meta["page_start"] = self.page_start
        if self.page_end is not None:
            meta["page_end"] = self.page_end
            
        # Keep all keys even if None for downstream compatibility
            
        return {
            "id": self.node_id,
            "text": self.text,
            "metadata": meta
        }

    @classmethod
    def from_dict(cls, data: dict[str, Any]) -> ChunkNode:
        meta = data.get("metadata", {})
        return cls(
            node_id=data["id"],
            text=data["text"],
            page_number=meta.get("page_number"),
            source_file=meta.get("source_file"),
            document_name=meta.get("document_name"),
            page_start=meta.get("page_start"),
            page_end=meta.get("page_end"),
            title_path=meta.get("title_path", []),
            chunk_type=meta.get("chunk_type", "text"),
            parent_node_id=meta.get("parent_node_id"),
            previous_sibling_node=meta.get("previous_sibling_node"),
            next_sibling_node=meta.get("next_sibling_node"),
            covered_node_ids=meta.get("covered_node_ids", []),
            source_document=meta.get("source_document"),
            guideline_version=meta.get("guideline_version"),
            organization=meta.get("organization"),
            language=meta.get("language"),
            effective_date=meta.get("effective_date"),
            source_url=meta.get("source_url"),
            chunk_hash=meta.get("chunk_hash")
        )


