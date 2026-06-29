/* ==========================================================================
   Patient Emergency Support & Health Evaluation Script (patient.js)
   Separates emergency SOS (navbar) and floating health warning indicators.
   ========================================================================== */

document.addEventListener("DOMContentLoaded", function() {
    // 1. Inject custom styles dynamically
    const style = document.createElement('style');
    style.innerHTML = `
        .btn-sos-nav {
            background: linear-gradient(135deg, #ff416c, #ff4b2b) !important;
            color: white !important;
            border: none !important;
            box-shadow: 0 0 10px rgba(255, 75, 43, 0.8) !important;
            font-weight: 700 !important;
            border-radius: 20px !important;
            padding: 6px 16px !important;
            animation: navSosPulse 1.5s infinite;
            text-decoration: none !important;
            display: inline-flex;
            align-items: center;
            gap: 4px;
        }
        .btn-sos-nav:hover {
            transform: scale(1.05);
            background: linear-gradient(135deg, #ff4b2b, #ff416c) !important;
        }
        @keyframes navSosPulse {
            0%   { box-shadow: 0 0 0 0 rgba(255, 75, 43, 0.7); }
            70%  { box-shadow: 0 0 0 10px rgba(255, 75, 43, 0); }
            100% { box-shadow: 0 0 0 0 rgba(255, 75, 43, 0); }
        }
        .health-status-btn {
            position: fixed;
            left: 20px;
            top: 90px;
            width: 62px;
            height: 62px;
            border-radius: 50%;
            color: #ffffff !important;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            font-size: .65rem;
            font-weight: 800;
            z-index: 1050;
            cursor: pointer;
            border: 3px solid #ffffff;
            text-decoration: none !important;
            transition: transform .2s;
            text-align: center;
            box-shadow: 0 6px 20px rgba(0,0,0,0.15);
        }
        .health-status-btn:hover { transform: scale(1.08); }
        .health-status-btn i { font-size: 1.25rem; margin-bottom: 2px; }
        .health-status-btn.level-1 { background: var(--gradient-teal) !important; }
        .health-status-btn.level-2 { background: #f1c40f !important; box-shadow: 0 6px 20px rgba(241,196,15,.4); }
        .health-status-btn.level-3 { background: #fd7e14 !important; box-shadow: 0 6px 20px rgba(253,126,20,.4); }
        .health-status-btn.level-4 { background: var(--gradient-danger) !important; animation: healthPulse 1.5s infinite; }
        @keyframes healthPulse {
            0%   { transform: scale(1);    box-shadow: 0 0 0 0 rgba(245,87,108,.6); }
            70%  { transform: scale(1.08); box-shadow: 0 0 0 16px rgba(245,87,108,0); }
            100% { transform: scale(1);    box-shadow: 0 0 0 0 rgba(245,87,108,0); }
        }
        .bg-orange {
            background-color: #fd7e14 !important;
        }
    `;
    document.head.appendChild(style);

    // 2. Fetch emergency status and metrics from API
    fetch('/api/patient/health/emergency-status')
        .then(res => res.json())
        .then(data => {
            const level = data.level || 1;
            const contactName = data.emergencyContactName || "Chưa thiết lập";
            const contactPhone = data.emergencyContactPhone || "";

            // 3. Create SOS Button in top navigation bar
            const navActions = document.querySelector('.navbar .d-flex.align-items-center');
            if (navActions) {
                const sosNavBtn = document.createElement('button');
                sosNavBtn.type = 'button';
                sosNavBtn.className = 'btn btn-sos-nav btn-danger btn-sm rounded-pill px-3 fw-bold me-3';
                sosNavBtn.innerHTML = '<i class="fa-solid fa-circle-exclamation"></i> SOS';
                sosNavBtn.setAttribute('data-bs-toggle', 'modal');
                sosNavBtn.setAttribute('data-bs-target', '#emergencyModal');
                navActions.insertBefore(sosNavBtn, navActions.firstChild);
            }

            // 4. Create floating health warning button (always visible)
            const healthBtn = document.createElement('div');
            healthBtn.id = 'healthStatusFloatingButton';
            healthBtn.className = `health-status-btn level-${level}`;
            healthBtn.innerHTML = '<i class="fa-solid fa-heart-pulse"></i><span>Chỉ số</span>';
            healthBtn.setAttribute('data-bs-toggle', 'modal');
            healthBtn.setAttribute('data-bs-target', '#healthEvaluationModal');
            document.body.appendChild(healthBtn);

            // 5. Build emergency guides HTML
            let guidesHtml = '';
            
            const defaultGuides = [
                {
                    title: "HẠ ĐƯỜNG HUYẾT (GLUCOSE < 4.4 MMOL/L)",
                    content: "Triệu chứng: Bủn rủn chân tay, vã mồ hôi lạnh, chóng mặt, tim đập nhanh, đói cồn cào.\nSơ cứu: Ăn hoặc uống ngay 15g đường nhanh (1 cốc nước đường, 1 ly nước trái cây ngọt, 3-4 viên kẹo hoặc 1 thìa mật ong).\nNằm nghỉ ngơi thoải mái ở nơi thoáng mát, đo lại đường huyết sau 15 phút.\nLưu ý: Nếu người bệnh lơ mơ hoặc hôn mê, tuyệt đối không cho ăn uống qua đường miệng để tránh sặc phổi, lập tức gọi cấp cứu 115."
                },
                {
                    title: "TĂNG HUYẾT ÁP KHẨN CẤP (HA >= 180/110 MMHG)",
                    content: "Triệu chứng: Đau đầu dữ dội, hoa mắt, chóng mặt, buồn nôn, tức ngực, khó thở.\nSơ cứu: Nằm nghỉ ngơi yên tĩnh ở phòng thoáng, nâng đầu cao khoảng 30 độ.\nTuyệt đối không tự ý uống các loại thuốc hạ huyết áp nhanh (nhỏ dưới lưỡi...) nếu không có chỉ định từ bác sĩ.\nTheo dõi sát sao các dấu hiệu đột quỵ (méo miệng, nói ngọng, yếu nửa người) và lập tức gọi 115 nếu triệu chứng xấu đi."
                }
            ];

            const allGuides = (data.guides && data.guides.length > 0) ? data.guides : defaultGuides;

            allGuides.forEach(g => {
                const paragraphs = g.content.split('\n')
                    .map(p => p.trim())
                    .filter(p => p.length > 0)
                    .map(p => `<li>${p}</li>`)
                    .join('');
                guidesHtml += `
                    <div class="card card-custom border-danger p-3 mb-3 bg-light" style="border-radius: 10px;">
                        <h6 class="fw-bold text-danger mb-2">
                            <i class="fa-solid fa-kit-medical me-2"></i>${g.title}
                        </h6>
                        <div class="small text-dark lh-base">
                            <ul class="ps-3 mb-0 text-secondary">
                                ${paragraphs}
                            </ul>
                        </div>
                    </div>
                `;
            });

            // 6. Create Emergency Modal HTML
            const emergencyModalHtml = `
                <div class="modal fade" id="emergencyModal" tabindex="-1" aria-labelledby="emergencyModalLabel" aria-hidden="true">
                    <div class="modal-dialog modal-dialog-centered">
                        <div class="modal-content modal-content-custom border-danger" style="border-radius: 16px; overflow: hidden;">
                            <div class="modal-header modal-header-custom bg-danger text-white border-0 py-3">
                                <h5 class="modal-title fw-bold" id="emergencyModalLabel">
                                    <i class="fa-solid fa-triangle-exclamation me-2"></i>CỨU TRỢ KHẨN CẤP (SOS)
                                </h5>
                                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                            </div>
                            <div class="modal-body modal-body-custom p-4">
                                <!-- Temporary first aid -->
                                ${guidesHtml}

                                <!-- Call options -->
                                <div class="d-flex flex-column gap-3 mt-2">
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

            // Append Emergency Modal
            const emergencyContainer = document.createElement('div');
            emergencyContainer.innerHTML = emergencyModalHtml;
            document.body.appendChild(emergencyContainer.firstElementChild);

            // 7. Create Health Evaluation Modal HTML
            let levelText = 'Bình thường';
            let levelBadgeClass = 'bg-success';
            let evaluationDesc = 'Các chỉ số sức khỏe của bạn đang ở ngưỡng an toàn. Hãy tiếp tục duy trì chế độ sinh hoạt và ăn uống lành mạnh.';
            
            if (level === 2) {
                levelText = 'Chú ý';
                levelBadgeClass = 'bg-warning text-dark';
                evaluationDesc = 'Chỉ số huyết áp hoặc đường huyết của bạn đang ở mức chú ý (Vàng). Hãy nghỉ ngơi, giảm bớt căng thẳng và hạn chế ăn mặn, ngọt.';
            } else if (level === 3) {
                levelText = 'Nguy hiểm';
                levelBadgeClass = 'bg-orange text-white';
                evaluationDesc = 'Chỉ số sức khỏe của bạn đang ở mức nguy hiểm (Cam). Hãy chú ý nghỉ ngơi, uống thuốc đúng giờ và báo cho bác sĩ nếu chỉ số không giảm.';
            } else if (level === 4) {
                levelText = 'Cấp cứu';
                levelBadgeClass = 'bg-danger text-white';
                evaluationDesc = 'Chỉ số sức khỏe của bạn đang ở mức cấp cứu nguy kịch (Đỏ)! Hãy dừng mọi hoạt động, nghỉ ngơi ngay lập tức, báo cho người thân và bác sĩ, hoặc bấm nút SOS trên thanh tiêu đề để gọi 115.';
            }

            const healthModalHtml = `
                <div class="modal fade" id="healthEvaluationModal" tabindex="-1" aria-labelledby="healthEvalLabel" aria-hidden="true">
                    <div class="modal-dialog modal-dialog-centered">
                        <div class="modal-content modal-content-custom" style="border-radius: 16px; overflow: hidden;">
                            <div class="modal-header modal-header-custom bg-light border-0 py-3 px-4">
                                <h5 class="modal-title fw-bold text-dark mb-0" id="healthEvalLabel">
                                    <i class="fa-solid fa-heart-pulse text-danger me-2"></i>ĐÁNH GIÁ SỨC KHỎE HÔM NAY
                                </h5>
                                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                            </div>
                            <div class="modal-body p-4">
                                <div class="text-center mb-4">
                                    <div class="d-inline-block px-4 py-2 rounded-pill fw-bold ${levelBadgeClass} mb-2" style="font-size: 1.1rem; box-shadow: 0 4px 10px rgba(0,0,0,0.08);">
                                        Trạng thái: ${levelText}
                                    </div>
                                    <p class="text-muted small mb-0">Cập nhật dựa trên các chỉ số đo gần đây nhất trong ngày</p>
                                </div>
                                
                                <div class="row g-3 mb-4">
                                    <div class="col-6">
                                        <div class="p-3 bg-light rounded-3 text-center border">
                                            <small class="text-muted fw-semibold d-block mb-1">Huyết áp</small>
                                            <span class="fw-bold fs-5 text-dark">
                                                ${(data.latestSystolic && data.latestDiastolic) ? `${data.latestSystolic}/${data.latestDiastolic}` : '--/--'}
                                            </span>
                                            <small class="text-muted d-block mt-1 small">mmHg</small>
                                        </div>
                                    </div>
                                    <div class="col-6">
                                        <div class="p-3 bg-light rounded-3 text-center border">
                                            <small class="text-muted fw-semibold d-block mb-1">Đường huyết</small>
                                            <span class="fw-bold fs-5 text-dark">
                                                ${data.latestGlucose ? data.latestGlucose : '--.-'}
                                            </span>
                                            <small class="text-muted d-block mt-1 small">mmol/L</small>
                                        </div>
                                    </div>
                                </div>

                                <div class="card p-3 border-0 bg-light rounded-3 shadow-sm mb-2">
                                    <h6 class="fw-bold text-secondary mb-2" style="font-size: 0.95rem;"><i class="fa-solid fa-comment-medical me-2 text-primary"></i>Nhận xét & Khuyên dùng</h6>
                                    <p class="small text-secondary mb-0 lh-base" style="font-size: 0.88rem;">${evaluationDesc}</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            `;

            // Append Health Evaluation Modal
            const healthContainer = document.createElement('div');
            healthContainer.innerHTML = healthModalHtml;
            document.body.appendChild(healthContainer.firstElementChild);
        })
        .catch(err => console.error("Error loading health & emergency details:", err));
});
