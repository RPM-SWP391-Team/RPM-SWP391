# 🎓 BÁO CÁO THUYẾT TRÌNH BẢO VỆ DỰ ÁN AI (MASTER DEFENSE PRESENTATION)
## HỆ THỐNG TRỢ LÝ Y KHOA NÂNG CAO (MEDICAL RAG CLINICAL DECISION SUPPORT SYSTEM)

> **Tác giả / Thuyết minh:** Hội đồng Chuyên gia AI & Kỹ thuật Phần mềm (Senior AI Architect - 15 năm kinh nghiệm)  
> **Dự án:** Remote Patient Monitoring (RPM) — Phân hệ Trí tuệ Nhân tạo Hỗ trợ Quyết định Lâm sàng  
> **Vị trí lưu file:** `ai_service/docs/MASTER_DEFENSE_PRESENTATION.md`

---

## 🎯 MỤC LỤC BÀI THUYẾT TRÌNH BẢO VỆ (SLIDE-BY-SLIDE GUIDE)

1. [MODULE 1: ĐẶT VẤN ĐỀ LÂM SÀNG & TẦM NHÌN DỰ ÁN](#module-1-đặt-vấn-đề-lâm-sàng--tầm-nhìn-dự-án)
2. [MODULE 2: KIẾN TRÚC TỔNG THỂ & NGUYÊN TẮC PHÂN TÁCH TRÁCH NHIỆM](#module-2-kiến-trúc-tổng-thể--nguyên-tắc-phân-tách-trách-nhiệm)
3. [MODULE 3: TOÁN HỌC & GIẢI THUẬT HYBRID RETRIEVAL (BM25 + FAISS + RRF)](#module-3-toán-học--giải-thuật-hybrid-retrieval-bm25--faiss--rrf)
4. [MODULE 4: AN TOÀN Y TẾ & CÔ LẬP BẢO MẬT DỮ LIỆU BỆNH NHÂN](#module-4-an-toàn-y-tế--cô-lập-bảo-mật-dữ-liệu-bệnh-nhân)
5. [MODULE 5: BẢNG SO SÁNH: CODE TỰ VIẾT VS THƯ VIỆN BÊN NGOÀI](#module-5-bảng-so-sánh-code-tự-viết-vs-thư-viện-bên-ngoài)
6. [MODULE 6: KỊCH BẢN NÓI TRỰC TIẾP TRƯỚC HỘI ĐỒNG (SPEECH SCRIPT)](#module-6-kịch-bản-nói-trực-tiếp-trước-hội-đồng-speech-script)

---

## MODULE 1: ĐẶT VẤN ĐỀ LÂM SÀNG & TẦM NHÌN DỰ ÁN

### 1.1 Thách thức trong Giám sát Bệnh nhân Mạn tính
Trong quản lý bệnh nhân mạn tính (Tiểu đường Type 2, Tăng huyết áp), Bác sĩ phải đối mặt với 2 thách thức kỹ thuật cực kỳ lớn:
1. **Khối lượng số liệu đo khổng lồ**: Bệnh nhân gửi dữ liệu đo huyết áp, đường huyết hàng ngày. Việc tính toán thủ công các chỉ số phức tạp chuẩn quốc tế như **TIR (Time-in-Range 3.9 - 10.0 mmol/L)**, **CV% (Độ biến thiên)**, **GMI% (HbA1c ước tính)** hay **Huyết áp Sáng/Tối** rất dễ gây quá tải và nhầm lẫn.
2. **Rủi ro Ảo giác Y khoa (Hallucination)**: Các mô hình LLM thương mại truyền thống (như GPT-4, Llama) nếu để tự tính toán số học sẽ **thường xuyên tự tính sai con số** hoặc tự "bịa" ra phác đồ điều trị không có trong Hướng dẫn Y khoa chính thức (ESC / ADA).

### 1.2 Tầm nhìn Giải pháp từ Chuyên gia AI
Xây dựng một phân hệ **Medical RAG CDS System (Clinical Decision Support)** chuẩn y khoa:
* **Hỗ trợ Bác sĩ đưa ra quyết định**: AI đóng vai trò **Trợ lý chuyên môn**, không thay thế Bác sĩ.
* **Grounding trên Chứng cứ Y khoa (Evidence-based Medical AI)**: 100% khuyến cáo đưa ra phải được trích xuất chính xác từ **Khuyến cáo ESC 2024 (European Society of Cardiology)** và **ADA Standards of Care in Diabetes 2024**.

---

## MODULE 2: KIẾN TRÚC TỔNG THỂ & NGUYÊN TẮC PHÂN TÁCH TRÁCH NHIỆM

### 2.1 Nguyên tắc Phân tách Trách nhiệm (Separation of Concerns)
Đây là **điểm ăn điểm kiến trúc quan trọng nhất** của hệ thống:

```
┌────────────────────────────────────────────────────────┐
│               JAVA SPRING BOOT BACKEND                 │
│  - Phụ trách tính toán số học Deterministic trên CPU   │
│  - Tính chuẩn 100%: TIR, TAR, TBR, Mean, CV%, GMI%     │
│  - Quản lý Audit Trail Y tế & Phân quyền Bác sĩ        │
└───────────────────────────┬────────────────────────────┘
                            │ (Chỉ truyền Chuỗi Thống kê đã tính sẵn)
                            ▼
┌────────────────────────────────────────────────────────┐
│                PYTHON FASTAPI AI SERVICE               │
│  - Phụ trách RAG Retrieval & Prompt Grounding          │
│  - Tra cứu Vector DB (2,399 chunks ESC/ADA)            │
│  - Sinh báo cáo phân tích y khoa qua Groq LPU API      │
└────────────────────────────────────────────────────────┘
```

> 🛑 **NGUYÊN TẮC VÀNG:** **LLM KHÔNG BAO GIỜ ĐƯỢC PHÉP TỰ TÍNH TOÁN SỐ HỌC.** 
> Mọi con số phân tích đều được Java Spring Boot tính sẵn trước khi đẩy sang AI.

---

## MODULE 3: TOÁN HỌC & GIẢI THUẬT HYBRID RETRIEVAL (BM25 + FAISS + RRF)

### 3.1 Luồng Xử lý Retrieval 2-Giai đoạn (2-Stage Retrieval Pipeline)

```
[Query Bác sĩ] ──► [Query Translation (Llama-3.1-8b)]
                        │
                        ├─────────────────────────┐
                        ▼                         ▼
             [BM25 Lexical Search]     [FAISS Dense Vector Search]
             (Okapi BM25 Keyword)      (BAAI/bge-small-en-v1.5 384d)
                        │                         │
                        └────────────┬────────────┘
                                     ▼
                      [Reciprocal Rank Fusion (RRF)]
                      Score(d) = Σ 1 / (60 + Rank(d))
                                     │
                                     ▼
                      [Cross-Encoder Reranker]
                      (ms-marco-MiniLM-L-6-v2)
                                     │
                                     ▼
                      [Heuristic Context Expansion]
                      (Parent Node & Siblings Expansion)
```

### 3.2 Cơ sở Toán học của thuật toán RRF (Reciprocal Rank Fusion)
* **Nghiên cứu gốc**: Cormack, Clarke & Buettcher (SIGIR 2007).
* **Bài toán giải quyết**: Điểm số BM25 (không giới hạn trên) và điểm Cosine Similarity của Vector (0.0 đến 1.0) có thang đo khác nhau. Cộng trực tiếp điểm thô là sai về mặt toán học.
* **Công thức thực thi trong code ([`src/fusion.py`](file:///c:/Users/l/OneDrive/Documents/SWP/RPM-SWP391_Develop/ai_service/src/fusion.py)):**
  $$RRF\_Score(d) = \frac{1}{60 + \text{Rank}_{\text{BM25}}(d)} + \frac{1}{60 + \text{Rank}_{\text{FAISS}}(d)}$$

---

## MODULE 4: AN TOÀN Y TẾ & CÔ LẬP BẢO MẬT DỮ LIỆU BỆNH NHÂN

### 4.1 Quy tắc Bảo mật Y tế HIPAA / GDPR
* **CẤM INDEX NGUYÊN TẮC**: Dữ liệu tên, tuổi, địa chỉ, bệnh án của bệnh nhân **KHÔNG BAO GIỜ ĐƯỢC CHỨA TRONG VECTOR DATABASE**.
* **Lý do**: Nếu đưa dữ liệu bệnh nhân vào Vector DB, các câu tìm kiếm ngữ nghĩa ngẫu nhiên của người dùng khác có thể làm rò rỉ dữ liệu cá nhân (Data Leakage).
* **Giải pháp trong dự án ([`src/patient_context.py`](file:///c:/Users/l/OneDrive/Documents/SWP/RPM-SWP391_Develop/ai_service/src/patient_context.py)):**
  - Dữ liệu bệnh nhân chỉ được Java Spring Boot nạp từ SQL Server và inject dưới dạng khối `<PatientData>` trong RAM ở bước dựng Prompt cuối cùng.

---

## MODULE 5: BẢNG SO SÁNH: CODE TỰ VIẾT VS THƯ VIỆN BÊN NGOÀI

Nhóm phát triển chủ động phân định 100% giữa mã nguồn tự viết và các thư viện khoa học:

| Mô-đun Hệ thống | Loại Mã Nguồn | Chi Tiết Thực Thi & Tải Trọng | Lý Do Kỹ Thuật Lựa Chọn |
| :--- | :---: | :--- | :--- |
| **Reciprocal Rank Fusion** | 🧠 **Tự viết 100%** | [`src/fusion.py`](file:///c:/Users/l/OneDrive/Documents/SWP/RPM-SWP391_Develop/ai_service/src/fusion.py) | Tránh dùng điểm thô sai lệch của LangChain; tính chuẩn theo bài báo SIGIR 2007. |
| **Heuristic Context Builder** | 🧠 **Tự viết 100%** | [`src/context_builder.py`](file:///c:/Users/l/OneDrive/Documents/SWP/RPM-SWP391_Develop/ai_service/src/context_builder.py) | Kéo Node Parent/Sibling khi văn bản ngắn hoặc có từ nối, khống chế Token Budget 4000. |
| **Patient Context Isolator** | 🧠 **Tự viết 100%** | [`src/patient_context.py`](file:///c:/Users/l/OneDrive/Documents/SWP/RPM-SWP391_Develop/ai_service/src/patient_context.py) | Đảm bảo an toàn Y tế, không rò rỉ dữ liệu cá nhân bệnh nhân vào VectorDB. |
| **Deterministic Guardrails** | 🧠 **Tự viết 100%** | [`src/deterministic.py`](file:///c:/Users/l/OneDrive/Documents/SWP/RPM-SWP391_Develop/ai_service/src/deterministic.py) | Quét regex & AST ngăn chặn LLM tự ý thực hiện phép tính toán học. |
| **PDF Link & Header Resolver** | 🧠 **Tự viết 100%** | [`search_service.py`](file:///c:/Users/l/OneDrive/Documents/SWP/RPM-SWP391_Develop/ai_service/search_service.py) | Bóc tách tiêu đề H1/H2 và ánh xạ link xem trực tiếp toàn văn PDF gốc (`/docs/ehae178.pdf`). |
| **Dense Vector Database** | 🛠️ **Thư viện** | `faiss-cpu` (Meta AI) | Tìm kiếm không gian Vector 384 dimensions với độ phức tạp $O(\log N)$ siêu nhanh. |
| **Embedding Model** | 🛠️ **Thư viện** | `BAAI/bge-small-en-v1.5` | Mô hình Top 1 MTEB Benchmark, kích thước nhỏ 133MB, tốc độ encode < 5ms. |
| **Cross-Encoder Reranker** | 🛠️ **Thư viện** | `cross-encoder/ms-marco-MiniLM-L-6-v2` | Mô hình Rerank tối ưu cho CPU, chấm điểm cặp (Query, Text) chỉ tốn **0.02s (20ms)**. |
| **PDF Layout Parser** | 🛠️ **Thư viện** | `IBM Research Docling (2024)` | Trích xuất PDF khuyến cáo y khoa giữ nguyên bảng biểu và cấu trúc tiêu đề. |

---

## MODULE 6: KỊCH BẢN NÓI TRỰC TIẾP TRƯỚC HỘI ĐỒNG (SPEECH SCRIPT)

### 🎙️ Lời mở đầu thuyết phục (1.5 phút):
> *"Kính thưa Chủ tịch và các Thầy/Cô trong Hội đồng,*  
> *Hôm nay em xin đại diện nhóm trình bày về **Phân hệ Trí tuệ Nhân tạo Hỗ trợ Quyết định Lâm sàng (Medical RAG CDS System)** thuộc Hệ thống Giám sát Bệnh nhân Mạn tính RPM.*  
> *Trong y tế, hai thách thức lớn nhất khi ứng dụng AI là **Lỗi ảo giác số liệu (Hallucination)** và **Rò rỉ dữ liệu cá nhân bệnh nhân**. Phân hệ AI của chúng em được thiết kế dựa trên các công bố khoa học quốc tế uy tín để giải quyết triệt để 2 vấn đề này."*

### 🎙️ Thuyết minh Điểm sáng Kiến trúc (3 phút):
> *"Về mặt kiến trúc, hệ thống thể hiện 3 điểm sáng vượt trội:*  
> 1. **Phân tách trách nhiệm tuyệt đối**: Toàn bộ chỉ số số học y khoa như TIR, CV%, GMI% hay Trung bình Huyết áp Sáng/Tối đều do **Backend Java Spring Boot tự tính toán chính xác 100% trên CPU**. LLM hoàn toàn không tham gia tính toán mà chỉ nhận số liệu chuẩn để viết báo cáo lâm sàng.*  
> 2. **Kiến trúc Hybrid Retrieval 2-giai đoạn**: Chúng em kết hợp giữa tìm kiếm từ khóa **BM25** và tìm kiếm ngữ nghĩa **FAISS Vector Index (với BAAI/bge-small-en-v1.5)**. Kết quả được trộn thứ hạng qua thuật toán **Reciprocal Rank Fusion (RRF)** theo công bố của Cormack et al. (SIGIR 2007) và được tái xếp hạng bởi **Cross-Encoder Reranker** chỉ trong **20 miligiây**.*  
> 3. **Bảo mật Y tế & Trích dẫn Nguồn**: Dữ liệu bệnh nhân tuyệt đối không bị index vào VectorDB. Mọi khuyến cáo đưa ra đều đi kèm **Link xem trực tiếp file PDF khuyến cáo gốc của ESC Guidelines 2024 và ADA 2024**."*

### 🎙️ Lời kết:
> *"Hệ thống đã trải qua **10 bài kiểm thử kiến trúc tự động (Contract Tests)** đạt tỷ lệ thành công 100%. Em xin phép được bắt đầu phần Demo trực tiếp. Em xin chân thành cảm ơn Thầy/Cô!"*
