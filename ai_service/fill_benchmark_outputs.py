import os
import sys
import time
import pandas as pd
from pathlib import Path
from dotenv import load_dotenv

# Ensure root ai_service path is in sys.path
ai_service_dir = Path(__file__).resolve().parent
sys.path.insert(0, str(ai_service_dir))

load_dotenv()

from src.llm_client import GroqLLMClient, GeminiLLMClient

# Ensure stdout supports UTF-8 on Windows
if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8', errors='replace')

CSV_PATH = ai_service_dir / "evaluation" / "Danh sách các khuyến nghị trong Hướng dẫn quản lý Huyết áp ESC 2024 - Table 1.csv"

def generate_answer_for_row(llm, question, main_point, evidence):
    prompt = f"""Bạn là bác sĩ/chuyên gia tư vấn y tế. Dựa trên thông tin bằng chứng y khoa bên dưới, hãy trả lời câu hỏi một cách chuẩn xác, chuyên nghiệp, rõ ràng và đầy đủ.

[THÔNG TIN BẰNG CHỨNG (EVIDENCE)]
{evidence}

[Ý CHÍNH KHUYẾN NGHỊ (MAIN POINT)]
{main_point}

[CÂU HỎI]
{question}

Hãy đưa ra câu trả lời chi tiết, chính xác dựa trên bằng chứng trên (bằng tiếng Việt):"""
    
    system_prompt = "Bạn là một trợ lý y khoa cao cấp. Trả lời chính xác, bám sát khuyến nghị y khoa và bằng chứng cung cấp."
    return llm.generate(prompt=prompt, system_prompt=system_prompt)

def main():
    print(f"[*] Đang đọc file: {CSV_PATH.name}")
    if not CSV_PATH.exists():
        print(f"[X] Không tìm thấy file tại {CSV_PATH}")
        return

    df = pd.read_csv(CSV_PATH)
    print(f"[*] Tổng số dòng trong file: {len(df)}")
    print(f"[*] Danh sách cột: {list(df.columns)}")

    # Ensure Output column exists
    if "Output" not in df.columns:
        df["Output"] = ""

    # Count empty outputs
    df["Output"] = df["Output"].fillna("")
    empty_indices = df[df["Output"].astype(str).str.strip() == ""].index.tolist()
    print(f"[*] Số dòng chưa có Output: {len(empty_indices)}")

    if not empty_indices:
        print("[!] Tất cả các dòng đều đã có Output!")
        return

    # Initialize LLM Client (Try Groq first, fallback to Gemini if needed)
    try:
        llm = GroqLLMClient(model_name="llama-3.3-70b-versatile")
        print("[*] Đã khởi tạo Groq LLM Client (llama-3.3-70b-versatile)")
    except Exception as e:
        print(f"[!] Groq không khả dụng ({e}), chuyển sang GeminiLLMClient...")
        llm = GeminiLLMClient()

    filled_count = 0
    token_exhausted = False

    for idx in empty_indices:
        row = df.loc[idx]
        question = str(row.get("Question", ""))
        main_point = str(row.get("Main point", ""))
        evidence = str(row.get("Evidence", ""))

        print(f"\n--- [{idx+1}/{len(df)}] Processing ID: {row.get('ID', idx+1)} ---")
        print(f"❓ Question: {question[:80]}...")

        try:
            output_text = generate_answer_for_row(llm, question, main_point, evidence)
            df.at[idx, "Output"] = output_text.strip()
            filled_count += 1
            print(f"✅ Generated Answer ({len(output_text)} chars):\n{output_text[:120]}...")

            # Save immediately to prevent data loss on crash/exit
            df.to_csv(CSV_PATH, index=False, encoding="utf-8-sig")
            print(f"💾 Đã lưu tiến trình dòng ID {row.get('ID', idx+1)} vào CSV!")

            # Slight delay to avoid hammering rate limits
            time.sleep(1.0)

        except Exception as e:
            err_str = str(e).lower()
            print(f"❌ Error row {idx+1}: {e}")
            if "rate" in err_str or "quota" in err_str or "429" in err_str or "exhausted" in err_str or "limit" in err_str:
                print("\n⚠️ HẾT TOKEN / CHẠM HẠN MỨC API (RATE LIMIT / QUOTA EXHAUSTED)!")
                print(f"🛑 Đã dừng lại an toàn. Tổng số câu hỏi đã điền Output thành công: {filled_count}")
                token_exhausted = True
                break
            else:
                print(f"[!] Bỏ qua dòng {idx+1} do lỗi: {e}")
                continue

    if not token_exhausted:
        print(f"\n🎉 HOÀN THÀNH TOÀN BỘ! Đã điền xong {filled_count} câu hỏi vào file {CSV_PATH.name}")

if __name__ == "__main__":
    main()
