"""
pdf_to_markdown_docling.py
──────────────────────────
Converts a single PDF to Markdown using Docling (free-tier, no extra models).
Output is intentionally formatted identically to pdf_to_markdown_v2.py (MarkItDown)
so results can be compared side-by-side.

Usage:
    py -3.14 pdf_to_markdown_docling.py <path_to_pdf>

Output:
    <same_dir>/<original_stem>_docling_converted.md

Diagnostics printed:
  1. Total character count
  2. Count of real Markdown headings (lines starting with #)   ← KEY metric vs MarkItDown
  3. All Markdown tables found (start line, rows, cols, 3-row preview)
  4. Warning for malformed tables (uneven column counts)
  5. Mojibake / encoding check
"""
from __future__ import annotations

import re
import sys
from pathlib import Path
from typing import Any


# ─── CLI ────────────────────────────────────────────────────────────────────

def get_input_pdf() -> Path:
    if len(sys.argv) < 2:
        print("Usage: py -3.14 pdf_to_markdown_docling.py <path_to_pdf>")
        sys.exit(1)
    p = Path(sys.argv[1])
    if not p.exists():
        print(f"[ERROR] File not found: {p}")
        sys.exit(1)
    if p.suffix.lower() != ".pdf":
        print(f"[ERROR] Expected a .pdf file, got: {p.suffix}")
        sys.exit(1)
    return p


# ─── CONVERSION ─────────────────────────────────────────────────────────────

def convert_pdf_to_markdown(pdf_path: Path) -> tuple[str, Path]:
    """
    Convert PDF → Markdown via Docling DocumentConverter (defaults only,
    no OCR model, no vision model — free-tier).
    Returns (markdown_text, output_path).
    """
    from docling.document_converter import DocumentConverter

    print("[INFO] Initialising DocumentConverter (default pipeline) ...")
    converter = DocumentConverter()

    print(f"[INFO] Converting {pdf_path.name} ...")
    result = converter.convert(str(pdf_path))
    markdown_text: str = result.document.export_to_markdown()

    out_path = pdf_path.parent / f"{pdf_path.stem}_docling_converted.md"
    out_path.write_text(markdown_text, encoding="utf-8")
    print(f"[OK] Saved markdown to: {out_path}")
    return markdown_text, out_path


# ─── HEADING COUNT ───────────────────────────────────────────────────────────

def count_headings(markdown_text: str) -> dict[str, Any]:
    """Count real Markdown headings (lines starting with one or more #)."""
    lines = markdown_text.splitlines()
    heading_lines = [ln for ln in lines if re.match(r"^#{1,6}\s", ln)]
    by_level: dict[int, int] = {}
    for ln in heading_lines:
        level = len(re.match(r"^(#+)", ln).group(1))
        by_level[level] = by_level.get(level, 0) + 1
    return {"total": len(heading_lines), "by_level": by_level, "examples": heading_lines[:10]}


def report_headings(hinfo: dict[str, Any]) -> None:
    sep = "=" * 60
    print(f"\n{sep}")
    print(f"[HEADINGS] Real Markdown headings (lines starting with #)")
    print(f"{sep}")
    print(f"  Total headings       : {hinfo['total']}")
    if hinfo["by_level"]:
        for lvl in sorted(hinfo["by_level"]):
            print(f"    H{lvl} ({('#'*lvl):6s}) : {hinfo['by_level'][lvl]}")
    else:
        print("  (none found)")
    if hinfo["examples"]:
        print("  First 10 examples:")
        for ex in hinfo["examples"]:
            print(f"    {ex[:120]}")


# ─── TABLE DETECTION (identical logic to pdf_to_markdown_v2.py) ─────────────

def _parse_table_rows(lines: list[str], start: int) -> list[list[str]]:
    rows: list[list[str]] = []
    i = start
    while i < len(lines):
        stripped = lines[i].strip()
        if not stripped.startswith("|"):
            break
        if re.fullmatch(r"[\|\-\:\s]+", stripped):
            i += 1
            continue
        cells = [c.strip() for c in stripped.strip("|").split("|")]
        rows.append(cells)
        i += 1
    return rows


