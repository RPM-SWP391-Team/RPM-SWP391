# REGRESSION TESTING GUIDE

Tài liệu này hướng dẫn quy trình chạy test hồi quy (Regression Testing) để phát hiện sự tụt lùi (regression) của hệ thống sau các đợt cập nhật.

## 1. Khi Nào Phải Chạy Regression Test?
BẮT BUỘC chạy `python benchmark/benchmark_pipeline.py` (hoặc qua CI/CD) khi:
- Thay đổi cấu hình Prompt (System Prompt, Patient Format).
- Nâng cấp phiên bản thư viện (FAISS, SentenceTransformers).
- Thay đổi thuật toán Context Expansion hoặc công thức RRF.
- Cập nhật bộ dữ liệu Chunks (Build Index lại).

## 2. Tiêu Chuẩn Vượt Qua (Acceptance Thresholds)
Hệ thống sử dụng các mốc tham chiếu cứng từ `evaluation/config.json`. Ví dụ mặc định:
- **Recall@5** >= 0.85 (85% cơ hội tìm thấy đoạn văn đúng trong Top 5).
- **MRR** >= 0.75 (Tài liệu đúng thường nằm ở vị trí thứ 1 hoặc thứ 2).
- **Average Latency** <= 2000 ms.
- **Citation Coverage** >= 95%.

## 3. Nếu Test Thất Bại (FAIL)
- CI/CD hoặc lệnh test sẽ trả về `exit code 1`.
- Không được bypass (bỏ qua) bài test.
- Kiểm tra `reports/error_analysis.json` để xác định chính xác câu hỏi nào không lấy được thông tin, hoặc độ trễ khâu nào (Vector vs BM25) bị kéo dài.

## 4. Determinism
- Một bộ test cực đoan sẽ chạy liên tục 100 lần 1 truy vấn. Nếu có 1 lần kết quả trả về bị sai lệch -> Thuật toán có tính ngẫu nhiên (Vượt rào Architecture). Test sẽ văng lỗi.
