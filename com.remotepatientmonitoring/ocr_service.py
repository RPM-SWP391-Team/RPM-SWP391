import sys
import json
import re
import os
import numpy as np

def preprocess_image(image_path):
    """
    Tiền xử lý ảnh chuyên sâu cho EasyOCR:
    Chuyển đổi sang ảnh xám, phóng to gấp 2.0 lần và làm mịn bằng Gaussian Blur.
    Không nhị phân hóa cục bộ để giữ độ tương phản tự nhiên của các ký tự nhãn máy.
    """
    import cv2
    
    img = cv2.imread(image_path)
    if img is None:
        raise Exception("Không thể đọc tệp ảnh qua OpenCV")
        
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    
    # Phóng to ảnh gấp 2.0 lần để tăng kích thước vùng nhãn và chữ số
    resized = cv2.resize(gray, None, fx=2.0, fy=2.0, interpolation=cv2.INTER_CUBIC)
    
    # Khử nhiễu nhẹ
    blurred = cv2.GaussianBlur(resized, (3, 3), 0)
    
    debug_path = "ocr_debug_prep.jpg"
    cv2.imwrite(debug_path, blurred)
    print(f"[DEBUG OCR Image Preprocessed] Saved to {os.path.abspath(debug_path)}", file=sys.stderr)
    
    return debug_path

def classify_device(ocr_results):
    """
    Tự động nhận diện loại máy đo dựa trên sự xuất hiện của các từ khóa đặc trưng:
    - Máy huyết áp: 'sys', 'dia', 'pulse', 'mmhg', 'omron', 'min'
    - Máy tiểu đường: 'mg/dl', 'mmol/l', 'glu', 'glucose', 'mmol', 'mg'
    """
    glucose_keywords = ['mg/dl', 'mmol/l', 'glu', 'glucose', 'mmol', 'mg']
    bp_keywords = ['sys', 'dia', 'pulse', 'mmhg', 'omron', 'min']
    
    glucose_score = 0
    bp_score = 0
    
    for (bbox, text, prob) in ocr_results:
        text_lower = text.strip().lower()
        for kw in glucose_keywords:
            if kw in text_lower:
                glucose_score += 1
        for kw in bp_keywords:
            if kw in text_lower:
                bp_score += 1
                
    print(f"[DEBUG OCR Classification] Glucose Score: {glucose_score}, BP Score: {bp_score}", file=sys.stderr)
    if glucose_score > bp_score:
        return "GLUCOSE"
    return "BLOOD_PRESSURE"

def extract_glucose_anchor(ocr_results):
    """
    Trích xuất chỉ số đường huyết dựa trên khoảng cách hình học tới đơn vị đo (mg/dL hoặc mmol/L):
    1. Lọc tất cả các số thực hoặc số nguyên hợp lệ trong khoảng [1.0, 600.0].
    2. Xác định các tọa độ tâm của các từ khóa đơn vị đo.
    3. Gán chỉ số đường huyết là con số nằm gần từ khóa đơn vị nhất (khoảng cách Euclidean ngắn nhất).
    """
    anchors = []
    candidates = []
    
    for (bbox, text, prob) in ocr_results:
        text_clean = text.strip()
        text_lower = text_clean.lower()
        
        x_center = (bbox[0][0] + bbox[1][0] + bbox[2][0] + bbox[3][0]) / 4.0
        y_center = (bbox[0][1] + bbox[1][1] + bbox[2][1] + bbox[3][1]) / 4.0
        
        # Nhận diện số thực hoặc số nguyên (ví dụ: "5.6", "5,6", "125")
        text_no_spaces = text_clean.replace(" ", "")
        float_matches = re.findall(r'\b\d+(?:[.,]\d+)?\b', text_no_spaces)
        if float_matches:
            val_str = float_matches[0].replace(',', '.')
            try:
                val = float(val_str)
                if 1.0 <= val <= 600.0:
                    candidates.append({
                        "value": val,
                        "x": x_center,
                        "y": y_center
                    })
                    continue
            except ValueError:
                pass
                
        # Phát hiện từ khóa đơn vị (Anchor)
        if any(kw in text_lower for kw in ['mg/dl', 'mmol/l', 'glu', 'glucose', 'mmol']):
            anchors.append((x_center, y_center))
            
    print(f"[DEBUG OCR Glucose] Found candidates: {[(c['value'], c['x'], c['y']) for c in candidates]}", file=sys.stderr)
    print(f"[DEBUG OCR Glucose] Found anchors: {anchors}", file=sys.stderr)
    
    if not candidates:
        return None
        
    # Trường hợp không thấy từ khóa đơn vị, lấy con số lớn nhất (thường là kết quả đo hiển thị to nhất)
    if not anchors:
        candidates.sort(key=lambda c: c["value"], reverse=True)
        return candidates[0]["value"]
        
    # Tính khoảng cách Euclidean ngắn nhất tới các Anchor
    best_candidate = None
    min_dist = float('inf')
    
    for c in candidates:
        for ax, ay in anchors:
            dist = np.sqrt((c["x"] - ax)**2 + (c["y"] - ay)**2)
            if dist < min_dist:
                min_dist = dist
                best_candidate = c
                
    return best_candidate["value"] if best_candidate else None

