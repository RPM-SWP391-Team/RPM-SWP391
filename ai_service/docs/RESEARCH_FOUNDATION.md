# Research Foundation — Vietnamese Medical RAG System

**Mục đích của tài liệu này:** đây là điểm neo (anchor) cho toàn bộ kiến trúc.
Mỗi thành phần trong hệ thống được gắn với 1 nguồn nghiên cứu cụ thể (hoặc
được đánh dấu rõ là "quyết định kiến trúc riêng — không có paper"). Từ nay:

> **Bất kỳ thay đổi kiến trúc nào (thêm module, đổi thuật toán, đổi cách
> chunk, đổi cách rerank...) đều phải trả lời được câu hỏi: "Nó thay thế
> hay bổ sung cho dòng nào trong bảng dưới đây?" Nếu không trả lời được —
> DỪNG LẠI, hỏi lại, không tự ý thêm.**

Quy tắc cho Antigravity: đọc tài liệu này trước khi đề xuất bất kỳ thay đổi
kiến trúc nào. Nếu thay đổi không nằm trong phạm vi đã chốt ở đây, phải nêu
rõ trong phần trả lời "Đây là thay đổi NGOÀI phạm vi đã chốt, cần anh/chị
duyệt trước khi làm" — không tự triển khai rồi báo cáo sau.

---

## 1. Kiến trúc tổng thể — Retrieval-Augmented Generation (RAG)

**Nguồn gốc:** Lewis et al., "Retrieval-Augmented Generation for
Knowledge-Intensive NLP Tasks", NeurIPS 2020.

**Nguyên tắc cốt lõi được áp dụng:** thay vì để LLM trả lời hoàn toàn từ
tri thức nội tại (dễ hallucinate), hệ thống truy xuất đoạn văn bản liên
quan từ nguồn đáng tin cậy (ADA 2025, KDIGO 2024) rồi mới đưa vào prompt
sinh câu trả lời. Đây là lý do rule 1 trong prompt của bạn cấm dùng kiến
thức ngoài ngữ cảnh — đúng tinh thần gốc của RAG, không phải tùy tiện.

**Đã khóa:** hệ thống PHẢI luôn grounding trên retrieved context, không
được bỏ qua bước retrieval trong bất kỳ luồng nào (kể cả quiz/flashcard
nếu sau này mở rộng).

---

## 2. Retrieval — Hybrid BM25 + Dense Vector Search

**Nguồn gốc:**
- BM25: Robertson & Zaragoza, "The Probabilistic Relevance Framework:
  BM25 and Beyond", Foundations and Trends in Information Retrieval, 2009
  — lexical/keyword matching, mạnh với thuật ngữ y khoa chính xác
  (tên thuốc, chỉ số eGFR...).
- Dense retrieval: Karpukhin et al., "Dense Passage Retrieval for
  Open-Domain Question Answering", EMNLP 2020 (DPR) — semantic matching,
  mạnh khi câu hỏi diễn đạt khác từ ngữ so với tài liệu gốc.

**Vì sao kết hợp cả hai (không chỉ dùng 1 loại):** BM25 và dense retrieval
bù trừ điểm yếu cho nhau — BM25 yếu khi câu hỏi paraphrase, dense yếu khi
cần khớp chính xác thuật ngữ/số liệu. Đây là lý do `retriever.py` có cả
`bm25_search` và `vector_search`.

**Đã khóa:** không thay 1-trong-2 bằng cách khác trừ khi có benchmark
Recall@K chứng minh cách mới tốt hơn trên chính corpus của bạn.

---

## 3. Fusion — Reciprocal Rank Fusion (RRF)

**Nguồn gốc:** Cormack, Clarke, Büttcher, "Reciprocal Rank Fusion
Outperforms Condorcet and Individual Rank Learning Methods", SIGIR 2009.

**Vì sao dùng RRF thay vì cộng điểm trực tiếp (bm25_score + vector_score):**
hai loại điểm có thang đo khác nhau (BM25 không chặn trên, cosine similarity
trong [-1,1]) — cộng trực tiếp sai về mặt toán học. RRF chỉ dùng **rank**
(thứ hạng), không cần normalize điểm, đã được paper gốc chứng minh vượt
trội hơn CombMNZ và các phương pháp rank-learning khác trên TREC.

**Công thức đã áp dụng đúng chuẩn** trong `reranker.py`:
```
score(d) = Σ 1 / (k + rank(d))    với k = 60 (hằng số khuyến nghị phổ biến)
```

**Đã khóa:** RRF là bước fusion CỐ ĐỊNH giữa BM25 và vector — không thay
bằng weighted-sum hay learned fusion trừ khi có lý do benchmark cụ thể.

---

## 4. Reranking — Cross-Encoder (2-stage retrieval)

