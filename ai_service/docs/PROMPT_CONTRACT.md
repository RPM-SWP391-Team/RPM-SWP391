# PROMPT CONTRACT

Bản hợp đồng kỹ thuật (Contract) này định nghĩa các giới hạn bắt buộc cho tầng Prompt Builder (Phase 4). Mọi Implementation vi phạm hợp đồng này sẽ làm hỏng toàn bộ pipeline bảo mật Y tế.

## 1. Data Isolation (Cách ly Dữ liệu)
- **CẤM TUYỆT ĐỐI**: Không được import hay gọi bất kỳ hàm nào thuộc `Retriever`, `ChunkStore`, `VectorIndex`, hay `BM25` vào trong tầng Prompt. Prompt Builder chỉ được phép giao tiếp với `ExpandedContext` thuần túy.
- Dữ liệu Patient JSON **vĩnh viễn không được Index**. Nó chỉ đi qua ống dẫn trực tiếp vào Prompt Builder thông qua `PatientContextFormatter`.

## 2. Token Budget & Truncation
- Prompt Builder **KHÔNG ĐƯỢC TỰ ĐỘNG CẮT (TRUNCATE) CONTEXT**. 
- Nếu kích thước Prompt vượt quá Token Budget (được định nghĩa trong Config/Model), Prompt Builder phải Raise Exception. Việc gọt giũa độ dài Context là trách nhiệm của `ContextBuilder` ở Phase 3.

## 3. Cấu trúc Prompt Tiêu Chuẩn
Prompt phải tuân thủ nghiêm ngặt theo các block định sẵn, KHÔNG được trộn lẫn hỗn loạn:
1. **System Instruction**: Mục đích chung của Bot.
2. **Medical Constraints**: Khóa chặt tư duy (Không tính toán, Không hallucinate, Chuyển hướng nếu thiếu info).
3. **Patient Context Block**: Thông tin bệnh nhân đã được format chuẩn.
4. **Retrieved Context Block**: Kiến thức được RAG nạp vào.
5. **User Question**: Câu hỏi thực tế của bác sĩ/bệnh nhân.
6. **Output Format**: Yêu cầu định dạng (Ví dụ Markdown, trả Citation dạng [Source X]).

## 4. Citation Rules
- Tuyệt đối không chèn trực tiếp đường dẫn URL, Title Path vào ruột đoạn text để LLM tự parse.
- LLM chỉ nhìn thấy mã định danh chuẩn `[Source 1]`, `[Source 2]`. 
- Giao lại toàn bộ danh sách map cho Phase 4 xử lý hậu kỳ (Response Parser).
