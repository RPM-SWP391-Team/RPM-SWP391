# RERANKER CONTRACT

Bản hợp đồng kỹ thuật (Contract) này định nghĩa các giới hạn bắt buộc cho tầng Reranking (Phase 3). Mọi Implementation vi phạm hợp đồng này sẽ làm hỏng toàn bộ pipeline.

## 1. Single Responsibility Principle (SRP)
- **Reranker CHỈ SẮP XẾP LẠI (Re-order)**: Nhận `query` và `candidates`, trả về danh sách `candidates` đã được re-order.
- **TUYỆT ĐỐI CẤM**:
  - Không truy cập VectorDB (FAISS) hay KeywordDB (BM25).
  - Không tự thêm candidate mới ngoài danh sách truyền vào.
  - Không gộp nội dung (Context Expansion).
  - Không sinh câu hỏi phụ (Query Rewrite).

## 2. Immutability & Data Schema
- **Input Không Đổi**: Cấm sửa đổi (mutate) đối tượng `RetrievedChunk` đầu vào (bao gồm `score`, `metadata`, `text`).
- **Output Mới**: Trả về danh sách đối tượng mới `RerankedChunk`, trong đó chứa tham chiếu đến chunk gốc và `rerank_score`.
- `Rerank_score` CHỈ ĐƯỢC DÙNG ĐỂ SORT. Cấm normalize hoặc cộng gộp với `RetrievedChunk.score`.

## 3. Fallback Mechanism (Graceful Degradation)
- **Không bao giờ Crash**: Nếu mô hình OOM, load lỗi, CUDA lỗi, hoặc timeout, hàm `rerank` bắt buộc phải trả về chính danh sách đầu vào theo đúng thứ tự RRF gốc, kèm Log Cảnh báo. Hệ thống RAG phải sống sót dưới mọi tình huống.

## 4. Giao Tiếp & Dependency Injection
- Cấm khởi tạo model ngầm bên trong class. Mọi model (thường là qua `ModelLoader`) phải được inject từ composition root vào lúc khởi tạo `Reranker`.

Mọi Contract Tests liên quan sẽ bám sát văn bản này.
