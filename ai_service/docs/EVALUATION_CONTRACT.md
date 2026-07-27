# EVALUATION CONTRACT

Bản hợp đồng này định nghĩa các giới hạn và quy tắc đánh giá hệ thống ở Phase 5. Phase 5 đóng vai trò là "Thẩm định viên độc lập".

## 1. Quyền Truy Cập (Read-Only Policy)
- Toàn bộ script trong thư mục `benchmark/` và `tests/` của Phase 5 **chỉ được phép đọc (read)** API của các class thuộc Phase 1–4.
- **TUYỆT ĐỐI CẤM**:
  - Ghi đè phương thức (Monkey Patching) làm sai lệch logic thực của hệ thống.
  - Sửa đổi cấu trúc Pipeline.
  - Sửa đổi tham số mặc định của Reranker hoặc Context Builder.

## 2. Tiêu Chí Khách Quan (No LLM-As-A-Judge)
- Đánh giá chất lượng Retrieval bằng Toán học: **Recall@K, MRR, Hit Rate**.
- Đánh giá chất lượng Generation bằng So khớp: So sánh sự tồn tại của `expected_answer_keywords` trong Answer text. 
- Không dùng một Prompt đưa cho GPT-4 bảo "Chấm điểm từ 1 đến 10". Dữ liệu y khoa cần độ chính xác 0 hoặc 1 (Đúng hoặc Sai).

## 3. Tính Toàn Vẹn Của Trích Dẫn (Citation Integrity)
- Phase 5 phải thẩm định ngược mã Citation `[Source X]`. 
- Một báo cáo Generation chỉ được tính là PASS nếu 100% `[Source X]` ánh xạ được về `ChunkNode` và có `Source URL` đính kèm. 

## 4. Xử Lý Khi Lỗi (Error Analysis)
- Nếu một câu hỏi rớt bài test (Miss ID), không được chỉ văng Log "Fail". Phải xuất ra `reports/error_analysis.json` ghi rõ ID lẽ ra phải tìm thấy, nhưng lại tìm ra ID nào khác.