def extract_blood_pressure_anchor(ocr_results):
    """
    Trích xuất chỉ số huyết áp dựa trên khoảng cách tọa độ trục Y tới nhãn tương ứng (SYS, DIA, PULSE):
    1. Xác định tọa độ Y của các nhãn SYS, DIA, PULSE.
    2. Điền khuyết các tọa độ nhãn bị thiếu dựa trên khoảng cách tương đối dọc.
    3. Gán các số tìm được trong khoảng [10, 250] vào nhãn có khoảng cách Y gần nhất.
    4. Tự động áp dụng logic bù số 1 hàng trăm cho tâm thu nếu số cạnh SYS <= 89.
    """
    sys_anchors = []
    dia_anchors = []
    pulse_anchors = []
    candidates = []
    
    for (bbox, text, prob) in ocr_results:
        text_clean = text.strip()
        text_lower = text_clean.lower()
        
        x_center = (bbox[0][0] + bbox[1][0] + bbox[2][0] + bbox[3][0]) / 4.0
        y_center = (bbox[0][1] + bbox[1][1] + bbox[2][1] + bbox[3][1]) / 4.0
        
        # Nhận diện số nguyên hợp lệ trong phạm vi huyết áp
        text_no_spaces = text_clean.replace(" ", "")
        numbers = re.findall(r'\d+', text_no_spaces)
        if numbers:
            val = int(numbers[0])
            if 10 <= val <= 250:
                candidates.append({
                    "value": val,
                    "x": x_center,
                    "y": y_center
                })
                continue
                
        # Nhận diện từ khóa nhãn (Anchor)
        if any(kw in text_lower for kw in ['sys', '5ys', 'sy5', 'systolic']):
            sys_anchors.append((x_center, y_center))
        elif any(kw in text_lower for kw in ['dia', 'd1a', 'diastolic']):
            dia_anchors.append((x_center, y_center))
        elif any(kw in text_lower for kw in ['pulse', 'pul', 'min', 'bpm']):
            pulse_anchors.append((x_center, y_center))
            
    print(f"[DEBUG OCR BP] Found candidates: {[(c['value'], c['x'], c['y']) for c in candidates]}", file=sys.stderr)
    
    # Dự phòng: Nếu hoàn toàn không tìm thấy nhãn chữ nào, sắp xếp theo trục Y từ trên xuống dưới
    if not sys_anchors and not dia_anchors and not pulse_anchors:
        print("[DEBUG OCR BP] No anchors found, falling back to vertical sorted Y logic.", file=sys.stderr)
        candidates.sort(key=lambda c: c["y"])
        systolic = None
        diastolic = None
        heart_rate = None
        if len(candidates) >= 1:
            systolic = candidates[0]["value"]
            if systolic <= 89:
                systolic += 100
        if len(candidates) >= 2:
            diastolic = candidates[1]["value"]
        if len(candidates) >= 3:
            heart_rate = candidates[2]["value"]
        if systolic is not None and diastolic is not None and systolic < diastolic:
            systolic, diastolic = diastolic, systolic
        return systolic, diastolic, heart_rate
        
    # Tính tọa độ trung bình cho từng nhãn
    def get_anchor_coord(anchors):
        if not anchors:
            return None
        xs = [a[0] for a in anchors]
        ys = [a[1] for a in anchors]
        return (sum(xs)/len(xs), sum(ys)/len(ys))
        
    sys_coord = get_anchor_coord(sys_anchors)
    dia_coord = get_anchor_coord(dia_anchors)
    pulse_coord = get_anchor_coord(pulse_anchors)
    
    # Ước lượng/Điền khuyết tọa độ các nhãn bị thiếu dựa trên các nhãn đã có
    if sys_coord and dia_coord and not pulse_coord:
        pulse_coord = (dia_coord[0], dia_coord[1] + (dia_coord[1] - sys_coord[1]))
    elif dia_coord and pulse_coord and not sys_coord:
        sys_coord = (dia_coord[0], dia_coord[1] - (pulse_coord[1] - dia_coord[1]))
    elif sys_coord and pulse_coord and not dia_coord:
        dia_coord = ((sys_coord[0] + pulse_coord[0])/2.0, (sys_coord[1] + pulse_coord[1])/2.0)
    elif sys_coord and not dia_coord and not pulse_coord:
        dia_coord = (sys_coord[0], sys_coord[1] + 150)
        pulse_coord = (sys_coord[0], sys_coord[1] + 300)
    elif dia_coord and not sys_coord and not pulse_coord:
        sys_coord = (dia_coord[0], dia_coord[1] - 150)
        pulse_coord = (dia_coord[0], dia_coord[1] + 150)
    elif pulse_coord and not sys_coord and not dia_coord:
        sys_coord = (pulse_coord[0], pulse_coord[1] - 300)
        dia_coord = (pulse_coord[0], pulse_coord[1] - 150)
        
    print(f"[DEBUG OCR BP Anchors] Estimated coordinates: SYS={sys_coord}, DIA={dia_coord}, PULSE={pulse_coord}", file=sys.stderr)
    
    # Định vị con số gần nhất cho từng nhãn sử dụng Khoảng cách Euclidean có trọng số dọc (Weight Y = 3.0)
    def find_closest_number(anchor_coord):
        if not anchor_coord or not candidates:
            return None
        dists = []
        for c in candidates:
            dx = c["x"] - anchor_coord[0]
            dy = c["y"] - anchor_coord[1]
            dist = np.sqrt(dx**2 + (3.0 * dy)**2)
            dists.append((dist, c["value"]))
        dists.sort()
        return dists[0][1]
        
    systolic = find_closest_number(sys_coord)
    diastolic = find_closest_number(dia_coord)
    heart_rate = find_closest_number(pulse_coord)
    
    # Hóa giải số tâm thu thiếu chữ số hàng trăm
    if systolic is not None and systolic <= 89:
        print(f"[DEBUG OCR BP Heuristic] Systolic {systolic} is too low. Adjusting to {systolic + 100}", file=sys.stderr)
        systolic += 100
        
    # Đảm bảo logic: systolic luôn lớn hơn diastolic
    if systolic is not None and diastolic is not None and systolic < diastolic:
        systolic, diastolic = diastolic, systolic
        
    return systolic, diastolic, heart_rate