def detect_tables(markdown_text: str) -> list[dict]:
    lines = markdown_text.splitlines()
    tables: list[dict] = []
    i = 0
    while i < len(lines):
        stripped = lines[i].strip()
        if stripped.startswith("|") and not re.fullmatch(r"[\|\-\:\s]+", stripped):
            start_line = i + 1  # 1-indexed
            rows = _parse_table_rows(lines, i)
            j = i
            while j < len(lines) and (lines[j].strip().startswith("|") or re.fullmatch(r"[\|\-\:\s]+", lines[j].strip())):
                j += 1
            i = j
            if not rows:
                continue
            col_counts = [len(r) for r in rows]
            max_cols = max(col_counts)
            # Classify: does this block have a proper separator row?
            block_text = "\n".join(lines[start_line - 1: j])
            has_separator = bool(re.search(r"^\|[\-\:\s\|]+\|", block_text, re.MULTILINE))
            tables.append({
                "start_line": start_line,
                "num_rows": len(rows),
                "num_cols": max_cols,
                "col_counts": col_counts,
                "preview_rows": rows[:3],
                "has_separator": has_separator,
            })
        else:
            i += 1
    return tables


def report_tables(tables: list[dict]) -> None:
    real_tables = [t for t in tables if t["has_separator"]]
    fake_tables = [t for t in tables if not t["has_separator"]]

    sep = "=" * 60
    print(f"\n{sep}")
    print(f"[TABLES] Found {len(tables)} Markdown table block(s)")
    print(f"         -> {len(real_tables)} with proper separator row (real tables)")
    print(f"         -> {len(fake_tables)} without separator row (layout/fake tables)")
    print(f"{sep}")

    if not tables:
        print("  (none detected)")
        return

    for idx, tbl in enumerate(tables, 1):
        flag = "[REAL]" if tbl["has_separator"] else "[FAKE/LAYOUT]"
        print(f"\n  Table #{idx} {flag}:")
        print(f"    Start line  : {tbl['start_line']}")
        print(f"    Rows        : {tbl['num_rows']}")
        print(f"    Cols (max)  : {tbl['num_cols']}")

        unique_counts = set(tbl["col_counts"])
        if len(unique_counts) > 1:
            print(f"    [WARNING] Uneven column counts: {sorted(unique_counts)} -- table may be broken!")

        print("    Preview (up to 3 rows):")
        for row_idx, row in enumerate(tbl["preview_rows"], 1):
            row_str = " | ".join(row)
            print(f"      Row {row_idx}: {row_str[:120]}")


# ─── ENCODING CHECK (identical to pdf_to_markdown_v2.py) ─────────────────────

_COMPILED_MOJIBAKE = [re.compile(p) for p in [
    r"Ã.",
    r"Â.",
    r"\ufffd",
]]


def check_encoding(markdown_text: str) -> None:
    sep = "=" * 60
    print(f"\n{sep}")
    print("[ENCODING CHECK] Scanning for mojibake / Vietnamese encoding errors")
    print(f"{sep}")

    lines = markdown_text.splitlines()
    hits: list[tuple[int, str, str]] = []
    for lineno, line in enumerate(lines, 1):
        for pat in _COMPILED_MOJIBAKE:
            if pat.search(line):
                snippet = line.strip()[:100]
                hits.append((lineno, pat.pattern, snippet))
                break

    repl_count = markdown_text.count("\ufffd")
    if not hits and repl_count == 0:
        print("  [OK] No mojibake patterns detected.")
    else:
        print(f"  [WARNING] {len(hits)} line(s) with suspected encoding issues:")
        for lineno, pat, snippet in hits[:20]:
            print(f"    Line {lineno:5d} | pattern={pat!r} | {snippet!r}")
        if len(hits) > 20:
            print(f"    ... and {len(hits)-20} more lines (truncated)")
        if repl_count:
            print(f"  [WARNING] Unicode REPLACEMENT CHARACTER (U+FFFD) found {repl_count} time(s)")


# ─── MAIN ────────────────────────────────────────────────────────────────────

def main() -> None:
    pdf_path = get_input_pdf()
    print(f"\n[INFO] Tool     : Docling DocumentConverter (default pipeline)")
    print(f"[INFO] Input    : {pdf_path.name}")

    markdown_text, out_path = convert_pdf_to_markdown(pdf_path)

    # 1. Character count
    char_count = len(markdown_text)
    sep = "=" * 60
    print(f"\n{sep}")
    print(f"[STATS] Total characters in Markdown output: {char_count:,}")
    print(f"{sep}")

    # 2. Heading count (KEY metric)
    hinfo = count_headings(markdown_text)
    report_headings(hinfo)

    # 3 & 4. Table detection + malformed warnings
    tables = detect_tables(markdown_text)
    report_tables(tables)

    # 5. Encoding check
    check_encoding(markdown_text)

    print(f"\n[DONE] Output saved to: {out_path}")


if __name__ == "__main__":
    main()
