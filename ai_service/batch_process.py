"""
batch_process.py
────────────────
Converts ALL .pdf files in data/raw_documents/ using Docling, then chunks
each Markdown output with Chonkie.

Usage:
    py -3.14 batch_process.py [pdf_dir]

    pdf_dir defaults to  data/raw_documents  (relative to CWD).

Outputs per file:
    {pdf_dir}/{stem}_docling_converted.md
    {pdf_dir}/{stem}_chunks.json

A summary report is printed after all files are processed.
"""
from __future__ import annotations

import json
import sys
import traceback
from pathlib import Path
from typing import Any


# ─── CONFIG ──────────────────────────────────────────────────────────────────

DEFAULT_PDF_DIR = Path("data/raw_documents")


# ─── HELPERS: reuse existing module functions directly ───────────────────────

def convert_one(pdf_path: Path) -> tuple[str, Path]:
    """Convert a single PDF to Markdown via Docling. Returns (markdown_text, md_path)."""
    from docling.document_converter import DocumentConverter
    converter = DocumentConverter()
    result = converter.convert(str(pdf_path))
    markdown_text: str = result.document.export_to_markdown()
    out_path = pdf_path.parent / f"{pdf_path.stem}_docling_converted.md"
    out_path.write_text(markdown_text, encoding="utf-8")
    return markdown_text, out_path


def chunk_one(md_path: Path, out_json: Path) -> list[dict[str, Any]]:
    """Chunk a Markdown file and save to JSON. Returns list of chunk dicts."""
    # Import the functions directly from the module we already maintain.
    import importlib.util, sys as _sys
    spec = importlib.util.spec_from_file_location(
        "chunk_with_chonkie",
        Path(__file__).parent / "chunk_with_chonkie.py"
    )
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)

    full_text = md_path.read_text(encoding="utf-8")
    table_blocks, prose_text = mod.extract_tables_and_prose(full_text)
    table_chunk_list = mod.chunk_tables(table_blocks)
    prose_chunk_list = mod.chunk_prose(prose_text)
    all_chunks = prose_chunk_list + table_chunk_list

    with out_json.open("w", encoding="utf-8") as f:
        json.dump(all_chunks, f, ensure_ascii=False, indent=2)

    return all_chunks


# ─── BATCH RUNNER ────────────────────────────────────────────────────────────

def process_all(pdf_dir: Path) -> None:
    pdf_files = sorted(pdf_dir.glob("*.pdf"))
    if not pdf_files:
        print(f"[ERROR] No .pdf files found in {pdf_dir}")
        sys.exit(1)

    print(f"[INFO] Found {len(pdf_files)} PDF file(s) in {pdf_dir}")
    print(f"[INFO] PROSE_CHUNK_SIZE=512  TABLE_CHUNK_SIZE=2000\n")

    # Per-file results
    results: list[dict[str, Any]] = []
    errors: list[tuple[str, str]] = []

    for idx, pdf_path in enumerate(pdf_files, 1):
        print(f"[{idx:02d}/{len(pdf_files)}] Processing: {pdf_path.name} ...", flush=True)
        rec: dict[str, Any] = {"file": pdf_path.name, "ok": False, "chunks": []}

        try:
            # ── Convert
            md_text, md_path = convert_one(pdf_path)
            rec["md_chars"] = len(md_text)
            print(f"       Converted -> {md_path.name}  ({len(md_text):,} chars)")

            # ── Chunk
            out_json = pdf_path.parent / f"{pdf_path.stem}_chunks.json"
            chunks = chunk_one(md_path, out_json)
            rec["chunks"] = chunks
            rec["ok"] = True
            print(f"       Chunked   -> {out_json.name}  ({len(chunks)} chunks)")

        except Exception as exc:
            msg = f"{type(exc).__name__}: {exc}"
            errors.append((pdf_path.name, msg))
            rec["error"] = msg
            print(f"       [ERROR] {msg}")
            traceback.print_exc()

        results.append(rec)

    # ─── SUMMARY REPORT ──────────────────────────────────────────────────────
    print_summary(results, errors)


# ─── SUMMARY ─────────────────────────────────────────────────────────────────

