# Vietnamese Medical RAG — Khung kiến trúc chống "AI bịa"

## Vấn đề khung này giải quyết

Khi giao việc cho AI coding agent (Antigravity, Cursor...), nếu chỉ đưa
`RESEARCH_FOUNDATION.md` làm ngữ cảnh, agent vẫn có thể "quên" giữa các
lượt chat dài, hoặc tự diễn giải sai. Khung này biến các quyết định kiến
trúc từ *văn bản mô tả* thành *code + test chạy được* — khó bịa hơn nhiều.

## Cấu trúc

```
vn_medical_rag/
├── AGENTS.md                    ← Antigravity/Cursor/Copilot tự đọc file này
├── docs/RESEARCH_FOUNDATION.md  ← nguồn sự thật gốc (file bạn đưa)
├── src/
│   ├── config.py                 ← MỌI hằng số kiến trúc, 1 chỗ duy nhất
│   ├── retriever.py               ← mục 2: hybrid BM25 + dense
│   ├── fusion.py                  ← mục 3: RRF (công thức chuẩn)
│   ├── reranker.py                ← mục 4: cross-encoder + fallback
│   ├── chunking.py                ← mục 6: hierarchical heading chunking
│   ├── context_builder.py         ← mục 7: small-to-big expansion
│   ├── patient_context.py         ← mục 8: injection, KHÔNG index
│   ├── deterministic.py           ← mục 9: LLM không tự tính số
│   └── pipeline.py                ← nối đúng thứ tự, nơi DUY NHẤT quyết định flow
├── benchmark/benchmark_fixes.py  ← mục 10: Recall@K/MRR
└── tests/test_contract.py        ← chạy pytest để BẮT vi phạm kiến trúc
```

## Cách dùng với Antigravity

1. Copy cả thư mục `vn_medical_rag/` vào workspace của Antigravity.
2. Trong prompt đầu tiên, nói rõ: *"Đọc AGENTS.md và docs/RESEARCH_FOUNDATION.md
   trước khi đề xuất bất kỳ thay đổi nào."*
3. Sau mỗi lần agent sửa code, chạy:
   ```
   pytest tests/test_contract.py -v
   ```
   Nếu fail → agent đã vi phạm 1 dòng "Đã khóa". Không tự sửa test cho
   pass, hỏi lại agent lý do và yêu cầu benchmark nếu cần.
4. Phần đánh dấu `NotImplementedError("TODO...")` là chỗ CẦN code thật —
   đây là những chỗ khung không tự bịa hộ bạn (vì cần dữ liệu/corpus thật
   của bạn: công thức eGFR chính xác từ KDIGO 2024, parser heading theo
   định dạng docx cụ thể...).

## Những gì khung này KHÔNG làm hộ bạn

- Không tự implement BM25/vector index thật (cần chọn thư viện: rank_bm25,
  FAISS/Qdrant/Milvus...).
- Không tự tải model `bge-reranker-v2-m3` hay `Vietnamese_Embedding_v2`.
- Không tự viết công thức y khoa chính xác (CKD-EPI, ngưỡng Metformin) —
  cố tình để trống (`NotImplementedError`) để bạn/agent phải tra đúng số
  từ ADA 2025/KDIGO 2024, không hardcode từ trí nhớ mô hình.

Đây là chủ đích: những chỗ "khóa cứng bằng test" (RRF formula, thứ tự
expansion, patient không index, LLM không tự tính) là chỗ agent hay bịa
nhất khi không có ràng buộc — nên được bảo vệ bằng test. Những chỗ cần dữ
liệu thật thì để trống rõ ràng, không giả vờ implement.
