# Medical RAG System - Project Handoff Prompt

Chào AI Assistant,

Dưới đây là toàn bộ bối cảnh của dự án **Vietnamese Medical RAG System** mà tôi và người dùng đã phát triển. Hãy đọc kỹ để nắm bắt kiến trúc và tiếp tục công việc nhé.

## 1. Tổng quan dự án (Project Overview)
- **Mục tiêu**: Xây dựng hệ thống RAG (Retrieval-Augmented Generation) chuyên ngành Y tế bằng tiếng Việt, giúp bác sĩ tra cứu phác đồ điều trị từ các file PDF y khoa (ví dụ: hướng dẫn điều trị tiểu đường, tim mạch).
- **Trạng thái**: Đã hoàn thành từ Phase 0 đến Phase 8 (Refactor code, Indexing, RAG Pipeline, Tích hợp Gemini LLM, và Framework Đánh giá Benchmark).

## 2. Kiến trúc cốt lõi (Core Architecture)
Hệ thống tuân thủ nghiêm ngặt các quy tắc kiến trúc (được khóa cứng tại `src/config.py` và `RESEARCH_FOUNDATION.md`):
- **Chunking**: Phân tách theo tiêu đề (Hierarchical Heading) từ file PDF khuyến cáo y khoa.
- **Retrieval**: Hybrid Search kết hợp:
  - Sparse: `BM25` (dùng `rank_bm25`).
  - Dense: `FAISS` Vector Index (sử dụng mô hình `BAAI/bge-small-en-v1.5`, 384 dimensions).
- **Fusion & Reranking**: Dùng `RRF` (Reciprocal Rank Fusion k=60) để gộp kết quả, sau đó Rerank bằng Cross-Encoder (`cross-encoder/ms-marco-MiniLM-L-6-v2`). Bắt buộc fallback về RRF nếu model lỗi.
- **Context Builder**: Mở rộng ngữ cảnh theo thứ tự: *Retrieved -> Parent -> Prev -> Next -> Sibling*. Chặn số token tối đa `token_budget = 4000`.
- **LLM Client**: Tích hợp REST Client tương thích `gemini-2.5-flash` (và `GroqLLMClient` cho Llama-3.3-70b) tại `src/llm_client.py`, load API Key từ file `.env`.

## 3. Trạng thái Vận hành hiện tại (Current Status)
- Service backend FastAPI đã được đóng gói sẵn sàng tại `api.py` (port 8000), cung cấp các endpoint `/api/chat`, `/api/search`, và `/api/summary`.
- Phân hệ Tra cứu Phác đồ (Semantic Search) đã được kết nối với Java Spring Boot Backend qua REST API (`WebConfig.java`).

## 4. Benchmark & Đánh giá (Evaluation)
- Dataset đánh giá: `evaluation/diabetes_clinical_candidates.csv` (có cột `question` và `answer`).
- Mã nguồn chạy đánh giá: `benchmark/run_benchmark.py`.
- Lệnh chạy đánh giá hệ thống:
  ```bash
  python benchmark/run_benchmark.py --dataset evaluation/diabetes_clinical_candidates.csv --top-k 5
  ```
  để chấm điểm hệ thống bằng các chỉ số Recall@K, MRR, BLEU và ROUGE.

## 5. Bước tiếp theo (Next Steps)
- Nếu Pipeline và Benchmark chạy tốt, hãy bắt đầu **Phase 9**: Đóng gói API (ví dụ dùng `FastAPI`) hoặc xây dựng giao diện UI (ví dụ dùng `Streamlit` hoặc `Gradio`) để bác sĩ có thể dễ dàng tải file PDF và chat với hệ thống.

---
**Yêu cầu dành cho bạn:** Đừng phá vỡ các cấu trúc đã có trong `src/config.py`. Hãy bắt đầu bằng việc kiểm tra file `src/embedder.py` và `build_index.py` để sửa lỗi treo CPU. Chúc may mắn!
