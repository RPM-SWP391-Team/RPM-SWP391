# GENERATION CONTRACT

Bản hợp đồng kỹ thuật (Contract) này định nghĩa các giới hạn bắt buộc cho Client giao tiếp với LLM và cơ chế Parse kết quả.

## 1. Giao Tiếp Mô Hình (LLM Client)
- **Protocol Based**: Bắt buộc triển khai thông qua Protocol `LLMClient`. Không hardcode sự phụ thuộc chặt chẽ vào thư viện OpenAI, Google hay Anthropic SDK bên trong ruột tầng Orchestration.
- **Trách Nhiệm Đơn Lẻ**: Interface duy nhất để gọi là `generate(prompt: str)`. Không được truyền `ExpandedContext` hay `PatientData` vào hàm này bắt model tự parse. Mọi thứ đã phải là chuỗi String chuẩn hóa do Prompt Builder xây dựng.
- **Retry Policy**: Cơ chế Retry (thử lại khi lỗi mạng, limit, timeout) phải nằm chìm bên trong Implementation của `LLMClient`. Tầng RAG không biết về mạng.

## 2. Response Parsing & Mapping
- Chịu trách nhiệm dịch output nguyên thủy của LLM thành một đối tượng Response cuối cùng.
- **Citation Mapping**: `ResponseParser` lấy Output String (chứa `[Source X]`) và kết hợp với `CitationMap` (từ Prompt Builder) để sinh ra mảng Sources chi tiết, chứa link URL/References thật.

## 3. Cấm Tự Sinh (No Advanced AI Injection)
- **Không suy nghĩ lại (No Reflection / Self-RAG)**: Phải xuất ngay kết quả sau 1 lần gọi (Single-pass generation). Không tạo vòng lặp LLM.
- **Không công cụ ngoài (No Tool Usage)**: Không thiết lập Functions/Tools (Calculation, Search Web) tại bước này. Hệ thống được yêu cầu là Deterministic RAG.
