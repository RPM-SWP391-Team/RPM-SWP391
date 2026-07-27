import os
import sys
import time
import pandas as pd
from pathlib import Path
from dotenv import load_dotenv

ai_service_dir = Path(__file__).resolve().parent
sys.path.insert(0, str(ai_service_dir))

load_dotenv()

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8', errors='replace')

from src.medical_pipeline import MedicalRAGPipeline

CSV_PATH = ai_service_dir / "evaluation" / "Danh sách các khuyến nghị trong Hướng dẫn quản lý Huyết áp ESC 2024 - Table 1.csv"

def save_csv_safely(df, path):
    max_retries = 5
    for attempt in range(max_retries):
        try:
            df.to_csv(path, index=False, encoding="utf-8-sig")
            return True
        except PermissionError:
            print(f"⚠️ [CẢNH BÁO] File CSV đang bị Excel mở! Đang thử lại ({attempt+1}/{max_retries})... Vui lòng ĐÓNG EXCEL!")
            time.sleep(2.0)
        except Exception as e:
            print(f"❌ Lỗi lưu CSV: {e}")
            return False
    return False

def main():
    print(f"[*] Đang đọc file benchmark: {CSV_PATH.name}")
    if not CSV_PATH.exists():
        print(f"[X] Không tìm thấy file tại {CSV_PATH}")
        return

    try:
        df = pd.read_csv(CSV_PATH)
    except PermissionError:
        print("❌ FILE ĐANG BỊ KHÓA BỞI EXCEL! Vui lòng đóng Excel rồi chạy lại.")
        return

    print(f"[*] Tổng số câu hỏi trong file: {len(df)}")

    for col in ("Output_Blind_RAG", "Retrieved_Chunk_IDs"):
        if col not in df.columns:
            df[col] = ""
        df[col] = df[col].fillna("")

    empty_indices = df[df["Output_Blind_RAG"].astype(str).str.strip() == ""].index.tolist()
    print(f"[*] Số câu hỏi chưa chạy Blind RAG: {len(empty_indices)} / {len(df)}")

    if not empty_indices:
        print("🎉 tất cả 154 câu hỏi đều đã chạy Blind RAG xong!")
        return

    print("\n[*] Đang khởi tạo Medical RAG Pipeline...")
    try:
        pipeline = MedicalRAGPipeline()
        print("✅ Khởi tạo Medical RAG Pipeline thành công!\n")
    except Exception as e:
        print(f"❌ Lỗi khi khởi tạo Pipeline: {e}")
        return

    filled_count = 0

    for idx in empty_indices:
        row = df.loc[idx]
        question = str(row.get("Question", ""))

        print(f"--- [{idx+1}/{len(df)}] Processing ID: {row.get('ID', idx+1)} ---")
        print(f"❓ Question: {question[:80]}...")

        success = False
        for attempt in range(3):
            try:
                res = pipeline.run(question=question)
                output_text = res.answer
                retrieved_chunk_ids = [str(getattr(c, 'node_id', 'unknown')) for c in res.retrieved_chunks]

                df.at[idx, "Output_Blind_RAG"] = output_text.strip()
                df.at[idx, "Retrieved_Chunk_IDs"] = ", ".join(retrieved_chunk_ids)
                filled_count += 1

                print(f"🔍 Chunks retrieved ({len(retrieved_chunk_ids)}): {retrieved_chunk_ids}")
                print(f"✅ Generated Answer ({len(output_text)} chars):\n{output_text[:120]}...")

                if save_csv_safely(df, CSV_PATH):
                    print(f"💾 Đã lưu tiến trình câu ID {row.get('ID', idx+1)} vào CSV!\n")

                success = True
                time.sleep(3.5) # Inter-question pacing delay for Groq rate limits
                break
            except Exception as e:
                print(f"❌ Lỗi câu {idx+1} (lần {attempt+1}): {e}")
                time.sleep(5.0)

        if not success:
            print(f"⚠️ Bỏ qua câu ID {row.get('ID', idx+1)}\n")

    print(f"\n🎉 HOÀN THÀNH BLIND TEST! Đã điền xong tổng cộng {filled_count} câu vào {CSV_PATH.name}")

if __name__ == "__main__":
    main()
