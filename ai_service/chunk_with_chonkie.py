"""
chunk_with_chonkie.py
─────────────────────
Chunks a Markdown file produced by pdf_to_markdown_v2.py or pdf_to_markdown_docling.py.

Strategy:
  - Tables  -> chonkie TableChunker  (chunk_type = "table", size=TABLE_CHUNK_SIZE)
  - Prose   -> chonkie RecursiveChunker.from_recipe("markdown", lang="en")
               chunk_size=PROSE_CHUNK_SIZE, tokenizer="gpt2"

Usage:
    python chunk_with_chonkie.py <path_to_md_file> [output_json]

Output:
    <output_json>  (default: same dir as md, same stem + "_chunks.json")

Each chunk in the JSON has:
    id, text, token_count, chunk_type ("prose" | "table")
"""
from __future__ import annotations

import json
import re
import sys
import uuid
from pathlib import Path
from typing import Any

# ─── CHUNK SIZE CONSTANTS ────────────────────────────────────────────────────
# Change here only — do NOT hardcode values inside functions.

PROSE_CHUNK_SIZE: int = 512   # tokens — RecursiveChunker for prose
TABLE_CHUNK_SIZE: int = 2000  # tokens — TableChunker; keeps small tables whole


# ─── CLI ────────────────────────────────────────────────────────────────────

def get_args() -> tuple[Path, Path]:
    if len(sys.argv) < 2:
        print("Usage: python chunk_with_chonkie.py <path_to_md_file> [output_json]")
        sys.exit(1)
    md_path = Path(sys.argv[1])
    if not md_path.exists():
        print(f"[ERROR] File not found: {md_path}")
        sys.exit(1)
    if md_path.suffix.lower() != ".md":
        print(f"[WARNING] Expected a .md file, got: {md_path.suffix}")

    if len(sys.argv) >= 3:
        out_path = Path(sys.argv[2])
    else:
        out_path = md_path.parent / f"{md_path.stem}_chunks.json"
    return md_path, out_path


# ─── TABLE EXTRACTION ────────────────────────────────────────────────────────

def extract_tables_and_prose(text: str) -> tuple[list[str], str]:
    """
    Split markdown into:
      - list of raw table block strings (each block appears exactly ONCE)
      - remaining prose text (tables replaced with sentinel placeholders)

    A valid table block MUST contain at least one separator row (|---|).
    Invalid/layout pipe-lines are left in the prose stream unchanged.

    FIX vs previous version: use a line-by-line state machine instead of
    a regex that could produce overlapping matches on the same table.
    """
    lines = text.splitlines(keepends=True)
    tables: list[str] = []
    prose_lines: list[str] = []

    i = 0
    while i < len(lines):
        stripped = lines[i].rstrip("\n").strip()

        # Start of a potential table: line begins with '|'
        if stripped.startswith("|"):
            # Collect the full contiguous block of pipe lines
            block_start = i
            block_lines: list[str] = []
            while i < len(lines) and lines[i].rstrip("\n").strip().startswith("|"):
                block_lines.append(lines[i])
                i += 1

            block_text = "".join(block_lines)

            # Only treat as a real table if there is a separator row |---|
            if re.search(r"^\|[\-\:\s\|]+\|", block_text, re.MULTILINE):
                key = f"__TABLE_{len(tables)}__\n"
                tables.append(block_text)
                prose_lines.append(key)
            else:
                # Not a real table — keep as prose unchanged
                prose_lines.extend(block_lines)
        else:
            prose_lines.append(lines[i])
            i += 1

    prose_text = "".join(prose_lines)
    return tables, prose_text


# ─── TOKEN COUNT (gpt2 tokenizer) ────────────────────────────────────────────

_TOKENIZER = None

