import streamlit as st
import pandas as pd
import time
import os
import json
from src.medical_pipeline import MedicalRAGPipeline

# Thiết lập giao diện cao cấp
st.set_page_config(
    page_title="Hệ Thống Trợ Lý Y Khoa (RAG)",
    page_icon="⚕️",
    layout="wide",
    initial_sidebar_state="expanded"
)

# Custom CSS cho Dark Mode và Typography đẹp
st.markdown("""
<style>
    /* Tổng thể */
    .main {
        background-color: #0e1117;
        color: #fafafa;
        font-family: 'Inter', sans-serif;
    }
    
    /* Header và Tiêu đề */
    h1, h2, h3 {
        color: #4CAF50 !important;
        font-weight: 600 !important;
    }
    
    /* Box tin nhắn AI */
    .stChatMessage {
        background-color: #1e2127;
        border-radius: 10px;
        padding: 10px;
        border-left: 4px solid #4CAF50;
        margin-bottom: 10px;
        box-shadow: 0 4px 6px rgba(0,0,0,0.3);
    }
    
    /* Box trích dẫn y khoa */
    .medical-citation {
        background-color: #262730;
        border-radius: 8px;
        padding: 15px;
        font-size: 0.9em;
        color: #a0aab2;
        border: 1px solid #333;
        margin-top: 10px;
    }
    
    /* Badge cho từ khóa */
    .badge {
        display: inline-block;
        padding: 0.25em 0.4em;
        font-size: 75%;
        font-weight: 700;
        line-height: 1;
        text-align: center;
        white-space: nowrap;
        vertical-align: baseline;
        border-radius: 0.25rem;
        background-color: #4CAF50;
        color: white;
        margin-right: 5px;
    }
</style>
""", unsafe_allow_html=True)

st.title("⚕️ Trợ Lý Y Khoa (Medical RAG System)")
st.markdown("Hệ thống tư vấn phác đồ điều trị dựa trên tài liệu Y khoa (PDF) được phân tích bởi AI (Gemini 1.5).")

# Khởi tạo Pipeline (Cached để tải nhanh)
@st.cache_resource
def load_pipeline():
    try:
        pipeline = MedicalRAGPipeline()
        return pipeline
    except Exception as e:
        st.error(f"Lỗi khởi tạo hệ thống RAG: {e}")
        return None

pipeline = load_pipeline()

# Giao diện Sidebar - Công cụ Lâm sàng
with st.sidebar:
    st.image("https://cdn-icons-png.flaticon.com/512/2966/2966327.png", width=100)
    st.header("🧮 Công cụ Lâm sàng")
    
    st.subheader("Tính eGFR (CKD-EPI 2021)")
    age = st.number_input("Tuổi", min_value=18, max_value=120, value=50)
    scr = st.number_input("Creatinine huyết thanh (mg/dL)", min_value=0.1, max_value=15.0, value=1.0, step=0.1)
    is_female = st.checkbox("Nữ giới")
    
    if st.button("Tính eGFR"):
        # Công thức CKD-EPI 2021 (Race-free)
        kappa = 0.7 if is_female else 0.9
        alpha = -0.241 if is_female else -0.302
        gender_mult = 1.012 if is_female else 1.0
        
        egfr = 142 * min(scr/kappa, 1)**alpha * max(scr/kappa, 1)**(-1.200) * (0.9938**age) * gender_mult
        
        st.success(f"eGFR = {egfr:.1f} mL/min/1.73m²")
        if egfr < 30:
            st.error("⚠️ Suy thận nặng (CKD Stage 4-5). Cẩn trọng khi kê đơn Metformin!")
            
    st.markdown("---")
    st.subheader("Chỉ số bệnh nhân")
    st.text_input("Bệnh lý mắc kèm", "Đái tháo đường Type 2")
    st.text_input("Thuốc đang dùng", "Metformin 500mg")

# Giao diện chính - Khung Chat
if "messages" not in st.session_state:
    st.session_state.messages = []

# Hiển thị lịch sử chat
for message in st.session_state.messages:
    with st.chat_message(message["role"]):
        st.markdown(message["content"])
        if "citations" in message:
            with st.expander("📚 Xem tài liệu trích dẫn"):
                for cite in message["citations"]:
                    st.markdown(f"""
                    <div class="medical-citation">
                        <strong>Nguồn:</strong> {cite['file']}<br>
                        <strong>Mức độ tin cậy:</strong> {cite['score']:.2f}<br>
                        <em>"{cite['text'][:300]}..."</em>
                    </div>
                    """, unsafe_allow_html=True)

# Khung nhập liệu
if prompt := st.chat_input("Nhập câu hỏi y khoa (VD: Phác đồ điều trị tiểu đường cho người suy thận)"):
    # Thêm câu hỏi của user vào giao diện
    st.session_state.messages.append({"role": "user", "content": prompt})
    with st.chat_message("user"):
        st.markdown(prompt)

    # Bot trả lời
    with st.chat_message("assistant"):
        message_placeholder = st.empty()
        message_placeholder.markdown("⏳ Đang tra cứu phác đồ và suy luận...")
        
        if pipeline is None:
            message_placeholder.markdown("❌ Hệ thống RAG chưa được khởi tạo thành công (thiếu Index hoặc API Key).")
        else:
            try:
                start_time = time.time()
                # Chạy pipeline RAG
                response = pipeline.run(question=prompt)
                latency = time.time() - start_time
                
                # Trích xuất dữ liệu
                answer = response.answer
                context_used = response.retrieved_chunks
                
                # Format trích dẫn
                citations = []
                for chunk in context_used:
                    metadata = getattr(chunk, "metadata", {}) or {}
                    file_name = metadata.get("source_file", metadata.get("document_name", "Unknown"))
                    citations.append({
                        "file": file_name,
                        "text": chunk.text,
                        "score": 0.0
                    })
                
                # Cập nhật UI
                message_placeholder.markdown(answer)
                st.caption(f"⚡ Thời gian xử lý: {latency:.2f} giây")
                
                if citations:
                    with st.expander("📚 Xem tài liệu trích dẫn"):
                        for cite in citations:
                            st.markdown(f"""
                            <div class="medical-citation">
                                <strong>Nguồn:</strong> {cite['file']}<br>
                                <strong>Độ liên quan:</strong> {cite['score']:.2f}<br>
                                <em>"{cite['text'][:300]}..."</em>
                            </div>
                            """, unsafe_allow_html=True)
                
                # Lưu vào lịch sử
                st.session_state.messages.append({
                    "role": "assistant", 
                    "content": answer,
                    "citations": citations
                })
                
            except Exception as e:
                message_placeholder.error(f"Đã xảy ra lỗi: {str(e)}")
