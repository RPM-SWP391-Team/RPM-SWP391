import sys
from src.medical_pipeline import MedicalRAGPipeline

def main():
    print("=" * 60)
    print("⚕️  HỆ THỐNG TRỢ LÝ Y KHOA (TERMINAL) ⚕️")
    print("=" * 60)
    print("Đang khởi tạo hệ thống (vui lòng chờ)...")
    try:
        pipeline = MedicalRAGPipeline()
        print("✅ Hệ thống đã sẵn sàng!\n")
    except Exception as e:
        print(f"❌ Lỗi khởi tạo: {e}")
        sys.exit(1)

    print("Gợi ý: Hỏi về phác đồ tiểu đường hoặc suy thận.")
    print("Nhập 'q' hoặc 'exit' để thoát chương trình.\n")
    
    while True:
        question = input("🧑‍⚕️ Bạn: ")
        if not question.strip():
            continue
        if question.strip().lower() in ['q', 'exit', 'quit']:
            break
            
        print("⏳ Đang tra cứu và suy luận...\n")
        try:
            response = pipeline.run(question=question)
            print(f"🤖 AI Trả lời:\n{response.answer}")
            
            print("\n📚 Tài liệu trích dẫn (Top chunks):")
            for i, chunk in enumerate(response.retrieved_chunks, 1):
                metadata = getattr(chunk, "metadata", {}) or {}
                source = metadata.get("source_file", metadata.get("document_name", "Unknown"))
                print(f"  [{i}] Nguồn: {source}")
                
            print("-" * 60 + "\n")
            
        except Exception as e:
            print(f"❌ Đã xảy ra lỗi khi xử lý: {e}\n")

if __name__ == "__main__":
    main()