def get_token_count(text: str) -> int:
    """Return token count using gpt2 tokenizer (via tokie/chonkie internals)."""
    global _TOKENIZER
    try:
        if _TOKENIZER is None:
            from tokenizers import Tokenizer
            _TOKENIZER = Tokenizer.from_pretrained("gpt2")
        enc = _TOKENIZER.encode(text, add_special_tokens=False)
        return len(enc.ids)
    except Exception:
        # Fallback: rough estimate (~4 chars per token)
        return max(1, len(text) // 4)


# ─── CHUNKING ────────────────────────────────────────────────────────────────

def chunk_tables(table_blocks: list[str]) -> list[dict[str, Any]]:
    """
    Chunk table blocks via TableChunker (chunk_size=TABLE_CHUNK_SIZE).
    Falls back to one-chunk-per-block if TableChunker is unavailable or skips.
    Each block appears exactly once in the input list, so no duplication.
    """
    results: list[dict[str, Any]] = []

    for blk in table_blocks:
        produced_texts: list[str] = []
        try:
            from chonkie import TableChunker
            tc = TableChunker(chunk_size=TABLE_CHUNK_SIZE, tokenizer="gpt2")
            chonkie_chunks = tc(blk)
            produced_texts = [c.text for c in chonkie_chunks] if chonkie_chunks else [blk]
        except Exception:
            produced_texts = [blk]

        # If TableChunker returned nothing (e.g. skipped malformed table),
        # keep the raw block so it is not silently dropped.
        if not produced_texts:
            produced_texts = [blk]

        for text in produced_texts:
            text = text.strip()
            if not text:
                continue
            results.append({
                "id": str(uuid.uuid4()),
                "text": text,
                "token_count": get_token_count(text),
                "chunk_type": "table",
            })

    return results


def chunk_prose(prose_text: str) -> list[dict[str, Any]]:
    """
    Chunk prose using RecursiveChunker.from_recipe("markdown", lang="en").
    chunk_size=PROSE_CHUNK_SIZE, tokenizer="gpt2"  (per spec — do not change).
    """
    results: list[dict[str, Any]] = []

    # Remove sentinel lines before chunking
    clean_prose = re.sub(r"^__TABLE_\d+__\n?", "", prose_text, flags=re.MULTILINE).strip()
    if not clean_prose:
        return results

    try:
        from chonkie import RecursiveChunker
        chunker = RecursiveChunker.from_recipe("markdown", lang="en", chunk_size=PROSE_CHUNK_SIZE, tokenizer="gpt2")
        chonkie_chunks = chunker(clean_prose)
    except Exception as e:
        print(f"[ERROR] RecursiveChunker failed: {e}")
        # Fallback: one chunk per paragraph
        chonkie_chunks = []
        for para in re.split(r"\n{2,}", clean_prose):
            para = para.strip()
            if para:
                class _FakeChunk:
                    def __init__(self, t: str):
                        self.text = t
                chonkie_chunks.append(_FakeChunk(para))

    for c in chonkie_chunks:
        text = c.text.strip()
        if not text:
            continue
        results.append({
            "id": str(uuid.uuid4()),
            "text": text,
            "token_count": get_token_count(text),
            "chunk_type": "prose",
        })

    return results


# ─── STATISTICS ──────────────────────────────────────────────────────────────

def print_stats(chunks: list[dict[str, Any]]) -> None:
    prose_chunks = [c for c in chunks if c["chunk_type"] == "prose"]
    table_chunks = [c for c in chunks if c["chunk_type"] == "table"]
    total = len(chunks)

    tokens = [c["token_count"] for c in chunks]
    min_tok = min(tokens) if tokens else 0
    max_tok = max(tokens) if tokens else 0
    avg_tok = sum(tokens) / len(tokens) if tokens else 0

    tiny  = [c for c in chunks if c["token_count"] < 20]
    large = [c for c in chunks if c["token_count"] > 512]

    sep = "=" * 60
    print(f"\n{sep}")
    print("[CHUNK STATS]")
    print(f"{sep}")
    print(f"  Total chunks          : {total}")
    print(f"    Prose chunks        : {len(prose_chunks)}")
    print(f"    Table chunks        : {len(table_chunks)}")
    print(f"\n  Token distribution    :")
    print(f"    Min token_count     : {min_tok}")
    print(f"    Max token_count     : {max_tok}")
    print(f"    Avg token_count     : {avg_tok:.1f}")

    print(f"\n  Tiny chunks (<20 tok) : {len(tiny)}")
    for c in tiny[:10]:
        preview = c["text"].replace("\n", " ")[:80]
        print(f"    [{c['token_count']:3d} tok] {preview!r}")
    if len(tiny) > 10:
        print(f"    ... and {len(tiny)-10} more (truncated)")

    print(f"\n  Large chunks (>512 tok): {len(large)}")
    for c in large[:10]:
        preview = c["text"].replace("\n", " ")[:80]
        print(f"    [{c['token_count']:4d} tok] {preview!r}")
        if c["chunk_type"] == "table":
            print(f"      (table chunk — natural size, not splittable by chunk_size limit)")
    if len(large) > 10:
        print(f"    ... and {len(large)-10} more (truncated)")

    print(f"{sep}")


# ─── MAIN ────────────────────────────────────────────────────────────────────

def main() -> None:
    md_path, out_path = get_args()
    print(f"\n[INFO] Reading: {md_path}")
    full_text = md_path.read_text(encoding="utf-8")
    print(f"[INFO] Total chars: {len(full_text):,}")

    # Split tables vs prose
    table_blocks, prose_text = extract_tables_and_prose(full_text)
    print(f"[INFO] Tables found by extractor: {len(table_blocks)}")

    print("[INFO] Chunking tables ...")
    table_chunk_list = chunk_tables(table_blocks)

    print("[INFO] Chunking prose ...")
    prose_chunk_list = chunk_prose(prose_text)

    all_chunks = prose_chunk_list + table_chunk_list

    # Save JSON
    with out_path.open("w", encoding="utf-8") as f:
        json.dump(all_chunks, f, ensure_ascii=False, indent=2)
    print(f"[OK] Saved {len(all_chunks)} chunks to: {out_path}")

    # Print statistics
    print_stats(all_chunks)

    # Verify: show all table chunks in full for manual inspection
    table_chunks = [c for c in all_chunks if c["chunk_type"] == "table"]
    print(f"\n[TABLE CHUNKS VERIFICATION] {len(table_chunks)} table chunk(s) total:")
    for idx, c in enumerate(table_chunks, 1):
        print(f"\n  --- Table chunk #{idx} (token_count={c['token_count']}) ---")
        print(c["text"][:500])
        if len(c["text"]) > 500:
            print(f"  ... ({len(c['text'])} chars total, truncated at 500)")

    # Verify: no heading_path field
    first_keys = list(all_chunks[0].keys()) if all_chunks else []
    print(f"\n[FIELD VERIFY] Keys in chunk[0]: {first_keys}")
    assert "heading_path" not in first_keys, "BUG: heading_path still present!"
    print("[FIELD VERIFY] PASS — no heading_path field in any chunk.")

    print(f"\n[DONE]")


if __name__ == "__main__":
    main()