**Nguồn gốc:**
- Nogueira & Cho, "Passage Re-ranking with BERT", arXiv 2019 — người đặt
  nền móng cho việc dùng cross-encoder (joint query-document encoding)
  làm stage 2 sau retrieval.
- Model cụ thể trong mã nguồn: `cross-encoder/ms-marco-MiniLM-L-6-v2` — mô hình Cross-Encoder siêu nhẹ, tối ưu chạy trên môi trường CPU với thời gian suy luận cực nhanh (~20ms), không tốn LLM token.
- Lựa chọn thay thế trên môi trường GPU: `BAAI/bge-reranker-v2-m3` hoặc `ViRanker` (arXiv:2509.09131).

**Kiến trúc 2 giai đoạn đã khóa:**
```
BM25 + Vector → RRF (lấy top ~20 ứng viên) → Cross-Encoder rerank
             → lấy top_k_final (mặc định 5) → context expansion → LLM
```

**Đã khóa:** cross-encoder chỉ chạy trên top-N từ RRF (không phải toàn bộ
candidates — quá chậm), có fallback về RRF-only nếu model load lỗi
(không được để hệ thống crash vì thiếu cross-encoder).

---

## 5. Embedding model

**Đã dùng trong mã nguồn (`src/config.py`):** `BAAI/bge-small-en-v1.5` — model embedding 384 dimensions, đạt hiệu năng cao trên MTEB Benchmark, kích thước 133MB tối ưu hóa cho CPU.

**Lộ trình phát triển:** Hệ thống có thể chuyển sang `AITeamVN/Vietnamese_Embedding_v2` (model tiếng Việt chuyên biệt) khi triển khai trên hạ tầng GPU lớn hơn.

**Đã khóa:** không đổi sang embedding model khác (kể cả OpenAI/Gemini
embedding API) trừ khi benchmark Recall@K trên chính corpus ADA/KDIGO của
bạn cho thấy cải thiện rõ rệt — đổi embedding kéo theo phải re-index toàn
bộ corpus, chi phí không nhỏ.

---

## 6. Hierarchical Chunking theo cấu trúc heading

**Nguồn gốc:** đây là điểm **KHÔNG có 1 paper cụ thể duy nhất** — nó là
thực hành phổ biến trong các framework RAG production (LlamaIndex,
LangChain's `MarkdownHeaderTextSplitter`), dựa trên nguyên tắc chung:
chunk theo ranh giới ngữ nghĩa tự nhiên (heading, section) cho ra chất
lượng retrieval tốt hơn cắt cơ học theo số ký tự, vì giữ được ngữ cảnh
"chunk này thuộc phần nào của tài liệu".

**Đối chứng thực nghiệm gần nhất bạn có:** dự án NotebookLM (AIO2025) so
sánh Recursive Chunking (cắt ký tự) và Semantic Chunking (cắt theo cosine
similarity câu liền kề) — CẢ HAI đều KHÔNG dùng heading hierarchy như bạn.
Recursive 1000/150 đạt Context Recall 0.92, Precision 0.86. Đây là baseline
tham khảo được, nhưng **không phải bằng chứng cho thấy heading-based
chunking của bạn kém hơn** — chỉ đơn giản là chưa có ai so sánh trực tiếp
2 cách trên cùng corpus y khoa tiếng Việt của bạn.

**Đã khóa (đây là phần khác biệt/đóng góp riêng của dự án bạn):**
- Chunk theo heading markdown thật (`#`, `##`, `**PHẦN N:**` cho docx).
- Metadata đầy đủ: `title_path`, `parent_node_id`, `previous_sibling_node`,
  `next_sibling_node`, `covered_node_ids`, `chunk_type`.
- Atomic blocks (bảng, công thức) không bị cắt ngang.
- Aggregation cho section con nhỏ.
- KHÔNG chuyển sang Recursive/Semantic Chunking của LangChain — đã phân
  tích ở phần trước: sẽ phá vỡ toàn bộ `context_builder.py` (thiếu
  `node_id`), mất `title_path` cho citation, và mất bảo vệ atomic blocks
  cho bảng liều lượng thuốc.

---

## 7. Context Expansion (parent/prev/next/sibling)

**Đây là phần KHÔNG có paper cụ thể — cần được đánh dấu rõ là quyết định
kiến trúc riêng của dự án bạn**, lấy cảm hứng từ nguyên tắc chung "small-to-
big retrieval" (retrieve chunk nhỏ để match chính xác, nhưng expand ra
ngữ cảnh lớn hơn trước khi đưa vào LLM) — nguyên tắc này xuất hiện trong
tài liệu của LlamaIndex (`AutoMergingRetriever`, `SentenceWindowRetrieval`)
nhưng không phải 1 paper học thuật cụ thể.

