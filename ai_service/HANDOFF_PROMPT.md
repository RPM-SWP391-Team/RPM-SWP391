# Medical RAG System - Project Handoff Prompt

Chào AI Assistant,

Dưới đây là toàn bộ bối cảnh của dự án **Vietnamese Medical RAG System** mà tôi và người dùng đã phát triển. Hãy đọc kỹ để nắm bắt kiến trúc và tiếp tục công việc nhé.

## 1. Tổng quan dự án (Project Overview)
- **Mục tiêu**: Xây dựng hệ thống RAG (Retrieval-Augmented Generation) chuyên ngành Y tế bằng tiếng Việt, giúp bác sĩ tra cứu phác đồ điều trị từ các file PDF y khoa (ví dụ: hướng dẫn điều trị tiểu đường, tim mạch).
- **Trạng thái**: Đã hoàn thành từ Phase 0 đến Phase 8 (Refactor code, Indexing, RAG Pipeline, Tích hợp Gemini LLM, và Framework Đánh giá Benchmark).

## 2. Kiến trúc cốt lõi (Core Architecture)
Hệ thống tuân thủ nghiêm ngặt các quy tắc kiến trúc (được khóa cứng tại `src/config.py` và `RESEARCH_FOUNDATION.md`):
- **Chunking**: Cắt theo tiêu đề (Hierarchical Heading) từ file PDF (xử lý tại `src/pdf_parser.py` và `src/chunking.py`).
- **Retrieval**: Hybrid Search kết hợp:
  - Sparse: `BM25` (dùng `rank_bm25`).
  - Dense: `FAISS` Vector Index (sử dụng mô hình `AITeamVN/Vietnamese_Embedding_v2`).
- **Fusion & Reranking**: Dùng `RRF` (Reciprocal Rank Fusion) để gộp kết quả, sau đó Rerank bằng Cross-Encoder (`BAAI/bge-reranker-v2-m3`). Bắt buộc fallback về RRF nếu model lỗi.
- **Context Builder**: Mở rộng ngữ cảnh theo thứ tự: *Retrieved -> Parent -> Prev -> Next -> Sibling*. Chặn số token tối đa.
- **LLM Client**: Đã tích hợp `google-generativeai` (Gemini 1.5 Flash) tại `src/llm_client.py`, load API Key từ file `.env`.

## 3. Vấn đề hiện tại cần bạn (AI mới) giải quyết (Current Issues)
Hệ thống đang gặp một "nút thắt cổ chai" (bottleneck) ở tiến trình `build_index.py` trên môi trường Windows CPU:
- **Lỗi đóng băng (Deadlock)**: Khi chạy `SentenceTransformer('AITeamVN/Vietnamese_Embedding_v2')` để mã hóa (encode) 2594 chunks, tiến trình bị treo cứng ở đoạn `self._model.encode()`. Đã thử set `OMP_NUM_THREADS=1` và `TOKENIZERS_PARALLELISM=false` nhưng vẫn treo.
- **Nhiệm vụ 1**: Khắc phục lỗi freeze của PyTorch/SentenceTransformers trên Windows CPU (hoặc hướng dẫn người dùng chạy trên môi trường có GPU CUDA/Linux).
- **Nhiệm vụ 2**: Chạy thành công lệnh `python build_index.py` để tạo ra `data/vector.index` và `data/bm25.pkl`.

## 4. Benchmark & Đánh giá (Evaluation)
- Bộ dataset chuẩn bị sẵn: `evaluation/diabetes_clinical_candidates.csv` (có cột `question` và `answer`).
- Mã nguồn chạy đánh giá: `benchmark/run_benchmark.py`.
- **Nhiệm vụ 3**: Sau khi Index được tạo thành công, chạy lệnh:
  `python benchmark/run_benchmark.py --dataset evaluation/diabetes_clinical_candidates.csv`
  để chấm điểm hệ thống bằng metrics BLEU và ROUGE.

## 5. Bước tiếp theo (Next Steps)
- Nếu Pipeline và Benchmark chạy tốt, hãy bắt đầu **Phase 9**: Đóng gói API (ví dụ dùng `FastAPI`) hoặc xây dựng giao diện UI (ví dụ dùng `Streamlit` hoặc `Gradio`) để bác sĩ có thể dễ dàng tải file PDF và chat với hệ thống.

---
**Yêu cầu dành cho bạn:** Đừng phá vỡ các cấu trúc đã có trong `src/config.py`. Hãy bắt đầu bằng việc kiểm tra file `src/embedder.py` và `build_index.py` để sửa lỗi treo CPU. Chúc may mắn!