def main():
    if len(sys.argv) < 2:
        print(json.dumps({"error": "No image path provided"}))
        sys.exit(1)

    image_path = sys.argv[1]
    if not os.path.exists(image_path):
        print(json.dumps({"error": f"Image path does not exist: {image_path}"}))
        sys.exit(1)

    temp_prep_path = None
    try:
        import easyocr
        
        # 1. Tiền xử lý ảnh gốc
        temp_prep_path = preprocess_image(image_path)
        
        # 2. Khởi chạy EasyOCR quét toàn bộ ảnh gốc (Full Scan)
        reader = easyocr.Reader(['en'], gpu=False)
        ocr_results = reader.readtext(temp_prep_path)
        
        # In tất cả các từ phát hiện được ra stderr để phục vụ debug
        print("[DEBUG OCR Raw Scan Results]:", file=sys.stderr)
        for (bbox, text, prob) in ocr_results:
            print(f"  - '{text}' (prob={prob:.2f}) bbox={bbox}", file=sys.stderr)
            
        # 3. Phân loại loại thiết bị tự động
        device_type = classify_device(ocr_results)
        print(f"[DEBUG OCR Device Detected] Type: {device_type}", file=sys.stderr)
        
        systolic = None
        diastolic = None
        heart_rate = None
        glucose = None
        
        # 4. Trích xuất chỉ số tương ứng với từng loại máy
        if device_type == "GLUCOSE":
            glucose = extract_glucose_anchor(ocr_results)
        else:
            systolic, diastolic, heart_rate = extract_blood_pressure_anchor(ocr_results)
            
        # In ra kết quả JSON hợp lệ
        output = {
            "systolic": systolic,
            "diastolic": diastolic,
            "heartRate": heart_rate,
            "glucose": glucose
        }
        print(json.dumps(output))
        
    except Exception as e:
        print(json.dumps({
            "error": f"Lỗi xử lý OCR: {str(e)}",
            "systolic": None,
            "diastolic": None,
            "heartRate": None,
            "glucose": None
        }))
        sys.exit(1)
    finally:
        if temp_prep_path and os.path.exists(temp_prep_path):
            try:
                os.remove(temp_prep_path)
            except:
                pass

if __name__ == "__main__":
    main()