def print_summary(results: list[dict], errors: list[tuple[str, str]]) -> None:
    sep = "=" * 70
    print(f"\n\n{sep}")
    print("[BATCH SUMMARY REPORT]")
    print(f"{sep}")

    total_files = len(results)
    ok_files = sum(1 for r in results if r["ok"])
    fail_files = len(errors)
    print(f"  Files total     : {total_files}")
    print(f"  Converted OK    : {ok_files}")
    print(f"  Failed          : {fail_files}")

    # Collect ALL chunks across all successful files
    all_prose: list[tuple[str, dict]] = []   # (filename, chunk)
    all_table: list[tuple[str, dict]] = []

    for r in results:
        if not r["ok"]:
            continue
        for c in r["chunks"]:
            if c["chunk_type"] == "prose":
                all_prose.append((r["file"], c))
            else:
                all_table.append((r["file"], c))

    total_chunks = len(all_prose) + len(all_table)
    print(f"\n  Total chunks (all corpus): {total_chunks}")
    print(f"    Prose chunks  : {len(all_prose)}")
    print(f"    Table chunks  : {len(all_table)}")

    # ── Token distribution — Prose
    print(f"\n  Token distribution — PROSE:")
    if all_prose:
        ptoks = [c["token_count"] for _, c in all_prose]
        print(f"    Min : {min(ptoks)}")
        print(f"    Max : {max(ptoks)}")
        print(f"    Avg : {sum(ptoks)/len(ptoks):.1f}")
    else:
        print("    (no prose chunks)")

    # ── Token distribution — Table
    print(f"\n  Token distribution — TABLE:")
    if all_table:
        ttoks = [c["token_count"] for _, c in all_table]
        print(f"    Min : {min(ttoks)}")
        print(f"    Max : {max(ttoks)}")
        print(f"    Avg : {sum(ttoks)/len(ttoks):.1f}")
    else:
        print("    (no table chunks)")

    # ── Tables split beyond TABLE_CHUNK_SIZE=2000
    TABLE_CHUNK_SIZE = 2000
    print(f"\n  Table chunks > TABLE_CHUNK_SIZE ({TABLE_CHUNK_SIZE} tok) [split large tables]:")
    oversized_tables = [(f, c) for f, c in all_table if c["token_count"] > TABLE_CHUNK_SIZE]
    if oversized_tables:
        from collections import Counter
        file_counts: Counter = Counter(f for f, _ in oversized_tables)
        for fname, cnt in sorted(file_counts.items()):
            print(f"    {fname}: {cnt} chunk(s) > {TABLE_CHUNK_SIZE} tok")
    else:
        print(f"    (none — all table chunks within {TABLE_CHUNK_SIZE} tok limit)")

    # ── Tiny prose chunks < 20 tok
    print(f"\n  Prose chunks < 20 tok (suspect fragmentation):")
    tiny_prose = [(f, c) for f, c in all_prose if c["token_count"] < 20]
    if tiny_prose:
        from collections import Counter
        file_counts_tiny: Counter = Counter(f for f, _ in tiny_prose)
        for fname, cnt in sorted(file_counts_tiny.items()):
            print(f"    {fname}: {cnt} tiny chunk(s)")
            for f2, c2 in tiny_prose:
                if f2 == fname:
                    preview = c2["text"].replace("\n", " ")[:60]
                    print(f"      [{c2['token_count']:2d} tok] {preview!r}")
    else:
        print("    (none)")

    # ── Prose chunks > 512 tok
    PROSE_CHUNK_SIZE = 512
    print(f"\n  Prose chunks > {PROSE_CHUNK_SIZE} tok (not split cleanly):")
    large_prose = [(f, c) for f, c in all_prose if c["token_count"] > PROSE_CHUNK_SIZE]
    if large_prose:
        from collections import Counter
        file_counts_large: Counter = Counter(f for f, _ in large_prose)
        for fname, cnt in sorted(file_counts_large.items()):
            print(f"    {fname}: {cnt} chunk(s) > {PROSE_CHUNK_SIZE} tok")
            for f2, c2 in large_prose:
                if f2 == fname:
                    preview = c2["text"].replace("\n", " ")[:80]
                    print(f"      [{c2['token_count']:4d} tok] {preview!r}")
    else:
        print(f"    (none)")

    # ── Errors
    print(f"\n  Failed files ({fail_files}):")
    if errors:
        for fname, msg in errors:
            print(f"    {fname}: {msg}")
    else:
        print("    (none)")

    print(f"\n{sep}")
    print("[DONE]")
    print(f"{sep}")


# ─── MAIN ────────────────────────────────────────────────────────────────────

def main() -> None:
    pdf_dir = Path(sys.argv[1]) if len(sys.argv) >= 2 else DEFAULT_PDF_DIR
    if not pdf_dir.exists():
        print(f"[ERROR] Directory not found: {pdf_dir}")
        sys.exit(1)
    process_all(pdf_dir)


if __name__ == "__main__":
    main()