**Đã khóa:** `context_builder.py` mở rộng theo thứ tự ưu tiên: retrieved →
parent → prev → next → sibling, trong giới hạn token budget. Đây là thiết
kế riêng, không thay đổi thứ tự ưu tiên này trừ khi có lý do cụ thể.

---

## 8. Patient Context Injection — KHÔNG index vào vector corpus

**Đây cũng là quyết định kiến trúc riêng, không phải từ paper** — nhưng có
cơ sở an toàn rõ ràng (đã phân tích ở phần trước của cuộc trò chuyện):

**Lý do:** nếu patient data được index vào vector corpus, retrieval có thể
bỏ sót thông tin quan trọng (vd. `eGFR = 45`) nếu câu hỏi không match ngữ
nghĩa rõ ràng với con số đó — trong khi thông tin này bắt buộc phải có để
trả lời an toàn (chống chỉ định Metformin khi eGFR thấp).

**Đã khóa:** patient JSON luôn là **context injection trực tiếp mỗi
session**, không index vào corpus — bất kể việc này "tốn token" ra sao.
Đây là nguyên tắc an toàn, không phải chỗ tối ưu chi phí.

---

## 9. Deterministic tính toán, LLM chỉ diễn giải

**Đây là nguyên tắc kiến trúc riêng**, phù hợp với khuyến nghị chung trong
các hệ thống y tế/tài chính an toàn: mọi phép tính có công thức rõ ràng
(BMR, trend statistics, kiểm tra chống chỉ định) PHẢI dùng code, không bao
giờ để LLM tự tính hoặc tự suy luận số học — LLM known yếu ở tính toán
chính xác và không deterministic giữa các lần gọi.

**Đã khóa:** không để LLM tự tính bất kỳ con số y khoa nào (liều lượng,
chỉ số, ngưỡng cảnh báo) — chỉ được diễn giải kết quả code đã tính sẵn.

---

## 10. Đánh giá hệ thống — Benchmark Framework
 
 **Nguồn gốc:** Framework Đánh giá RAG — đo lường chất lượng hai tầng:
 1. **Retrieval Assessment**: Recall@K, MRR, Hit Rate (đánh giá độ bao phủ tài liệu đúng).
 2. **Generation Assessment**: BLEU, ROUGE (và tùy chọn BERTScore) để đo độ chính xác câu trả lời so với ground truth y khoa.
 
 **Đã triển khai trong mã nguồn:** Hệ thống đã hoàn thiện script `benchmark/run_benchmark.py` và cấu hình ngưỡng `evaluation/config.json`. Báo cáo đánh giá được tự động xuất ra thư mục `reports/` (bao gồm `evaluation_report.md` và `errors.json`).
 
 ---
 
 ## Bảng tóm tắt — những gì ĐƯỢC và KHÔNG ĐƯỢC thay đổi tự do
 
 | Thành phần | Trạng thái | Model / Thuật toán trong Code | Có paper gốc? |
 |---|---|---|---|
 | RAG tổng thể | Đã khóa | Grounding trên Context | Có (Lewis et al. 2020) |
 | BM25 + Dense hybrid | Đã khóa | BM25 + FAISS IndexFlatIP | Có (Robertson 2009, Karpukhin 2020) |
 | RRF fusion | Đã khóa | RRF $k=60$ (`src/fusion.py`) | Có (Cormack 2009) |
 | Cross-encoder rerank | Đã khóa | `cross-encoder/ms-marco-MiniLM-L-6-v2` | Có (Nogueira & Cho 2019) |
 | Embedding model | Đã khóa | `BAAI/bge-small-en-v1.5` (384d) | Model card MTEB |
 | Hierarchical chunking theo heading | Đã khóa — đóng góp riêng | `hierarchical_heading` | Không — thực hành phổ biến |
 | Context expansion | Đã khóa — đóng góp riêng | `retrieved` → `parent` → `prev` → `next` → `sibling` (`token_budget=4000`) | Không — lấy cảm hứng small-to-big |
 | Patient injection, không index | Đã khóa — nguyên tắc an toàn riêng | `<PatientData>` injection RAM | Không |
 | Deterministic calculation | Đã khóa — nguyên tắc an toàn riêng | `src/deterministic.py` | Không |
 | Benchmark Evaluation Framework | Đã triển khai | `benchmark/run_benchmark.py` | Có (Standard NLP Metrics) |

**Quy tắc chốt:** những dòng "đóng góp riêng, không có paper" KHÔNG có
nghĩa là chúng yếu hơn — chúng là phần luận điểm của bạn trong đồ án.
Nhưng đúng vì không có paper đối chứng, KHÔNG được thay đổi tùy tiện —
mọi thay đổi ở các dòng này phải đi kèm lý do cụ thể + (nếu có thể) số
liệu benchmark từ `benchmark_fixes.py`, không phải "Antigravity nghĩ nên
sửa lại thế này".
