/* ==========================================================================
   Patient Emergency Support Script (patient.js)
   Dynamically renders SOS floating button (top-left) and Emergency Modal
   ========================================================================== */

document.addEventListener("DOMContentLoaded", function() {
    // 1. Fetch emergency status from API
    fetch('/api/patient/health/emergency-status')
        .then(res => res.json())
        .then(data => {
            const level = data.level || 1;
            const contactName = data.emergencyContactName || "Chưa thiết lập";
            const contactPhone = data.emergencyContactPhone || "";

            // 2. Create SOS button element
            const sosBtn = document.createElement('div');
            sosBtn.id = 'sosFloatingButton';
            sosBtn.className = `sos-btn level-${level}`;
            sosBtn.innerHTML = '<i class="fa-solid fa-triangle-exclamation"></i><span>SOS</span>';
            document.body.appendChild(sosBtn);

            // 3. Create Emergency Modal element
            const modalHtml = `
                <div class="modal fade" id="emergencyModal" tabindex="-1" aria-labelledby="emergencyModalLabel" aria-hidden="true">
                    <div class="modal-dialog modal-dialog-centered">
                        <div class="modal-content modal-content-custom border-danger">
                            <div class="modal-header modal-header-custom bg-danger text-white">
                                <h5 class="modal-title fw-bold" id="emergencyModalLabel">
                                    <i class="fa-solid fa-triangle-exclamation me-2"></i>CỨU TRỢ KHẨN CẤP
                                </h5>
                                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                            </div>
                            <div class="modal-body modal-body-custom p-4">
                                <!-- 1. Sơ cứu tạm thời tại chỗ -->
                                <div class="card card-custom border-danger p-3 mb-4 bg-light">
                                    <h6 class="fw-bold text-danger mb-2">
                                        <i class="fa-solid fa-kit-medical me-2"></i>HƯỚNG DẪN SƠ CỨU TẠI CHỖ
                                    </h6>
                                    <div class="small text-dark lh-base">
                                        <p class="mb-1 text-danger"><strong>Huyết áp tăng cao (&ge; 140/90 mmHg):</strong></p>
                                        <ul class="ps-3 mb-3 text-secondary">
                                            <li>Nằm nghỉ ngơi ở nơi yên tĩnh, thoáng mát, kê đầu cao khoảng 30 độ.</li>
                                            <li>Tránh vận động mạnh hoặc thay đổi tư thế đột ngột.</li>
                                            <li>Uống 1 cốc nước ấm. Dùng thuốc hạ huyết áp khẩn cấp theo chỉ dẫn trước đó của bác sĩ nếu có.</li>
                                        </ul>
                                        <p class="mb-1 text-danger"><strong>Đường huyết bất thường (> 130 mg/dL):</strong></p>
                                        <ul class="ps-3 mb-0 text-secondary">
                                            <li>Nằm nghỉ ngơi, theo dõi các triệu chứng lơ mơ, mệt mỏi.</li>
                                            <li>Uống nhiều nước lọc (không ga, không đường).</li>
                                            <li>Dùng insulin hoặc thuốc hạ đường huyết theo đúng phác đồ khẩn cấp đã chỉ định.</li>
                                        </ul>
                                    </div>
                                </div>

                                <!-- 2. Gọi khẩn cấp -->
                                <div class="d-flex flex-column gap-3">
                                    <a href="tel:115" class="btn btn-danger btn-lg py-3 fw-bold rounded-pill text-white d-flex align-items-center justify-content-center gap-2 shadow">
                                        <i class="fa-solid fa-phone-flip"></i> GỌI CẤP CỨU 115 (Chính)
                                    </a>
                                    <a href="tel:115" class="btn btn-outline-danger btn-lg py-3 fw-bold rounded-pill d-flex align-items-center justify-content-center gap-2 shadow-sm">
                                        <i class="fa-solid fa-phone-volume"></i> GỌI CẤP CỨU 115 (Dự phòng)
                                    </a>
                                    ${contactPhone ? `
                                        <a href="tel:${contactPhone}" class="btn btn-warning btn-lg py-3 fw-bold rounded-pill text-white d-flex align-items-center justify-content-center gap-2 shadow">
                                            <i class="fa-solid fa-user-shield"></i> GỌI NGƯỜI THÂN: ${contactName}
                                        </a>
                                    ` : `
                                        <button class="btn btn-secondary btn-lg py-3 fw-bold rounded-pill text-white d-flex align-items-center justify-content-center gap-2" disabled>
                                            <i class="fa-solid fa-user-slash"></i> Người thân: Chưa thiết lập SĐT
                                        </button>
                                    `}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            `;

            // Append modal HTML to body
            const tempDiv = document.createElement('div');
            tempDiv.innerHTML = modalHtml;
            document.body.appendChild(tempDiv.firstElementChild);

            // 4. Click event to open Modal
            sosBtn.addEventListener('click', function() {
                const modalEl = document.getElementById('emergencyModal');
                if (modalEl && typeof bootstrap !== 'undefined') {
                    const bsModal = new bootstrap.Modal(modalEl);
                    bsModal.show();
                } else {
                    console.error("Bootstrap is not loaded yet.");
                }
            });
        })
        .catch(err => console.error("Error loading emergency status:", err));
});
