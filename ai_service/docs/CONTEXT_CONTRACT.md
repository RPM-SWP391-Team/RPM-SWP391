# CONTEXT BUILDER CONTRACT

Bản hợp đồng kỹ thuật (Contract) này định nghĩa các giới hạn bắt buộc cho tầng Context Expansion (Phase 3).

## 1. Single Responsibility Principle (SRP)
- **Context Builder CHỈ MỞ RỘNG (Expand)**: Nhận danh sách candidates đã rerank, sử dụng quan hệ cây (parent, prev, next, sibling) để gộp thêm thông tin, trả về `ExpandedContext`.
- **TUYỆT ĐỐI CẤM**:
  - Không can thiệp vào điểm số, không đổi thứ tự gốc của mảng kết quả.
  - Không gọi LLM, không sinh Prompt.
  - Không đọc FAISS/BM25. Mọi tra cứu chunk phụ phải thông qua `ChunkStore`.

## 2. Thứ Tự Mở Rộng Cố Định
Nếu quyết định expand một node, thứ tự ưu tiên gộp văn bản phải tuân thủ nghiêm ngặt:
1. `retrieved` (Bản thân node đó)
2. `parent`
3. `prev`
4. `next`
5. `sibling`

## 3. Rủi Ro Lặp (Loops) và Token Overflow
- **Khử Lặp (Deduplication)**: Một node id KHÔNG BAO GIỜ được xuất hiện 2 lần trong Output. Phải duy trì cơ chế check trùng lặp (`seen_node_ids`).
- **Ngân Sách Token (Token Budget)**: Phải tính lượng token ước tính TRƯỚC KHI gộp. Nếu vượt ngưỡng cho phép, nhánh expansion đó bị cắt đứt lập tức.

## 4. Output Duy Nhất
- Đầu ra của `ContextBuilder` bắt buộc là `ExpandedContext`.
- Bắt buộc bảo tồn Metadata (`node_id`, `title_path`, `source`, `citation`) của từng khối ngữ cảnh độc lập.
- `ExpandedContext` là input duy nhất đi vào Phase 4 (Prompt Builder).

## 5. Determinism
- Gọi `expand` 1.000 lần với cùng input và cùng ChunkStore phải sinh ra đúng 1.000 kết quả giống hệt nhau (không sai 1 byte). Không dùng `random` hay các cấu trúc Set làm mất thứ tự tuần tự.
