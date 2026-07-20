# AGENTS.md — Luật bắt buộc cho AI coding agent

> File này áp dụng cho MỌI agent code trong repo này: Antigravity, Cursor,
> GitHub Copilot, Claude Code, hay bất kỳ tool nào khác. Nếu tool của bạn hỗ
> trợ đọc `AGENTS.md` / `CLAUDE.md` / `.cursorrules` tự động — nó sẽ tự nạp
> file này. Nếu không, **dán nội dung file này vào đầu system prompt** trước
> khi giao việc.

## 0. Nguồn sự thật duy nhất

`docs/RESEARCH_FOUNDATION.md` là **nguồn sự thật duy nhất (single source of
truth)** cho kiến trúc. Bảng "ĐƯỢC / KHÔNG ĐƯỢC thay đổi" ở cuối file đó là
tối thượng.

## 1. Quy tắc bắt buộc trước khi sửa bất kỳ file nào trong `src/`

Trước khi thêm module, đổi thuật toán, đổi cách chunk, đổi cách rerank, đổi
embedding model, hoặc đổi cách fusion — agent PHẢI:

1. Đọc `docs/RESEARCH_FOUNDATION.md`.
2. Trả lời được câu hỏi: **"Thay đổi này thay thế hay bổ sung cho dòng nào
   trong bảng tóm tắt?"**
3. Nếu không map được vào dòng nào → **DỪNG LẠI**, không tự triển khai, in
   ra: `"NGOÀI PHẠM VI ĐÃ CHỐT — cần duyệt trước khi làm"` kèm lý do.
4. Nếu đổi 1 trong các dòng "Đã khóa" mà có paper gốc (RAG, BM25+Dense, RRF)
   → bắt buộc phải có **benchmark Recall@K/MRR từ `benchmark/benchmark_fixes.py`
   chứng minh cách mới tốt hơn trên corpus hiện tại**. Không có số liệu =
   không được đổi.
5. Nếu đổi 1 trong các dòng "đóng góp riêng, không có paper" (hierarchical
   chunking, context expansion, patient injection, deterministic calc) →
   vẫn phải hỏi lại người dùng, vì đây là phần luận điểm riêng của đồ án,
   không phải chỗ để "tối ưu theo best practice chung chung".

## 2. Cấm tuyệt đối (không cần hỏi, cứ từ chối làm)

- Không được để **patient JSON bị index vào vector corpus** (mục 8). Đây là
  nguyên tắc an toàn, không phải chỗ tối ưu chi phí/token.
- Không được để **LLM tự tính số liệu y khoa** (liều lượng, eGFR, ngưỡng
  cảnh báo...) — mọi phép tính phải qua `src/deterministic.py` (mục 9).
- Không được bỏ qua bước retrieval trong bất kỳ luồng nào, kể cả tính năng
  mới thêm sau này (quiz, flashcard...) (mục 1).
- Không được cộng điểm BM25 + vector trực tiếp (thang đo khác nhau về mặt
  toán học) — chỉ dùng RRF theo rank (mục 3).
- Không được để cross-encoder chạy trên toàn bộ candidates thay vì top-N từ
  RRF, và không được để hệ thống crash nếu cross-encoder load lỗi — phải có
  fallback về RRF-only (mục 4).

## 3. Khi không chắc

Agent không được "đoán rồi báo cáo sau". Nếu mơ hồ → hỏi lại người dùng
bằng câu hỏi cụ thể, KHÔNG tự chọn phương án rồi implement.

## 4. Cách kiểm tra mình có đang tuân thủ không

Chạy `pytest tests/test_contract.py`. Đây là bộ test bất biến kiến trúc —
nếu sửa code mà làm fail các test này, gần như chắc chắn bạn đã vi phạm một
dòng "Đã khóa". Đọc lại thông báo lỗi, nó sẽ trỏ thẳng tới mục tương ứng
trong `RESEARCH_FOUNDATION.md`.
