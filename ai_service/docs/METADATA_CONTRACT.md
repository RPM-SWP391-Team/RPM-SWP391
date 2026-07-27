# Metadata Contract cho Hierarchical Chunking (Vietnamese Medical RAG)

Tài liệu này định nghĩa cấu trúc dữ liệu bắt buộc (Contract) cho mỗi `ChunkNode` sau khi đi qua quá trình phân tách (Chunking). Định dạng này phải được tuân thủ nghiêm ngặt để đảm bảo sự ổn định cho các thành phần phía sau (Retrieval, Context Builder) và tương thích hoàn toàn khi Serialize/Deserialize vào Vector DB.

## Cấu trúc JSON chuẩn

Mọi ChunkNode khi lưu trữ vào hệ thống Vector DB sẽ có cấu trúc JSON như sau:

```json
{
  "id": "node_id_123",
  "text": "Nội dung văn bản (đã bao gồm các heading nhỏ gộp vào nếu có)...",
  "metadata": {
    "title_path": ["Chương 1", "1.1 Bệnh học"],
    "chunk_type": "text",
    "parent_node_id": "node_id_456",
    "previous_sibling_node": "node_id_789",
    "next_sibling_node": "node_id_012",
    "covered_node_ids": ["node_id_con_1", "node_id_con_2"],
    
    "source_document": "ADA_2025.pdf",
    "guideline_version": "2025",
    "organization": "ADA",
    "language": "vi",
    "page_number": 12,
    "effective_date": "2025-01-01",
    "source_url": "https://diabetes.org/guidelines/2025",
    "chunk_hash": "a1b2c3d4..."
  }
}
```

## Giải thích chi tiết các trường

### 1. Thông tin cốt lõi
- `id` (string): Node ID. Đây là định danh duy nhất (Deterministic) của khối text. Thuật toán tạo ID: `MD5(source_document + title_path + chunk_index_in_section)`.
- `text` (string): Nội dung văn bản thực tế để nhúng (Embedding) và giao cho LLM. Nếu là kết quả của Aggregation, text này có thể chứa thêm nội dung của các heading con.

### 2. Định tuyến cấu trúc (Hierarchy Routing)
Các trường này bắt buộc phải có để `ContextBuilder` (`src/context_builder.py`) có thể khôi phục lại ngữ cảnh rộng hơn:
- `title_path` (array of strings): Mảng chứa chuỗi tiêu đề từ root đến chunk hiện tại. VD: `["Phần 1: Chẩn đoán", "1.1 Xét nghiệm máu"]`. Dùng để làm Citation (Trích dẫn) sau này.
- `chunk_type` (string): Một trong các giá trị `["text", "table", "formula", "code", "image"]`. Giúp UI render đúng định dạng (VD: không cắt ghép lung tung đối với table).
- `parent_node_id` (string | null): ID của chunk chứa heading cha trực tiếp. Bắt buộc có nếu `title_path` có độ dài > 1.
- `previous_sibling_node` (string | null): ID của chunk trước đó cùng cấp heading.
- `next_sibling_node` (string | null): ID của chunk tiếp theo cùng cấp heading.
- `covered_node_ids` (array of strings): Mảng chứa các ID của những chunk con (Leaf nodes) đã bị gộp (Merge) vào chunk này do quá trình Chunk Aggregation (vì chúng < `min_chunk_chars`).

### 3. Thông tin Nguồn (Provenance & Filtering)
Cực kỳ quan trọng trong Medical RAG. Dùng để cấu hình Pre-filtering trên Vector DB trước khi tìm kiếm semantic.
- `source_document` (string): Tên file gốc hoặc định danh tài liệu.
- `guideline_version` (string): Năm hoặc phiên bản phác đồ (VD: "2025"). Rất quan trọng để loại bỏ phác đồ cũ.
- `organization` (string): Tổ chức ban hành (VD: "ADA", "KDIGO", "Bộ Y Tế").
- `language` (string): Ngôn ngữ của chunk (VD: "vi", "en").
- `page_number` (integer | null): Số trang bắt đầu của chunk trong tài liệu gốc. Dùng để UI dẫn link chính xác đến trang PDF.
- `effective_date` (string): Ngày bắt đầu có hiệu lực (Format `YYYY-MM-DD`). 
- `source_url` (string): URL gốc (nếu có) để bác sĩ có thể click vào đối chiếu nguồn gốc.

### 4. Quản lý Vòng đời (Lifecycle Management)
- `chunk_hash` (string): Mã băm được sinh ra bằng `SHA-256(text + JSON(quan_trọng_metadata))`. Dùng để phát hiện Content Drift. Nếu re-index mà `Node ID` tồn tại nhưng `chunk_hash` thay đổi, hệ thống sẽ thực hiện thao tác Update thay vì Insert mới.

## Quy định thay đổi
Mọi thay đổi cấu trúc Metadata này bắt buộc phải review qua file `AGENTS.md` và `RESEARCH_FOUNDATION.md`, không tự ý thêm bớt các trường Routing làm vỡ cơ chế Context Expansion.
