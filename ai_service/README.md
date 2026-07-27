# Vietnamese Medical RAG — Khung kiến trúc chống "AI bịa"

## Vấn đề khung này giải quyết

Khi giao việc cho AI coding agent (Antigravity, Cursor...), nếu chỉ đưa
`RESEARCH_FOUNDATION.md` làm ngữ cảnh, agent vẫn có thể "quên" giữa các
lượt chat dài, hoặc tự diễn giải sai. Khung này biến các quyết định kiến
trúc từ *văn bản mô tả* thành *code + test chạy được* — khó bịa hơn nhiều.

## Cấu trúc hệ thống

```
ai_service/
├── AGENTS.md                    ← Luật bắt buộc cho AI coding agent
├── HANDOFF_PROMPT.md            ← Tài liệu bàn giao bối cảnh dự án
├── api.py                       ← FastAPI entrypoint cho AI Service (port 8000)
├── search_service.py            ← Phân hệ tra cứu phác đồ y khoa (Semantic Search)
├── summary_service.py           ← Phân hệ sinh báo cáo lâm sàng từ số liệu Java
├── build_index.py               ← Tiến trình tạo FAISS Vector Index & BM25 Index
├── docs/                        ← Tài liệu kiến trúc, Hợp đồng (Contracts) & Bảo vệ đồ án
│   ├── RESEARCH_FOUNDATION.md   ← Nguồn sự thật gốc cho toàn bộ kiến trúc
│   ├── CODE_DEEPDIVE_15YR_EXPERT.md
│   └── MASTER_DEFENSE_PRESENTATION.md
├── src/                         ← Core RAG Pipeline modules
│   ├── config.py                ← MỌI hằng số kiến trúc chuẩn hóa ở 1 chỗ duy nhất
│   ├── retriever.py              ← Hybrid Search (BM25 + FAISS Dense Vector)
│   ├── fusion.py                 ← Reciprocal Rank Fusion (RRF k=60)
│   ├── reranker.py               ← Cross-Encoder (ms-marco-MiniLM-L-6-v2) + Fallback
│   ├── chunking.py               ← Hierarchical Heading Chunking
│   ├── context_builder.py        ← Small-to-big Context Expansion (Parent/Sibling)
│   ├── patient_context.py        ← Patient Data Injection (KHÔNG index)
│   ├── deterministic.py          ← Deterministic Guardrails (LLM không tự tính toán)
│   ├── llm_client.py             ← LLM Client Interface (Gemini 2.5 Flash / Groq Llama 3.3)
│   └── medical_pipeline.py       ← Pipeline điều phối RAG y tế hoàn chỉnh
├── benchmark/                   ← Bộ công cụ đánh giá benchmark (Recall@K, MRR, BLEU, ROUGE)
│   └── run_benchmark.py
└── tests/
    └── test_contract.py         ← Test suite khóa cứng bất biến kiến trúc
```

## Cách dùng

1. Môi trường chạy FastAPI Service:
   ```bash
   py api.py
   ```
2. Kiểm tra tính toàn vẹn kiến trúc (Contract Tests):
   ```bash
   pytest tests/test_contract.py -v
   ```

## Thông số mô hình chuẩn hóa trong mã nguồn

- **Embedding Model**: `BAAI/bge-small-en-v1.5` (384 dimensions).
- **Reranker Model**: `cross-encoder/ms-marco-MiniLM-L-6-v2` (tối ưu CPU, top-5 final).
- **LLM Engine**: `gemini-2.5-flash` (gọi trực tiếp qua Google REST API) / `GroqLLMClient` (`llama-3.3-70b-versatile`).
- **Fusion Formula**: RRF $k=60$ ($Score = \sum 1 / (60 + Rank)$).
