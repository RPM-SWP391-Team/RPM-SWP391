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
- Model cụ thể: BAAI, "BGE-Reranker-v2-M3: Multilingual Passage Reranker",
  Hugging Face model card, 2023/2024 — đa ngôn ngữ, hỗ trợ tiếng Việt,
  chạy local qua `sentence-transformers`, không tốn LLM token.
- Bằng chứng thực nghiệm bên ngoài: dự án "Simple NotebookLM" (AI Việt
  Nam, AIO2025) benchmark bằng Ragas cho thấy thêm `bge-reranker-v2-m3`
  cải thiện Context Precision từ 0.86 lên 0.92, Answer Relevance từ
  0.73 lên 0.80 trên chính use-case RAG tiếng Việt.
- Lựa chọn thay thế nếu cần (chưa áp dụng, cần benchmark trước): ViRanker
  — cross-encoder huấn luyện riêng cho tiếng Việt trên nền BGE-M3, công bố
  tại arXiv:2509.09131 (2025), đạt NDCG@3 = 0.6815 trên benchmark MMARCO-VI.

**Kiến trúc 2 giai đoạn đã khóa:**
```
BM25 + Vector → RRF (lấy top ~20 ứng viên) → Cross-Encoder rerank
             → lấy top_k_final (mặc định 5) → context expansion → LLM
```

**Đã khóa:** cross-encoder chỉ chạy trên top-N từ RRF (không phải toàn bộ
candidates — quá chậm), có fallback về RRF-only nếu model load lỗi
(không được để hệ thống crash vì thiếu cross-encoder).

---

## 5. Embedding model cho tiếng Việt

**Đã dùng:** `AITeamVN/Vietnamese_Embedding_v2` — model embedding
tiếng Việt chuyên biệt, được khuyến nghị thay vì multilingual model chung
chung vì mật độ tiếng Việt trong dữ liệu huấn luyện cao hơn.

**Nguyên tắc:** với văn bản y khoa tiếng Việt có nhiều thuật ngữ chuyên
ngành + dấu thanh phức tạp, model embedding **phải** được huấn luyện/
fine-tune có tiếng Việt trong tập dữ liệu — không dùng model chỉ tiếng Anh
rồi kỳ vọng generalize tốt.

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

## 10. Đánh giá hệ thống — Ragas framework

**Nguồn gốc:** Ragas (Retrieval-Augmented Generation Assessment) — bộ 4
chỉ số: Context Recall, Context Precision, Faithfulness, Answer Relevancy,
dùng phương pháp LLM-as-judge để tự động hóa đánh giá thay vì gán nhãn thủ
công.

**Đã tham khảo được (từ dự án NotebookLM):** chiến lược đúng thứ tự là tối
ưu **Recall trước** (đảm bảo không bỏ sót thông tin) rồi mới tối ưu
**Precision** (giảm nhiễu) — vì generation quality phụ thuộc hoàn toàn vào
độ bao phủ ngữ cảnh trước.

**Chưa áp dụng — cần quyết định:** hệ thống của bạn hiện chưa có framework
đánh giá tương đương Ragas. `benchmark_fixes.py` mới chỉ có Recall@K/MRR
(không tốn token, nhưng không đo được Faithfulness/Answer Relevancy vì
thiếu LLM-as-judge). Đây là khoảng trống thật sự — không phải "chưa có
nghiên cứu" mà là "chưa triển khai phần đánh giá generation".

---

## Bảng tóm tắt — những gì ĐƯỢC và KHÔNG ĐƯỢC thay đổi tự do

| Thành phần | Trạng thái | Có paper gốc? |
|---|---|---|
| RAG tổng thể | Đã khóa | Có (Lewis et al. 2020) |
| BM25 + Dense hybrid | Đã khóa | Có (Robertson 2009, Karpukhin 2020) |
| RRF fusion | Đã khóa | Có (Cormack 2009) |
| Cross-encoder rerank (bge-reranker-v2-m3) | Đang triển khai, cần benchmark | Có (Nogueira & Cho 2019, BAAI) |
| Embedding Vietnamese_Embedding_v2 | Đã khóa | Model card, chưa có paper học thuật riêng |
| Hierarchical chunking theo heading | Đã khóa — đóng góp riêng | Không — thực hành phổ biến |
| Context expansion (parent/prev/next/sibling) | Đã khóa — đóng góp riêng | Không — lấy cảm hứng small-to-big retrieval |
| Patient injection, không index | Đã khóa — nguyên tắc an toàn riêng | Không |
| Deterministic calculation | Đã khóa — nguyên tắc an toàn riêng | Không |
| Đánh giá Ragas-style | **Chưa triển khai** | Có (Ragas framework) |

**Quy tắc chốt:** những dòng "đóng góp riêng, không có paper" KHÔNG có
nghĩa là chúng yếu hơn — chúng là phần luận điểm của bạn trong đồ án.
Nhưng đúng vì không có paper đối chứng, KHÔNG được thay đổi tùy tiện —
mọi thay đổi ở các dòng này phải đi kèm lý do cụ thể + (nếu có thể) số
liệu benchmark từ `benchmark_fixes.py`, không phải "Antigravity nghĩ nên
sửa lại thế này".
